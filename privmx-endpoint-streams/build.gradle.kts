import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinPluginSerialization)
    id("maven-publish")
    id("signing")
}

group = "com.simplito.kotlin"
version = libs.versions.publishPrivmxEndpoint.get()

kotlin {

    iosSimulatorArm64().let {
        it.compilations.getByName("main") {
            val webrtcInterop by cinterops.creating {
                definitionFile.set(project.file("src/iosMain/cinterop/WebRTCFramework.def"))

//                // Tell cinterop where to look for the framework headers

                compilerOpts(
                    "-framework",
                    "WebRTC",
                    "-F${project.projectDir.absolutePath}/src/iosMain/cinterop/webrtc/WebRTC.xcframework/ios-arm64-simulator"
                )
                linkerOpts(
                    "-all_load",
                    "-framework",
                    "WebRTC",
                    "-F${project.projectDir.absolutePath}/src/iosMain/cinterop/webrtc/WebRTC.xcframework/ios-arm64-simulator"
                )
            }
        }
        it.binaries.all {
            linkerOpts(
                "-all_load",
                "-framework",
                "WebRTC",
                "-F${project.projectDir.absolutePath}/src/iosMain/cinterop/webrtc/WebRTC.xcframework/ios-arm64-simulator"
            )
        }
    }
    iosArm64().let {
        it.compilations.getByName("main") {
            val webrtcInterop by cinterops.creating {
                definitionFile.set(project.file("src/iosMain/cinterop/WebRTCFramework.def"))

//                // Tell cinterop where to look for the framework headers

                compilerOpts(
                    "-framework",
                    "WebRTC",
                    "-F${project.projectDir.absolutePath}/src/iosMain/cinterop/webrtc/WebRTC.xcframework/ios-arm64/"
                )
                linkerOpts(
                    "-framework",
                    "WebRTC",
                    "-F${project.projectDir.absolutePath.also { println("abPath $it") }}/src/iosMain/cinterop/webrtc/WebRTC.xcframework/ios-arm64/"
                )
            }
        }
        it.binaries.all {
            linkerOpts(
                "-framework",
                "WebRTC",
                "-F${project.projectDir.absolutePath.also { println("abPath $it") }}/src/iosMain/cinterop/webrtc/WebRTC.xcframework/ios-arm64/"
            )
        }
    }
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":privmx-endpoint"))
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

publishing {
    val properties = Properties()
    properties.load(file(rootDir.absolutePath + "/local.properties").inputStream())
    val repositoryURL: String = properties.getProperty("repositoryURL")
    repositories {
        maven {
            name = "localRepo"
            url = uri(repositoryURL)
        }
    }

    publications {
        withType<MavenPublication>().configureEach {
            groupId = "com.simplito.kotlin"
            version = project.version as String
            pom {
                name = "PrivMX Endpoint Kotlin Streams"
                description =
                    "PrivMX Endpoint Kotlin Streams is module implementing high-level StreamApi"
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

android {
    namespace = "com.simplito.kotlin.privmx_endpoint_streams_android"
    //TODO: Add minimum sdk version
    compileSdk = 36
}

