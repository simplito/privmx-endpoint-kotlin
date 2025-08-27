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
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.CustomEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import com.simplito.kotlin.privmx_endpoint.utils.makeArgs
import com.simplito.kotlin.privmx_endpoint.utils.pson
import com.simplito.kotlin.privmx_endpoint.utils.typedList
import com.simplito.kotlin.privmx_endpoint.utils.typedValue
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
     * Subscribe for the custom events on the given subscription query.
     *
     * @param subscriptionQueries list of queries
     * @return list of subscriptionIds in matching order to subscriptionQueries
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(exceptionClasses = [PrivmxException::class, NativeException::class, IllegalStateException::class])
    actual fun subscribeFor(subscriptionQueries: List<String>): List<String> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(subscriptionQueries.map { it.pson }.pson)

        try {
            privmx_endpoint_execEventApi(nativeEventApi.value, 4, args, pson_result.ptr)
            val list = pson_result.value!!.asResponse?.getResultOrThrow()!!
            list.typedList().map { it.typedValue() }
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Unsubscribe from events for the given subscriptionId.
     *
     * @param subscriptionIds list of subscriptionId
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(exceptionClasses = [PrivmxException::class, NativeException::class, IllegalStateException::class])
    actual fun unsubscribeFrom(subscriptionIds: List<String>) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(subscriptionIds.map { it.pson }.pson)

        try {
            privmx_endpoint_execEventApi(nativeEventApi.value, 5, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Generate subscription Query for the custom events.
     *
     * @param channelName  name of the Channel
     * @param selectorType selector of scope on which you listen for events
     * @param selectorId   ID of the selector
     * @return // todo - add return description
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(exceptionClasses = [PrivmxException::class, NativeException::class, IllegalStateException::class])
    actual fun buildSubscriptionQuery(
        channelName: String,
        selectorType: CustomEventSelectorType,
        selectorId: String
    ): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            channelName.pson,
            selectorType.ordinal.toLong().pson,
            selectorId.pson
        )

        try {
            privmx_endpoint_execEventApi(nativeEventApi.value, 6, args, pson_result.ptr)
            val query = pson_result.value!!.asResponse?.getResultOrThrow()!!
            query.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
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