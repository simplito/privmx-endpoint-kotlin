package io.github.kotlin.fibonacci
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.*
actual class Connection {
    @OptIn(ExperimentalForeignApi::class)
    actual fun connect(host: String, solutionId: String, userPrivKey: String){
        memScoped {
        }

    }
}