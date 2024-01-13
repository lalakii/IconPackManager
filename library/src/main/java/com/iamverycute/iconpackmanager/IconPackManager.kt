package com.iamverycute.iconpackmanager

import android.os.Build
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.RectF
import android.graphics.Paint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import org.xmlpull.v1.XmlPullParser

@Suppress(
    "All",
    "Unused",
    "Deprecation",
    "SpellCheckingInspection",
    "MemberVisibilityCanBePrivate"
)
open class IconPackManager(ctx: Context) {
    private val pm = ctx.packageManager
    private val iconPacks = mutableListOf<IconPack>()
    open fun isSupportedIconPacks() = isSupportedIconPacks(false)
    open fun isSupportedIconPacks(reload: Boolean): MutableList<IconPack> {
        if (iconPacks.isEmpty() || reload) {
            iconPacks.clear()
            for (theme in arrayOf("org.adw.launcher.THEMES", "com.gau.go.launcherex.theme")) {
                val intent = Intent(theme)
                for (info in if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)
                } else {
                    pm.queryIntentActivities(
                        intent,
                        PackageManager.ResolveInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                    )
                }) {
                    if (iconPacks.none { it.packageName == info.activityInfo.packageName }) iconPacks += IconPack(
                        info.activityInfo.packageName, info.activityInfo.applicationInfo.labelRes
                    )
                }
            }
        }
        return iconPacks
    }

    inner class IconPack(val packageName: String, labelRes: Int) {
        private val res = pm.getResourcesForApplication(packageName)
        private var rules: HashMap<String, Array<out String>>? = null
        private val icons = hashMapOf<String, String>()
        val name = res.getString(labelRes)

        init {
            val id = res.getIdentifier("appfilter", "xml", packageName)
            if (id > 0) {
                res.getXml(id).run {
                    try {
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_TAG && name == "item") {
                                val key = getAttributeValue(null, "component")
                                val value = getAttributeValue(null, "drawable")
                                if (!key.isNullOrEmpty() && !value.isNullOrEmpty()) icons[key] =
                                    value
                            }
                            next()
                        }
                    } catch (_: Exception) {
                    } finally {
                        close()
                    }
                }
            }
        }

        fun loadIcon(launchIntent: Intent): Drawable? {
            val comp = launchIntent.component
            if (comp != null) {
                var drawableVal = icons[comp.toString()]
                if (drawableVal.isNullOrEmpty()) {
                    if (rules == null) return null
                    val words =
                        rules!![rules!!.keys.find { comp.packageName.contains(it) }] ?: return null
                    for (word in words) {
                        drawableVal = icons[icons.keys.find { it.contains(word) }]
                        if (drawableVal != null) break
                    }
                }
                if (!drawableVal.isNullOrEmpty()) {
                    val id = res.getIdentifier(drawableVal, "drawable", packageName)
                    if (id > 0) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            return res.getDrawable(id)
                        }
                        return res.getDrawable(id, null)
                    }
                }
            }
            return null
        }

        fun iconCutCircle(icon: Bitmap, scale: Float) = iconCut(icon, 0.5f, scale)
        fun iconCut(icon: Bitmap, radius: Float, scale: Float): BitmapDrawable {
            val bmp = Bitmap.createBitmap(icon.width, icon.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            val cre = canvas.width * radius
            val most = cre.coerceAtMost(cre)
            val paint = Paint()
            val cwh = canvas.width * 0.5f
            canvas.scale(scale, scale, cwh, cwh)
            val side = bmp.width.toFloat()
            canvas.drawRoundRect(RectF(0f, 0f, side, side), most, most, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            paint.isAntiAlias = false
            canvas.drawBitmap(icon, 0f, 0f, paint)
            return BitmapDrawable(null, bmp)
        }

        fun setRules(r: HashMap<String, Array<out String>>) {
            this.rules = r
        }
    }
}