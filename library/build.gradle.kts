plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
    signing
}
android {
    namespace = "cn.lalaki.iconpackmanager"
    compileSdkPreview = "VanillaIceCream"
    version = 6.9
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
tasks.configureEach {
    if (name.contains("AarMetadata", ignoreCase = true)) {
        enabled = false
    }
}
publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("D:\\repo\\")
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
