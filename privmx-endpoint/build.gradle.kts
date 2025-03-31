import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
}

group = "com.simplito.kotlin"
version = "1.0.0"

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
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
                val libprivmxendpoint by creating
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
        val androidMain by getting{
            dependsOn(jvmMain.get())
        }

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

android {
    namespace = "org.jetbrains.kotlinx.multiplatform.library.template"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

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
