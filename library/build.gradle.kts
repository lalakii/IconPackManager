import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("cn.lalaki.central") version "3.0.3"
}
group = "cn.lalaki"
android {
    namespace = "cn.lalaki.iconpackmanager"
    compileSdkPreview = "CinnamonBun"
    version = 8.6
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildToolsVersion = "37.0.0"
    publishing {
        singleVariant("release")
    }
}
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}
centralPortalPlus {
    tokenXml = uri("D:\\BIN\\token.txt")
}
publishing {
    repositories {
        mavenLocal()
    }
    publications {
        create<MavenPublication>("IconPackManager") {
            afterEvaluate { from(components["release"]) }
            pom {
                name = "IconPackManager"
                artifactId = "IconPackManager"
                url = "https://github.com/lalakii/IconPackManager"
                description = "Library for loading icon pack resources."
                inceptionYear = "2023"
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
                        email = "i@lalaki.cn"
                        organization = "lalaki.cn"
                        organizationUrl = "https://github.com/lalakii"
                        roles = listOf("developer")
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
