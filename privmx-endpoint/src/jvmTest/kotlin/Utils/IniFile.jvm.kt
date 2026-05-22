package Utils

import kotlinx.io.IOException
import kotlinx.io.RawSource
import kotlinx.io.asSource

actual fun getResource(resourceName: String): RawSource {
    val resourceStream = ClassLoader.getSystemResourceAsStream("$resourceName") ?: throw IOException("")
    return resourceStream.asSource()
}