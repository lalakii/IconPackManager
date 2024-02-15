package cn.lalaki.tinydesk

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.KeyEvent
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import android.widget.TextView
import com.iamverycute.iconpackmanager.IconPackManager
import net.sourceforge.pinyin4j.PinyinHelper
import java.io.File
import java.lang.IllegalArgumentException

@Suppress("QueryPermissionsNeeded", "InternalInsetResource", "DiscouragedApi")
class MainActivity : Activity(), OnQueryTextListener {
    private val appMap = linkedMapOf<String, TextView>()
    private val grid by lazy { GridLayout(this) }
    private val innerDir by lazy { filesDir }
    private val search by lazy {
        SearchView(this).apply {
            setOnQueryTextListener(this@MainActivity)
            isIconifiedByDefault = false
            queryHint = "搜索本机应用"
            setBackgroundColor(Color.argb(50, 255, 255, 255))
        }
    }

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var statusBarHeight = 20
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            isFocusableInTouchMode = true
            setBackgroundColor(Color.argb(35, 255, 255, 255))
            addView(
                search, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    leftMargin = 20
                    rightMargin = 20
                    topMargin = statusBarHeight + 20
                }
            )
            addView(
                ScrollView(this@MainActivity).apply {
                    addView(grid)
                }, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
                )
            )
        })
        var column = 3
        if (innerDir.exists()) {
            val files = innerDir.list()
            if (files != null) {
                for (it in files) {
                    val col = (it.first() + "").toIntOrNull()
                    if (col != null && col > 0)
                        column = col
                }
            }
        }
        grid.columnCount = column
        val side = resources.displayMetrics.widthPixels / grid.columnCount
        val density = resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT
        val mTextSize = side / density * 0.135f
        val paddingSide = side / 5
        val halfSide = side / 2
        grid.setPadding(0, paddingSide, 0, paddingSide)
        val topVal = side / 8
        val pm = packageManager
        val iconPack = IconPackManager(pm).isSupportedIconPacks().firstOrNull()
        for (app in pm.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(
                Intent.CATEGORY_LAUNCHER
            ), PackageManager.GET_ACTIVITIES
        )) {
            val appName = app.loadLabel(pm).toString()
            if (appName.contains("输入法") || packageName == app.activityInfo.packageName || app.activityInfo.packageName == iconPack?.packageName) continue
            var appIcon = app.loadIcon(pm)
            val launchIntent =
                Intent().setClassName(app.activityInfo.packageName, app.activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (iconPack != null) {
                val customIcon = iconPack.loadIcon(launchIntent)
                val iconPackName = iconPack.name.toString().lowercase()
                if (customIcon == null) {
                    appIcon = if (iconPackName.contains("aura")) { //这些图标默认是圆角
                        iconPack.transformIcon(
                            appIcon, 0.3f, 0.92f
                        )
                    } else {
                        if (iconPackName.contains("delta")) {
                            iconPack.transformIcon(
                                appIcon, 0.5f, 0.89f, 0.72f
                            )
                        } else {
                            iconPack.transformIcon(
                                appIcon, 0.5f, 0.89f
                            )
                        }
                    }
                } else {
                    appIcon = customIcon
                }
            }
            val app0 = TextView(this).apply {
                text = appName
                setTextColor(Color.BLACK)
                maxLines = 1
                textSize = mTextSize
                ellipsize = TextUtils.TruncateAt.END
                gravity = Gravity.CENTER
                appIcon.setBounds(0, 0, halfSide, halfSide)
                setCompoundDrawables(null, appIcon, null, null)
                setOnClickListener { startActivity(launchIntent) }
                setOnLongClickListener {
                    startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${app.activityInfo.packageName}")
                        )
                    )
                    true
                }
                layoutParams = GridLayout.LayoutParams().apply {
                    height = side
                    width = side
                    topMargin = topVal
                }
            }
            if (!appMap.containsKey(app.activityInfo.packageName)) {
                var pinyin = ""
                for (c in appName) {
                    try {
                        pinyin += PinyinHelper.toHanyuPinyinStringArray(c)
                            .joinToString(transform = { it.replace(Regex("\\d+"), "") })
                    } catch (_: Exception) {
                    }
                }
                app0.tag = pinyin
                appMap[app.activityInfo.packageName] = app0
                grid.addView(app0)
            }
        }
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(c: Context, i: Intent) {
                val packageName = i.dataString.toString().split(':').last()
                when (i.action) {
                    Intent.ACTION_PACKAGE_ADDED -> {
                        if (appMap[packageName] == null) {
                            recreate()
                        }
                    }

                    Intent.ACTION_PACKAGE_REMOVED -> {
                        try {
                            pm.getApplicationEnabledSetting(packageName)
                        } catch (_: IllegalArgumentException) {
                            if (appMap.containsKey(packageName)) {
                                grid.removeView(appMap[packageName])
                                appMap.remove(packageName)
                            }
                        }
                    }
                }
            }
        }, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        })
    }

    override fun onResume() {
        super.onResume()
        clearSearchText()
    }

    private fun clearSearchText() {
        if (search.isFocusable && !searchText.isNullOrEmpty()) {
            search.setQuery("", false)
            search.clearFocus()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            clearSearchText()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    private var searchText: String? = null

    override fun onQueryTextChange(newText: String?): Boolean {
        searchText = newText
        if (newText != null && newText.length > 8 && 1 == newText.toSet().size) {
            if (newText.toIntOrNull() != null) {
                val files = innerDir.listFiles()
                if (files != null) {
                    for (it in files) {
                        if (it.isDirectory)
                            it.delete()
                    }
                }
                File(innerDir, newText).mkdirs()
                recreate()
            }
        }
        grid.removeAllViews()
        for (it in appMap.values) {
            grid.addView(it)
        }
        val text = newText?.replace(Regex("\\s+"), "").toString()
        if (text.isNotEmpty()) {
            for (it in appMap.values) {
                if (!it.text.toString().contains(text, ignoreCase = true) && !it.tag.toString()
                        .contains(text, ignoreCase = true)
                ) {
                    grid.removeView(it)
                }
            }
        }
        return false
    }
}