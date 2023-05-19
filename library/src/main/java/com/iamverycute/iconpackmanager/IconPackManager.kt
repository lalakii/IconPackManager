package com.iamverycute.iconpackmanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
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


@Suppress("Unused", "Deprecation")
open class IconPackManager(private val mContext: Context) {
    private val pm: PackageManager = mContext.packageManager
    private val customRules = hashMapOf<String, String>()

    fun addRule(key: String, value: String): IconPackManager {
        customRules[key] = value
        return this
    }

    fun isSupportedIconPacks(): HashMap<String?, IconPack> {
        val iconPacks = hashMapOf<String?, IconPack>()
        val adwlauncherthemes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                Intent("org.adw.launcher.THEMES"),
                PackageManager.ResolveInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            )
        } else {
            pm.queryIntentActivities(
                Intent("org.adw.launcher.THEMES"),
                PackageManager.GET_META_DATA
            )
        }
        val golauncherthemes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                Intent("com.gau.go.launcherex.theme"),
                PackageManager.ResolveInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            )
        } else {
            pm.queryIntentActivities(
                Intent("com.gau.go.launcherex.theme"),
                PackageManager.GET_META_DATA
            )
        }
        // merge those lists
        val resolves = adwlauncherthemes.union(golauncherthemes)
        resolves.forEach {
            val ip = IconPack()
            ip.packageName = it.activityInfo.packageName
            try {
                val ai: ApplicationInfo =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pm.getApplicationInfo(
                            ip.packageName!!, PackageManager.ApplicationInfoFlags.of(
                                PackageManager.GET_META_DATA.toLong()
                            )
                        )
                    } else {
                        pm.getApplicationInfo(
                            ip.packageName!!, PackageManager.GET_META_DATA
                        )
                    }
                ip.name = pm.getApplicationLabel(ai).toString()
                iconPacks[ip.packageName] = ip
            } catch (_: PackageManager.NameNotFoundException) {
            }
        }
        return iconPacks
    }

    inner class IconPack {
        var packageName: String? = null
        var name: String? = null
        private var mLoaded = false
        private val mPackagesDrawables = hashMapOf<String?, String?>()
        private lateinit var iconPackRes: Resources

        private fun load() {
            try {
                iconPackRes = pm.getResourcesForApplication(packageName!!)
                iconPackRes.assets?.open("appfilter.xml").run {
                    val xpp = XmlPullParserFactory.newInstance().newPullParser()
                    xpp.setInput(this?.reader())
                    var eventType = xpp.eventType
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            if (xpp.name == "item") {
                                val componentName = xpp.getAttributeValue(null, "component")
                                val drawableName = xpp.getAttributeValue(null, "drawable")
                                if (!mPackagesDrawables.containsKey(componentName))
                                    mPackagesDrawables[componentName] = drawableName
                            }
                        }
                        eventType = xpp.next()
                    }
                    mLoaded = true
                }
            } catch (_: XmlPullParserException) {
                Log.d(TAG, "Cannot parse icon pack appfilter.xml")
            } catch (_: IOException) {
            }
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        private fun findDrawable(
            appPackageName: String,
            defaultDrawable: Drawable?
        ): Drawable {
            val drawableName =
                mPackagesDrawables[pm.getLaunchIntentForPackage(appPackageName)?.component.toString()]
            if (drawableName != null) {
                val icon = getDrawable(drawableName) //icon pack support
                if (icon != null) return icon
            }
            for ((key, value) in mPackagesDrawables) {
                val keywords = getKeywordsForRules(appPackageName)
                if (keywords != null && key!!.contains(keywords)) {
                    if (value != null) {
                        val customIcon = getDrawable(value) //find icon with same type
                        if (customIcon != null) return customIcon
                    }
                }
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

        @SuppressLint("UseCompatLoadingForDrawables", "DiscouragedApi")
        fun getDrawable(drawableName: String): Drawable? {
            val id = iconPackRes.getIdentifier(drawableName, "drawable", packageName)
            return if (id > 0) iconPackRes.getDrawable(id, null) else null
        }

        fun getDrawableIconWithApplicationInfo(
            info: ApplicationInfo?
        ): Drawable {
            if (!mLoaded) {
                load()
            }
            return findDrawable(
                info?.packageName!!,
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
            for ((key, value) in customRules) {
                if (appPackageName.contains(key))
                    return value
            }
            return null
        }
    }

    companion object {
        private const val TAG = "IconPackManager"
    }
}