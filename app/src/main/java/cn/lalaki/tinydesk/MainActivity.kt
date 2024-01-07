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

@Suppress("all")
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = ScrollView(this)
        setContentView(root)
        val grid = GridLayout(this)
        root.addView(grid)
        var pixel = resources.displayMetrics.widthPixels
        if (resources.displayMetrics.heightPixels < 481) {
            grid.columnCount = 2
        } else if (resources.displayMetrics.heightPixels < pixel) {
            grid.columnCount = 6
            pixel = resources.displayMetrics.heightPixels
        } else {
            grid.columnCount = 3
        }
        val apps = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(
                    ApplicationInfo.FLAG_INSTALLED.toLong() or ApplicationInfo.FLAG_SYSTEM.toLong()
                )
            )
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            packageManager.getInstalledApplications(ApplicationInfo.FLAG_INSTALLED or ApplicationInfo.FLAG_SYSTEM)
        } else {
            packageManager.getInstalledApplications(ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA or ApplicationInfo.FLAG_SYSTEM)
        }
        var iconPack: IconPackManager.IconPack? = null
        for (pack in IconPackManager(this).isSupportedIconPacks()) {
            apps.remove(apps.find { it.packageName == pack.packageName })
            iconPack = pack
        }
        val side = resources.displayMetrics.widthPixels / (grid.columnCount + 1)
        val textSize = pixel / 25f
        val radius = side / 3.5f
        val bottomVal = side / 2
        val topVal = side / 8
        val lowSide =  pixel / grid.columnCount
        grid.setPadding(0, side / 3, 0, side / 4)
        for (app in apps) {
            val appName = app.loadLabel(packageManager).toString()
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent == null || app.packageName == packageName || appName.contains(
                    "输入法"
                )
            ) continue
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
                    val radiusIcon = arrayOf("aura") //这些图标默认是圆角
                    appIcon = if (radiusIcon.any { icon -> iconPackName.contains(icon) }) {
                        iconPack.iconCut(
                            icon2bmp, side, radius, 0.92f
                        )
                    } else {
                        iconPack.iconCutCircle(
                            icon2bmp, side, 0.9f
                        )
                    }
                } else {
                    appIcon = customIcon
                }
            }
            grid.addView(ImageViewEx(this, appName, textSize).apply {
                setImageDrawable(appIcon)
                setPadding(0, 0, 0, bottomVal)
                setOnClickListener {
                    startActivity(launchIntent)
                }
                setOnLongClickListener { _ ->
                    startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${app.packageName}")
                        )
                    )
                    return@setOnLongClickListener true
                }
            }, GridLayout.LayoutParams().apply {
                height = side
                topMargin = topVal
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    columnSpec = rowSpec
                }
                width = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    lowSide
                } else {
                    side
                }
            })
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

    inner class PackageStateReceiver : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            recreate()
        }
    }

    class ImageViewEx(ctx: Context?, private var label: String = "", private val size: Float) :
        ImageView(ctx) {
        private val paint = Paint()
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            paint.textSize = size
            val xPos: Float
            val textWidth = paint.measureText(label)
            if (textWidth > width) {
                label = label.dropLast(
                    paint.breakText(
                        label, 0, label.length, true, width.toFloat(), null
                    ) - 3
                ) + "..."
                xPos = width - paint.measureText(label)
            } else {
                xPos = width - textWidth
            }
            val fm = paint.fontMetrics
            canvas.drawText(label, xPos / 2f, height - fm.descent + fm.ascent, paint)
        }
    }
}