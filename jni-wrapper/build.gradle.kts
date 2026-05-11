import org.gradle.internal.jvm.Jvm
import org.gradle.kotlin.dsl.support.zipTo
import java.util.Properties
import kotlin.text.replace

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
val nativeEndpointVersion = libs.versions.nativePrivmxEndpoint
val nativeAdditionalReleaseConanSuffix = "-dev.2"
val buildType = BuildTypes.Debug
private val privmxEndpointJavaVersion get() = project(":privmx-endpoint").version

object AndroidProfileConfig {
    val Properties.sdkPath: String? get() = this["sdk.dir"] as? String
    val Properties.ndkVersion: String? get() = this["ndk.version"] as? String
    val Properties.ndkPath: String?
        get() = if (sdkPath.isNullOrBlank() || ndkVersion.isNullOrBlank()) throw IllegalArgumentException(
            "sdk.dir or ndk.version is not defined in local.properties"
        ) else "$sdkPath/ndk/$ndkVersion"
}

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
                            " --requires \"privmx-endpoint/$nativeEndpointVersion$nativeAdditionalReleaseConanSuffix\"" +
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
                        " --requires \"privmx-endpoint/$nativeEndpointVersion$nativeAdditionalReleaseConanSuffix\"" +
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
                        " --output-folder=build/conan" +
                        " --deployer-folder build/native/install/iOSSimulator/$privmxEndpointJavaVersion/$arch" +
                        " -c \"tools.cmake.cmake_layout:build_folder_vars=['settings.os','settings.arch']\""
            )
        }
    }
}

//val privmxEndpointVersionTag = "v2.7.5"

tasks.register("buildIOSSimulatorStaticFromSources") {
    dependsOn("clonePrivmxSources")
    doFirst {
        val clonedEndpointDir = layout.buildDirectory.dir("privmx-endpoint").get().asFile
        val profile = layout.projectDirectory.file("conan/profiles/iosSimulator").asFile.absolutePath
        exec {
            workingDir = clonedEndpointDir
            commandLine(
                "sh", "-c",
                "conan install ." +
                        " -pr $profile" +
                        " -s build_type=${buildType.name}" +
                        " --build missing" +
                        " --deployer=runtime_deploy" +
                        " --output-folder=../conan" +
                        " --deployer-folder ../native/install/iOSSimulator/$privmxEndpointJavaVersion/arm64" +
                        " -o \"*:shared=False\"" +
                        " -c \"tools.cmake.cmake_layout:build_folder_vars=['settings.os', 'settings.os.sdk','settings.arch']\""
            )
        }

        val INSTALL_DIR =
            layout.buildDirectory.get().dir("native/install/iOSSimulator/$privmxEndpointJavaVersion/arm64").asFile
        val COMPILED_DIR =
            layout.buildDirectory.get().dir("native/compile/iOSSimulator/$privmxEndpointJavaVersion/arm64").asFile
        exec {
            workingDir = clonedEndpointDir
            commandLine(
                "sh", "-c",
                "cmake" +
                        " -B\"${COMPILED_DIR.absolutePath}\"" +
                        " -DCMAKE_BUILD_TYPE=${buildType.name}" +
                        " -DCMAKE_CXX_FLAGS=-std=c++17" +
                        " -DPRIVMX_BUILD_ENDPOINT=ON" +
                        " -DBUILD_SHARED_LIBS=OFF" +
                        " -DPRIVMX_BUILD_ENDPOINT_ENDPOINT=ON" +
                        " -DPRIVMX_CONAN=ON" +
                        " -DPRIVMX_DRIVER_CRYPTO=ON" +
                        " -DPRIVMX_DRIVER_NET=ON" +
                        " -DCMAKE_TOOLCHAIN_FILE=\"${layout.buildDirectory.asFile.get().absolutePath}/conan/build/ios-iphonesimulator-armv8/${buildType.name}/generators/conan_toolchain.cmake\"" +
                        " -DCMAKE_INSTALL_PREFIX=\"${INSTALL_DIR.absolutePath}\""
            )
        }

        exec {
            workingDir = COMPILED_DIR
            commandLine("sh", "-c", "cmake --build .")
        }

        exec {
            workingDir = COMPILED_DIR
            commandLine("sh", "-c", "make -s -j8")
        }
        exec {
            workingDir = COMPILED_DIR
            commandLine("sh", "-c", "make -s install")
        }
        exec {
            workingDir = INSTALL_DIR
            commandLine("sh", "-c", "/usr/bin/libtool -static -o libpmxend.a ./lib/*.a")
        }
    }
}


tasks.register("buildIOSStaticFromSources") {
    dependsOn("clonePrivmxSources")
    doFirst {
        val clonedEndpointDir = layout.buildDirectory.dir("privmx-endpoint").get().asFile
        val profile = layout.projectDirectory.file("conan/profiles/ios").asFile.absolutePath
        exec {
            workingDir = clonedEndpointDir
            commandLine(
                "sh", "-c",
                "conan install ." +
                        " -pr $profile" +
                        " -s build_type=${buildType.name}" +
                        " --build missing" +
                        " --deployer=runtime_deploy" +
                        " --output-folder=../conan" +
                        " --deployer-folder ../native/install/iOS/$privmxEndpointJavaVersion/arm64" +
                        " -o \"*:shared=False\"" +
                        " -c \"tools.cmake.cmake_layout:build_folder_vars=['settings.os','settings.arch']\""
            )
        }

        //iphoneSimulator
        val INSTALL_DIR = layout.buildDirectory.get().dir("native/install/iOS/$privmxEndpointJavaVersion/arm64").asFile
        val COMPILED_DIR = layout.buildDirectory.get().dir("native/compile/iOS/$privmxEndpointJavaVersion/arm64").asFile

        exec {
            workingDir = clonedEndpointDir
            commandLine(
                "sh", "-c",
                "cmake" +
                        " -B\"${COMPILED_DIR.absolutePath}\"" +
                        " -DCMAKE_BUILD_TYPE=${buildType.name}" +
                        " -DPRIVMX_BUILD_ENDPOINT=ON" +
                        " -DCMAKE_CXX_FLAGS=-std=c++17" +
                        " -DBUILD_SHARED_LIBS=OFF" +
                        " -DPRIVMX_BUILD_ENDPOINT_ENDPOINT=ON" +
                        " -DPRIVMX_CONAN=ON" +
                        " -DPRIVMX_DRIVER_CRYPTO=ON" +
                        " -DPRIVMX_DRIVER_NET=ON" +
                        " -DCMAKE_TOOLCHAIN_FILE=\"${layout.buildDirectory.asFile.get().absolutePath}/conan/build/ios-armv8/${buildType.name}/generators/conan_toolchain.cmake\"" +
                        " -DCMAKE_INSTALL_PREFIX=\"${INSTALL_DIR.absolutePath}\""
            )
        }

        exec {
            workingDir = COMPILED_DIR
            commandLine("sh", "-c", "cmake --build .")
        }

        exec {
            workingDir = COMPILED_DIR
            commandLine("sh", "-c", "make -s -j8")
        }

        exec {
            workingDir = COMPILED_DIR
            commandLine("sh", "-c", "make -s install")
        }

        exec {
            workingDir = INSTALL_DIR
            commandLine("sh", "-c", "/usr/bin/libtool -static -o ./libpmxend.a ./lib/*.a")
        }
    }
}

tasks.register("preparePmxEndpointXCFramework") {
    dependsOn("buildIOSSimulatorStaticFromSources")
    dependsOn("buildIOSStaticFromSources")
    doFirst {
        val installDir = layout.buildDirectory.get().dir("native/install")
        val iosDir = installDir.dir("iOS/$privmxEndpointJavaVersion/arm64")
        val iosSimulatorDir = installDir.dir("iOSSimulator/$privmxEndpointJavaVersion/arm64")
        exec {
            workingDir = installDir.asFile
            commandLine(
                "sh", "-c", "xcodebuild -create-xcframework" +
                        " -output frameworks/$privmxEndpointJavaVersion/privmx-endpoint.xcframework" +
                        " -library ${iosDir.file("libpmxend.a").asFile.absolutePath} -headers ${iosDir.dir("include").asFile.absolutePath}" +
                        " -library ${iosSimulatorDir.file("libpmxend.a").asFile.absolutePath} -headers ${
                            iosSimulatorDir.dir(
                                "include"
                            ).asFile.absolutePath
                        }"
            )
        }
    }
}

tasks.register("clonePrivmxSources") {
    doFirst {
        exec {
            workingDir = layout.buildDirectory.asFile.get()
            commandLine(
                "sh", "-c",
                "git clone --depth 1 -b v$nativeEndpointVersion https://github.com/simplito/privmx-endpoint.git"
            )
        }
    }
}