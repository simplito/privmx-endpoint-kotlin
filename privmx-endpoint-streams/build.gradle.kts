import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.library)
    id("maven-publish")
    id("signing")
}

group = "com.simplito.kotlin"
version = libs.versions.publishPrivmxEndpoint.get()

kotlin {

    iosSimulatorArm64()
    iosArm64()
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":privmx-endpoint"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
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

android{
    //TODO: Add minimum sdk version
    compileSdk=36
}

