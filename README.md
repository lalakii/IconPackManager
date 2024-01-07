# IconPackManager
[![Maven Central](https://img.shields.io/maven-central/v/com.iamverycute/IconPackManager.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.iamverycute/IconPackManager/) ![API: 14+ (shields.io)](https://img.shields.io/badge/API-14+-green) ![License: Apache-2.0 (shields.io)](https://img.shields.io/badge/license-Apache--2.0-brightgreen)

[ [中文说明](README_zh_cn.md) | [English](#) ]

**Quick load icon pack.**

*Read icon pack resources, support keywords to find icons, support icon cropping. The demo app, a simple Android desktop, allows for a more intuitive understanding of the usefulness of this library*

## Prerequisites
+ SDK Version >= 14
+ kotlin
+ install the custom icon pack [Pure Icon Pack](https://www.coolapk.com/apk/me.morirain.dev.iconpack.pure) or Others you like

## Quick Start

1. Import AAR

+ Gradle or Download [IconPackManager](https://github.com/lalakii/IconPackManager/releases)

```groovy
dependencies {
    implementation 'com.iamverycute:IconPackManager:4.8'
}
```

2. Code Sample

```kotlin
import com.iamverycute.iconpackmanager.IconPackManager

// IconPackManager
val ipm = IconPackManager(applicationContext)

val iconPacks = ipm.isSupportedIconPacks()

iconPacks.forEach {
    //……
}

// Add rules for a custom icon
iconPackItem.addRule("com.android.chrome", "browser")
        .addRule("com.android.email", "mail","message") /** Use this method to add rules when you need to specify icons for an application, 
                                                 parameter 1: package name, parameter 2: keyword (icon resource name) Fuzzy Matching
                                                 How to get keywords? see icon pack.apk assets/appfilter.xml
                                                 */

// clear all rules, if need
iconPackItem.clearRules()
```
```xml
<resources> <!-- appfilter.xml, extract [icon pack].apk, see the assets directory -->
    <!-- Get the keyword from the "component" property -->
    <item component="ComponentInfo{cn.nubia.browser/com.android.browser.BrowserLauncher}" drawable="browser"/>
    <item component="ComponentInfo{com.android.browser/com.android.browser.BrowserActivity}" drawable="browser"/>
    <item component="ComponentInfo{com.motorola.camera/com.motorola.camera.Camera}" drawable="camera_2"/>
    <item component="ComponentInfo{cn.nubia.deskclock.preset/cn.nubia.deskclock.DeskClock}" drawable="clock"/>
    <item component="ComponentInfo{com.android.alarmclock/com.meizu.flyme.alarmclock.DeskClock}" drawable="flyme_clock"/>
    <item component="ComponentInfo{com.android.BBKClock/com.android.BBKClock.Timer}" drawable="clock"/>
</resources>
```
*ipm.isSupportedIconPacks() is a list that can be traversed to get all the icon packs.*
```kotlin
// load icon pack
ipm.isSupportedIconPacks().forEach {
    /** If you have more than one icon pack theme installed, you need to exclude it here
    filter other icon pack**/
    // if (it.name != "your icon pack")
    
    //get icon pack name
    val iconPackName = it.name

    //get icon from icon pack
    val launchIntent = getLaunchIntentForPackage(packageName)
    val icon = it.loadIcon(launchIntent)     
    
    //if not found icon
    if(icon == null)
    {
        //Cutting circles
        it.iconCutCircle(applicationInfo.loadIcon(packageManager).toBitmap(),side,scaleF)
        
        //Rounded corner
        it.iconCut(applicationInfo.loadIcon(packageManager).toBitmap(),side,radius,scaleF)
    }
}
```

## Demo

![img0](https://cdn.jsdelivr.net/gh/lalakii/IconPackManager/video/demo.gif?v=4.8)

## About

Generating electricity for love.

+ feedback：dazen@189.cn

