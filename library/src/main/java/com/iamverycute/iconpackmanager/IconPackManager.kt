package com.iamverycute.iconpackmanager

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException

@Suppress("SpellCheckingInspection", "Deprecation", "DiscouragedApi")
open class IconPackManager(private val mContext: Context) {
    private val flag = PackageManager.GET_META_DATA
    private val pm: PackageManager = mContext.packageManager
    private val customRules = hashMapOf<String, String>()
    private val themes = mutableListOf("org.adw.launcher.THEMES", "com.gau.go.launcherex.theme")
    private var iconPacks: HashMap<String?, IconPack>? = null

    fun addRule(key: String, value: String): IconPackManager {
        customRules[key] = value
        return this
    }

    fun isSupportedIconPacks(): HashMap<String?, IconPack> {
        return isSupportedIconPacks(false)
    }

    open fun isSupportedIconPacks(reload: Boolean): HashMap<String?, IconPack> {
        if (iconPacks == null || reload) {
            iconPacks = hashMapOf()
            themes.forEach {
                val intent = Intent(it)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.queryIntentActivities(
                        intent,
                        PackageManager.ResolveInfoFlags.of(flag.toLong())
                    )
                } else {
                    pm.queryIntentActivities(intent, flag)
                }.forEach { info ->
                    val iconPackPackageName = info.activityInfo.packageName
                    try {
                        iconPacks!![iconPackPackageName] = IconPack(
                            iconPackPackageName, pm.getApplicationLabel(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    pm.getApplicationInfo(
                                        iconPackPackageName, PackageManager.ApplicationInfoFlags.of(
                                            flag.toLong()
                                        )
                                    )
                                } else {
                                    pm.getApplicationInfo(iconPackPackageName, flag)
                                }
                            ).toString()
                        )
                    } catch (_: PackageManager.NameNotFoundException) {
                    }
                }
            }
        }
        return iconPacks!!
    }

    inner class IconPack(val packageName: String, val name: String) {
        private val mPackagesDrawables = hashMapOf<String?, String?>()
        private val iconPackRes = pm.getResourcesForApplication(packageName)

        init {
            try {
                iconPackRes.assets.open("appfilter.xml").use {
                    XmlPullParserFactory.newInstance().newPullParser().run {
                        setInput(it.reader())
                        var eventType = eventType
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_TAG) {
                                if (name == "item") {
                                    val componentValue = getAttributeValue(null, "component")
                                    val drawableValue = getAttributeValue(null, "drawable")
                                    if (!mPackagesDrawables.containsKey(componentValue))
                                        mPackagesDrawables[componentValue] = drawableValue
                                }
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

        private fun findDrawable(
            appPackageName: String,
            defaultDrawable: Drawable?
        ): Drawable {
            var drawableValue =
                mPackagesDrawables[pm.getLaunchIntentForPackage(appPackageName)?.component.toString()]
            if (drawableValue == null) {
                mPackagesDrawables.forEach {
                    val keywords = getKeywordsForRules(appPackageName)
                    val componentKey = it.key
                    if (componentKey != null && keywords != null && componentKey.contains(keywords)) {
                        if (it.value != null) {
                            drawableValue = it.value
                            return@forEach
                        }
                    }
                }

            }
            if (drawableValue != null) {
                val id = iconPackRes.getIdentifier(
                    drawableValue,
                    "drawable",
                    packageName
                )//load icon from pack
                if (id > 0) return iconPackRes.getDrawable(id, null)
            }
            if (defaultDrawable != null)
                return bitmapCutCircle(
                    defaultDrawable.toBitmap() //default icon cut circle
                )
            return mContext.resources.getDrawable(
                android.R.drawable.sym_def_app_icon,
                null
            ) //sys default icon
        }

        fun getDrawableIconWithApplicationInfo(
            info: ApplicationInfo
        ): Drawable {
            return findDrawable(
                info.packageName,
                info.loadIcon(pm)
            )
        }

        private fun bitmapCutCircle(icon: Bitmap): BitmapDrawable {
            val bmp = Bitmap.createBitmap(
                icon.width,
                icon.height, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bmp)
            val paint = Paint()
            val rect = Rect(
                0, 0, icon.width,
                icon.height
            )
            paint.isAntiAlias = true
            paint.isFilterBitmap = true
            paint.isDither = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = Color.BLACK
            canvas.drawCircle(
                icon.width / 2 + 0.7f,
                icon.height / 2 + 0.7f,
                icon.width / 2 + 0.1f,
                paint
            )
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(icon, rect, rect, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 24f
            paint.color = Color.TRANSPARENT
            canvas.drawCircle(
                icon.width / 2 + 1f,
                icon.height / 2 + 1f,
                icon.width / 2 + 1f,
                paint
            )
            return bmp.toDrawable(mContext.resources)
        }

        private fun getKeywordsForRules(appPackageName: String): String? {
            customRules.forEach {
                if (appPackageName.contains(it.key))
                    return it.value
            }
            return null
        }
    }

    companion object {
        private const val TAG = "IconPackManager"
    }
}