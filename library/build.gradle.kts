plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    signing
    id("cn.lalaki.publisher") version "1.0.5"
}
android {
    namespace = "cn.lalaki.iconpackmanager"
    compileSdk = 35
    version = 7.0
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
    buildToolsVersion = "35.0.0"
}
tasks.configureEach {
    if (name.contains("AarMetadata", ignoreCase = true)) {
        enabled = false
    }
}
signing {
    useGpgCmd()
}
centralPortal {
    name = rootProject.name
    group = "cn.lalaki"
    username = System.getenv("TEMP_USER")
    password = System.getenv("TEMP_PASS")
    publishingType = cn.lalaki.pub.internal.BaseCentralPortalExtension.PublishingType.USER_MANAGED
    pom {
        url = "https://github.com/lalakii/IconPackManager"
        description = "Library for loading icon pack resources."
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
