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

        commonMain.dependencies {
                implementation(libs.kotlinx.coroutines)
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

tasks.register<Jar>("desktopJar"){
    archiveClassifier="desktop"
    val binariesDir = project(":jni-wrapper").layout.buildDirectory.dir("native/install/Darwin/$version").get()
    dependsOn(project(":jni-wrapper").tasks.named("compileDarwin"))
    from(binariesDir)
    include("**/**")
    into("lib/Darwin")
    destinationDirectory = layout.buildDirectory.dir("nativeJars/desktop")

    doFirst {
        binariesDir.asFile.listFiles()?.filter {
            it.isDirectory && !it.isHidden
        }?.forEach { archDir ->
            println(archDir.path)
            File("${archDir.path}/fileNames.txt").run {
                createNewFile()
                outputStream().use {
                    writeText(archDir.listFiles()?.joinToString(";") { it.name } ?: "")
                }
            }
        }
    }
}

tasks.register<Jar>("androidJar"){
    archiveClassifier="android"
    val binariesDir = project(":jni-wrapper").layout.buildDirectory.dir("native/install/Android/$version").get()
    dependsOn(project(":jni-wrapper").tasks.named("compileAndroid"))
    from(binariesDir)
    include("**/**")
    into("lib")
    destinationDirectory = layout.buildDirectory.dir("nativeJars/android")
}

publishing {
    repositories {
        val localProperties = Properties().apply {
            load(file(rootDir.absolutePath + "/local.properties").inputStream())
        }
        val repositoryURL: String = localProperties.getProperty("repositoryURL") ?: rootProject.layout.buildDirectory.get().dir("publications").asFile.absolutePath
        maven {
            name = "localRepo"
            url = uri(repositoryURL)
        }
    }

    publications {
        withType<MavenPublication>().configureEach {
            groupId = "com.simplito.kotlin"
            version = project.version as String
            if(this.name == "jvm"){
                artifact(tasks["desktopJar"])
                artifact(tasks["androidJar"])
            }
            afterEvaluate {
                pom {
                    name = "PrivMX Endpoint Kotlin"
                    description =
                        "PrivMX Endpoint Kotlin is a minimal wrapper library declaring native functions in Kotlin using JNI."
                    licenses {
                        license {
                            name = "Apache-2.0"
                            url =
                                "https://openssl-library.org/source/license/apache-license-2.0.txt"
                            comments = "OpenSSL native libraries license"
                        }

                        license {
                            name = "BSL-1.0"
                            url = "https://www.boost.org/LICENSE_1_0.txt"
                            comments = "POCO native libraries license"
                        }

                        license {
                            name = "LGPL-3.0-only"
                            url = "https://www.gnu.org/licenses/lgpl-3.0.txt"
                            comments = "GMP native libraries license"
                        }

                        license {
                            name = "PrivMX Free License ver. 1.0"
                            url = "https://github.com/simplito/privmx-endpoint/blob/aea8de762b3fe4e1054fb185a8ec2ce40c6f9ddf/LICENSE.md"
                            comments = "PrivMX Endpoint native libraries license"
                        }

                        license {
                            name = "PrivMX Free License ver. 1.0"
                            url = "https://github.com/simplito/pson-cpp/blob/46451d80eb8abc5897a644ff437916a48d185419/LICENSE.md"
                            comments = "pson-cpp native libraries license"
                        }
                    }
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}
