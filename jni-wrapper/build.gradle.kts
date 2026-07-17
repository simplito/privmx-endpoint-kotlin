import org.gradle.internal.jvm.Jvm
import org.gradle.kotlin.dsl.support.zipTo
import java.util.Properties
import kotlin.text.replace

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKMPLibrary)
}

enum class BuildTypes {
    Release,
    Debug,
    MinSizeRel
}

kotlin {
    android {
        namespace = "com.simplito.privmx_endpoint_jni"
        compileSdk = 36
//        ndkVersion = "29.0.13599879"
//        defaultConfig {
//            minSdk = 24
//            externalNativeBuild {
//                cmake {
//                    cppFlags("-std=c++17")
//                    this.arguments.addAll(
//                        listOf(
//                            "-DBUILD_ENDPOINT=ON",
//                            "-DBUILD_ANDROID_STREAM=ON",
//                            "-DCMAKE_TOOLCHAIN_FILE=conan_android_toolchain.cmake"
//                        )
//                    )
//                }
//            }
//        }
//
//        externalNativeBuild {
//            cmake {
//                path = file("CMakeLists.txt")
//                version = "3.22.1"
//            }
//        }
    }
}
val localProperties = Properties().apply {
    load(file(rootDir.absolutePath + "/local.properties").inputStream())
}

val androidArchs = listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
val darwinArchs = listOf("arm64")
val nativeEndpointVersion = libs.versions.nativePrivmxEndpoint.get()
val nativeAdditionalReleaseConanSuffix = ""
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
    var usePrebuiltEndpoint: Boolean = false
    if(buildType == BuildTypes.MinSizeRel || buildType == BuildTypes.Release) {
        dependsOn("buildAndroidWithConan")
    }else{
        dependsOn("buildAndroidFromSources")
        usePrebuiltEndpoint = true
    }

    val sdkDir = localProperties.getProperty("sdk.dir")
    val ndkVersion = localProperties.getProperty("ndk.version")
    val compileDir = layout.buildDirectory.dir("native/compile").get()
    val installDir = layout.buildDirectory.dir("native/install").get()
    val prebuildEndpointDir = layout.buildDirectory.dir("endpoint-prebuild/install").get()
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
            val endpointArgs = if (usePrebuiltEndpoint) {
                " -DPRIVMX_USE_PREBUILT=ON" +
                        " -DPRIVMX_PREBUILT_DIR=\"${prebuildEndpointDir.file("$os/$privmxEndpointJavaVersion/$ARCH").asFile.absolutePath}\"" +
                        " -DCMAKE_TOOLCHAIN_FILE=\"$androidNdkPath/build/cmake/android.toolchain.cmake\""
            } else {
                " -DCMAKE_TOOLCHAIN_FILE=\"conan_android_toolchain.cmake\""
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
                            endpointArgs
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
    var usePrebuiltEndpoint: Boolean = false
    if(buildType == BuildTypes.MinSizeRel || buildType == BuildTypes.Release) {
        dependsOn("buildMacosWithConan")
    }else{
        dependsOn("buildMacosFromSources")
        usePrebuiltEndpoint = true
    }
    val os = "Darwin"
    val conanArch = "armv8"
    val compileDir = layout.buildDirectory.dir("native/compile").get()
    val installDir = layout.buildDirectory.dir("native/install").get()
    val prebuildEndpointDir = layout.buildDirectory.dir("endpoint-prebuild/install").get()
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
            val endpointArgs = if (usePrebuiltEndpoint) {
                " -DPRIVMX_USE_PREBUILT=ON" +
                        " -DPRIVMX_PREBUILT_DIR=\"${prebuildEndpointDir.file("$os/$privmxEndpointJavaVersion/$ARCH").asFile.absolutePath}\""
            } else {
                " -DCMAKE_TOOLCHAIN_FILE=\"${layout.buildDirectory.asFile.get().absolutePath}/conan/build/macos-$conanArch/${buildType.name}/generators/conan_toolchain.cmake\""
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
                            endpointArgs
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
val compileLinux = tasks.create("compileLinux") {
    group = "privmx native"
    dependsOn("buildLinuxWithConan")
    val os = "Linux"
    val conanArch = "x86_64"
    val arch = "x86_64"
    val compileDir = layout.buildDirectory.dir("native/compile").get()
    val installDir = layout.buildDirectory.dir("native/install").get()
    doFirst {
        val platformInstallDir = installDir.file("$os/$privmxEndpointJavaVersion/$arch").asFile
        val platformCompileDir = compileDir.file("$os/$privmxEndpointJavaVersion/$arch").asFile
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
                        " -DCMAKE_TOOLCHAIN_FILE=\"${layout.buildDirectory.asFile.get().absolutePath}/conan/build/linux-$conanArch/${buildType.name}/generators/conan_toolchain.cmake\""
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

        fileTree(platformInstallDir) { include("**/*.so") }.forEach { soFile ->
            exec {
                commandLine("patchelf", "--set-rpath", "\$ORIGIN", soFile.absolutePath)
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
    dependsOn("updateConanfileVersion")
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

tasks.register("buildAndroidFromSources") {
    dependsOn("clonePrivmxSources")
    val conanArchsMap = mapOf(
        "armeabi-v7a" to "armv7",
        "arm64-v8a" to "armv8",
        "x86" to "x86",
        "x86_64" to "x86_64"
    )
//    onlyIf { layout.buildDirectory.dir("native/install/iOS/$privmxEndpointJavaVersion/arm64").orNull?.asFile?.exists() != true }
    doFirst {
        val clonedEndpointDir = layout.buildDirectory.dir("privmx-endpoint").get().asFile
        val profile = layout.projectDirectory.file("conan/profiles/android").asFile
        val ndkPath = with(AndroidProfileConfig) { localProperties.ndkPath }
        androidArchs.forEach { arch ->
            val conanArch = conanArchsMap[arch]
                ?: throw IllegalArgumentException("Not supported android $arch architecture by conan")
            val INSTALL_DIR =
                layout.buildDirectory.get().dir("endpoint-prebuild/install/Android/$privmxEndpointJavaVersion/$arch").asFile
            val COMPILED_DIR =
                layout.buildDirectory.get().dir("endpoint-prebuild/compile/Android/$privmxEndpointJavaVersion/$arch").asFile
            if(!INSTALL_DIR.exists()) {
                conanInstall(
                    clonedEndpointDir,
                    profile,
                    "../conan",
                    "../native/install/Android/$privmxEndpointJavaVersion/$arch",
                    additionalParams = listOf(
                        " -s arch=${conanArch}",
                        " -c \"tools.android:ndk_path=$ndkPath\""
                    )
                )
                buildFromSources(
                    INSTALL_DIR,
                    COMPILED_DIR,
                    "${layout.buildDirectory.asFile.get().absolutePath}/conan/build/android-$conanArch/${buildType.name}/generators/conan_toolchain.cmake",
                )
                copyFilesFromDeploy(INSTALL_DIR,".so")
            }
        }
    }
}

tasks.register("buildMacosWithConan") {
    dependsOn("updateConanfileVersion")
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

tasks.register("buildMacosFromSources") {
    dependsOn("clonePrivmxSources")
//    onlyIf { !layout.buildDirectory.get().dir("endpoint-prebuild/install/Darwin/$privmxEndpointJavaVersion/arm64").asFile.exists() }
    doFirst {
        val conanArch = "armv8"
        val arch = "arm64"
        val clonedEndpointDir = layout.buildDirectory.dir("privmx-endpoint").get().asFile
        val profile = layout.projectDirectory.file("conan/profiles/macos").asFile
        val INSTALL_DIR =
            layout.buildDirectory.get().dir("endpoint-prebuild/install/Darwin/$privmxEndpointJavaVersion/$arch").asFile
        val COMPILED_DIR =
            layout.buildDirectory.get().dir("endpoint-prebuild/compile/Darwin/$privmxEndpointJavaVersion/$arch").asFile
        conanInstall(
            clonedEndpointDir,
            profile,
            "../conan",
            "../endpoint-prebuild/install/Darwin/$privmxEndpointJavaVersion/$arch",
            additionalParams = listOf(" -s arch=${conanArch}")
        )
        buildFromSources(
            INSTALL_DIR,
            COMPILED_DIR,
            "${layout.buildDirectory.asFile.get().absolutePath}/conan/build/macos-$conanArch/${buildType.name}/generators/conan_toolchain.cmake",
            true
        )
        copyFilesFromDeploy(INSTALL_DIR,".dylib")
    }
}

tasks.register("buildLinuxWithConan") {
    dependsOn("updateConanfileVersion")
    doFirst {
        val conanArch = "x86_64"
        val arch = "x86_64"
        exec {
            workingDir = layout.projectDirectory.asFile
            commandLine(
                "sh", "-c",
                "conan install ." +
                        " -pr ./conan/profiles/linux-x86_64" +
                        " -s build_type=${buildType.name}" +
                        " -s arch=${conanArch}" +
                        " --build missing" +
                        " --deployer=runtime_deploy" +
                        " --output-folder=build/conan" +
                        " --deployer-folder build/native/install/Linux/$privmxEndpointJavaVersion/$arch" +
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
//    onlyIf { layout.buildDirectory.dir("native/install/iOSSimulator/$privmxEndpointJavaVersion/arm64").orNull?.asFile?.exists() != true }
    doFirst {
        val clonedEndpointDir = layout.buildDirectory.dir("privmx-endpoint").get().asFile
        val profile = layout.projectDirectory.file("conan/profiles/iosSimulator").asFile
        conanInstall(
            clonedEndpointDir,
            profile,
            "../conan",
            "../native/install/iOSSimulator/$privmxEndpointJavaVersion/arm64",
            buildShared = false,
            additionalSdkFolderVar = true
        )

        val INSTALL_DIR =
            layout.buildDirectory.get().dir("native/install/iOSSimulator/$privmxEndpointJavaVersion/arm64").asFile
        val COMPILED_DIR =
            layout.buildDirectory.get().dir("native/compile/iOSSimulator/$privmxEndpointJavaVersion/arm64").asFile

        buildFromSources(
            INSTALL_DIR,
            COMPILED_DIR,
            "${layout.buildDirectory.asFile.get().absolutePath}/conan/build/ios-iphonesimulator-armv8/${buildType.name}/generators/conan_toolchain.cmake",
            false
        )
        copyFilesFromDeploy(INSTALL_DIR,".a")
        exec {
            workingDir = INSTALL_DIR
            commandLine("sh", "-c", "/usr/bin/libtool -static -o libpmxend.a ./lib/*.a")
        }
    }
}


tasks.register("buildIOSStaticFromSources") {
    dependsOn("clonePrivmxSources")
    onlyIf { layout.buildDirectory.dir("native/install/iOS/$privmxEndpointJavaVersion/arm64").orNull?.asFile?.exists() != true }
    doFirst {
        val clonedEndpointDir = layout.buildDirectory.dir("privmx-endpoint").get().asFile
        val profile = layout.projectDirectory.file("conan/profiles/ios").asFile
        conanInstall(
            clonedEndpointDir,
            profile,
            "../conan",
            "../native/install/iOS/$privmxEndpointJavaVersion/arm64",
            buildShared = false,
            additionalSdkFolderVar = true
        )

        //iphoneSimulator
        val INSTALL_DIR = layout.buildDirectory.get().dir("native/install/iOS/$privmxEndpointJavaVersion/arm64").asFile
        val COMPILED_DIR = layout.buildDirectory.get().dir("native/compile/iOS/$privmxEndpointJavaVersion/arm64").asFile

        buildFromSources(
            INSTALL_DIR,
            COMPILED_DIR,
            "${layout.buildDirectory.asFile.get().absolutePath}/conan/build/ios-iphoneos-armv8/${buildType.name}/generators/conan_toolchain.cmake",
            false
        )
        copyFilesFromDeploy(INSTALL_DIR,".a")
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
        installDir.dir("frameworks/$privmxEndpointJavaVersion/privmx-endpoint.xcframework").asFile.apply {
            if (exists()) {
                deleteRecursively()
            }
        }
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

tasks.register("updateConanfileVersion") {
    group = "privmx native"
    doFirst {
        val conanfile = layout.projectDirectory.file("conanfile.txt").asFile
        val conanfileContent = conanfile.readText()

        val version = "$nativeEndpointVersion$nativeAdditionalReleaseConanSuffix"

        val requiresSectionRegex =
            Regex(
                ".*\\[requires\\]\\s*(?<content>(?:(?<line>(?!(?:\\[.*\\])).)*\\s*)*)", RegexOption.MULTILINE
            )
        val requiresSectionContent = requiresSectionRegex.find(conanfileContent)?.value

        val updatedConanfileContent = if (requiresSectionContent == null) {
            """
            [requires]
            privmx-endpoint/$version
        """.trimIndent() + conanfileContent
        } else {
            val updated = requiresSectionContent.replace(Regex("privmx-endpoint/\\S*"), "privmx-endpoint/$version")
            conanfileContent.replace(requiresSectionContent, updated)
        }
        conanfile.writeText(updatedConanfileContent)
    }
}

tasks.register("clonePrivmxSources") {
    doFirst {
        val buildDirFile = layout.buildDirectory.locationOnly.get().asFile
        val repoDir = File(buildDirFile, "privmx-endpoint")
        if (!buildDirFile.exists()) buildDirFile.mkdirs()
        if (repoDir.exists()) repoDir.deleteRecursively()
        exec {
            workingDir = buildDirFile
            commandLine(
                "sh", "-c",
                "git clone --depth 1 -b v$nativeEndpointVersion https://github.com/simplito/privmx-endpoint.git"
            )
        }
        val conanfile = File(repoDir, "conanfile.txt")
        val conanfileContent = conanfile.readText()
        println(conanfile.absolutePath)

        val requiresSectionRegex =
            Regex(
                ".*\\[requires\\]\\s*(?<content>(?:(?<line>(?!(?:^\\[.*\\]$)).)*\\s*)*)", RegexOption.MULTILINE
            )
        val requiresSectionContent = requiresSectionRegex.find(conanfileContent)?.value

        val updatedConanfileContent = if (requiresSectionContent == null) {
            conanfileContent
        } else {
            val updated = requiresSectionContent.replace(Regex("libwebrtc/\\S*"), "").replace(Regex("gtest/\\S*"), "").replace(Regex("readline/\\S*"), "")
            println(updated)
            conanfileContent.replace(requiresSectionContent, updated)
        }
        conanfile.writeText(updatedConanfileContent)
    }
}

private fun Project.buildFromSources(
    installDir: File,
    compileDir: File,
    toolChainPath: String,
    buildShared: Boolean = true
) {
    val clonedEndpointDir = layout.buildDirectory.dir("privmx-endpoint").get().asFile
    exec {
        workingDir = clonedEndpointDir
        commandLine(
            "sh", "-c",
            "cmake" +
                    " -B\"${compileDir.absolutePath}\"" +
                    " -DCMAKE_BUILD_TYPE=${buildType.name}" +
                    " -DPRIVMX_BUILD_ENDPOINT=ON" +
                    " -DCMAKE_CXX_FLAGS=-std=c++17" +
                    " -DBUILD_SHARED_LIBS=${if (buildShared) "ON" else "OFF"}" +
                    " -DPRIVMX_BUILD_ENDPOINT_ENDPOINT=ON" +
                    " -DPRIVMX_CONAN=ON" +
                    " -DPRIVMX_DRIVER_CRYPTO=ON" +
                    " -DPRIVMX_DRIVER_NET=ON" +
                    " -DCMAKE_TOOLCHAIN_FILE=\"$toolChainPath\"" +
                    " -DCMAKE_INSTALL_PREFIX=\"${installDir.absolutePath}\""
        )
    }

    exec {
        workingDir = compileDir
        commandLine("sh", "-c", "cmake --build .")
    }

    exec {
        workingDir = compileDir
        commandLine("sh", "-c", "make -s -j8")
    }

    exec {
        workingDir = compileDir
        commandLine("sh", "-c", "make -s install")
    }
}

private fun Project.conanInstall(
    workingDir: File,
    profile: File,
    outputFolderPath: String,
    deployerFolderPath: String,
    buildShared: Boolean = true,
    additionalSdkFolderVar: Boolean = false,
    additionalParams: List<String> = emptyList()
) {
    exec {
        this.workingDir = workingDir
        commandLine(
            "sh", "-c",
            "conan install ." +
                    " -pr \"${profile.absolutePath}\"" +
                    " -s build_type=${buildType.name}" +
                    " --build missing" +
                    " --deployer=full_deploy" +
                    " --output-folder=\"${outputFolderPath}\"" +
                    " --deployer-folder \"${deployerFolderPath}\"" +
                    " -o \"*:shared=${if (buildShared) "True" else "False"}\"" +
                    " -c \"tools.cmake.cmake_layout:build_folder_vars=['settings.os'${if (additionalSdkFolderVar) ", 'settings.os.sdk'" else ""}, 'settings.arch']\"" +
                    additionalParams.joinToString(" ")
        )
    }
}

private fun Project.copyFilesFromDeploy(
    workingDir: File,
    extension: String,
){

    val deployDir = File(workingDir,"full_deploy").also { println(it.absolutePath) }
    copy {
        from(deployDir.absolutePath)
        include("**/libPoco*$extension","**/libcrypto*$extension","**/libPson*$extension","**/libssl*$extension","**/libprivmx*$extension")
        this.includeEmptyDirs = false
        eachFile { path=name }
        into(File(workingDir,"lib"))
    }
    deployDir.deleteRecursively()
}