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
package com.simplito.kotlin.privmx_endpoint_extra.lib

import com.simplito.kotlin.privmx_endpoint.model.Event
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint_extra.events.EventCallback
import com.simplito.kotlin.privmx_endpoint_extra.events.EventDispatcher
import com.simplito.kotlin.privmx_endpoint_extra.events.EventType
import com.simplito.kotlin.privmx_endpoint_extra.model.Modules
import com.simplito.kotlin.privmx_endpoint.modules.crypto.CryptoApi

/**
 * Extends [BasicPrivmxEndpoint] with event callbacks dispatcher.
 *
 * @param enableModule   set of modules to initialize; should contain [Modules.THREAD]
 * to enable Thread module or [Modules.STORE] to enable Store module
 * @param bridgeUrl      Bridge's Endpoint URL
 * @param solutionId     `SolutionId` of the current project
 * @param userPrivateKey user private key used to authorize; generated from:
 * [CryptoApi.generatePrivateKey] or [CryptoApi.derivePrivateKey2]
 * @throws IllegalStateException thrown if there is an exception during init modules
 * @throws PrivmxException       thrown if there is a problem during login
 * @throws NativeException       thrown if there is an **unknown** problem during login
 * @category core
 */
class PrivmxEndpoint
@Throws(
    IllegalStateException::class,
    PrivmxException::class,
    NativeException::class
)
constructor(
    enableModule: Set<Modules>,
    userPrivateKey: String,
    solutionId: String,
    bridgeUrl: String
) : BasicPrivmxEndpoint(enableModule, userPrivateKey, solutionId, bridgeUrl), AutoCloseable {
    private val onRemoveChannel = { channel: String ->
        try {
            unsubscribeChannel(channel)
        } catch (e: Exception) {
            println("Cannot unsubscribe channel")
        }
    }
    private val eventDispatcher: EventDispatcher = EventDispatcher(onRemoveChannel)

    /**
     * Registers callbacks with the specified type.
     *
     * @param T         type of data passed to callback
     * @param context   an object that identifies callbacks in the list
     * @param eventType type of event to listen to
     * @param callback  a block of code to execute when event was handled
     * @throws RuntimeException thrown when method encounters an exception during subscribing on channel.
     */
    @Throws(RuntimeException::class)
    suspend fun <T> registerCallback(
        context: Any,
        eventType: EventType<T>,
        callback: EventCallback<T>
    ) {
        if (eventDispatcher.register(eventType.channel, eventType.eventType, context, callback)) {
            try {
                subscribeChannel(eventType.channel)
            } catch (e: Exception) {
                throw RuntimeException("Cannot subscribe event channel for this event (detail message: " + e.message + ")")
            }
        }
    }

    /**
     * Unregisters all callbacks registered by [registerCallback] and identified with given Context.
     *
     * @param context an object that identifies callbacks in the list.
     */
    suspend fun unregisterCallbacks(context: Any) {
        eventDispatcher.unbind(context)
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

    private fun subscribeChannel(channelStr: String) {
        val channel = Channel.fromString(channelStr)
        if (channel == null) {
            println("Cannot subscribe on events channel (pattern not found)")
            return
        }
        if (channel.module.startsWith("thread") && threadApi != null) {
            if (channel.type != null && channel.type.equals("messages")) {
                if (channel.instanceId != null) {
                    threadApi.subscribeForMessageEvents(channel.instanceId)
                } else {
                    println("No threadId to subscribeChannel: $channelStr")
                }
                return
            }
            threadApi.subscribeForThreadEvents()
            return
        }

        if (channel.module.startsWith("store") && storeApi != null) {
            if (channel.type != null && channel.type.equals("files")) {
                if (channel.instanceId != null) {
                    storeApi.subscribeForFileEvents(channel.instanceId)
                } else {
                    println("No storeId to subscribeChannel: $channelStr")
                }
                return
            }
            storeApi.subscribeForStoreEvents()
            return
        }

        if (channel.module.startsWith("inbox") && inboxApi != null) {
            if (channel.type != null && channel.type.equals("entries")) {
                if (channel.instanceId != null) {
                    inboxApi.subscribeForEntryEvents(channel.instanceId)
                } else {
                    println("No inboxId to subscribeChannel: $channelStr")
                }
                return
            }
            inboxApi.subscribeForInboxEvents()
        }
    }

    private fun unsubscribeChannel(channelStr: String) {
        val channel = Channel.Companion.fromString(channelStr)
        if (channel == null) {
            println("Cannot unsubscribe on events channel (pattern not found)")
            return
        }
        if (channel.module.startsWith("thread") && threadApi != null) {
            if (channel.type != null && channel.type == "messages") {
                if (channel.instanceId != null) {
                    threadApi.unsubscribeFromMessageEvents(channel.instanceId)
                } else {
                    println("No threadId to unsubscribeChannel: $channelStr")
                }
                return
            }
            threadApi.unsubscribeFromThreadEvents()
            return
        }

        if (channel.module.startsWith("store") && storeApi != null) {
            if (channel.type != null && channel.type == "files") {
                if (channel.instanceId != null) {
                    storeApi.unsubscribeFromFileEvents(channel.instanceId)
                } else {
                    println("No storeId to unsubscribeChannel: $channelStr")
                }
                return
            }
            storeApi.unsubscribeFromStoreEvents()
            return
        }

        if (channel.module.startsWith("inbox") && inboxApi != null) {
            if (channel.type != null && channel.type == "entries") {
                if (channel.instanceId != null) {
                    inboxApi.unsubscribeFromEntryEvents(channel.instanceId)
                } else {
                    println("No inboxId to unsubscribeChannel: $channelStr")
                }
                return
            }
            inboxApi.unsubscribeFromInboxEvents()
        }
    }

    private class Channel(
        val module: String,
        val instanceId: String?,
        val type: String?
    ) {
        companion object {
            fun fromString(channel: String): Channel? {
                return Regex("(?<module>(?:(?!/).)*)(/(?<instanceId>(?:(?!/).)*)/(?<type>(?:(?!/).)*))?")
                    .find(channel)
                    ?.run {
                        val (module, instanceId, type) = this.destructured
                        Channel(
                            module,
                            instanceId,
                            type
                        )
                    }
            }
        }
    }
}