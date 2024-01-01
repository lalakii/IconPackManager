# IconPackManager
[![Maven Central](https://img.shields.io/maven-central/v/com.iamverycute/IconPackManager.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.iamverylovely/IconPackManager/) ![API: 21+ (shields.io)](https://img.shields.io/badge/API-21+-green) ![License: Apache-2.0 (shields.io)](https://img.shields.io/badge/license-Apache--2.0-brightgreen)

[ [中文说明](#) | [English](README.md) ]

**快速加载图标包**

*读取图标包资源，支持关键词查找图标，支持图标裁剪。演示app，是一个简易的安卓桌面，可以更加直观的了解这个库的用处。*

## Prerequisites
+ SDK 版本 大于 21
+ kotlin
+ 测试图标包 [Pure Icon Pack](https://www.coolapk.com/apk/me.morirain.dev.iconpack.pure) 或你自己喜欢的

## Quick Start

1. 导入

+ 使用Gradle，或者直接下载AAR [IconPackManager](https://github.com/iamverycute/IconPackManager/releases)

```groovy
dependencies {
    implementation 'com.iamverycute:IconPackManager:4.0' 
}
```

2. 代码示例

```kotlin
import com.iamverycute.iconpackmanager.IconPackManager

// IconPackManager
val ipm = IconPackManager(applicationContext)

// 添加规则，当指定的包名不存在图标主题时，根据规则查找图标，第一个参数为应用包名，后面的参数为关键字，可传入多个
ipm.addRule("com.android.chrome", "browser")
        .addRule("com.android.email", "mail","message") /** Use this method to add rules when you need to specify icons for an application, 
                                                 parameter 1: package name, parameter 2: keyword (icon resource name) Fuzzy Matching
                                                 How to get keywords? see icon pack.apk assets/appfilter.xml
                                                 */

// 清除所有规则
ipm.clearRules()
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
*ipm.isSupportedIconPacks()是一个Map，遍历可获取图标包名称以及相应的工具类*
```kotlin
// load icon pack
ipm.isSupportedIconPacks().forEach {
    /** 根据图标包名称，筛选图标包 **/
    // if (it.value.name != "your icon pack")
    
    //get icon pack name
    val iconPackName = it.value.name

    //get icon from icon pack
    val icon = it.value.loadIcon(applicationInfo)       
    
    //如果未找到图标，可选
    if(icon == null)
    {
        //裁剪圆形
        it.value.iconCutCircle(applicationInfo.loadIcon(packageManager).toBitmap(),side,scaleF)
        
        //裁剪圆角 需要自己定义一些参数
        it.value.iconCut(applicationInfo.loadIcon(packageManager).toBitmap(),side,radius,scaleF)
    }
}
```

## Demo

![img0](https://cdn.jsdelivr.net/gh/iamverycute/IconPackManager/video/demo.gif)

## About

为爱发电。

+ feedback：dazen@189.cn

