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
    private val contextRes = ctx.resources
    private val iconPacks = mutableListOf<IconPack>()
    private val themes = arrayOf("org.adw.launcher.THEMES", "com.gau.go.launcherex.theme")

    open fun isSupportedIconPacks() = isSupportedIconPacks(false)

    open fun isSupportedIconPacks(reload: Boolean): List<IconPack> {
        if (iconPacks.isEmpty() || reload) {
            iconPacks.clear()
            for (theme in themes) {
                val intent = Intent(theme)
                for (info in if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                    pm.queryIntentActivities(
                        intent,
                        PackageManager.ResolveInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                    )
                } else {
                    pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)
                }) {
                    val packageName = info.activityInfo.packageName
                    if (iconPacks.none { it.packageName == packageName })
                        iconPacks += IconPack(
                            packageName,
                            info.activityInfo.applicationInfo.labelRes
                        )
                }
            }
        }
        return iconPacks
    }

    inner class IconPack(val packageName: String, labelRes: Int) {
        private val iconPackRes = pm.getResourcesForApplication(packageName)
        private val customRules = hashMapOf<String, Array<out String>>()
        private val mComponentDrawables = hashMapOf<String?, String?>()
        val name = iconPackRes.getString(labelRes)

        init {
            val id = iconPackRes.getIdentifier("appfilter", "xml", packageName)
            if (id > 0) {
                iconPackRes.getXml(id).run {
                    try {
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_TAG && name == "item") {
                                mComponentDrawables[getAttributeValue(null, "component")] =
                                    getAttributeValue(null, "drawable")
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
                var drawableValue =
                    mComponentDrawables[comp.toString()]
                if (drawableValue.isNullOrEmpty()) {
                    val words = getKeywordsForRules(comp.packageName)
                    if (words != null)
                        for (word in words) {
                            drawableValue = mComponentDrawables[mComponentDrawables.keys.find {
                                it?.contains(word) == true
                            }]
                            if (drawableValue != null) break
                        }
                }
                if (!drawableValue.isNullOrEmpty()) {
                    val id = iconPackRes.getIdentifier(drawableValue, "drawable", packageName)
                    if (id > 0) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            return iconPackRes.getDrawable(id)
                        }
                        return iconPackRes.getDrawable(id, null)
                    }
                }
            }
            return null
        }

        fun iconCutCircle(icon: Bitmap, scale: Float) =
            iconCut(icon, 0.5f, scale)

        fun iconCut(icon: Bitmap, radius: Float, scale: Float): BitmapDrawable {
            val bmp = Bitmap.createBitmap(icon.width, icon.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            val coer = canvas.width * radius
            val most = coer.coerceAtMost(coer)
            val paint = Paint()
            canvas.scale(scale, scale, coer, coer)
            canvas.drawRoundRect(
                RectF(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat()),
                most,
                most,
                paint
            )
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            paint.isAntiAlias = false
            canvas.drawBitmap(icon, 0f, 0f, paint)
            return BitmapDrawable(contextRes, bmp)
        }

        fun addRule(key: String, vararg value: String): IconPack {
            customRules[key] = value
            return this
        }

        fun clearRules() = customRules.clear()
        private fun getKeywordsForRules(appPackageName: String): Array<out String>? {
            return customRules[customRules.keys.find { appPackageName.contains(it) }]
        }
    }
}