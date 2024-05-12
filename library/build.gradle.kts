import com.android.build.gradle.internal.tasks.AarMetadataTask

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
    signing
}
android {
    namespace = "com.iamverycute.iconpackmanager"
    compileSdk = 34
    version = 6.6
    buildTypes {
        named("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions.jvmTarget = "17"
    base.archivesName = namespace
}
tasks.withType<AarMetadataTask> {
    isEnabled = false
}
tasks.configureEach {
    if (name.contains("checkDebugAndroidTestAarMetadata"))
        enabled = false
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
            artifactId = "IconPackManager"
            groupId = "com.iamverycute"
            afterEvaluate { artifact(tasks.named("bundleReleaseAar")) }
            pom {
                name = "IconPackManager"
                description = "Library for loading icon pack resources."
                url = "https://github.com/lalakii/IconPackManager"
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
                    connection = "scm:git:https://github.com/lalakii/IconPackManager.git"
                    developerConnection = "scm:git:https://github.com/lalakii/IconPackManager.git"
                    url = "https://github.com/lalakii/IconPackManager"
                }
            }
        }
    }
}
signing {
    useGpgCmd()
    sign(publishing.publications["release"])
}