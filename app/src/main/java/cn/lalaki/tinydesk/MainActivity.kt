package cn.lalaki.tinydesk

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.iamverycute.iconpackmanager.IconPackManager
import net.sourceforge.pinyin4j.PinyinHelper
import java.io.File
import java.io.FileReader
import java.io.PrintWriter
import java.util.Collections

@Suppress("QueryPermissionsNeeded", "InternalInsetResource", "DiscouragedApi")
class MainActivity : Activity(), OnQueryTextListener, OnLongClickListener {
    private val appMap = linkedMapOf<String, AppModel>()
    private val recycler by lazy {
        RecyclerView(this).apply {
            clipToPadding = false
        }
    }
    private val sortConfig by lazy {
        File(innerDir.absoluteFile, "sortConfig.txt")
    }
    private val colConfig by lazy {
        File(innerDir.absoluteFile, "colConfig.txt")
    }
    private val innerDir by lazy { filesDir }
    private val search by lazy {
        SearchView(this).apply {
            setOnQueryTextListener(this@MainActivity)
            setOnLongClickListener(this@MainActivity)
            isIconifiedByDefault = false
            queryHint = "搜索本机应用"
            setBackgroundColor(Color.argb(50, 255, 255, 255))
        }
    }

    class AppModel(
        val appName: String,
        val icon: Drawable,
        val mTextSize: Float,
        val side: Int,
        val halfSide: Int,
        val launchIntent: Intent,
        val detail: Uri?,
        val topVal: Int,
        var backupKey: String
    ) {
        var pinyin: String = ""
    }

    inner class AppViewHolder(private val adapter: AppListAdapter, itemView: View) :
        ViewHolder(itemView) {
        private var view = itemView.findViewById<TextView>(R.id.tv)
        private val animaCache by lazy {
            AnimationUtils.loadAnimation(itemView.context, R.anim.shake)
        }

        fun startAnimation() {
            if (itemView.animation != animaCache) {
                itemView.animation = animaCache
            }
            animaCache?.startNow()
        }

        fun stopAnimation() {
            itemView.animation?.cancel()
            itemView.animation = null
        }

        fun bind(m: AppModel) {
            if (adapter.canDrag) {
                startAnimation()
            } else {
                stopAnimation()
            }
            if (!view.compoundDrawables.contains(m.icon)) {
                view.text = m.appName
                view.textSize = m.mTextSize
                val params = view.layoutParams
                if (params is RecyclerView.LayoutParams) {
                    params.width = m.side
                    params.height = m.side
                    params.topMargin = m.topVal
                }
                m.icon.setBounds(0, 0, m.halfSide, m.halfSide)
                view.setCompoundDrawables(null, m.icon, null, null)
                view.setOnClickListener {
                    startActivity(m.launchIntent)
                }
                view.setOnLongClickListener {
                    val adapter = recycler.adapter
                    if (adapter is AppListAdapter) {
                        if (!adapter.canDrag) {
                            startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS, m.detail
                                )
                            )
                        }
                    }
                    false
                }
            }
        }
    }

    inner class AppListAdapter(var map: LinkedHashMap<String, AppModel>) :
        RecyclerView.Adapter<AppViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
            return AppViewHolder(
                this,
                LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
            )
        }

        fun swap(source: Int, target: Int) = run {
            val tempArray = map.map { it.value }
            Collections.swap(tempArray, source, target)
            appMap.clear()
            val sb = StringBuilder()
            tempArray.forEach {
                appMap[it.backupKey] = it
                sb.append("${it.backupKey};")
            }
            writeConfig(sortConfig, sb.toString())
            map = appMap
        }

        private val helper = HolderTouchHelper()
        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            helper.attachToView(recyclerView)
            super.onAttachedToRecyclerView(recyclerView)
        }

        fun disableDrag() {
            canDrag = false
            helper.disableDrag()
            animationRefresh()
        }

        var canDrag = false
        fun enableDrag() {
            canDrag = true
            helper.enableDrag()
            animationRefresh()
        }

        private fun animationRefresh() {
            map.keys.forEachIndexed { index, _ ->
                notifyItemChanged(index)
            }
        }

        override fun getItemCount() = map.size

        override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
            holder.bind(map.values.elementAt(position))
        }
    }

    @Suppress("ResourceType")
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
            addView(search, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = 20
                rightMargin = 20
                topMargin = statusBarHeight + 20
            })
            addView(
                recycler, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
                )
            )
        })
        var column = 3
        val colTxt = readConfig(colConfig)
        if (colTxt.isNotEmpty()) {
            val col = colTxt.toIntOrNull()
            if (col != null && col > 0) column = col
        }
        recycler.layoutManager = GridLayoutManager(this, column)
        val side =
            resources.displayMetrics.widthPixels / (recycler.layoutManager as GridLayoutManager).spanCount
        val density = resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT
        val mTextSize = side / density * 0.135f
        val paddingSide = side / 5
        val halfSide = side / 2
        recycler.setPadding(0, paddingSide, 0, paddingSide)
        val topVal = side / 8
        val pm = packageManager
        val iconPack = IconPackManager(pm).isSupportedIconPacks().firstOrNull()
        for (app in pm.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(
                Intent.CATEGORY_LAUNCHER
            ), PackageManager.GET_ACTIVITIES
        )) {
            val appName = app.loadLabel(pm).toString()
            if (appName.contains("输入法") || appName.contains(
                    "gboard", ignoreCase = true
                ) || packageName == app.activityInfo.packageName || app.activityInfo.packageName == iconPack?.packageName
            ) continue
            val launchIntent =
                Intent().setClassName(app.activityInfo.packageName, app.activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            var icon = app.loadIcon(pm)
            if (iconPack != null) {
                val customIcon = iconPack.loadIcon(launchIntent)
                val iconPackName = iconPack.name.toString().lowercase()
                if (customIcon == null) {
                    icon = if (iconPackName.contains("aura")) { //这些图标默认是圆角
                        iconPack.transformIcon(
                            icon, 0.3f, 0.92f
                        )
                    } else {
                        if (iconPackName.contains("delta")) {
                            iconPack.transformIcon(
                                icon, 0.5f, 0.89f, 0.72f
                            )
                        } else {
                            iconPack.transformIcon(
                                icon, 0.5f, 0.89f
                            )
                        }
                    }
                } else {
                    icon = customIcon
                }
            }
            val app0 = AppModel(
                appName,
                icon,
                mTextSize,
                side,
                halfSide,
                launchIntent,
                Uri.parse("package:${app.activityInfo.packageName}"),
                topVal,
                app.activityInfo.packageName
            )
            appMap[app.activityInfo.packageName] = app0
            if (app0.pinyin.isEmpty()) {
                for (c in appName) {
                    try {
                        app0.pinyin += PinyinHelper.toHanyuPinyinStringArray(c)
                            .joinToString(transform = { it.replace(Regex("\\d+"), "") })
                    } catch (_: Exception) {
                    }
                }
            }
        }
        val sortsTxt = readConfig(sortConfig)
        if (sortsTxt.isNotEmpty()) {
            val sortArr = sortsTxt.split(";")
            val sortedList = appMap.map { it.value }.sortedBy {
                val index = sortArr.indexOf(it.backupKey)
                if (index == -1) 999 else index
            }
            appMap.clear()
            sortedList.forEach {
                appMap[it.backupKey] = it
            }
        }
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(c: Context, i: Intent) {
                val packageName = i.dataString.toString().split(':').last()
                val adapter = recycler.adapter as AppListAdapter
                when (i.action) {
                    Intent.ACTION_PACKAGE_ADDED -> {
                        if (!adapter.map.containsKey(packageName)) recreate()
                    }

                    Intent.ACTION_PACKAGE_REMOVED -> {
                        try {
                            pm.getApplicationEnabledSetting(packageName)
                        } catch (_: IllegalArgumentException) {
                            var delIndex = -1
                            adapter.map.onEachIndexed { index, entry ->
                                if (entry.key == packageName) delIndex = index
                            }
                            if (delIndex != -1) {
                                adapter.map.remove(packageName)
                                adapter.notifyItemRemoved(delIndex)
                            }
                            if (appMap.containsKey(packageName)) {
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
        recycler.adapter = AppListAdapter(appMap)
    }

    fun writeConfig(config: File, configTxt: String) {
        val out = PrintWriter(config)
        out.print(configTxt)
        out.close()
    }

    private fun readConfig(config: File): String {
        var configTxt = ""
        if (config.exists()) {
            val reader = FileReader(config)
            configTxt = reader.readText()
            reader.close()
        }
        return configTxt
    }

    override fun onResume() {
        super.onResume()
        clearSearchText()
    }

    private fun clearSearchText() {
        if (search.isFocusable || search.query.toString().isNotEmpty()) {
            search.setQuery("", false)
        }
        search.clearFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            clearSearchText()
            val adapter = recycler.adapter
            if (adapter is AppListAdapter) {
                if (adapter.canDrag)
                    adapter.disableDrag()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    @Suppress("NotifyDataSetChanged", "UNCHECKED_CAST")
    override fun onQueryTextChange(newText: String?): Boolean {
        val appAdapter = recycler.adapter
        if (appAdapter is AppListAdapter) {
            appAdapter.map = appMap.clone() as LinkedHashMap<String, AppModel>
            if (newText != null) {
                if (1 == newText.toSet().size && newText.length > 8) {
                    val num = newText[0].digitToIntOrNull()
                    if (num != null && num > 0) {
                        if (num == 3) {
                            colConfig.delete()
                        } else {
                            writeConfig(colConfig, num.toString())
                        }
                        recreate()
                    }
                }
                val text = newText.replace(Regex("\\s+"), "")
                if (text.isNotEmpty()) {
                    if (text == "设置壁纸") {
                        try {
                            startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SET_WALLPAPER),
                                    "选择壁纸"
                                )
                            )
                        } catch (_: java.lang.Exception) {

                        }
                        return true
                    }
                    val removeList = arrayListOf<String>()
                    for (it in appAdapter.map) {
                        if (!it.value.appName.contains(
                                text, ignoreCase = true
                            ) && !it.value.pinyin.contains(
                                text, ignoreCase = true
                            ) && !it.key.contains(text, ignoreCase = true)
                        ) {
                            removeList.add(it.key)
                        }
                    }
                    removeList.forEach { appAdapter.map.remove(it) }
                }
            }
            appAdapter.notifyDataSetChanged()
        }
        return false
    }

    class HolderTouchHelper : ItemTouchHelper.SimpleCallback(0, 0) {
        private val helper = ItemTouchHelper(this)
        fun attachToView(view: RecyclerView?) = helper.attachToRecyclerView(view)
        fun disableDrag() {
            setDefaultDragDirs(0)
        }

        fun enableDrag() {
            setDefaultDragDirs(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN)
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) = Unit
        private var viewHolder: AppViewHolder? = null
        override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if (this.viewHolder == null && viewHolder == null) return
            when (actionState) {
                ItemTouchHelper.ACTION_STATE_DRAG -> {
                    if (viewHolder is AppViewHolder) {
                        this.viewHolder = viewHolder
                        viewHolder.stopAnimation()
                    }
                }

                ItemTouchHelper.ACTION_STATE_IDLE -> {
                    val holder = this.viewHolder
                    if (holder is AppViewHolder) {
                        holder.startAnimation()
                        this.viewHolder = null
                    }
                }
            }
        }

        override fun onMove(
            recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder
        ): Boolean {
            if (target is AppViewHolder) {
                val sourcePos = viewHolder.layoutPosition
                val targetPos = target.layoutPosition
                val adapter = recyclerView.adapter
                if (adapter is AppListAdapter) {
                    if (sourcePos < targetPos) {
                        for (i in sourcePos until targetPos) {
                            adapter.swap(i, i + 1)
                        }
                    } else {
                        for (i in sourcePos downTo targetPos + 1) {
                            adapter.swap(i, i - 1)
                        }
                    }
                    adapter.notifyItemMoved(sourcePos, targetPos)
                }
            }
            return true
        }
    }

    override fun onLongClick(p0: View?): Boolean {
        val adapter = recycler.adapter
        if (adapter is AppListAdapter) {
            if (adapter.canDrag) {
                adapter.disableDrag()
            } else {
                adapter.enableDrag()
            }
        }
        return true
    }
}