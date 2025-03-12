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
package com.simplito.java.privmx_endpoint.modules.core

import com.simplito.java.privmx_endpoint.model.Event
import com.simplito.java.privmx_endpoint.model.exceptions.NativeException
import com.simplito.java.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.java.privmx_endpoint.utils.PsonValue
import com.simplito.java.privmx_endpoint.utils.asResponse
import com.simplito.java.privmx_endpoint.utils.makeArgs
import com.simplito.java.privmx_endpoint.utils.toEvent
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execEventQueue
import libprivmxendpoint.privmx_endpoint_newEventQueue
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_value

/**
 * Defines methods to working with Events queue.
 */
@OptIn(ExperimentalForeignApi::class)
actual object EventQueue {
    private val nativeEventQueue = nativeHeap.allocPointerTo<libprivmxendpoint.EventQueue>()

    init {
        privmx_endpoint_newEventQueue(nativeEventQueue.ptr)
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
        val result: CPointerVar<pson_value>
        result = allocPointerTo<pson_value>()
        try {
            privmx_endpoint_execEventQueue(nativeEventQueue.value, 2, makeArgs(), result.ptr)
            result.value!!.asResponse?.getResultOrThrow()
        } finally {
            pson_free_result(result.value)
        }
        Unit
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
        try {
            privmx_endpoint_execEventQueue(nativeEventQueue.value, 0, makeArgs(), result.ptr)
            val native_event = result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            native_event.toEvent()
        } finally {
            pson_free_result(result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class
    )
    actual fun getEvent(): Event<*>? = memScoped {
        val result = allocPointerTo<pson_value>()
        try {
            privmx_endpoint_execEventQueue(nativeEventQueue.value, 1, makeArgs(), result.ptr)
            val native_event =
                result.value!!.asResponse?.getResultOrThrow() as? PsonValue.PsonObject
            native_event?.toEvent()
        } finally {
            pson_free_result(result.value)
        }
    }
}
