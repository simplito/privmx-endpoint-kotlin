import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.kotlin.dsl.implementation
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidApplication)
}

group = "com.simplito.kotlin"
version = "1.0.0"

kotlin {

    androidTarget {
//        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

//    iosX64()
//    iosArm64()
//    iosSimulatorArm64()
//    jvm()
//    linuxX64()

    listOf(
//        iosX64(),
//        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        val androidMain by getting{
            dependencies{
                implementation(libs.androidx.activity.compose)
                //TODO: should be debug implementation
                implementation(libs.androidx.ui.tooling)
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
//                @OptIn(ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                implementation(project(":privmx-endpoint"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
//    androidTarget{
//
//    }
}

android {
    namespace = "org.jetbrains.kotlinx.multiplatform.library.template"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    sourceSets{
        get("main").jni.srcDirs("src/androidMain/jniLibs")
    }
}
dependencies {
    implementation(libs.activity.ktx)
}

