import org.gradle.internal.jvm.Jvm
import org.gradle.kotlin.dsl.support.zipTo
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
}

enum class BuildTypes {
    Release,
    Debug,
    MinSizeRel
}

android {
    namespace = "com.simplito.privmx_endpoint_jni"
    compileSdk = 36
    ndkVersion = "29.0.13599879"
    defaultConfig {
        minSdk = 24
        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17")
                this.arguments.addAll(
                    listOf(
                        "-DBUILD_ENDPOINT=ON",
                        "-DBUILD_ANDROID_STREAM=ON",
                        "-DCMAKE_TOOLCHAIN_FILE=conan_android_toolchain.cmake"
                    )
                )
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("CMakeLists.txt")
            version = "3.22.1"
        }
    }
}
val localProperties = Properties().apply {
    load(file(rootDir.absolutePath + "/local.properties").inputStream())
}

val androidArchs = listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
//val androidArchs = listOf("arm64-v8a")
val darwinArchs = listOf("arm64")

//TODO: Use cpp-library plugin compilation tasks
val compileAndroid = tasks.create("compileAndroid") {
    group = "privmx native"
    dependsOn("buildAndroidWithConan")
    val sdkDir = localProperties.getProperty("sdk.dir")
    val ndkVersion = localProperties.getProperty("ndk.version")
    val compileDir = layout.buildDirectory.dir("native/compile").get()
    val installDir = layout.buildDirectory.dir("native/install").get()
    val androidNdkPath = "$sdkDir/ndk/$ndkVersion"
    val os = "Android"
    val APILevel = "24"
    doFirst {
        androidArchs.onEach { ARCH ->
            val platformInstallDir = installDir.file("$os/$privmxEndpointJavaVersion/$ARCH").asFile
            val platformCompileDir = compileDir.file("$os/$privmxEndpointJavaVersion/$ARCH").asFile
            if (!platformCompileDir.exists()) {
                platformCompileDir.mkdirs()
            }
            if (!platformInstallDir.exists()) {
                platformInstallDir.mkdirs()
            }
            exec {
                workingDir = layout.projectDirectory.asFile
                commandLine(
                    "sh", "-c",
                    "cmake" +
                            " -B${platformCompileDir.absolutePath}" +
                            " -DCMAKE_BUILD_TYPE=${buildType.name}" +
                            " -DANDROID_NDK=\"$androidNdkPath\"" +
                            " -DCMAKE_CXX_FLAGS=-std=c++17" +
                            " -DANDROID_PLATFORM=\"android-$APILevel\"" +
                            " -DANDROID_ABI=\"$ARCH\"" +
                            " -DJAVA_HOME=\"${Jvm.current().javaHome}\"" +
                            " -DCMAKE_INSTALL_PREFIX=\"${platformInstallDir.absolutePath}\"" +
                            " -DCMAKE_TOOLCHAIN_FILE=\"conan_android_toolchain.cmake\""
                )
            }

            exec {
                workingDir = platformCompileDir
                commandLine("sh", "-c", "cmake --build .")
            }

            exec {
                workingDir = platformCompileDir
                commandLine("sh", "-c", "make -s -j8")
            }

            exec {
                workingDir = platformCompileDir
                commandLine("sh", "-c", "make -s install")
            }
        }
    }
}

val buildType = BuildTypes.Debug

//TODO: Use cpp-library plugin compilation tasks
val compileDarwin = tasks.create("compileDarwin") {
    group = "privmx native"
    dependsOn("buildMacosWithConan")
    val os = "Darwin"
    val conanArch = "armv8"
    val compileDir = layout.buildDirectory.dir("native/compile").get()
    val installDir = layout.buildDirectory.dir("native/install").get()
    doFirst {
        println(Jvm.current().javaHome.absolutePath)
        darwinArchs.onEach { ARCH ->
            val platformInstallDir = installDir.file("$os/$privmxEndpointJavaVersion/$ARCH").asFile
            val platformCompileDir = compileDir.file("$os/$privmxEndpointJavaVersion/$ARCH").asFile
            if (!platformCompileDir.exists()) {
                platformCompileDir.mkdirs()
            }
            if (!platformInstallDir.exists()) {
                platformInstallDir.mkdirs()
            }
            exec {
                workingDir = layout.projectDirectory.asFile
                commandLine(
                    "sh", "-c",
                    "cmake" +
                            " -B${platformCompileDir.absolutePath}" +
                            " -DCMAKE_BUILD_TYPE=${buildType.name}" +
                            " -DCMAKE_CXX_FLAGS=-std=c++17" +
                            " -DJAVA_HOME=\"${Jvm.current().javaHome.absolutePath}\"" +
                            " -DCMAKE_INSTALL_PREFIX=\"${platformInstallDir.absolutePath}\"" +
                            " -DCMAKE_TOOLCHAIN_FILE=\"${layout.buildDirectory.asFile.get().absolutePath}/conan/build/macos-$conanArch/${buildType.name}/generators/conan_toolchain.cmake\""
                )
            }

            exec {
                workingDir = platformCompileDir
                commandLine("sh", "-c", "cmake --build .")
            }

            exec {
                workingDir = platformCompileDir
                commandLine("sh", "-c", "make -s -j8")
            }
            exec {
                workingDir = platformCompileDir
                commandLine("sh", "-c", "make -s install")
            }
        }
    }
}

tasks.create("zipDarwin") {
    group = "privmx native"
    val dstDir = layout.buildDirectory.dir("native/zip").get()
    dependsOn(compileDarwin)
    doFirst {
        darwinArchs.onEach { arch ->
            val formattedArch = arch.replace("-", "_")
            val zipFile =
                dstDir.file("Darwin/$privmxEndpointJavaVersion/$arch/darwin-$formattedArch.zip").asFile
            val sourceFile =
                layout.buildDirectory.dir("native/install/Darwin/$privmxEndpointJavaVersion/$arch")
                    .get().asFile
            zipFile.parentFile.mkdirs()
            zipFile.createNewFile()
            zipTo(zipFile, sourceFile)
        }
    }
}

tasks.create("zipAndroidNative") {
    group = "privmx native"
    val dstDir = layout.buildDirectory.dir("native/zip").get()
    dependsOn(compileAndroid)
    doFirst {
        androidArchs.onEach { arch ->
            val formattedArch = arch.replace("-", "_")
            val zipFile =
                dstDir.file("Android/$privmxEndpointJavaVersion/$arch/android-$formattedArch.zip").asFile
            val sourceFile =
                layout.buildDirectory.dir("native/install/Android/$privmxEndpointJavaVersion/$arch")
                    .get().asFile
            zipFile.parentFile.mkdirs()
            zipFile.createNewFile()
            zipTo(zipFile, sourceFile)
        }
    }
}

private val privmxEndpointJavaVersion get() = project(":privmx-endpoint").version

object AndroidProfileConfig {
    val Properties.sdkPath: String? get() = this["sdk.dir"] as? String
    val Properties.ndkVersion: String? get() = this["ndk.version"] as? String
    val Properties.ndkPath: String?
        get() = if (sdkPath.isNullOrBlank() || ndkVersion.isNullOrBlank()) throw IllegalArgumentException(
            "sdk.dir or ndk.version is not defined in local.properties"
        ) else "$sdkPath/ndk/$ndkVersion"
}

tasks.register("buildAndroidWithConan") {
    val conanArchsMap = mapOf(
        "armeabi-v7a" to "armv7",
        "arm64-v8a" to "armv8",
        "x86" to "x86",
        "x86_64" to "x86_64"
    )
    doFirst {
        val ndkPath = with(AndroidProfileConfig) { localProperties.ndkPath }
        androidArchs.forEach { arch ->
            val conanArch = conanArchsMap[arch]
                ?: throw IllegalArgumentException("Not supported android $arch architecture by conan")
            exec {
                workingDir = layout.projectDirectory.asFile
                commandLine(
                    "sh", "-c",
                    "conan install ." +
                            " -pr ./conan/profiles/android" +
                            " -s build_type=${buildType.name}" +
                            " -s arch=${conanArch}" +
                            " --build missing" +
                            " --deployer=runtime_deploy" +
                            " --output-folder=build/conan" +
                            " --deployer-folder build/native/install/Android/$privmxEndpointJavaVersion/$arch" +
                            " -c \"tools.android:ndk_path=$ndkPath\"" +
                            " -c \"tools.cmake.cmake_layout:build_folder_vars=['settings.os','settings.arch']\""
                )
            }
        }
    }
}

tasks.register("buildMacosWithConan") {
    doFirst {
        val conanArch = "armv8"
        val arch = "arm64"
        exec {
            workingDir = layout.projectDirectory.asFile
            commandLine(
                "sh", "-c",
                "conan install ." +
                        " -pr ./conan/profiles/macos" +
                        " -s build_type=${buildType.name}" +
                        " -s arch=${conanArch}" +
                        " --build missing" +
                        " --deployer=runtime_deploy" +
                        " --output-folder=build/conan" +
                        " --deployer-folder build/native/install/Darwin/$privmxEndpointJavaVersion/$arch" +
                        " -c \"tools.cmake.cmake_layout:build_folder_vars=['settings.os','settings.arch']\""
            )
        }
    }
}

tasks.register("buildIosWithConan") {
    doFirst {
        val conanArch = "armv8"
        val arch = "arm64"
        exec {
            workingDir = layout.projectDirectory.asFile
            commandLine(
                "sh", "-c",
                "conan install ." +
                        " -pr ./conan/profiles/ios" +
                        " -s build_type=${buildType.name}" +
                        " -s arch=${conanArch}" +
                        " --build missing" +
                        " --deployer=runtime_deploy" +
                        " --output-folder=build/conan" +
                        " --deployer-folder build/native/install/iOS/$privmxEndpointJavaVersion/$arch" +
                        " -c \"tools.cmake.cmake_layout:build_folder_vars=['settings.os','settings.arch']\""
            )
        }
    }
}

tasks.register("buildIosSimulatorWithConan") {
    doFirst {
        val conanArch = "armv8"
        val arch = "arm64"
        exec {
            workingDir = layout.projectDirectory.asFile
            commandLine(
                "sh", "-c",
                "conan install ." +
                        " -pr ./conan/profiles/iosSimulator" +
                        " -s build_type=${buildType.name}" +
                        " -s arch=${conanArch}" +
                        " --build missing" +
                        " --deployer=runtime_deploy" +
                        " --output-folder=build/conan-ios-simulator" +
                        " --deployer-folder build/native/install/iOSSimulator/$privmxEndpointJavaVersion/$arch" +
                        " -c \"tools.cmake.cmake_layout:build_folder_vars=['settings.os','settings.arch']\""
            )
        }
    }
}