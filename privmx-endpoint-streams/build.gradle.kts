@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKMPLibrary)
    alias(libs.plugins.kotlinPluginSerialization)
    id("maven-publish")
    id("signing")
    id("de.undercouch.download") version "5.7.0"
}

group = "com.simplito.kotlin"
version = libs.versions.publishPrivmxEndpoint.get()

kotlin {
    val combinedFramework = XCFramework("privmx-endpoint-streams-objc")
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
        it.binaries {
            all {
                linkerOpts(
                    "-all_load",
                    "-framework",
                    "WebRTC",
                    "-F${project.projectDir.absolutePath}/src/iosMain/cinterop/webrtc/WebRTC.xcframework/ios-arm64-simulator"
                )
            }
            framework {
                baseName = "privmx-endpoint-streams-objc"
                combinedFramework.add(this)
            }
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
        it.binaries {
            all {
                linkerOpts(
                    "-framework",
                    "WebRTC",
                    "-F${project.projectDir.absolutePath.also { println("abPath $it") }}/src/iosMain/cinterop/webrtc/WebRTC.xcframework/ios-arm64/"
                )
            }

            framework {
                baseName = "privmx-endpoint-streams-objc"
                combinedFramework.add(this)
            }
        }
    }

    androidLibrary {
        namespace = "com.simplito.kotlin.privmx_endpoint_streams_android"
        compileSdk = 36
        minSdk = 24

        // Enables Java compilation support.
        // This improves build times when Java compilation is not needed
        withJava()


        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            animationsDisabled = true
        }

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(
                    JvmTarget.JVM_11
                )
            }
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
        val androidMain by getting {
            dependencies {
                implementation(libs.privmx.endpoint.webrtc.android)
            }
        }

        listOf(
            iosSimulatorArm64Test.get(),
            iosArm64Test.get()
        ).forEach {
            it.languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }

        val commonTest by getting {
            dependencies {
                implementation(project(":tests:shared"))
                implementation(libs.kotlinx.io.core)
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines)
            }
        }

        val androidDeviceTest by getting {
            dependencies {
                implementation(libs.androidx.test.core)
                implementation(libs.androidx.test.runner)
                implementation(libs.androidx.test.rules)
                implementation(libs.privmx.endpoint.webrtc.android)
            }
        }
    }
}

publishing {
    val properties = Properties()
    properties.load(file(rootDir.absolutePath + "/local.properties").inputStream())
    val repositoryURL: String = properties.getProperty("repositoryURL") ?: rootProject.layout.buildDirectory.get().dir("publications").asFile.absolutePath
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


tasks.register<de.undercouch.gradle.tasks.download.Download>("downloadWebrtcFramework") {
    val xcframeworkDir = layout.projectDirectory.dir("src/iosMain/cinterop/webrtc/WebRTC.xcframework").asFile
    val releaseVersion = libs.versions.apple.endpoint.frameworks.get()
    val webrtcVersion = libs.versions.webrtc.get()
    val fileName = "webrtc-privmx-m$webrtcVersion.xcframework.zip"
    val zipFile = layout.buildDirectory.file("tmp/$fileName").get().asFile

    src("https://github.com/simplito/privmx-endpoint-xcframeworks/releases/download/$releaseVersion/$fileName")
    dest(zipFile)
    overwrite(true)

    doLast {
        copy {
            from(zipTree(zipFile))
            into(xcframeworkDir.parentFile)
        }
    }
}

tasks.register("syncTestData") {
    group = "verification"
    val source = project(":privmx-endpoint")
        .layout.projectDirectory.file("src/commonTest/resources/TestData.ini").asFile
    val targets = listOf(
        layout.projectDirectory.file("src/commonTest/resources/TestData.ini").asFile,
        layout.projectDirectory.file("src/androidDeviceTest/resources/assets/TestData.ini").asFile,
        layout.projectDirectory.file("src/iosTest/resources/TestData.ini").asFile,
    )
    doFirst {
        if (!source.exists()) {
            throw GradleException(
                "${source.absolutePath} not found - run :privmx-endpoint:testsPreConfig first."
            )
        }
        targets.forEach { target ->
            target.parentFile.mkdirs()
            source.copyTo(target, overwrite = true)
        }
    }
}

val jniWrapperAndroidInstallDir = project(":jni-wrapper").layout.buildDirectory
    .dir("native/install/Android/${project(":privmx-endpoint").version}")

tasks.register<Copy>("syncAndroidJniLibs") {
    group = "build"
    from(jniWrapperAndroidInstallDir)
    include("*/*.so")
    into(layout.projectDirectory.dir("src/androidMain/jniLibs"))
    doFirst {
        val installDir = jniWrapperAndroidInstallDir.get().asFile
        if (!installDir.exists()) {
            throw GradleException(
                "${installDir.absolutePath} not found - run " +
                        "`:jni-wrapper:compileAndroid` (optionally with -PandroidAbis=<abi>) first, " +
                        "or use the `buildAndroidJniLibs` task."
            )
        }
    }
}