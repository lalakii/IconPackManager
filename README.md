# IconPackManager

Quick load icon pack.

Prerequisites
+ SDK Version >= 29

## Quick Start

1. Import AAR

+ Gradle or Download [IconPackManager](https://github.com/iamverycute/IconPackManager/releases)

```groovy
dependencies {
    implementation 'com.iamverylovely:IconPackManager:1.0' //release
}
```

2. Code Sample

```kotlin
import com.iamverycute.iconpackmanager.IconPackManager

// IconPackManager
val ipm = IconPackManager(applicationContext)

// Add rules for custom icon
ipm.addRule("com.android.chrome", "browser")
        .addRule("com.iamverycute.example", "launcher")

// load icon pack
ipm.isSupportedIconPacks().iterator().forEach {
    //get icon pack name
    val iconPackName = it.value.name

    //get icon from icon pack
    val icon = it.value.getDrawableIconWithApplicationInfo(applicationInfo)              
}
```

## About

Generating electricity for love.

+ feedback：dazen@189.cn

