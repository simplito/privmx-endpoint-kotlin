//
// PrivMX Endpoint Kotlin.
// Copyright © 2025 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.simplito.kotlin.privmx_endpoint

import java.io.File
import java.io.FileOutputStream
import java.io.IOException


internal typealias ResourcePaths = List<String>

internal object LibLoader {
    var libsDir: File? = null

    private val nativeLibrariesLoaded: Unit by lazy {
        System.loadLibrary("privmx-endpoint-kotlin")
        registerShutdownHook()
    }

    init {
        try {
            //Check if it's android
            Class.forName("android.os.Bundle");
        } catch (_: Throwable) {
            extractLibraries()
        }
    }

    fun loadPrivmxLibraries() {
        nativeLibrariesLoaded
    }

    /**
     * Tells the native layer that the JVM is going down.
     *
     * Endpoint stops its threads during static destruction, which may happen while (or after)
     * the JVM is being destroyed. Without this signal such a thread could call
     * DetachCurrentThread() on a dying VM and hang the shutdown.
     */
    private external fun notifyVmShutdown()

    private fun registerShutdownHook() {
        try {
            Runtime.getRuntime().addShutdownHook(
                Thread { notifyVmShutdown() }.apply {
                    name = "privmx-endpoint-shutdown"
                }
            )
        } catch (_: Throwable) {
            //shutdown already in progress or hooks not permitted - nothing to do
        }
    }

    @Throws(UnsatisfiedLinkError::class)
    private fun getPlatformLibsResourceDirPath(): String {
        val os = System.getProperty("os.name")
        val arch = System.getProperty("os.arch")
        if (os == null) throw NoSuchFieldError("Cannot find os name")
        if (arch == null) throw NoSuchFieldError("Cannot find arch name")

        //TODO: Add support for more platforms
        if (os.startsWith("mac os", ignoreCase = true)) {
            if (arch.equals("aarch64", ignoreCase = true)) return "/lib/Darwin/arm64"
        }

        if (os.startsWith("linux", ignoreCase = true)) {
            if (arch.equals("amd64", ignoreCase = true) || arch.equals("x86_64", ignoreCase = true)) return "/lib/Linux/x86_64"
        }

        throw UnsatisfiedLinkError("os: $os, arch: $arch is not supported.")
    }

    private fun getBinaryResourcePaths(platformLibsResourceDirPath: String): ResourcePaths {
        val fileNames = (
                LibLoader::class.java
                    .getResourceAsStream("$platformLibsResourceDirPath/fileNames.txt")
                    ?.reader()
                    ?.readText()
                ) ?: ""

        return fileNames.split(";").map { fileName ->
            "$platformLibsResourceDirPath/$fileName"
        }
    }

    @Throws(UnsatisfiedLinkError::class)
    private fun extractResource(resourcePath: String) {
        val localLibFile = File(
            libsDir,
            resourcePath.substring(
                resourcePath.lastIndexOf("/")
            )
        )
        try {
            LibLoader::class.java.getResourceAsStream(resourcePath).use { iS ->
                if (iS == null) return@use

                if (localLibFile.exists()) localLibFile.delete()
                if (localLibFile.createNewFile()) {
                    val data = ByteArray(1024)
                    var read: Int
                    try {
                        FileOutputStream(localLibFile).use { oS ->
                            while ((iS.read(data).also { read = it }) >= 0) {
                                oS.write(data.copyOf(read))
                            }
                        }
                    } catch (e: IOException) {
                        throw UnsatisfiedLinkError("Cannot extract PrivMX binaries")
                    }
                }
            }
        } catch (_: IOException) {
            localLibFile.delete()
            throw UnsatisfiedLinkError("Cannot extract PrivMX binaries")
        } catch (_: NullPointerException) {
            localLibFile.delete()
            throw UnsatisfiedLinkError("Cannot extract PrivMX binaries")
        }
    }

    @Throws(UnsatisfiedLinkError::class)
    private fun extractLibraries() {
        if (libsDir != null) return
        val librariesDirectoryPath = System.getProperty("java.library.path").let {
            if (it.contains(Regex(":\\.(?::?|$)"))) {
                System.getProperty("user.dir")
            } else {
                it.substringAfterLast(":")
            }
        }
        val librariesDirectory = File(librariesDirectoryPath)

        if (!librariesDirectory.exists() && !librariesDirectory.mkdirs()) {
            return
        }

        libsDir = librariesDirectory
        try {
            getBinaryResourcePaths(
                getPlatformLibsResourceDirPath()
            ).forEach { resourcePath ->
                try {
                    extractResource(resourcePath)
                }catch (e: Exception){
//                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }
}