import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("cn.lalaki.repack") version "1.0.11"
}
android {
    namespace = "cn.lalaki.tinydesk"
    compileSdkPreview = "VanillaIceCream"
    defaultConfig {
        applicationId = namespace
        minSdk = 14
        //noinspection ExpiredTargetSdkVersion
        targetSdk = 29
        versionCode = 5
        versionName =
            "$versionCode.${
                ZonedDateTime.now().toLocalDate().format(DateTimeFormatter.ofPattern("MMdd"))
            }"
        resourceConfigurations.add("en")
    }
    signingConfigs {
        getByName("debug") {
            storeFile = file("D:\\imoe.jks")
            storePassword = System.getenv("mystorepass")
            keyAlias = "dazen@189.cn"
            keyPassword = System.getenv("mystorepass2")
        }
        register("release") {
            storeFile = file("D:\\imoe.jks")
            storePassword = System.getenv("mystorepass")
            keyAlias = "dazen@189.cn"
            keyPassword = System.getenv("mystorepass2")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            isJniDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions.jvmTarget = "21"
    buildToolsVersion = "35.0.0 rc4"
}
repackConfig {
    sevenZip =
        File("C:\\Users\\sa\\Downloads\\7z2404-extra\\x64\\7za.exe") //7zip的可执行文件（控制台版本），可以在：https://www.7-zip.org 下载
    resign = true //对重新打包的apk签名
    addV2Sign = false //v2签名，android9以下需要
    addV1Sign = true
    disableV3V4 = true
    blacklist = arrayOf(
        "META-INF",
        "kotlin-tooling-metadata.json",
        "kotlin"
    ) //重新打包时，可以排除某些无用的文件或文件夹，可以为null
    quiet = false //控制台输出日志
}
dependencies {
    implementation(project(":library"))
    implementation("com.belerweb:pinyin4j:2.5.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0-alpha01")
}
tasks.configureEach {
    if (arrayOf("aarmetadata", "artprofile", "jni", "native").any {
            name.contains(it, ignoreCase = true)
        }) {
        enabled = false
    }
}