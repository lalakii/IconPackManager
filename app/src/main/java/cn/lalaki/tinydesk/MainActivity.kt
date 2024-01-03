package cn.lalaki.tinydesk

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.ScrollView
import com.iamverycute.iconpackmanager.IconPackManager

class MainActivity : Activity() {
    private lateinit var gird: GridLayout

    @Suppress("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = ScrollView(this)
        setContentView(root)
        gird = GridLayout(this)
        root.addView(gird)
        gird.setPadding(0, 100, 0, 60)
        if (resources.displayMetrics.widthPixels < 499) {
            gird.columnCount = 1
        } else if (resources.displayMetrics.widthPixels > resources.displayMetrics.heightPixels) {
            gird.columnCount = 6
        } else {
            gird.columnCount = 3
        }
        val apps = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(
                    ApplicationInfo.FLAG_INSTALLED.toLong() or ApplicationInfo.FLAG_SYSTEM.toLong()
                )
            )
        } else {
            packageManager.getInstalledApplications(ApplicationInfo.FLAG_INSTALLED or ApplicationInfo.FLAG_SYSTEM)
        }
        var iconPack: IconPackManager.IconPack? = null
        IconPackManager(this).isSupportedIconPacks().forEach {
            apps.remove(apps.find { item -> item.packageName == it.value.getPackageName() })
            iconPack = it.value
        }
        apps.forEach {
            val appName = it.loadLabel(packageManager).toString()
            val launchIntent = packageManager.getLaunchIntentForPackage(it.packageName)
            if (launchIntent == null || it.packageName == packageName || appName.contains(
                    "输入法"
                )
            ) return@forEach
            var appIcon: Drawable?
            if (iconPack == null) {
                appIcon = it.loadIcon(packageManager)
            } else {
                appIcon = iconPack!!.loadIcon(it)
                if (appIcon == null) {
                    val icon2bmp = icon2Bitmap(it.loadIcon(packageManager))
                    appIcon = if (iconPack!!.name.contains("Pure")) {
                        iconPack!!.iconCutCircle(
                            icon2bmp, 128, 0.9f
                        )
                    } else {
                        iconPack!!.iconCut(
                            icon2bmp, 128, 38f, 0.92f
                        )
                    }
                }
            }
            val icon = ImageViewEx(this)
            icon.setImageDrawable(appIcon)
            icon.setLabel(appName)
            val lp = GridLayout.LayoutParams()
            lp.width = 200
            lp.height = 200
            lp.topMargin = 50
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                lp.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            icon.layoutParams = lp
            icon.setPadding(0, 0, 0, 80)
            icon.setOnClickListener {
                startActivity(launchIntent)
            }
            icon.setOnLongClickListener { _ ->
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", it.packageName, null)
                    )
                )
                return@setOnLongClickListener true
            }
            gird.addView(icon)
        }
        registerReceiver(PackageStateReceiver(), IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        })
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK) return true
        return super.dispatchKeyEvent(event)
    }

    /**
     * 为了兼容 Android 5.x 及以下设备做的兼容处理
     * **/
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && gird.columnCount == 3 && hasFocus) {
            var minRight = 0
            for (i in 0..<gird.childCount) {
                val child = gird.getChildAt(i)
                if (minRight == 0 || child.right < minRight) {
                    minRight = child.right
                }
            }
            gird.translationX = minRight / 3f
        }
        super.onWindowFocusChanged(hasFocus)
    }

    private fun icon2Bitmap(icon: Drawable): Bitmap {
        val bmp = Bitmap.createBitmap(
            icon.intrinsicWidth, icon.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmp)
        icon.setBounds(0, 0, canvas.width, canvas.height)
        icon.draw(canvas)
        return bmp
    }

    inner class PackageStateReceiver : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            recreate()
        }
    }

    class ImageViewEx(context: Context?) : ImageView(context) {
        private var labelText = ""
        private var paint: Paint? = null

        fun setLabel(text: String) {
            labelText = text
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (paint == null) {
                paint = Paint()
                paint!!.color = Color.BLACK
                paint!!.textSize = 38f
            }
            val textWidth = paint!!.measureText(labelText)
            val xPos: Float
            if (textWidth > width) {
                labelText = labelText.substring(
                    0, paint!!.breakText(
                        labelText, 0, labelText.length, true, width.toFloat(), null
                    ) - 2
                ) + "..."
                xPos = width - paint!!.measureText(labelText)
            } else {
                xPos = width - textWidth
            }
            canvas.drawText(labelText, xPos / 2f, height - 10f, paint!!)
        }
    }
}