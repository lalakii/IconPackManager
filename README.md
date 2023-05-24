# IconPackManager
[![Maven Central](https://img.shields.io/maven-central/v/com.iamverylovely/IconPackManager.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.iamverylovely/IconPackManager/) ![API: 19-33 (shields.io)](https://img.shields.io/badge/API-21+-green) ![License: Apache-2.0 (shields.io)](https://img.shields.io/badge/license-Apache--2.0-brightgreen)

**Quick load icon pack.**

*Read the icon package resources, unify the size, and crop the original icons if the resources do not contain theme icons*

## Prerequisites
+ SDK Version >= 21
+ kotlin
+ install the custom icon pack [Pure Icon Pack](https://www.coolapk.com/apk/me.morirain.dev.iconpack.pure) or Others you like

## Quick Start

1. Import AAR

+ Gradle or Download [IconPackManager](https://github.com/iamverycute/IconPackManager/releases)

```groovy
dependencies {
    implementation 'com.iamverylovely:IconPackManager:3.3' //release
}
```

2. Code Sample

```kotlin
import com.iamverycute.iconpackmanager.IconPackManager

// IconPackManager
val ipm = IconPackManager(applicationContext)

// Add rules for custom icon
ipm.addRule("com.android.chrome", "browser")
        .addRule("com.android.email", "mail") /** Use this method to add rules when you need to specify icons for an application, 
                                                 parameter 1: package name, parameter 2: keyword (icon resource name) Fuzzy Matching
                                                 How to get keywords? see icon pack.apk assets/appfilter.xml
                                                 */
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
*Iterate over this Map(ipm.isSupportedIconPacks()) to get all the icon package names if necessary.*
```kotlin
// load icon pack
ipm.isSupportedIconPacks().forEach {
    /** If you have more than one icon pack theme installed, you need to exclude it here
    filter other icon pack**/
    // if (it.value.name != "your icon pack")
    
    //get icon pack name
    val iconPackName = it.value.name

    //get icon from icon pack
    val icon = it.value.loadIcon(applicationInfo)              
}
```

## Demo

![img0](https://cdn.jsdelivr.net/gh/iamverycute/IconPackManager/video/demo.gif)

## About

Generating electricity for love.

+ feedback：dazen@189.cn

