import org.gradle.internal.jvm.Jvm
import java.util.Properties

val localProperties = Properties().apply {
    rootDir.resolve("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

val netDrvLibName = "privmxdrvnet"
val netDrvBuildType = "MinSizeRel"
val netDrvAndroidAbis = listOf("arm64-v8a")
val netDrvAndroidApiLevel = 24
val netDrvDarwinArchs = listOf("arm64")

val compileRoot = layout.buildDirectory.dir("native/compile")
val installRoot = layout.buildDirectory.dir("native/install")

tasks.register("buildNetDrvJniAndroid") {
    group = "privmx native"
    description = "Builds lib$netDrvLibName.so (JNI net driver) for Android ${netDrvAndroidAbis.joinToString()}."
    inputs.dir("src")
    inputs.file("CMakeLists.txt")
    inputs.file("../include/privmx/drv/net.h")
    outputs.dir(installRoot.map { it.dir("Android") })
    doLast {
        val sdkDir = localProperties.getProperty("sdk.dir")
            ?: throw GradleException("sdk.dir is not set in local.properties")
        val ndkVersion = localProperties.getProperty("ndk.version")
            ?: throw GradleException("ndk.version is not set in local.properties")
        val ndkDir = File(sdkDir, "ndk/$ndkVersion")
        val llvmStrip = ndkDir.resolve("toolchains/llvm/prebuilt").listFiles()
            ?.singleOrNull { it.isDirectory }
            ?.resolve("bin/llvm-strip")
            ?: throw GradleException("Expected exactly one LLVM host toolchain in $ndkDir/toolchains/llvm/prebuilt")

        netDrvAndroidAbis.forEach { abi ->
            val compileDir = compileRoot.get().dir("Android/$abi").asFile
            val installDir = installRoot.get().dir("Android/$abi").asFile.apply { mkdirs() }
            exec {
                commandLine(
                    "cmake", "-S", projectDir.absolutePath, "-B", compileDir.absolutePath,
                    "-DCMAKE_BUILD_TYPE=$netDrvBuildType",
                    "-DCMAKE_TOOLCHAIN_FILE=${ndkDir.resolve("build/cmake/android.toolchain.cmake").absolutePath}",
                    "-DANDROID_ABI=$abi",
                    "-DANDROID_PLATFORM=android-$netDrvAndroidApiLevel",
                    // 16 KB pages (Android 15+), as the rest of the native libraries
                    "-DCMAKE_SHARED_LINKER_FLAGS=-Wl,-z,max-page-size=16384",
                )
            }
            exec { commandLine("cmake", "--build", compileDir.absolutePath, "--config", netDrvBuildType) }
            val lib = installDir.resolve("lib$netDrvLibName.so")
            compileDir.resolve("lib$netDrvLibName.so").copyTo(lib, overwrite = true)
            exec { commandLine(llvmStrip.absolutePath, "--strip-unneeded", lib.absolutePath) }
            logger.lifecycle("Built ${lib.relativeTo(projectDir)} (${lib.length() / 1024} KB)")
        }
    }
}

tasks.register("buildNetDrvJniDarwin") {
    group = "privmx native"
    description = "Builds lib$netDrvLibName.dylib (JNI net driver) for macOS ${netDrvDarwinArchs.joinToString()}."
    inputs.dir("src")
    inputs.file("CMakeLists.txt")
    inputs.file("../include/privmx/drv/net.h")
    outputs.dir(installRoot.map { it.dir("Darwin") })
    doLast {
        netDrvDarwinArchs.forEach { arch ->
            val compileDir = compileRoot.get().dir("Darwin/$arch").asFile
            val installDir = installRoot.get().dir("Darwin/$arch").asFile.apply { mkdirs() }
            exec {
                // CMakeLists.txt takes the JNI headers from $JAVA_HOME
                environment("JAVA_HOME", Jvm.current().javaHome.absolutePath)
                commandLine(
                    "cmake", "-S", projectDir.absolutePath, "-B", compileDir.absolutePath,
                    "-DCMAKE_BUILD_TYPE=$netDrvBuildType",
                    "-DCMAKE_OSX_ARCHITECTURES=$arch",
                    "-DCMAKE_INSTALL_NAME_DIR=@rpath",
                )
            }
            exec { commandLine("cmake", "--build", compileDir.absolutePath, "--config", netDrvBuildType) }
            val lib = installDir.resolve("lib$netDrvLibName.dylib")
            compileDir.resolve("lib$netDrvLibName.dylib").copyTo(lib, overwrite = true)
            exec { commandLine("xcrun", "strip", "-x", lib.absolutePath) }
            // strip invalidates the signature, and arm64 macOS kills a process loading an invalid one
            exec { commandLine("codesign", "--force", "--sign", "-", lib.absolutePath) }
            logger.lifecycle("Built ${lib.relativeTo(projectDir)} (${lib.length() / 1024} KB)")
        }
    }
}

tasks.register("buildNetDrvJni") {
    group = "privmx native"
    description = "Builds the JNI net driver for Android and macOS."
    dependsOn("buildNetDrvJniAndroid", "buildNetDrvJniDarwin")
}
