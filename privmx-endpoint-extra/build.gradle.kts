import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("maven-publish")
    id("signing")
}

group = "com.simplito.kotlin"
version = "1.0.0"
kotlin {
    jvm{
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    iosSimulatorArm64()
    iosArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":privmx-endpoint"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.6.0")
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
                name = "PrivMX Endpoint Kotlin Extra"
                description =
                    "PrivMX Endpoint Kotlin Extra is an extension of Privmx Endpoint Kotlin. It's extended with additional logic that makes using our libraries simpler and less error-prone."
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}
