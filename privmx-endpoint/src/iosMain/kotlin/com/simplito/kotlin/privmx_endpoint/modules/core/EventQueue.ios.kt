//
// PrivMX Endpoint Java.
// Copyright © 2024 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.simplito.kotlin.privmx_endpoint.modules.core

import cnames.structs.pson_value
import com.simplito.kotlin.privmx_endpoint.model.Event
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.utils.PsonValue
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import com.simplito.kotlin.privmx_endpoint.utils.makeArgs
import com.simplito.kotlin.privmx_endpoint.utils.toEvent
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execEventQueue
import libprivmxendpoint.privmx_endpoint_newEventQueue
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_free_value

/**
 * Defines methods to working with Events queue.
 */
@OptIn(ExperimentalForeignApi::class)
actual object EventQueue {
    private val _nativeEventQueue = nativeHeap.allocPointerTo<cnames.structs.EventQueue>()
    private val nativeEventQueue
        get() = _nativeEventQueue.value?.let { _nativeEventQueue }
            ?: throw IllegalStateException("EventQueue has been closed.")

    init {
        privmx_endpoint_newEventQueue(_nativeEventQueue.ptr)
    }

    /**
     * Puts the break event on the events queue.
     * You can use it to break the [.waitEvent].
     *
     * @throws PrivmxException thrown when method encounters an exception.
     * @throws NativeException thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual fun emitBreakEvent() = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execEventQueue(nativeEventQueue.value, 2, args, result.ptr)
            result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }

    /**
     * Waits for event on current thread.
     *
     * @return Caught event
     * @throws PrivmxException thrown when method encounters an exception.
     * @throws NativeException thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual fun waitEvent(): Event<*>? = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execEventQueue(nativeEventQueue.value, 0, args, result.ptr)
            val native_event = result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            native_event.toEvent()
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class
    )
    actual fun getEvent(): Event<*>? = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execEventQueue(nativeEventQueue.value, 1, args, result.ptr)
            val native_event =
                result.value!!.asResponse?.getResultOrThrow() as? PsonValue.PsonObject
            native_event?.toEvent()
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }
}
