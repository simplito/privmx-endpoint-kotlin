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
package com.simplito.kotlin.privmx_endpoint_extra.lib

import com.simplito.kotlin.privmx_endpoint.model.Event
import com.simplito.kotlin.privmx_endpoint.model.PKIVerificationOptions
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.CoreEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.CustomEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.InboxEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.KvdbEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.StoreEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.ThreadEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.CoreEventType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.InboxEventType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.KvdbEventType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.StoreEventType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.ThreadEventType
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.crypto.CryptoApi
import com.simplito.kotlin.privmx_endpoint_extra.events.CallbackRegistration
import com.simplito.kotlin.privmx_endpoint_extra.events.EventCallback
import com.simplito.kotlin.privmx_endpoint_extra.events.EventDispatcher
import com.simplito.kotlin.privmx_endpoint_extra.events.EventDispatcher.SubscriptionModule
import com.simplito.kotlin.privmx_endpoint_extra.events.EventRegistrationInfo
import com.simplito.kotlin.privmx_endpoint_extra.events.EventType
import com.simplito.kotlin.privmx_endpoint_extra.events.isLibEvent
import com.simplito.kotlin.privmx_endpoint_extra.model.Modules
import kotlin.jvm.JvmOverloads


/**
 * Extends [BasicPrivmxEndpoint] with event callbacks dispatcher.
 *
 * @param enableModule   set of modules to initialize; should contain [Modules.THREAD]
 * to enable Thread module or [Modules.STORE] to enable Store module
 * @param bridgeUrl      Bridge Server URL
 * @param solutionId     `SolutionId` of the current project
 * @param userPrivateKey user private key used to authorize; generated from:
 * [CryptoApi.generatePrivateKey] or [CryptoApi.derivePrivateKey2]
 * @param verificationOptions PrivMX Bridge server instance verification options using a PKI server
 * @throws IllegalStateException thrown if there is an exception during init modules
 * @throws PrivmxException       thrown if there is a problem during login
 * @throws NativeException       thrown if there is an **unknown** problem during login
 */
class PrivmxEndpoint
@Throws(
    IllegalStateException::class,
    PrivmxException::class,
    NativeException::class
)
@JvmOverloads
constructor(
    enableModule: Set<Modules>,
    userPrivateKey: String,
    solutionId: String,
    bridgeUrl: String,
    verificationOptions: PKIVerificationOptions? = null
) : BasicPrivmxEndpoint(enableModule, userPrivateKey, solutionId, bridgeUrl, verificationOptions),
    AutoCloseable {
    private val onRemoveSubscriptionEntry = { removedSubscriptions: Map<SubscriptionModule, List<String>> ->
        try {
            removedSubscriptions.forEach { (module, subscriptions) ->
                unsubscribeMany(module, subscriptions)
            }
        } catch (_: Exception) {
        }
    }
    private val eventDispatcher: EventDispatcher = EventDispatcher(onRemoveSubscriptionEntry)


    /**
     * Registers single callback for a specified event type.
     * If you need to register multiple callbacks simultaneously, consider using the [registerManyCallbacks].
     *
     * @param T         type of data passed to callback
     * @param callbackGroup An identifier used to group related callbacks
     * @param eventType type of event to listen to
     * @param callback  a block of code to execute when event was handled
     * @throws RuntimeException thrown when method encounters an exception during subscribing on channel
     */
    @Throws(RuntimeException::class)
    suspend fun <T : Any> registerCallback(
        callbackGroup: Any,
        eventType: EventType<T>,
        callback: EventCallback<T>
    ): RegistrationResult {
        return registerManyCallbacks(
            CallbackRegistration(callbackGroup, eventType, callback)
        ).first()
    }

    /**
     * Unregisters all callbacks associated with the given group references.
     *
     * @param callbackGroups callback groups to unregister. Passing more groups allows optimize
     *                       amount of request sending to server.
     */
    suspend fun unregisterCallbacks(vararg callbackGroups: Any) {
        eventDispatcher.unbind(callbackGroups)
    }

    /**
     * Unregisters all callbacks registered by [registerCallback].
     */
    suspend fun unregisterAll() {
        eventDispatcher.unbindAll()
    }

    /**
     * Handles event and invokes all related callbacks. It should only be called by event loops.
     *
     * @param event event to handle
     */
    suspend fun handleEvent(event: Event<out Any>) {
        eventDispatcher.emit(event)
    }

    /**
     * Registers multiple callbacks in a batch.
     * This method allows for the registration of several event listeners at once,
     * which is more efficient than registering each callback individually,
     * because the number of requests can be minimized.
     *
     * @param registrations A list of [CallbackRegistration] objects. Each object
     *                      encapsulates the details for a single event listener to be
     *                      registered, including the event type, the callback to execute,
     *                      and a callback group identifier.
     * @return A list of results, in an order matching the input {@code registrations}.
     *                      Each result contains a {@link Throwable} error if an exception
     *                      occurred during its corresponding registration.
     */
    suspend fun registerManyCallbacks(
        vararg registrations: CallbackRegistration<out Any>
    ): List<RegistrationResult> {
        val results = registrations.map { CallbackRegistrationWithResult(it, null) }
        val eventsToSubscribeByModule: MutableMap<SubscriptionModule, EventsToSubscribe> = mutableMapOf()

        results.forEach { result ->
            val registration = result.registration
            var module: SubscriptionModule? = null
            var query: String? = null
            val eventType = registration.eventType

            val registrationInfo = eventDispatcher.registerCallback(registration)

            if (registrationInfo.subscriptionID != null || isLibEvent(eventType.eventName)) {
                result.result = RegistrationResult(null);
            } else {
                if (eventType.channelName != null && eventType.eventSelectorType is CustomEventSelectorType) {
                    module = SubscriptionModule.CUSTOM_EVENT;
                    query = eventApi?.buildSubscriptionQuery(
                        eventType.channelName!!,
                        eventType.eventSelectorType as CustomEventSelectorType,
                        eventType.eventSelectorId!!
                    )
                } else if (eventType.libEventType is ThreadEventType) {
                    module = SubscriptionModule.THREAD
                    query = threadApi?.buildSubscriptionQuery(
                        eventType.libEventType as ThreadEventType,
                        eventType.eventSelectorType as ThreadEventSelectorType,
                        eventType.eventSelectorId!!
                    )
                } else if (eventType.libEventType is StoreEventType) {
                    module = SubscriptionModule.STORE
                    query = storeApi?.buildSubscriptionQuery(
                        eventType.libEventType as StoreEventType,
                        eventType.eventSelectorType as StoreEventSelectorType,
                        eventType.eventSelectorId!!
                    )
                } else if (eventType.libEventType is InboxEventType) {
                    module = SubscriptionModule.INBOX;
                    query = inboxApi?.buildSubscriptionQuery(
                        eventType.libEventType as InboxEventType,
                        eventType.eventSelectorType as InboxEventSelectorType,
                        eventType.eventSelectorId!!
                    )
                } else if (eventType.libEventType is KvdbEventType) {
                    module = SubscriptionModule.KVDB;
                    query = kvdbApi?.buildSubscriptionQuery(
                        eventType.libEventType as KvdbEventType,
                        eventType.eventSelectorType as KvdbEventSelectorType,
                        eventType.eventSelectorId!!
                    )
                } else if (eventType.libEventType is CoreEventType) {
                    module = SubscriptionModule.CORE;
                    query = connection.buildSubscriptionQuery(
                        eventType.libEventType as CoreEventType,
                        eventType.eventSelectorType as CoreEventSelectorType,
                        eventType.eventSelectorId!!
                    )
                }
                eventsToSubscribeByModule.getOrPut(module!!) {
                    EventsToSubscribe()
                }.add(query!!, result, registrationInfo)
            }
        }
        subscribeAll(eventsToSubscribeByModule)
        return results.map { it.result!! }
    }

    @Throws(IllegalStateException::class, NativeException::class, PrivmxException::class)
    private fun unsubscribeMany( module: SubscriptionModule,  subscriptionIds: List<String>) {
        when (module) {
            SubscriptionModule.CUSTOM_EVENT -> {
                checkNotNull(eventApi) { "eventApi is not initialized" }
                eventApi.unsubscribeFrom(subscriptionIds)
            }

            SubscriptionModule.THREAD -> {
                checkNotNull(threadApi) { "threadApi is not initialized" }
                threadApi.unsubscribeFrom(subscriptionIds)
            }

            SubscriptionModule.STORE -> {
                checkNotNull(storeApi) { "storeApi is not initialized" }
                storeApi.unsubscribeFrom(subscriptionIds)
            }

            SubscriptionModule.INBOX -> {
                checkNotNull(inboxApi) { "inboxApi is not initialized" }
                inboxApi.unsubscribeFrom(subscriptionIds)
            }

            SubscriptionModule.KVDB -> {
                checkNotNull(kvdbApi) { "kvdbApi is not initialized" }
                kvdbApi.unsubscribeFrom(subscriptionIds)
            }

            SubscriptionModule.CORE -> {
                checkNotNull(connection) { "Connection is not initialized" }
                connection.unsubscribeFrom(subscriptionIds)
            }
        }
    }

    @Throws(
        IllegalStateException::class,
        PrivmxException::class,
        NativeException::class,
        NullPointerException::class
    )
    private fun subscribeFor(
        queriesAndCallbacks: Map<String, List<EventToSubscribe>>,
        subscribeMethod: (List<String>) -> List<String>
    ) {
        val queries: List<String> = queriesAndCallbacks.keys.toList()
        val ids: List<String?> = subscribeMethod(queries)

        if (ids.size == queries.size) {
            for (i in 0..<ids.size) {
                val query = queries[i]
                val id = ids[i]
                queriesAndCallbacks[query]?.forEach { subscribedEvent ->
                    subscribedEvent.eventRegistrationInfo.subscriptionID = id
                    subscribedEvent.callbackRegistrationWithResult.result = RegistrationResult(null)
                }
            }
        }
    }

    private suspend fun subscribeAll(eventsToSubscribeByModule: Map<SubscriptionModule, EventsToSubscribe>) {
        eventsToSubscribeByModule.forEach { (key, value) ->
            try {
                when (key) {
                    SubscriptionModule.CUSTOM_EVENT -> {
                        checkNotNull(eventApi) { "eventApi is not initialized" }
                        subscribeFor(value.getQueriesMap(), eventApi::subscribeFor)
                    }

                    SubscriptionModule.THREAD -> {
                        checkNotNull(threadApi) { "threadApi is not initialized" }
                        subscribeFor(value.getQueriesMap(), threadApi::subscribeFor)
                    }

                    SubscriptionModule.STORE -> {
                        checkNotNull(storeApi) { "storeApi is not initialized" }
                        subscribeFor(value.getQueriesMap(), storeApi::subscribeFor)
                    }

                    SubscriptionModule.INBOX -> {
                        checkNotNull(inboxApi) { "inboxApi is not initialized" }
                        subscribeFor(value.getQueriesMap(), inboxApi::subscribeFor)
                    }

                    SubscriptionModule.KVDB -> {
                        checkNotNull(kvdbApi) { "kvdbApi is not initialized" }
                        subscribeFor(value.getQueriesMap(), kvdbApi::subscribeFor)
                    }

                    SubscriptionModule.CORE -> {
                        checkNotNull(connection) { "Connection is not initialized" }
                        subscribeFor(value.getQueriesMap(), connection::subscribeFor)
                    }
                }
            } catch (e: RuntimeException) {
                when(e){
                    is IllegalStateException, is NativeException, is PrivmxException ->{
                        val callbacks: List<EventToSubscribe> = value.getQueriesMap().values.flatten()
                        callbacks.forEach{ it ->
                            it.callbackRegistrationWithResult.result = RegistrationResult(e)
                        }
                    }else -> throw e
                }

            }
        }
        eventDispatcher.removeNotSubscribedEvents()
    }


}

private class CallbackRegistrationWithResult(
    val registration: CallbackRegistration<*>,
    var result: RegistrationResult?
)

/**
 * A result for single [CallbackRegistration].
 * @property error The [Throwable] representing the error if one occurred;
 *          otherwise, returns `null` indicating a successful registration.
 */
class RegistrationResult internal constructor(val error: Throwable?) {
    /**
     * Checks if the registration attempt associated with this result encountered an error.
     *
     * @return `true` if an error is present (i.e., an exception occurred),
     *          `false` if the registration was successful.
     */
    val isError: Boolean
        get() = error != null
}

private class EventToSubscribe(
    val callbackRegistrationWithResult: CallbackRegistrationWithResult,
    val eventRegistrationInfo: EventRegistrationInfo
)

private class EventsToSubscribe {
    private val queriesMap: MutableMap<String, MutableList<EventToSubscribe>> = mutableMapOf()

    fun add(
        query: String,
        callbackRegistrationWithResult: CallbackRegistrationWithResult,
        eventRegistrationInfo: EventRegistrationInfo
    ) {
        queriesMap.getOrPut(query) {
            mutableListOf()
        }.add(
            EventToSubscribe(callbackRegistrationWithResult, eventRegistrationInfo)
        )
    }

    fun getQueriesMap(): Map<String, List<EventToSubscribe>> = queriesMap
}