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

@Suppress("unused", "deprecation", "DiscouragedApi")
open class IconPackManager(
    val pm: PackageManager,
) {
    private val iconPacks by lazy { mutableListOf<IconPack>() }
    private val paint by lazy { Paint() }
    private val rect by lazy { RectF() }
    private val path by lazy { Path() }
    private val type = "drawable"
    open fun isSupportedIconPacks() = isSupportedIconPacks(false)
    open fun isSupportedIconPacks(reload: Boolean): MutableList<IconPack> {
        if (iconPacks.isEmpty() || reload) {
            iconPacks.clear()
            for (info in pm.queryIntentActivities(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
                PackageManager.GET_META_DATA,
            )) {
                if (info.activityInfo.flags and (ApplicationInfo.FLAG_SYSTEM) == 0) {
                    try {
                        val res = pm.getResourcesForApplication(info.activityInfo.packageName)
                        val id =
                            getIdentifier(res, "appfilter", "xml", info.activityInfo.packageName)
                        if (id > 0) {
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
        packageName: String,
    ) = res.getIdentifier(name, type, packageName)

    open inner class IconPack(
        xml: XmlResourceParser,
        private val res: Resources,
        val packageName: String,
        val name: CharSequence,
    ) {
        private val caches by lazy { hashMapOf<String, String>() }
        private var rules: HashMap<String, Array<out String>>? = null
        private val icons = hashMapOf<String, String>()
        private var saturation = 1f
        private val colorFilter by lazy {
            ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(saturation) })
        }

        init {
            xml.run {
                use {
                    while (eventType != XmlResourceParser.END_DOCUMENT) {
                        if (eventType == XmlResourceParser.START_TAG && "item".equals(
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
                        next()
                    }
                }
            }
        }

        open fun clearCache() {
            caches.clear()
        }

        @Deprecated("There may be serious performance loss.")
        open fun getAllIconResources(): HashMap<String, BitmapDrawable> {
            val drawables = hashMapOf<String, BitmapDrawable>()
            val distinctIcon =
                icons.entries.distinctBy { it.value }.associate { it.key to it.value }
            for (it in distinctIcon) {
                val icon = getDrawable(it.value)
                if (icon != null) {
                    drawables[it.key] = icon
                }
            }
            return drawables
        }

        private fun getBitmapDrawable(bmp: Bitmap): BitmapDrawable {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                BitmapDrawable(bmp)
            } else {
                BitmapDrawable(res, bmp)
            }
        }

        private fun getBitmap(id: Int): Bitmap {
            val drawable = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                res.getDrawable(id)
            } else {
                res.getDrawable(id, null)
            }
            var width = drawable.intrinsicWidth
            var height = drawable.intrinsicHeight
            if (width < 1 || height < 1) {
                width = 512
                height = 512
            }
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            drawable.setBounds(0, 0, width, height)
            drawable.draw(Canvas(bmp))
            return bmp
        }

        private fun getDrawable(value: String): BitmapDrawable? {
            val id = getIdentifier(res, value, type, packageName)
            if (id > 0) {
                var bmp = BitmapFactory.decodeResource(res, id)
                if (bmp == null) {
                    bmp = getBitmap(id)
                }
                return getBitmapDrawable(bmp)
            }
            return null
        }

        open fun loadIcon(info: ApplicationInfo): BitmapDrawable? {
            if (caches.containsKey(info.packageName)) {
                val comp = caches[info.packageName]
                if (comp != null) {
                    val icon = loadIcon(comp, info.packageName)
                    if (icon != null) return icon
                }
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
            val comp = launchIntent.component ?: return null
            return loadIcon(comp) ?: loadIcon(
                pm.getApplicationInfo(
                    comp.packageName,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        PackageManager.MATCH_UNINSTALLED_PACKAGES
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                        PackageManager.GET_UNINSTALLED_PACKAGES
                    } else {
                        0
                    },
                ),
            )
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
                if (words == null) return null
                for (icon in icons.keys) {
                    if (words.any { icon.contains(it, ignoreCase = true) }) {
                        drawableVal = icons[icon]
                        break
                    }
                }
            }
            if (!drawableVal.isNullOrEmpty()) {
                caches[pkgName] = comp
                return getDrawable(drawableVal)
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
            val icon = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888,
            )
            val canvas = Canvas(icon)
            val side = canvas.width.toFloat()
            if (scale != null) {
                val cwh = side / 2
                canvas.scale(scale, scale, cwh, cwh)
            }
            drawable.setBounds(0, 0, side.toInt(), side.toInt())
            rect.set(0f, 0f, side, side)
            var roundedWidth = side
            if (radius != null) {
                roundedWidth *= radius
            }
            path.reset()
            path.addRoundRect(rect, roundedWidth, roundedWidth, Path.Direction.CW)
            canvas.clipPath(path)
            drawable.draw(canvas)
            if (this.saturation != 1f) {
                paint.reset()
                paint.colorFilter = colorFilter
                Canvas(icon).drawBitmap(icon, 0f, 0f, paint)
            }
            return getBitmapDrawable(icon)
        }
    }
}
