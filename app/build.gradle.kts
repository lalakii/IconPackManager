import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("cn.lalaki.repack") version "3.0.0-LTS"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}
android {
    namespace = "cn.lalaki.tinydesk"
    compileSdk = 35
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
        register("release") {
            storeFile = file("D:\\imoe.jks")
            storePassword = System.getenv("mystorepass")
            keyAlias = "dazen@189.cn"
            keyPassword = System.getenv("mystorepass2")
        }
    }
    buildTypes {
        release {
            isDefault = true
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            isJniDebuggable = false
            renderscriptOptimLevel = 3
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs["release"]
        }
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_22
        targetCompatibility = JavaVersion.VERSION_22
    }
    kotlinOptions.jvmTarget = "22"
    buildToolsVersion = "35.0.0"
}
repackConfig {
    resign = true // 对重新打包的apk签名
    addV2Sign = false // v2签名，android9以下需要
    addV1Sign = true
    disableV3V4 = true
    blacklist =
        arrayOf(
            "META-INF",
            "kotlin-tooling-metadata.json",
            "kotlin",
            "unused",
        ) // 重新打包时，可以排除某些无用的文件或文件夹，可以为null
    quiet = false // 控制台输出日志
}
dependencies {
    implementation(project(":library"))
    implementation("cn.lalaki:pinyin4j-chinese-simplified:1.0.7")
    implementation("androidx.recyclerview:recyclerview:1.4.0-alpha01")
}
tasks.configureEach {
    if (arrayOf("aarmetadata", "artprofile", "jni", "native").any {
            name.contains(it, ignoreCase = true)
        }
    ) {
        enabled = false
    }
}
configurations.all {
    exclude("androidx.profileinstaller", "profileinstaller")
    exclude("androidx.versionedparcelable", "versionedparcelable")
    exclude("androidx.emoji2", "emoji2")
    exclude("androidx.appcompat", "appcompat-resources")
}
