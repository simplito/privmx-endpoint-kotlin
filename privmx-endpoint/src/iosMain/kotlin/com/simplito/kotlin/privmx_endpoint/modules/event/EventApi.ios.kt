package com.simplito.kotlin.privmx_endpoint.modules.event

import cnames.structs.pson_value
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import com.simplito.kotlin.privmx_endpoint.utils.makeArgs
import com.simplito.kotlin.privmx_endpoint.utils.pson
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execEventApi
import libprivmxendpoint.privmx_endpoint_freeEventApi
import libprivmxendpoint.privmx_endpoint_newEventApi
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_free_value
import libprivmxendpoint.pson_new_array

@OptIn(ExperimentalForeignApi::class)
actual class EventApi
@Throws(IllegalStateException::class)
actual constructor(connection: Connection) : AutoCloseable {
    private val _nativeEventApi = nativeHeap.allocPointerTo<cnames.structs.EventApi>()
    private val nativeEventApi
        get() = _nativeEventApi.value?.let { _nativeEventApi }
            ?: throw IllegalStateException("EventApi has been closed.")

    internal fun getEventPtr() = nativeEventApi.value

    init {
        privmx_endpoint_newEventApi(connection.getConnectionPtr(), _nativeEventApi.ptr)
        memScoped {
            val args = pson_new_array()
            val result = allocPointerTo<pson_value>()
            try {
                privmx_endpoint_execEventApi(nativeEventApi.value, 0, args, result.ptr)
                result.value!!.asResponse?.getResultOrThrow()
            } finally {
                pson_free_value(args)
                pson_free_result(result.value)
            }
        }
    }

    @Throws(exceptionClasses = [PrivmxException::class, NativeException::class, IllegalStateException::class])
    actual fun emitEvent(
        contextId: String, users: List<UserWithPubKey>, channelName: String, eventData: ByteArray
    ): Unit = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId.pson, users.map { it.pson }.pson, channelName.pson, eventData.pson
        )
        try {
            privmx_endpoint_execEventApi(nativeEventApi.value, 1, args, result.ptr)
            result.value?.asResponse?.getResultOrThrow()
        } finally {
            pson_free_value(args)
            pson_free_result(result.value)
        }
    }

    @Throws(exceptionClasses = [PrivmxException::class, NativeException::class, IllegalStateException::class])
    actual fun subscribeForCustomEvents(contextId: String, channelName: String): Unit = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId.pson, channelName.pson
        )
        try {
            privmx_endpoint_execEventApi(nativeEventApi.value, 2, args, result.ptr)
            result.value?.asResponse?.getResultOrThrow()
        } finally {
            pson_free_value(args)
            pson_free_result(result.value)
        }
    }

    @Throws(exceptionClasses = [PrivmxException::class, NativeException::class, IllegalStateException::class])
    actual fun unsubscribeFromCustomEvents(contextId: String, channelName: String): Unit =
        memScoped {
            val result = allocPointerTo<pson_value>()
            val args = makeArgs(
                contextId.pson, channelName.pson
            )
            try {
                privmx_endpoint_execEventApi(nativeEventApi.value, 3, args, result.ptr)
                result.value?.asResponse?.getResultOrThrow()
            } finally {
                pson_free_value(args)
                pson_free_result(result.value)
            }
        }

    actual override fun close() {
        if (_nativeEventApi.value == null) return
        privmx_endpoint_freeEventApi(_nativeEventApi.value)
        _nativeEventApi.value = null
    }
}