import org.gradle.internal.jvm.Jvm
import org.gradle.kotlin.dsl.support.zipTo
import java.util.Properties

plugins {
    id("cpp-library")
}

library {
    linkage.set(listOf(Linkage.SHARED))
}

val localProperties = Properties().apply {
    load(file(rootDir.absolutePath + "/local.properties").inputStream())
}

val androidArchs = listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
val darwinArchs = listOf("arm64")

//TODO: Use cpp-library plugin compilation tasks
val compileAndroid = tasks.create("compileAndroid") {
    group = "privmx native"
    val sdkDir = localProperties.getProperty("sdk.dir")
    val ndkVersion = localProperties.getProperty("ndk.version")
    val compileDir = layout.buildDirectory.dir("native/compile").get()
    val androidNdkPath = "$sdkDir/ndk/$ndkVersion"
    val androidToolChainPath = "$sdkDir/ndk/$ndkVersion/build/cmake/android.toolchain.cmake"
    val os = "Android"
    val APILevel = "24"
    val installDir = layout.buildDirectory.dir("native/install").get()
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
                workingDir = layout.projectDirectory.dir("src/cpp/").asFile
                commandLine(
                    "sh", "-c",
                    "cmake" +
                            " -B${platformCompileDir.absolutePath}" +
                            " -DCMAKE_BUILD_TYPE=Release" +
                            " -DANDROID_NDK=\"$androidNdkPath\"" +
                            " -DCMAKE_TOOLCHAIN_FILE=\"$androidToolChainPath\"" +
                            " -DCMAKE_CXX_FLAGS=-std=c++17" +
                            " -DCMAKE_DESTINATION_OS=$os" +
                            " -DCMAKE_DESTINATION_ARCHITECTURE=\"$ARCH\"" +
                            " -DANDROID_PLATFORM=\"android-$APILevel\"" +
                            " -DANDROID_ABI=\"$ARCH\"" +
                            " -DJAVA_HOME=\"${Jvm.current().javaHome}\"" +
                            " -DCMAKE_INSTALL_PREFIX=\"${platformInstallDir.absolutePath}\"",
                )
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
    val compileDir = layout.buildDirectory.dir("native/compile").get()
    val os = "Darwin"
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
                workingDir = layout.projectDirectory.dir("src/cpp/").asFile
                commandLine(
                    "sh", "-c",
                    "cmake" +
                            " -B${platformCompileDir.absolutePath}" +
                            " -DCMAKE_BUILD_TYPE=Release" +
                            " -DCMAKE_CXX_FLAGS=-std=c++17" +
                            " -DCMAKE_DESTINATION_OS=$os" +
                            " -DCMAKE_DESTINATION_ARCHITECTURE=\"$ARCH\"" +
                            " -DJAVA_HOME=\"${Jvm.current().javaHome.absolutePath}\"" +
                            " -DCMAKE_INSTALL_PREFIX=\"${platformInstallDir.absolutePath}\"",
                )
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
