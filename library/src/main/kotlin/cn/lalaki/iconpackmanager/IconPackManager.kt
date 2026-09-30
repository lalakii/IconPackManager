package cn.lalaki.iconpackmanager

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Suppress("unused", "Deprecation", "DiscouragedApi")
open class IconPackManager(
    val pm: PackageManager,
    var hardware: Boolean = false,
) {
    private val iconPacks by lazy { mutableListOf<IconPack>() }
    private val paint by lazy { Paint() }
    private val rect by lazy { RectF() }
    private val path by lazy { Path() }
    private val maxSide = 192
    private val type = "drawable"
    open fun isSupportedIconPacks() = isSupportedIconPacks(false)
    open fun isSupportedIconPacks(reload: Boolean): MutableList<IconPack> {
        if (iconPacks.isEmpty() || reload) {
            iconPacks.clear()
            for (info in pm.queryIntentActivities(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
                0,
            )) {
                if (info.activityInfo.flags and (ApplicationInfo.FLAG_SYSTEM) == 0) {
                    try {
                        val res = pm.getResourcesForApplication(info.activityInfo.packageName)
                        val id =
                            getIdentifier(res, "appfilter", "xml", info.activityInfo.packageName)
                        if (id != 0) {
                            iconPacks += IconPack(
                                res.getXml(id),
                                res,
                                info.activityInfo.packageName,
                                info.loadLabel(pm),
                            )
                        }
                    } catch (_: Throwable) {
                    }
                }
            }
        }
        return iconPacks
    }

    private fun getIdentifier(
        res: Resources,
        name: String,
        type: String,
        pkgName: String,
    ) = res.getIdentifier(name, type, pkgName)

    open inner class IconPack(
        xml: XmlResourceParser,
        private val res: Resources,
        val packageName: String,
        val name: CharSequence,
    ) {
        private var saturation = 1f
        private val icons = hashMapOf<String, String>()
        private val caches by lazy { hashMapOf<String, Int>() }
        private val drawableCaches by lazy { ConcurrentHashMap<String, Int>() }
        private val colorFilter by lazy {
            ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(saturation) })
        }
        private var rules: HashMap<String, Array<out String>>? = null

        init {
            xml.run {
                while (next() != XmlResourceParser.END_DOCUMENT) {
                    if ("item".equals(
                            name, ignoreCase = true
                        )
                    ) {
                        val value = getAttributeValue(null, type)
                        if (!value.isNullOrEmpty()) {
                            val key = getAttributeValue(null, "component")
                            if (!key.isNullOrEmpty()) {
                                icons[key] = value
                            }
                        }
                    }
                }
                close()
            }
        }

        open fun clearCache() {
            caches.clear()
        }

        open fun clearDrawableCache() {
            drawableCaches.clear()
        }

        @Deprecated("May cause significant performance issues; call on the main thread.")
        open fun getAllIconResources(): HashMap<String, BitmapDrawable> {
            val drawables = hashMapOf<String, BitmapDrawable>()
            val loadedDrawables = hashMapOf<String, BitmapDrawable>()
            for ((key, value) in icons) {
                val icon = loadedDrawables[value] ?: getDrawable(value, null)
                if (icon != null) {
                    drawables[key] = icon
                    loadedDrawables[value] = icon
                }
            }
            loadedDrawables.clear()
            return drawables
        }

        open fun getAllIconResources(
            timeoutSeconds: Long = 10L, callback: Callback<Pair<String, BitmapDrawable>>
        ) {
            Thread {
                val iconLoaderExecutor = Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors() + 1
                ) {
                    Thread(it).apply { isDaemon = true }
                }
                val loadedDrawables = ConcurrentHashMap<String, BitmapDrawable>()
                for ((key, value) in icons) {
                    iconLoaderExecutor.execute {
                        val icon = loadedDrawables[value] ?: getDrawable(value, null)
                        if (icon != null) {
                            callback.onAdd(key to icon)
                            loadedDrawables[value] = icon
                        }
                    }
                }
                iconLoaderExecutor.shutdown()
                iconLoaderExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)
                callback.onAddFinished()
                loadedDrawables.clear()
            }.start()
        }

        private fun createBitmapDrawable(icon: Bitmap): BitmapDrawable {
            var bmp = icon
            if (hardware && Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                bmp = icon.copy(Bitmap.Config.HARDWARE, false)
                if (!icon.isRecycled) {
                    icon.recycle()
                }
            }
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.DONUT) {
                BitmapDrawable(bmp)
            } else {
                BitmapDrawable(res, bmp)
            }
        }

        private fun createBitmap(drawable: Drawable): Bitmap {
            var width = drawable.intrinsicWidth
            var height = drawable.intrinsicHeight
            if (width < 1 || height < 1) {
                height = maxSide
                width = maxSide
            } else if (width > maxSide) {
                height = (height * maxSide.toFloat() / width).toInt()
                width = maxSide
            }
            return Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888,
            )
        }

        private fun getBitmap(id: Int): Bitmap {
            val drawable = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                res.getDrawable(id)
            } else {
                res.getDrawable(id, null)
            }
            val icon = createBitmap(drawable)
            val canvas = Canvas(icon)
            drawable.bounds = canvas.clipBounds
            drawable.draw(canvas)
            return icon
        }

        private fun getDrawable(id: Int): BitmapDrawable {
            return createBitmapDrawable(BitmapFactory.decodeResource(res, id) ?: getBitmap(id))
        }

        private fun getDrawable(value: String, pkgName: String?): BitmapDrawable? {
            val id = drawableCaches[value] ?: getIdentifier(res, value, type, packageName)
            drawableCaches[value] = id
            if (id != 0) {
                if (pkgName != null) {
                    caches[pkgName] = id
                }
                return getDrawable(id)
            }
            return null
        }

        open fun loadIcon(info: ApplicationInfo): BitmapDrawable? {
            caches[info.packageName]?.let {
                return getDrawable(it)
            }
            val activities =
                pm.getPackageArchiveInfo(info.sourceDir, PackageManager.GET_ACTIVITIES)?.activities
            if (activities != null) {
                for (it in activities) {
                    val icon = loadIcon(ComponentName(info.packageName, it.name))
                    if (icon != null) {
                        return icon
                    }
                }
                for (it in icons) {
                    if (it.key.contains("${info.packageName}/", ignoreCase = true)) {
                        val icon = loadIcon(it.key, info.packageName)
                        if (icon != null) {
                            return icon
                        }
                    }
                }
            }
            return null
        }

        open fun loadIcon(launchIntent: Intent): BitmapDrawable? {
            return launchIntent.component?.let {
                loadIcon(it) ?: loadIcon(
                    pm.getApplicationInfo(
                        it.packageName,
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
                            PackageManager.MATCH_UNINSTALLED_PACKAGES
                        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.BASE_1_1) {
                            PackageManager.GET_UNINSTALLED_PACKAGES
                        } else {
                            0
                        },
                    ),
                )
            }
        }

        open fun loadIcon(comp: ComponentName): BitmapDrawable? {
            return loadIcon("$comp", comp.packageName)
        }

        open fun loadIcon(comp: String, pkgName: String): BitmapDrawable? {
            var drawableVal = icons[comp]
            if (drawableVal.isNullOrEmpty()) {
                val rules = this.rules ?: return null
                var words: Array<out String>? = null
                for (it in rules) {
                    if (pkgName.contains(it.key, ignoreCase = true)) {
                        words = rules[it.key]
                        break
                    }
                }
                if (words == null) {
                    return null
                }
                for (icon in icons.keys) {
                    if (words.any { icon.contains(it, ignoreCase = true) }) {
                        drawableVal = icons[icon]
                        break
                    }
                }
            }
            if (!drawableVal.isNullOrEmpty()) {
                return getDrawable(drawableVal, pkgName)
            }
            return null
        }

        open fun setRules(r: HashMap<String, Array<out String>>) {
            this.rules = r
        }

        open fun transformIcon(
            drawable: Drawable,
            vararg params: Float,
        ): BitmapDrawable {
            var radius: Float? = null
            var scale: Float? = null
            for (p in params) {
                if (radius == null) {
                    radius = p
                } else if (scale == null) {
                    scale = p
                } else {
                    this.saturation = p
                }
            }
            val icon = createBitmap(drawable)
            val canvas = Canvas(icon)
            var side = canvas.width.toFloat()
            drawable.bounds = canvas.clipBounds
            val saved = canvas.save()
            if (scale != null) {
                val cwh = side / 2f
                canvas.scale(scale, scale, cwh, cwh)
            }
            rect.set(0f, 0f, side, side)
            if (radius != null) {
                side *= radius
            }
            path.reset()
            path.addRoundRect(rect, side, side, Path.Direction.CW)
            canvas.clipPath(path)
            drawable.draw(canvas)
            if (this.saturation != 1f) {
                paint.reset()
                paint.colorFilter = colorFilter
                canvas.restoreToCount(saved)
                canvas.drawBitmap(icon, 0f, 0f, paint)
            }
            return createBitmapDrawable(icon)
        }
    }

    interface Callback<T> {
        fun onAdd(item: T)
        fun onAddFinished()
    }
}
