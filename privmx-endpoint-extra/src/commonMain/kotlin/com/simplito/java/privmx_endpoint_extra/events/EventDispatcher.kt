//
// PrivMX Endpoint Java Extra.
// Copyright © 2024 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.simplito.java.privmx_endpoint_extra.events


import com.simplito.java.privmx_endpoint.model.Event
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

//TODO: Try to use function type instead of SAM/functional interface
fun interface EventCallback<T>{
    operator fun invoke(event: Event<out T>)
}

//typealias EventCallback<T>  = (event: Event<out T>) -> Unit

/**
 * Implements a list of registered event callbacks.
 *
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
    suspend fun register(
        channel: String,
        type: String,
        context: Any,
        callback: EventCallback<*>
    ): Boolean {
        val needSubscribe = channelHasNoCallbacks(channel)
        getCallbacks(getFormattedType(channel, type)).add(Pair(context, callback))
        return needSubscribe
    }

    /**
     * Emits specified event. It should only be called by event loops.
     *
     * @param <T>   type of event data
     * @param event event data to emit
    </T> */
    suspend fun <T> emit(event: Event<out T>) {
        val callbacks = getCallbacks(getFormattedType(event.channel!!, event.type!!))
        for (p in callbacks) {
            try {
                try {
                    (p.callback as EventCallback<T>)(event)
                } catch (ignored: Exception) {
                }
            } catch (e: ClassCastException) {
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
     * Removes all callbacks registered by [.register]. It's identified by given Context.
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


    private data class Pair(val context: Any, val callback: EventCallback<*>)
}
