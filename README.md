# IconPackManager

**Quick load icon pack.**

*Read the icon package resources, unify the size, and crop the original icons if the resources do not contain theme icons*

## Prerequisites
+ SDK Version >= 29
+ kotlin
+ install the custom icon pack

## Quick Start

1. Import AAR

+ Gradle or Download [IconPackManager](https://github.com/iamverycute/IconPackManager/releases)

```groovy
dependencies {
    implementation 'com.iamverylovely:IconPackManager:2.0' //release
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
<resources> <!--appfilter.xml-->
    <!-- Get the keyword from the "component" property -->
    <item component="ComponentInfo{cn.nubia.browser/com.android.browser.BrowserLauncher}" drawable="browser"/>
    <item component="ComponentInfo{com.android.browser/com.android.browser.BrowserActivity}" drawable="browser"/>
    <item component="ComponentInfo{com.motorola.camera/com.motorola.camera.Camera}" drawable="camera_2"/>
    <item component="ComponentInfo{cn.nubia.deskclock.preset/cn.nubia.deskclock.DeskClock}" drawable="clock"/>
    <item component="ComponentInfo{com.android.alarmclock/com.meizu.flyme.alarmclock.DeskClock}" drawable="flyme_clock"/>
    <item component="ComponentInfo{com.android.BBKClock/com.android.BBKClock.Timer}" drawable="clock"/>
</resources>
```

```kotlin
// load icon pack
ipm.isSupportedIconPacks().forEach {
    //get icon pack name
    val iconPackName = it.value.name

    //get icon from icon pack
    val icon = it.value.getDrawableIconWithApplicationInfo(applicationInfo)              
}
```

## Demo

![img0](https://cdn.jsdelivr.net/gh/iamverycute/IconPackManager/video/demo.gif)

## About

Generating electricity for love.

+ feedback：dazen@189.cn

