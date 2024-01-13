# IconPackManager
[![Maven Central](https://img.shields.io/maven-central/v/com.iamverycute/IconPackManager.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.iamverycute/IconPackManager/) ![API: 14+ (shields.io)](https://img.shields.io/badge/API-14+-green) ![License: Apache-2.0 (shields.io)](https://img.shields.io/badge/license-Apache--2.0-brightgreen)

[ [中文说明](#) | [English](README.md) ]

**快速加载图标包**

*读取图标包资源，支持关键词查找图标，支持图标裁剪。演示app，是一个简易的安卓桌面，可以更加直观的了解这个库的用处。*

## Prerequisites
+ SDK 版本 大于 14
+ kotlin
+ 测试图标包 [Pure Icon Pack](https://www.coolapk.com/apk/me.morirain.dev.iconpack.pure) 或你自己喜欢的

## Quick Start

1. 导入

+ 使用Gradle，或者直接下载AAR [IconPackManager](https://github.com/lalakii/IconPackManager/releases)

```groovy
dependencies {
    implementation 'com.iamverycute:IconPackManager:5.0' 
}
```

2. 代码示例

```kotlin
import com.iamverycute.iconpackmanager.IconPackManager

// IconPackManager
val ipm = IconPackManager(applicationContext)

val iconPacks = ipm.isSupportedIconPacks()

iconPacks.forEach {
    //……遍历所以图标包
}

// 为某个图标包添加自定义规则，图标包没有相应图标时，按照规则查找图标包内的图标
val rules = HashMap<String, Array<out String>> //key是包名，可模糊匹配；value是需要匹配的应用图标包名，可模糊匹配，支持多个参数
rules["com.android.chrome"]=arrayOf("browser","...")
rules["com.android.email"]=arrayOf("mail","message")
rules["video"]=arrayOf("tencent","youtube")
iconPackItem.setRules(rules)

//清除当前图标包的自定义规则
iconPackItem.setRules(null)
```
```xml
<resources> <!-- appfilter.xml, 从图标包解压出来的文件，位于assets -->
    <!-- component 的 值即为 规则中定义的value -->
    <item component="ComponentInfo{cn.nubia.browser/com.android.browser.BrowserLauncher}" drawable="browser"/>
    <item component="ComponentInfo{com.android.browser/com.android.browser.BrowserActivity}" drawable="browser"/>
    <item component="ComponentInfo{com.motorola.camera/com.motorola.camera.Camera}" drawable="camera_2"/>
    <item component="ComponentInfo{cn.nubia.deskclock.preset/cn.nubia.deskclock.DeskClock}" drawable="clock"/>
    <item component="ComponentInfo{com.android.alarmclock/com.meizu.flyme.alarmclock.DeskClock}" drawable="flyme_clock"/>
    <item component="ComponentInfo{com.android.BBKClock/com.android.BBKClock.Timer}" drawable="clock"/>
</resources>
```
*ipm.isSupportedIconPacks()是一个list，遍历可获取图标包名称以及相应的工具类*
```kotlin
// load icon pack
ipm.isSupportedIconPacks().forEach {
    /** 根据图标包名称，筛选图标包 **/
    // if (it.name != "your icon pack")
    
    //get icon pack name
    val iconPackName = it.name

    //get icon from icon pack; 
    val launchIntent = getLaunchIntentForPackage(packageName)
    val icon = it.loadIcon(launchIntent)       
    
    //如果未找到图标，可选
    if(icon == null)
    {
        //裁剪圆形
        it.iconCutCircle(applicationInfo.loadIcon(packageManager).toBitmap(),scaleF)
        
        //裁剪圆角 需要自己定义一些参数
        it.iconCut(applicationInfo.loadIcon(packageManager).toBitmap(),radius,scaleF)
    }
}
```

## Demo

![img0](https://cdn.jsdelivr.net/gh/lalakii/IconPackManager/video/demo.gif?v=5)

## About

为爱发电。

+ feedback：dazen@189.cn

