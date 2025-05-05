plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinPluginSerialization)
}

group = "com.simplito.privmx-endpoint-snippets"
version = "2.2.0"

kotlin {
    jvmToolchain(21)

    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets{
        val commonMain by getting {
            dependencies {
                implementation(project(":privmx-endpoint"))
                implementation(project(":privmx-endpoint-extra"))
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}