# IconPackManager
[![Maven Central](https://img.shields.io/maven-central/v/cn.lalaki/IconPackManager.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/cn.lalaki/IconPackManager/) ![API: 14+ (shields.io)](https://img.shields.io/badge/API-14+-green) ![License: Apache-2.0 (shields.io)](https://img.shields.io/badge/license-Apache--2.0-brightgreen)

[ [中文说明](README_zh_cn.md) | [English](#) ]

**Quick load icon pack.**

*Library for reading icon pack resources, support custom keywords to find icons, support icon cropping, scaling and color saturation modification. Demo app, a simple Android desktop, can be more intuitive to understand the usefulness of this library.*

## Prerequisites
+ SDK Version >= 14
+ kotlin
+ install the custom icon pack [Pure Icon Pack](https://apkpure.net/cn/pure-circle-icon-pack/me.morirain.dev.iconpack.pure) or Others you like

## Quick Start

1. Import AAR

    Gradle or Download [IconPackManager](https://github.com/lalakii/IconPackManager/releases)

    ```kotlin
    dependencies {
        implementation("cn.lalaki:IconPackManager:6.7")
    }
    ```

2. Code Sample

   ```kotlin
   import cn.lalaki.iconpackmanager.IconPackManager
   
   // IconPackManager
   val ipm = IconPackManager(packageManager)
   
   val iconPacks = ipm.isSupportedIconPacks()
   
   ipm.isSupportedIconPacks(true) //Force Flush IconPack cache
   
   iconPacks.forEach {
       //……
   }
   
   // Add rules for a custom icon
   val rules = HashMap<String, Array<out String>>
   rules["com.android.chrome"]=arrayOf("browser","...")
   rules["com.android.email"]=arrayOf("mail","message")
   rules["video"]=arrayOf("tencent","youtube")
   iconPackItem.setRules(rules)
   /** Use this method to add rules when you need to specify icons for an application, 
                                                    parameter 1: package name, parameter 2: keyword (icon resource name) Fuzzy Matching
                                                    How to get keywords? see icon pack.apk assets/appfilter.xml
                                                    */
   
   // clear all rules, if need
   iconPackItem.setRules(null)
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
       ...
       ...
       ...
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
       val icon = it.loadIcon(launchIntent)   //Intent or ComponentName or ApplicationInfo
       
       //if not found icon, modify the original icon
       if(icon == null)
       {
           //Cutting circles
           it.transformIcon(applicationInfo.loadIcon(packageManager),0.5f,scaleF,saturation)
   
           //Cropping rounded corners requires a number of parameters to be defined,
           // in order of radius, scale, and color saturation, 
           // and a variable type parameter, Float, which defaults to 1f.
           it.transformIcon(applicationInfo.loadIcon(packageManager),radius,scaleF,saturation)
       }
   }
   ```

## Demo

<img src="https://cdn.jsdelivr.net/gh/lalakii/IconPackManager/video/demo.gif?v=6.0" width="240">

## About

Generating electricity for love.

+ feedback：dazen@189.cn