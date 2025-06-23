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

/**
 * Manages PrivMX Bridge context custom events.
 *
 * @param connection active connection to PrivMX Bridge
 * @throws IllegalStateException when passed [Connection] is not connected.
 */
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

    /**
     * Emits the custom event on the given Context and channel.
     *
     * @param contextId   ID of the Context
     * @param users       list of [UserWithPubKey] objects which defines the recipients of the event
     * @param channelName name of the Channel
     * @param eventData   event's data
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
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

    /**
     * Subscribe for the custom events on the given channel.
     *
     * @param contextId   ID of the Context
     * @param channelName name of the Channel
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
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

    /**
     * Unsubscribe from the custom events on the given channel.
     *
     * @param contextId   ID of the Context
     * @param channelName name of the Channel
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun unsubscribeFromCustomEvents(contextId: String, channelName: String): Unit =
        memScoped {
            val result = allocPointerTo<pson_value>()
            val args = makeArgs(contextId.pson, channelName.pson)
            try {
                privmx_endpoint_execEventApi(nativeEventApi.value, 3, args, result.ptr)
                result.value?.asResponse?.getResultOrThrow()
            } finally {
                pson_free_value(args)
                pson_free_result(result.value)
            }
        }

    /**
     * Frees memory.
     */
    actual override fun close() {
        privmx_endpoint_freeEventApi(nativeEventApi.value)
        _nativeEventApi.value = null
    }
}