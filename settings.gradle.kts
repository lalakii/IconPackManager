pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven(url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
    }
}
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven(url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
    }
}
rootProject.name = "IconPackManager"
include(":app")
include(":library")
