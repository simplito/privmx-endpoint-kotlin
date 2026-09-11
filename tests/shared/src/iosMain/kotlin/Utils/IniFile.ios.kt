package Utils

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.io.IOException
import kotlinx.io.RawSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.Foundation.NSBundle

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
actual fun getResource(resourceName: String): RawSource {
    val pathForResource = NSBundle.mainBundle.pathForResource("resources/${resourceName}",null)
        ?: throw IOException("Could not find $resourceName")
    return SystemFileSystem.source(Path(pathForResource))
}