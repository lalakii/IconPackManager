import com.android.build.gradle.tasks.PackageAndroidArtifact
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "cn.lalaki.tinydesk"
    compileSdkPreview = "VanillaIceCream"
    defaultConfig {
        applicationId = namespace
        minSdk = 14
        //noinspection ExpiredTargetSdkVersion
        targetSdk = 29
        versionCode = 4
        versionName =
            "$versionCode.${
                ZonedDateTime.now().toLocalDate().format(DateTimeFormatter.ofPattern("MMdd"))
            }"
    }
    signingConfigs {
        register("lalaki_config") {
            enableV1Signing = true
            enableV2Signing = false
            enableV3Signing = false
            enableV4Signing = false
            keyAlias = "dazen@189.cn"
            storeFile = file("D:\\imoe.jks")
            storePassword = System.getenv("mystorepass")
            keyPassword = System.getenv("mystorepass2")
        }
    }
    buildTypes {
        named("release") {
            signingConfig = signingConfigs.getByName("lalaki_config")
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = true
            isJniDebuggable = false
            isDebuggable = false
            setProguardFiles(
                setOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
                )
            )
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
    packaging.resources.excludes.addAll(
        mutableSetOf(
            "META-INF",
            "META-INF/**",
            "kotlin/**",
            "DebugProbesKt.bin",
            "kotlin-tooling-metadata.json",
        )
    )
    buildToolsVersion = "35.0.0 rc2"
}

dependencies {
    implementation(project(":library"))
    implementation("com.belerweb:pinyin4j:2.5.1")
}

tasks.withType<PackageAndroidArtifact> {
    doFirst { appMetadata.asFile.get().writeText("") }
}

tasks.configureEach {
    if (arrayOf("aarmetadata", "artprofile", "jni", "native").any {
            name.lowercase().contains(it)
        }) {
        enabled = false
    }
    if (this.name.contains("assembleRelease")) {
        this.doLast {
            android.applicationVariants.all {
                outputs.forEach {
                    val outputDirectory = it.outputFile.parent
//                    if (outputDirectory.contains("release"))
//                        exec {
//                            executable = "cmd.exe"
//                            args("/c", "start", outputDirectory)
//                        }
                }
            }
        }
    }
}