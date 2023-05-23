package com.iamverycute.iconpackmanager.sample

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.core.graphics.drawable.toBitmap
import com.iamverycute.iconpackmanager.IconPackManager

class MainActivity : Activity(), AdapterView.OnItemSelectedListener {
    private lateinit var ipm: IconPackManager
    private lateinit var layout: ViewGroup
    private lateinit var apps: MutableList<ApplicationInfo>
    private val iconPacks = mutableListOf<String>()

    @Suppress("Deprecation", "SetTextI18n", "QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        // Get installed application
        apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(
                    ApplicationInfo.FLAG_INSTALLED.toLong()
                )
            )
        } else {
            packageManager.getInstalledApplications(ApplicationInfo.FLAG_INSTALLED)
        }
        // IconPackManager
        ipm = IconPackManager(applicationContext)
        // Add rules for custom icon, OnDemand
        ipm.addRule("com.android.chrome", "browser")
            .addRule("com.android.email", "mail")
            .addRule("zte", "appstore")
            .addRule("updater", "update")
        layout = findViewById(R.id.custom)
        val spinner = findViewById<Spinner>(R.id.iconPacks)
        loadIcons()
        spinner.adapter = ArrayAdapter(this, R.layout.item, iconPacks)
        spinner.onItemSelectedListener = this
    }

    private fun loadIcons() {
        layout.removeAllViews()
        apps.forEach { item ->
            run {
                if (item.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM) return@forEach
                ipm.isSupportedIconPacks().forEach {
                    //get icon pack name, icon pack packageName: it.value.getPackageName()
                    val iconPackName = it.value.name
                    if (!iconPacks.contains(iconPackName)) {
                        iconPacks.add(iconPackName)
                    }

                    /** If you have more than one icon pack theme installed, you need to exclude it here
                    filter other icon pack**/
                    if (iconPacks.size != 0 && iconPacks[0] == it.value.name) {
                        // Get icon by applicationInfo
                        val icon = it.value.loadIcon(item)
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
                                LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    1F
                                )
                            imageViewLayout.topMargin = 10
                            imgView.layoutParams = imageViewLayout
                            customLayout.addView(imgView)
                        }
                    }
                }
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val iconPackName = parent?.adapter?.getItem(position)
        iconPacks.remove(iconPackName)
        iconPacks.add(0, iconPackName.toString())
        loadIcons()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}