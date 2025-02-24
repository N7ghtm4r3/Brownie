import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("maven-publish")
    id("com.android.library") version "8.2.2"
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.1.0"
}

group = "com.tecknobit.browniecore"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            this@jvm.compilerOptions {
                jvmTarget.set(JvmTarget.JVM_18)
            }
        }
    }
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_18)
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "brownie-core"
            isStatic = true
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        browser {
            webpackTask {
                dependencies {
                }
            }
        }
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation("io.github.n7ghtm4r3:equinox-core:1.0.8")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
            }
        }

    }

    jvmToolchain(18)
}


android {
    namespace = "com.tecknobit.browniecore"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "com.tecknobit.browniecore"
                artifactId = "browniecore"
                version = "1.0.0"
                from(components["kotlin"])
            }
        }
    }
}