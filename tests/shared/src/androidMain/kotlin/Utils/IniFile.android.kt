package Utils

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.io.RawSource
import kotlinx.io.asSource

actual fun getResource(resourceName: String): RawSource {
    val context = InstrumentationRegistry.getInstrumentation().context
    return context.assets.open(resourceName).asSource()
}