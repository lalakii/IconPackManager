import com.android.build.gradle.tasks.PackageAndroidArtifact
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "cn.lalaki.tinydesk"
    compileSdk = 34
    defaultConfig {
        applicationId = namespace
        minSdk = 14
        targetSdk = 34
        versionCode = 3
        versionName =
            "$versionCode.${
                ZonedDateTime.now().toLocalDate().format(DateTimeFormatter.ofPattern("MMdd"))
            }"
    }
    signingConfigs {
        register("lalaki_config") {
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
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
        sourceCompatibility = JavaVersion.VERSION_20
        targetCompatibility = JavaVersion.VERSION_20
    }
    kotlinOptions.jvmTarget = "20"
    packaging.resources.excludes.addAll(
        mutableSetOf(
            "META-INF",
            "META-INF/**",
            "kotlin/**",
            "DebugProbesKt.bin",
            "kotlin-tooling-metadata.json",
        )
    )
}

dependencies {
    implementation(project(":library"))
}

tasks.withType<PackageAndroidArtifact> {
    doFirst { appMetadata.asFile.get().writeText("") }
}

tasks.configureEach {
    if (arrayOf("aarmetadata", "artprofile", "debug", "jni", "native").any {
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