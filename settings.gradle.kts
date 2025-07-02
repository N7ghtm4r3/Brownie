rootProject.name = "Brownie"

pluginManagement {
    plugins {
        kotlin("jvm") version "2.2.0"
        kotlin("multiplatform") version "2.2.0"
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

include("backend")
include("core")
