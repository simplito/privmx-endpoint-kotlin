@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKMPLibrary)
    alias(libs.plugins.kotlinPluginSerialization)
}

group = "com.simplito.privmx-endpoint-snippets"
version = "2.2.0"

kotlin {
    jvmToolchain(21)

    jvm()

    androidLibrary {
        namespace = "com.simplito.privmx.endpoint.snippets"
        compileSdk = 36
        minSdk = 24

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }
    }

    iosArm64()
    iosSimulatorArm64()

    // Has to be applied explicitly, because the custom `streamsMain` dependsOn edge
    // below would otherwise disable the default hierarchy template.
    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":privmx-endpoint"))
                implementation(project(":privmx-endpoint-extra"))
                implementation(libs.kotlinx.serialization.json)
            }
        }

        val streamsMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(project(":privmx-endpoint-streams"))
                implementation(libs.kotlinx.coroutines)
            }
        }

        androidMain {
            dependsOn(streamsMain)
            dependencies {
                implementation(libs.core.ktx)
                implementation(libs.privmx.endpoint.webrtc.android)
            }
        }

        iosMain {
            dependsOn(streamsMain)
        }
    }
}
