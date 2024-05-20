# IconPackManager
[![Maven Central](https://img.shields.io/maven-central/v/com.iamverycute/IconPackManager.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.iamverycute/IconPackManager/) ![API: 14+ (shields.io)](https://img.shields.io/badge/API-14+-green) ![License: Apache-2.0 (shields.io)](https://img.shields.io/badge/license-Apache--2.0-brightgreen)

[ [中文说明](#) | [English](README.md) ]

**快速加载图标包**

*用于读取图标包资源的库，支持自定义关键词查找图标，支持图标裁剪、缩放以及色彩饱和度修改。演示app，是一个简易的安卓桌面，可以更加直观的了解这个库的用处。*

## 必要条件
+ SDK 版本 大于 14
+ kotlin
+ 测试图标包 [Pure Icon Pack](https://www.coolapk.com/apk/me.morirain.dev.iconpack.pure) 或你自己喜欢的

## 快速开始

1. 导入

    使用Gradle，或者直接下载AAR [IconPackManager](https://github.com/lalakii/IconPackManager/releases)

    ```kotlin
    dependencies {
        implementation("com.iamverycute:IconPackManager:6.6")
    }
    ```

2. 代码示例

   ```kotlin
   import com.iamverycute.iconpackmanager.IconPackManager
   
   // IconPackManager
   val ipm = IconPackManager(packageManager)
   
   val iconPacks = ipm.isSupportedIconPacks()
   
   ipm.isSupportedIconPacks(true)// 强制刷新缓存，如果需要
   
   iconPacks.forEach {
       //……遍历所有图标包
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
   <resources> <!-- appfilter.xml, 这是我从图标包解压出来的文件，位于图标包的assets目录 -->
       <!-- component 的 值即为 规则中定义的value 这是其中一段示例内容 -->
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
       val icon = it.loadIcon(launchIntent)  //可选Intent or ComponentName or ApplicationInfo
       
       //如果未找到图标，可选 裁剪原始图标
       if(icon == null)
       {
           //裁剪圆形
           it.transformIcon(applicationInfo.loadIcon(packageManager),0.5f,scaleF,saturation)
           
           //裁剪圆角 需要自己定义一些参数 radius是半径，scaleF是图标缩放，saturation是色彩饱和度。Float可变类型参数，默认值为1f
           it.transformIcon(applicationInfo.loadIcon(packageManager),radius,scaleF,saturation)
       }
   }
   ```

## 演示

<img src="https://cdn.jsdelivr.net/gh/lalakii/IconPackManager/video/demo.gif?v=6.0" width="240">

## 关于

为爱发电。

+ feedback：dazen@189.cn

