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
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.ScrollView
import com.iamverycute.iconpackmanager.IconPackManager

@Suppress("QueryPermissionsNeeded", "ViewConstructor")
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = ScrollView(this)
        setContentView(root)
        val grid = GridLayout(this)
        root.addView(grid)
        var pixel = resources.displayMetrics.widthPixels
        if (resources.displayMetrics.heightPixels < 479) {
            grid.columnCount = 2
        } else if (resources.displayMetrics.heightPixels < pixel) {
            grid.columnCount = 6
            pixel = resources.displayMetrics.heightPixels
        } else {
            grid.columnCount = 3
        }
        val side = resources.displayMetrics.widthPixels / grid.columnCount
        val textSize = pixel / 25f
        val bottomVal = side / 2
        val topVal = side / 10
        grid.setPadding(0, side / 3, 0, side / 4)
        val iconPack = IconPackManager(this).isSupportedIconPacks().firstOrNull()
        for (app in if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA else ApplicationInfo.FLAG_INSTALLED
            )
        } else {
            packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(
                    ApplicationInfo.FLAG_INSTALLED.toLong()
                )
            )
        }) {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName) ?: continue
            val appName = app.loadLabel(packageManager).toString()
            if (appName.contains("输入法") || packageName == app.packageName || packageName == iconPack?.packageName) continue
            var appIcon = app.loadIcon(packageManager)
            if (iconPack != null) {
                val customIcon = iconPack.loadIcon(launchIntent)
                val iconPackName = iconPack.name.lowercase()
                if (customIcon == null) {
                    val icon2bmp = Bitmap.createBitmap(
                        appIcon.intrinsicWidth, appIcon.intrinsicHeight, Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(icon2bmp)
                    appIcon.setBounds(0, 0, canvas.width, canvas.height)
                    appIcon.draw(canvas)
                    if (iconPackName.contains("delta")) { //降低饱和度，使色彩颜色更接近delta icon pack
                        val cm = ColorMatrix()
                        cm.setSaturation(0.7f)
                        val paint = Paint()
                        paint.colorFilter = ColorMatrixColorFilter(cm)
                        canvas.drawBitmap(icon2bmp, 0f, 0f, paint)
                    }
                    appIcon =
                        if (iconPackName.contains("aura")) { //这些图标默认是圆角
                            iconPack.iconCut(
                                icon2bmp, 0.3f, 0.92f
                            )
                        } else {
                            iconPack.iconCutCircle(
                                icon2bmp, 0.89f
                            )
                        }
                } else {
                    appIcon = customIcon
                }
            }
            grid.addView(
                ImageViewEx(this, appName).apply {
                    paint.textSize = textSize
                    setImageDrawable(appIcon)
                    setPadding(0, 0, 0, bottomVal)
                    setOnClickListener { startActivity(launchIntent) }
                    setOnLongClickListener {
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${app.packageName}")
                            )
                        )
                        true
                    }
                    layoutParams = GridLayout.LayoutParams().apply {
                        height = side
                        topMargin = topVal
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                            rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                            width = 0
                        } else {
                            width = side
                        }
                    }
                },
                if (app.flags and ApplicationInfo.FLAG_SYSTEM == 0) 0 else grid.childCount
            )
        }
        registerReceiver(PackageStateReceiver(), IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) return true
        return super.onKeyDown(keyCode, event)
    }

    inner class PackageStateReceiver : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) = recreate()
    }

    class ImageViewEx(ctx: Context, private var label: String = "") : ImageView(ctx) {
        val paint = Paint()
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            var textWidth = paint.measureText(label)
            if (textWidth > width) {
                val ellipsis = "..."
                while (textWidth > width) {
                    label = label.substring(0, label.length - 1)
                    textWidth = paint.measureText(label + ellipsis)
                }
                label += ellipsis
            }
            canvas.drawText(
                label,
                (width - textWidth) * 0.5f,
                height - (paint.fontMetrics.bottom - paint.fontMetrics.top) * 1.5f,
                paint
            )
        }
    }
}