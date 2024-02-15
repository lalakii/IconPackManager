import com.android.build.gradle.internal.tasks.AarMetadataTask

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.iamverycute.iconpackmanager"
    compileSdk = 34
    version = 6.0
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
    if (name == "assembleRelease") {
        doLast {
            val buildRoot = file("${project.layout.buildDirectory.get()}\\outputs\\aar")
            val pomXml = """<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <!-- This module was also published with a richer model, Gradle metadata,  -->
  <!-- which should be used instead. Do not delete the following line which  -->
  <!-- is to indicate to Gradle or any Gradle module metadata file consumer  -->
  <!-- that they should prefer consuming it instead. -->
  <!-- do_not_remove: published-with-gradle-metadata -->
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.iamverycute</groupId>
  <artifactId>IconPackManager</artifactId>
  <version>${version}</version>
  <packaging>aar</packaging>
  <name>IconPackManager</name>
  <description>Get all icon pack applications, read the corresponding icon resources and also customize rules to find icons.</description>
  <url>https://github.com/lalakii/IconPackManager</url>
  <inceptionYear>2024</inceptionYear>
  <licenses>
    <license>
      <name>The Apache Software License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <developers>
    <developer>
      <name>lalakii</name>
      <email>dazen@189.cn</email>
      <organization>lalaki.cn</organization>
    </developer>
  </developers>
  <scm>
    <connection>scm:git:https://github.com/lalakii/IconPackManager.git</connection>
    <url>https://github.com/lalakii/IconPackManager</url>
  </scm>
</project>
"""
            val pomFile = file("${buildRoot.absolutePath}\\IconPackManager-${version}.pom")
            pomFile.writeText(pomXml)
            sign(pomFile.parentFile, "*.pom")
            sign(pomFile.parentFile, "*.aar")
            val osName = System.getProperty("os.name").lowercase()
            if (osName.contains("windows"))
                openExplorer()
        }
    }
}

fun openExplorer() {
    exec {
        executable = "cmd.exe"
        args(
            "/c",
            "sleep",
            "3",
            "&&",
            "start",
            "${project.layout.buildDirectory.get()}\\outputs\\aar\\"
        )
    }
}

fun sign(path: File, fileName: String) {
    try {
        exec {
            workingDir = path
            executable = "gpg"
            args("--yes", "--armor", "--detach-sign", fileName)
        }
    } catch (_: Exception) {
        System.err.println("Please install GnuPG: https://gnupg.org/download/")
    }
}