package cn.lalaki.tinydesk

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.widget.GridLayout
import androidx.core.graphics.drawable.toBitmap
import com.iamverycute.iconpackmanager.IconPackManager

class MainActivity : Activity(), OnClickListener, OnLongClickListener {
    private lateinit var gird: GridLayout

    @Suppress("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        gird = findViewById(R.id.grid)
        val apps = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(
                    ApplicationInfo.FLAG_INSTALLED.toLong() or ApplicationInfo.FLAG_SYSTEM.toLong()
                )
            )
        } else {
            packageManager.getInstalledApplications(ApplicationInfo.FLAG_INSTALLED or ApplicationInfo.FLAG_SYSTEM)
        }
        val ipm = IconPackManager(applicationContext)
        ipm.addRule("com.android.email","mail","message")
        ipm.clearRules()
        val iconPacks = mutableListOf<IconPackManager.IconPack>()
        ipm.isSupportedIconPacks().forEach {
            apps.removeIf { item -> item.packageName.equals(it.value.getPackageName()) }
            iconPacks.add(it.value)
        }
        apps.forEach { item ->
            run {
                val appName = item.loadLabel(packageManager).toString()
                if (packageManager.getLaunchIntentForPackage(item.packageName) == null || item.packageName.equals(
                        packageName
                    ) || appName.contains("输入法")
                ) return@forEach
                var iconInfo: Drawable?
                val iconFirst = iconPacks.firstOrNull()
                if (iconFirst != null) {
                    iconInfo = iconFirst.loadIcon(item)
                    if (iconInfo == null) iconInfo = iconFirst.iconCutCircle(
                        item.loadIcon(packageManager).toBitmap(),128,1f
                    )
                } else {
                    iconInfo = item.loadIcon(packageManager)
                }
                val appIcon = ImageViewEx(this)
                appIcon.setImageDrawable(iconInfo)
                appIcon.setLabel(appName)
                val lp = GridLayout.LayoutParams()
                lp.width = 200
                lp.height = 200
                lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                appIcon.layoutParams = lp
                appIcon.setPadding(0, 0, 0, 90)
                appIcon.tag = item.packageName
                appIcon.setOnClickListener(this)
                appIcon.setOnLongClickListener(this)
                gird.addView(appIcon)
            }
        }

        registerReceiver(PackageStateReceiver(), IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        })
    }

    override fun onClick(v: View?) {
        if (v?.tag != null) {
            val itemPackageName = v.tag.toString()
            val startIntent = packageManager.getLaunchIntentForPackage(itemPackageName)
            if (startIntent != null) startActivity(startIntent)
        }
    }

    override fun onLongClick(v: View?): Boolean {
        if (v?.tag != null) startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", v.tag.toString(), null)
            )
        )
        return true
    }

    inner class PackageStateReceiver : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    recreate()
                }

                Intent.ACTION_PACKAGE_REMOVED -> {
                    for (i in 0 until gird.childCount - 1) {
                        val child: View = gird.getChildAt(i)
                        if (child.tag == intent.dataString!!.substringAfter(':')) {
                            gird.removeView(child)
                            break
                        }
                    }
                }
            }
        }
    }
}