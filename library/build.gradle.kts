import com.android.build.gradle.internal.tasks.AarMetadataTask

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
    signing
}
android {
    namespace = "cn.lalaki.iconpackmanager"
    compileSdkPreview = "VanillaIceCream"
    version = 6.7
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
    kotlinOptions.jvmTarget = "17"
    buildToolsVersion = "35.0.0 rc4"
}
tasks.withType<AarMetadataTask> {
    isEnabled = false
}
tasks.configureEach {
    if (name.contains("checkDebugAndroidTestAarMetadata", ignoreCase = true)) {
        enabled = false
    }
}
publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            val publishToLocal = false
            if (publishToLocal) {
                url = uri("D:\\repo\\")
            } else {
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = "iamverycute"
                    password = System.getenv("my_final_password")
                }
            }
        }
    }
    publications {
        create<MavenPublication>("release") {
            val githubUrl = "https://github.com/lalakii/IconPackManager"
            artifactId = "IconPackManager"
            groupId = "cn.lalaki"
            afterEvaluate { artifact(tasks.named("bundleReleaseAar")) }
            pom {
                name = "IconPackManager"
                description = "Library for loading icon pack resources."
                url = githubUrl
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        name = "lalakii"
                        email = "dazen@189.cn"
                    }
                }
                scm {
                    url = githubUrl
                    connection = "scm:git:$githubUrl.git"
                    developerConnection = "scm:git:$githubUrl.git"
                }
            }
        }
    }
}
signing {
    useGpgCmd()
    sign(publishing.publications["release"])
}
