package com.iamverycute.iconpackmanager.sample

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import com.iamverycute.iconpackmanager.IconPackManager

class MainActivity : Activity() {
    @Suppress("Deprecation", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        // Get installed application
        val apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(
                    ApplicationInfo.FLAG_INSTALLED.toLong()
                )
            )
        } else {
            packageManager.getInstalledApplications(ApplicationInfo.FLAG_INSTALLED)
        }
        // IconPackManager
        val ipm = IconPackManager(applicationContext)
        // Add rules for custom icon
        ipm.addRule("com.android.chrome", "browser")
            .addRule("com.android.email", "mail")
        val layout = findViewById<ViewGroup>(R.id.custom)
        apps.forEach { item ->
            run {
                if (item.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM) return@forEach
                ipm.isSupportedIconPacks().forEach {
                    /** If you have more than one icon pack theme installed, you need to exclude it here
                    filter other icon pack**/

                    // if (it.value.name != "Pure Icon Pack") return@run

                    //get icon pack name && icon pack packageName
                    findViewById<TextView>(R.id.iconPackInfo).text =
                        it.value.name + "\n" + it.value.packageName
                    // Get icon by applicationInfo
                    val icon = it.value.getDrawableIconWithApplicationInfo(item)
                    //create custom layout
                    val customLayout = LinearLayout(this)
                    customLayout.layout(0, 0, 0, 0)
                    customLayout.layoutParams =
                        LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200)
                    customLayout.orientation = LinearLayout.HORIZONTAL
                    layout.addView(customLayout)

                    //custom icon and default icon
                    var defaultIcon = getDrawable(android.R.drawable.sym_def_app_icon)
                    try {
                        defaultIcon = packageManager.getApplicationIcon(item.packageName)
                    } catch (_: PackageManager.NameNotFoundException) {
                    }
                    val drawables =
                        mutableListOf(icon, defaultIcon)
                    drawables.forEach { drawable ->
                        //show icon in imageView
                        val imgView = ImageView(this)
                        imgView.setImageBitmap(drawable?.toBitmap())
                        imgView.scaleType = ImageView.ScaleType.FIT_CENTER
                        imgView.adjustViewBounds = true
                        val imageViewLayout =
                            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
                        imageViewLayout.topMargin = 10
                        imgView.layoutParams = imageViewLayout
                        customLayout.addView(imgView)
                    }
                }
            }
        }
    }
}