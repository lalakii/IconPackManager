package com.iamverycute.iconpackmanager

import android.os.Build
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
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
                        iconPacks +=
                            IconPack(
                                packageName,
                                pm.getApplicationLabel(info.activityInfo.applicationInfo).toString()
                            )
                }
            }
        }
        return iconPacks
    }

    inner class IconPack(val packageName: String, val name: String) {
        private val mComponentDrawables = hashMapOf<String?, String?>()
        private val customRules = hashMapOf<String, Array<out String>>()
        private val iconPackRes = pm.getResourcesForApplication(packageName)

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

        fun iconCutCircle(icon: Bitmap, side: Int, scale: Float) =
            iconCut(icon, side, side * 0.5f, scale)

        fun iconCut(raw: Bitmap, side: Int, radius: Float, scale: Float): BitmapDrawable {
            val icon = Bitmap.createScaledBitmap(raw, side, side, false)
            val bmp = Bitmap.createBitmap(icon.width, icon.height, Bitmap.Config.ARGB_8888)
            val paint = Paint()
            paint.isAntiAlias = false
            val canvas = Canvas(bmp)
            val halfWidth = canvas.width * 0.5f
            val most = halfWidth.coerceAtMost(radius)
            canvas.scale(scale, scale, halfWidth, halfWidth)
            canvas.drawARGB(0, 0, 0, 0)
            canvas.drawRoundRect(
                RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat()),
                most,
                most,
                paint
            )
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            val rect = Rect(0, 0, bmp.width, bmp.height)
            canvas.drawBitmap(icon, rect, rect, paint)
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