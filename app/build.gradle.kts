import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.android.application")
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}
android {
    namespace = "cn.lalaki.tinydesk"
    compileSdkPreview = "CinnamonBun"
    defaultConfig {
        applicationId = namespace
        minSdk = 21
        targetSdk = 37
        versionCode = 8
        versionName =
            "$versionCode.${
                ZonedDateTime.now().toLocalDate().format(DateTimeFormatter.ofPattern("MMdd"))
            }"
        @Suppress("Deprecation")
        val config = resourceConfigurations
        config.clear()
        config.add("en")
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
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }
    buildToolsVersion = "37.0.0"
}
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_24
    }
}
dependencies {
    implementation(project(":library"))
    implementation("cn.lalaki:pinyin4j-chinese-simplified:1.0.7")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
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
