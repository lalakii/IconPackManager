package com.iamverycute.iconpackmanager

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

@Suppress(
    "SpellCheckingInspection",
    "DiscouragedApi",
    "Unused",
    "Deprecation",
    "MemberVisibilityCanBePrivate"
)
open class IconPackManager(mContext: Context) {
    private val contextRes = mContext.resources
    private val pm: PackageManager = mContext.packageManager
    private val flag = PackageManager.GET_META_DATA
    private val customRules = hashMapOf<String, Array<out String>>()
    private val themes = mutableListOf("org.adw.launcher.THEMES", "com.gau.go.launcherex.theme")
    private var iconPacks: HashMap<String?, IconPack>? = null

    open fun addRule(key: String, vararg value: String): IconPackManager {
        customRules[key] = value
        return this
    }

    open fun clearRules() = customRules.clear()

    open fun isSupportedIconPacks() = isSupportedIconPacks(false)

    open fun isSupportedIconPacks(reload: Boolean): HashMap<String?, IconPack> {
        if (iconPacks == null || reload) {
            iconPacks = hashMapOf()
            themes.forEach {
                val intent = Intent(it)
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                    pm.queryIntentActivities(
                        intent, PackageManager.ResolveInfoFlags.of(flag.toLong())
                    )
                } else {
                    pm.queryIntentActivities(intent, flag)
                }.forEach { info ->
                    val iconPackPackageName = info.activityInfo.packageName
                    try {
                        iconPacks!![iconPackPackageName] = IconPack(
                            iconPackPackageName,
                            pm.getApplicationLabel(getApplicationInfo(iconPackPackageName))
                                .toString()
                        )
                    } catch (_: PackageManager.NameNotFoundException) {
                    }
                }
            }
        }
        return iconPacks!!
    }

    private fun getApplicationInfo(iconPackPackageName: String): ApplicationInfo {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            pm.getApplicationInfo(
                iconPackPackageName,
                PackageManager.ApplicationInfoFlags.of(flag.toLong())
            )
        } else {
            pm.getApplicationInfo(iconPackPackageName, flag)
        }
    }

    inner class IconPack(private val packageName: String, val name: String) {
        private val mComponentDrawables = hashMapOf<String?, String?>()
        private val iconPackRes = pm.getResourcesForApplication(packageName)

        fun getPackageName(): String = packageName

        init {
            try {
                iconPackRes.assets.open("appfilter.xml").use {
                    Xml.newPullParser().run {
                        setInput(it.reader())
                        var eventType = eventType
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_TAG && name == "item") {
                                val componentValue = getAttributeValue(null, "component")
                                if (!mComponentDrawables.containsKey(componentValue)) mComponentDrawables[componentValue] =
                                    getAttributeValue(null, "drawable")
                            }
                            eventType = next()
                        }
                    }
                }
            } catch (_: XmlPullParserException) {
                Log.d(TAG, "Cannot parse icon pack appfilter.xml")
            } catch (_: IOException) {
            }
        }

        private fun getDrawable(appPackageName: String): Drawable? {
            var drawableValue =
                mComponentDrawables[pm.getLaunchIntentForPackage(appPackageName)?.component.toString()]
            if (drawableValue.isNullOrEmpty()) {
                getKeywordsForRules(appPackageName)?.forEach { word ->
                    run {
                        mComponentDrawables.filterKeys {
                            it?.contains(word) == true
                        }.firstNotNullOfOrNull {
                            drawableValue = it.value
                        }
                    }
                }
            }
            if (!drawableValue.isNullOrEmpty()) {
                val id = iconPackRes.getIdentifier(drawableValue, "drawable", packageName)
                try {
                    if (id > 0) if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                        return iconPackRes.getDrawable(id, null)
                    }
                    return iconPackRes.getDrawable(id)
                } catch (_: Resources.NotFoundException) {
                }
            }
            return null
        }

        fun loadIcon(info: ApplicationInfo) = getDrawable(info.packageName)

        fun iconCutCircle(icon: Bitmap, side: Int, scale: Float) =
            iconCut(icon, side, side * 0.5f, scale)

        fun iconCut(raw: Bitmap, side: Int, radius: Float, scale: Float): BitmapDrawable {
            val icon = Bitmap.createScaledBitmap(raw, side, side, true)
            val bmp = Bitmap.createBitmap(icon.width, icon.height, Bitmap.Config.ARGB_8888)
            val paint = Paint()
            paint.isDither = true
            paint.isAntiAlias = true
            paint.isFilterBitmap = true
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

        private fun getKeywordsForRules(appPackageName: String): Array<out String>? {
            customRules.forEach { if (appPackageName.contains(it.key)) return it.value }
            return null
        }
    }

    companion object {
        private const val TAG = "IconPackManager"
    }
}