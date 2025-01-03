import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
//    alias(libs.plugins.androidLibrary)
    id("maven-publish")
//    id("signing")
//    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "com.simplito.kotlin"
version = "1.0.0"
//TODO: JvmNativeTarget is implementation contains jni wrapper (maybe contains from Java lib)
//TODO: How to load jni wrapper?
//TODO: All implementations for non jvm in nativeTarget
kotlin {

//    val hostOs = System.getProperty("os.name")
//    val isArm64 = System.getProperty("os.arch") == "aarch64"
//    val isMingwX64 = hostOs.startsWith("Windows")
//    val isAndroid = 0 //TODO: Is Android?
//
//    val nativeJVMTarget = when {
//        hostOs == "Mac OS X" && isArm64 -> macosArm64("native")
//        hostOs == "Mac OS X" && !isArm64 -> macosX64("native")
//        hostOs == "Linux" && isArm64 -> linuxArm64("native")
//        hostOs == "Linux" && !isArm64 -> linuxX64("native")
//        isMingwX64 -> mingwX64("native")
//        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
//    }
//
//    nativeJVMTarget.apply {
//        compilations.getByName("main") {    // NL
//            cinterops {                     // NL
//                val libprivmxendpoint by creating     // NL
//            }                               // NL
//        }
//    }
//    androidNativeArm64{
//        compilations.getByName("main") {
//            cinterops {
//                val libprivmxendpoint by creating
//            }
//        }
//    }
//    androidTarget {
//        publishLibraryVariants("release")
//    }

    jvm()
////    androidTarget {
////        publishLibraryVariants("release")
////        @OptIn(ExperimentalKotlinGradlePluginApi::class)
////        compilerOptions {
////            jvmTarget.set(JvmTarget.JVM_1_8)
////        }
////    }
//
//    iosX64()
//    iosArm64() {
////        compilations.getByName("main"){
////            cinterops{
////                val libprivmxendpoint by creating     // NL
////            }
////        }
//    }
//    iosSimulatorArm64()
    listOf(
        iosSimulatorArm64(),
        iosX64(),
        iosArm64(),
    ).forEach {
        it.compilations.getByName("main") {
            cinterops{
                val libprivmxendpoint by creating
            }
        }
    }
//    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

//android {
//    namespace = "org.jetbrains.kotlinx.multiplatform.library.template"
//    compileSdk = libs.versions.android.compileSdk.get().toInt()
//    defaultConfig {
//        minSdk = libs.versions.android.minSdk.get().toInt()
//    }
//}

publishing {
    afterEvaluate {
        publications.withType<MavenPublication>().onEach {
            it.artifactId = it.artifactId.replace("library", "privmx-endpoint")
        }
        publications.forEach {
            println("Publication: ${it.name}")
        }
    }

    val properties = Properties()
    properties.load(file(rootDir.absolutePath + "/local.properties").inputStream())
    val repositoryURL: String = properties.getProperty("repositoryURL")
    repositories {
        maven {
            name = "localFiles"
            url = uri(repositoryURL)
        }
    }
}

//mavenPublishing {
//    val properties = Properties()
//    properties.load(file(rootDir.absolutePath + "/local.properties").inputStream())
//    val repositoryURL: String = properties.getProperty("repositoryURL")
//    publishToMavenCentral("")
//    publishToMavenCentral(repositoryURL)
////    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
//
//    signAllPublications()
//
//    coordinates(group.toString(), "library", version.toString())
//
//    pom {
//        name = ""
//        description = "A library."
//        inceptionYear = "2024"
//        url = "https://github.com/kotlin/multiplatform-library-template/"
//        licenses {
//            license {
//                name = "XXX"
//                url = "YYY"
//                distribution = "ZZZ"
//            }
//        }
//        developers {
//            developer {
//                id = "XXX"
//                name = "YYY"
//                url = "ZZZ"
//            }
//        }
//        scm {
//            url = "XXX"
//            connection = "YYY"
//            developerConnection = "ZZZ"
//        }
//    }
//}
