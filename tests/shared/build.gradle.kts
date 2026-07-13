@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.library)
}

group = "com.simplito.kotlin"
version = libs.versions.publishPrivmxEndpoint.get()

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    iosSimulatorArm64()
    iosArm64()

    sourceSets {
        commonMain {
            dependencies {
                api(project(":privmx-endpoint"))
                api(libs.kotlinx.io.core)
                api(libs.kotlin.test)
            }
        }
        jvmMain {
        }
        androidMain {
            dependencies {
                api("androidx.test:runner:1.7.0")
            }
        }
        iosMain {
        }
    }
}

android {
    namespace = "com.simplito.kotlin.tests.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}