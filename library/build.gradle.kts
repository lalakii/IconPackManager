import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    signing
    `maven-publish`
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("cn.lalaki.central") version "2.0.2"
}
android {
    namespace = "cn.lalaki.iconpackmanager"
    compileSdkPreview = "CinnamonBun"
    version = 8.1
    buildTypes {
        named("release") {
            isMinifyEnabled = false
            isDefault = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }
    buildToolsVersion = "36.1.0"
}
tasks.configureEach {
    if (name.contains("AarMetadata", ignoreCase = true)) {
        // enabled = false
    }
}
signing {
    useGpgCmd()
    sign(publishing.publications)
}
centralPortalPlus {
    tokenXml = uri("D:\\BIN\\token.txt")
}
group = "cn.lalaki"

publishing {
    repositories {
        maven {
            url = uri("D:\\repo\\")
        }
    }
    publications {
        create<MavenPublication>("IconPackManager") {
            afterEvaluate { artifact(tasks.getByName("bundleReleaseAar")) }
            pom {
                name = "IconPackManager"
                artifactId = "IconPackManager"
                url = "https://github.com/lalakii/IconPackManager"
                description = "Library for loading icon pack resources."
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                issueManagement {
                    url = "https://github.com/lalakii/IconPackManager/issues"
                }
                developers {
                    developer {
                        name = "lalakii"
                        email = "dazen@189.cn"
                        organization = "lalaki.cn"
                        organizationUrl = "https://github.com/lalakii"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/lalakii/IconPackManager"
                    developerConnection = "scm:git:https://github.com/lalakii/IconPackManager"
                    url = "https://github.com/lalakii/IconPackManager"
                }
            }
        }
    }
}