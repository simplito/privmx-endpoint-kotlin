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
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.CustomEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.CoreEventType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.InboxEventType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.KvdbEventType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.StoreEventType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.ThreadEventType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Represents a callback for catching events data.
 * @param T type of the caught event data
 */
fun interface EventCallback<T : Any> {
    /**
     * Called to handle data from a captured event.
     *
     *  @param eventData the caught event data
     */
    operator fun invoke(eventData: T)
}


/**
 * Implements a list of registered event callbacks.
 * @param onRemoveSubscriptionEntry callback triggered when all events
 *                         from channel entry have been removed
 *                         (it can also unsubscribe from the channel)
 */
class EventDispatcher(
    private val onRemoveSubscriptionEntry: (removedSubscriptions: Map<SubscriptionModule, List<String>>) -> Unit
) {
    private val map: MutableMap<String, MutableList<Pair>> = mutableMapOf()
    private val callbackMap: MutableMap<EventRegistrationInfo, MutableList<Pair>> = mutableMapOf()
    private val mapMutex = Mutex()

    /**
     * Registers new event callback.
     *
     * @param callbackRegistration object describing single callback registration
     * @return this callback registration info
     */
    suspend fun registerCallback(callbackRegistration: CallbackRegistration<out Any>): EventRegistrationInfo {
        val info = getRegistrationInfo(callbackRegistration.eventType)
        getCallbackList(info).add(
            Pair(
                callbackRegistration.callbackGroup,
                callbackRegistration.callback
            )
        )
        return info
    }

    /**
     * Emits specified event. It should only be called by event loops.
     *
     * @param T     type of event data
     * @param event event data to emit
    </T> */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T : Any> emit(event: Event<out T>) {
        val callbacks = if (isLibEvent(event.type)) {
            getCallbacksByType(event.type)
        } else {
            getCallbacks(event.subscriptions)
        }
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

    /**
     * Removes all events, excluding internal library events, that do not have {@code subscriptionId}.
     */
    suspend fun removeNotSubscribedEvents() = mapMutex.withLock {
        val entrySetIterator = callbackMap.iterator()
        entrySetIterator.forEach { (eventRegistrationInfo,_) ->
            if (eventRegistrationInfo.subscriptionID == null && !isLibEvent(eventRegistrationInfo.eventType.eventName)) {
                entrySetIterator.remove()
            }
        }
    }

    /**
     * Removes all callbacks registered by [EventDispatcher.register]. It's identified by given callback group identifiers.
     *
     * @param callbackGroups one or more callback group identifiers used to select callbacks to unbind
     */
    suspend fun unbind(vararg callbackGroups: Any) {
        if (callbackGroups.isEmpty()) return
        val callbackGroupsList: MutableList<Any> = callbackGroups.toMutableList()
        val selectorsToUnsubscribe: MutableMap<SubscriptionModule, MutableList<String>> = mutableMapOf()
        mapMutex.withLock {
            val mapIterator = callbackMap.entries.iterator()
            mapIterator.forEach { (key, callbacks) ->
                if (key.subscriptionID != null) {
                    val pairsOfCallbacks: List<Pair> =
                        callbacks.filter { p -> callbackGroupsList.contains(p.context) }
                    callbacks.removeAll(pairsOfCallbacks)
                    callbackGroupsList.removeAll(pairsOfCallbacks)

                    if (callbacks.isEmpty()) {
                        val module: SubscriptionModule? = getModuleFromEventRegistrationInfo(key)
                        if (module != null) {
                            selectorsToUnsubscribe.getOrPut(module) { mutableListOf() }
                                .add(key.subscriptionID!!)
                        }
                    }
                    mapIterator.remove()
                }
            }
        }
        onRemoveSubscriptionEntry(selectorsToUnsubscribe)
    }

    private fun getModuleFromEventRegistrationInfo(key: EventRegistrationInfo): SubscriptionModule? {
        var module: SubscriptionModule? = null
        if (key.eventType.eventSelectorType is CustomEventSelectorType) {
            module = SubscriptionModule.CUSTOM_EVENT
        } else if (key.eventType.libEventType is ThreadEventType) {
            module = SubscriptionModule.THREAD
        } else if (key.eventType.libEventType is StoreEventType) {
            module = SubscriptionModule.STORE
        } else if (key.eventType.libEventType is InboxEventType) {
            module = SubscriptionModule.INBOX
        } else if (key.eventType.libEventType is KvdbEventType) {
            module = SubscriptionModule.KVDB
        } else if (key.eventType.libEventType is CoreEventType) {
            module = SubscriptionModule.CORE
        }
        return module
    }

    /**
     * Removes all callbacks.
     */
    suspend fun unbindAll() = mapMutex.withLock {
        val callbacksToUnsubscribe: Map<SubscriptionModule, MutableList<String>> = callbackMap
            .mapNotNull { (key, _) ->
                val module: SubscriptionModule? = getModuleFromEventRegistrationInfo(key)
                if (key.subscriptionID != null && module != null) {
                    module to key.subscriptionID!!
                } else null
            }.groupingBy { it.first }
            .aggregate { key, accumulator, element, first ->
                if (first) {
                    mutableListOf(element.second)
                } else {
                    accumulator?.add(element.second)
                    accumulator!!
                }
            }
        onRemoveSubscriptionEntry(callbacksToUnsubscribe)
        map.clear()
    }


    private suspend fun getRegistrationInfo(eventType: EventType<*>): EventRegistrationInfo =
        mapMutex.withLock {
            callbackMap.keys.firstOrNull { it.eventType == eventType }
                ?: EventRegistrationInfo(null, eventType)
        }

    /**
     * Get reference to a list that can be used to add or remove callbacks.
     */
    private suspend fun getCallbackList(eventRegistrationInfo: EventRegistrationInfo): MutableList<Pair> =
        mapMutex.withLock {
            callbackMap.getOrPut(eventRegistrationInfo) { mutableListOf() }
        }

    /**
     * Get list of all callbacks identified by this subscriptionIds.
     */
    private suspend fun getCallbacks(subscriptionIds: List<String>): List<Pair> =
        mapMutex.withLock {
            callbackMap.filter { (key, _) ->
                subscriptionIds.contains(
                    key.subscriptionID
                )
            }.flatMap { it.value }
        }

    private suspend fun getCallbacksByType(eventType: String): List<Pair> = mapMutex.withLock {
        callbackMap.filter { (key, _) ->
            key.eventType.eventName == eventType
        }.flatMap { it.value }
    }

    /**
     * Available subscription modules for event handling.
     */
    enum class SubscriptionModule {
        /**
         * Thread module case.
         */
        THREAD,

        /**
         * Store module case.
         */
        STORE,

        /**
         * Inbox module case.
         */
        INBOX,

        /**
         * Custom Event module case.
         */
        CUSTOM_EVENT,

        /**
         * KVDB module case.
         */
        KVDB,

        /**
         * CoreModules
         */
        CORE
    }

    private data class Pair(val context: Any, val callback: EventCallback<out Any>)
}

/**
 * Holds essential information about a single event registration.
 *
 * @property subscriptionID Unique identifier for the event subscription
 * @property eventType Type of registered event
 */
class EventRegistrationInfo(var subscriptionID: String?, var eventType: EventType<*>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as EventRegistrationInfo

        if (subscriptionID != other.subscriptionID) return false
        if (eventType != other.eventType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = subscriptionID?.hashCode() ?: 0
        result = 31 * result + eventType.hashCode()
        return result
    }
}
