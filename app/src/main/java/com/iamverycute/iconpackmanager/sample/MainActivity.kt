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
import com.iamverycute.iconpackmanager.IconPackManager

class MainActivity : Activity() {
    @Suppress("Deprecation")
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
        val layoutRight = findViewById<ViewGroup>(R.id.raw)

        apps.forEach { item ->
            run {
                if (item.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM) return@forEach
                ipm.isSupportedIconPacks().forEach {
                    /** If you have more than one icon pack theme installed, you need to exclude it here
                    filter other icon pack**/

                   // if (it.value.name != "Pure Icon Pack") return@run

                    //get icon pack name
                    findViewById<TextView>(R.id.iconPackName).text = it.value.name
                    // Get icon by applicationInfo
                    val icon = it.value.getDrawableIconWithApplicationInfo(item)

                    //create custom layout
                    val customLayout = LinearLayout(this)
                    customLayout.layout(0, 0, 100, 0)
                    customLayout.layoutParams = LinearLayout.LayoutParams(120, 120)
                    customLayout.orientation = LinearLayout.HORIZONTAL
                    layout.addView(customLayout)

                    //show custom in imageView
                    val imgView = ImageView(this)
                    imgView.background = icon
                    customLayout.addView(imgView)

                    //create raw layout
                    val rawLayout = LinearLayout(this)
                    rawLayout.layout(0, 0, 100, 0)
                    rawLayout.layoutParams = LinearLayout.LayoutParams(220, 220)
                    rawLayout.orientation = LinearLayout.HORIZONTAL
                    layoutRight.addView(rawLayout)

                    //show raw icon in imageView
                    val imgViewRaw = ImageView(this)
                    imgViewRaw.background = item.loadIcon(packageManager)
                    rawLayout.addView(imgViewRaw)
                }
            }
        }
    }
}