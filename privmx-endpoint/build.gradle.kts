@file:OptIn(ExperimentalEncodingApi::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import kotlin.io.encoding.ExperimentalEncodingApi

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("maven-publish")
    id("signing")
}

group = "com.simplito.kotlin"
version = libs.versions.publishPrivmxEndpoint.get()

kotlin {

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvm{
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    listOf(
        iosSimulatorArm64(),
        iosArm64(),
    ).forEach {
        it.compilations.getByName("main") {
            cinterops {
                val libprivmxendpoint by creating {
                    this.extraOpts = listOf(
                        "-libraryPath",
                        "src/nativeInterop/cinterop/privmx-endpoint/${it.name}/lib",
                        "-compilerOpts",
                        "-Isrc/nativeInterop/cinterop/privmx-endpoint/${it.name}/include"
                    )
                    val headerFiles = fileTree("src/nativeInterop/cinterop/privmx-endpoint/${it.name}/include").matching {
                        include("privmx/endpoint/**/cinterface/*.h")
                        include("Pson/pson.h")
                    }.files
                    headers(headerFiles)
                }
            }
        }
    }

    sourceSets {
        listOf(
            iosSimulatorArm64Main.get(),
            iosArm64Main.get(),
        ).forEach {
            it.dependsOn(iosMain.get())
        }

        val iosMain by getting {
            dependsOn(commonMain.get())
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

publishing {
    repositories {
        val localProperties = Properties().apply {
            load(file(rootDir.absolutePath + "/local.properties").inputStream())
        }
        val repositoryURL: String = localProperties.getProperty("repositoryURL")
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
                name = "PrivMX Endpoint Kotlin"
                description =
                    "PrivMX Endpoint Kotlin is a minimal wrapper library declaring native functions in Kotlin using JNI."
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}
