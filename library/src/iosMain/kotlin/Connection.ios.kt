package io.github.kotlin.fibonacci
import kotlinx.cinterop.*
import libprivmxendpoint.*
import libprivmxendpoint.Connection
import platform.posix.NULL

actual class Connection {
    @OptIn(ExperimentalForeignApi::class)
    actual fun connect(host: String, solutionId: String, userPrivKey: String) {
        memScoped {

//            libprivmxendpoint.PSON_ARRAY
//            cValue<>()
//            val connection = cValue<libprivmxendpoint.Connection>()
            val connection = allocPointerTo<Connection>()

//            val connection = cValue<IntVar>().ptr
//            CValues<Connection>()
            val args = pson_new_object()
            val result = pson_new_object()
            pson_set_object_value(args,"host",pson_new_string(host))
            pson_set_object_value(args,"solutionId",pson_new_string(solutionId))
            pson_set_object_value(args,"userPrivKey",pson_new_string(userPrivKey))
            privmx_endpoint_newConnection(connection.ptr)
        }
    }
}