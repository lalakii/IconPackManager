package com.iamverycute.iconpackmanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
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

@Suppress("SpellCheckingInspection", "Deprecation")
open class IconPackManager(private val mContext: Context) {
    private val pm: PackageManager = mContext.packageManager
    private val customRules = hashMapOf<String, String>()
    private val themes = mutableListOf("org.adw.launcher.THEMES", "com.gau.go.launcherex.theme")

    fun addRule(key: String, value: String): IconPackManager {
        customRules[key] = value
        return this
    }

    fun isSupportedIconPacks(): HashMap<String?, IconPack> {
        val iconPacks = hashMapOf<String?, IconPack>()
        val resolves = mutableListOf<ResolveInfo>()
        themes.forEach {
            val intent = Intent(it)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                resolves.addAll(
                    pm.queryIntentActivities(
                        intent,
                        PackageManager.ResolveInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                    )
                )
            } else {
                resolves.addAll(
                    pm.queryIntentActivities(
                        intent,
                        PackageManager.GET_META_DATA
                    )
                )
            }
        }
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
                                val componentValue = xpp.getAttributeValue(null, "component")
                                val drawableValue = xpp.getAttributeValue(null, "drawable")
                                if (!mPackagesDrawables.containsKey(componentValue))
                                    mPackagesDrawables[componentValue] = drawableValue
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

        @SuppressLint("UseCompatLoadingForDrawables", "DiscouragedApi")
        private fun findDrawable(
            appPackageName: String,
            defaultDrawable: Drawable?
        ): Drawable {
            var drawableValue =
                mPackagesDrawables[pm.getLaunchIntentForPackage(appPackageName)?.component.toString()]
            if (drawableValue == null) {
                for ((key, value) in mPackagesDrawables) {
                    val keywords = getKeywordsForRules(appPackageName)
                    if (keywords != null && key!!.contains(keywords)) {
                        if (value != null) drawableValue = value
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