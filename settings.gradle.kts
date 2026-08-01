rootProject.name = "io.github.astridha.smalldecimal"
include(":library")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage") // See https://github.com/gradle/gradle/issues/32443
    repositories {
        google()
        mavenCentral()
    }
}

