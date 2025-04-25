//
// PrivMX Endpoint Kotlin Extra.
// Copyright © 2025 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.simplito.kotlin.privmx_endpoint_extra.events


import com.simplito.kotlin.privmx_endpoint.model.Event
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Represents a callback for catching events data.
 * @param T type of the caught event data
 *
 * @category core
 */
fun interface EventCallback<T: Any>{
    /**
     * Called to handle data from a captured event.
     *
     *  @param eventData the caught event data
     */
    operator fun invoke(eventData: T)
}


/**
 * Implements a list of registered event callbacks.
 * @param onRemoveEntryKey callback triggered when all events
 *                         from channel entry have been removed
 *                         (it can also unsubscribe from the channel)
 * @category core
 */
class EventDispatcher(
    private val onRemoveEntryKey: (removedKey: String) -> Unit
) {
    private val map: MutableMap<String, MutableList<Pair>> = mutableMapOf()
    private val mapMutex = Mutex()
    private fun getFormattedType(channel: String, type: String): String {
        return channel.toString() + "_" + type
    }

    /**
     * Registers new event callback.
     *
     * @param channel  channel of registered event
     * @param type     type of registered event
     * @param context  ID of registered callback
     * @param callback block of code to call when the specified event has been caught
     * @return `true` if the channel is not already subscribed
     */
    suspend fun <T: Any> register(
        channel: String,
        type: String,
        context: Any,
        callback: EventCallback<T>
    ): Boolean {
        val needSubscribe = channelHasNoCallbacks(channel)
        getCallbacks(getFormattedType(channel, type)).add(Pair(context, callback))
        return needSubscribe
    }

    /**
     * Emits specified event. It should only be called by event loops.
     *
     * @param T     type of event data
     * @param event event data to emit
    </T> */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T: Any> emit(event: Event<out T>) {
        val callbacks = getCallbacks(getFormattedType(event.channel, event.type))
        for (p in callbacks) {
            try {
                try {
                    (p.callback as EventCallback<T>)(event.data)
                } catch (_: Exception) {
                }
            } catch (_: ClassCastException) {
                println("Cannot process event: issue with cast event data")
            }
        }
    }

    private fun channelHasNoCallbacks(channel: String?): Boolean {
        return map
            .entries
            .filter { it -> it.key.substringBefore("_") == channel }
            .sumOf { it -> it.value.size } == 0
    }

    /**
     * Removes all callbacks registered by [EventDispatcher.register]. It's identified by given Context.
     *
     * @param context callback identifier
     */
    suspend fun unbind(context: Any) = map.entries
        .mapTo(mutableSetOf()) { it.key.substringBefore("_") to it.value }
        .filter { it.second.isNotEmpty() }
        .forEach {
            val (key, value) = it
            mapMutex.withLock {
                value.removeAll { it.context == context }
            }
            if (channelHasNoCallbacks(key)) {
                onRemoveEntryKey(key)
            }
        }

    /**
     * Removes all callbacks.
     */
    suspend fun unbindAll() = mapMutex.withLock {
        map.keys
            .map { it -> it.substringBefore("_") }
            .forEach(onRemoveEntryKey::invoke)
        map.clear()
    }


    private suspend fun getCallbacks(type: String): MutableList<Pair> = mapMutex.withLock {
        map.getOrPut(type) { mutableListOf() }
    }


    private data class Pair(val context: Any, val callback: EventCallback<out Any>)
}
