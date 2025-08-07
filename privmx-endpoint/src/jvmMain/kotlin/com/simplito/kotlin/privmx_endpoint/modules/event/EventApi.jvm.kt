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

import com.simplito.kotlin.privmx_endpoint.LibLoader
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.CustomEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection

/**
 * Manages PrivMX Bridge custom events.
 *
 * @param connection active connection to PrivMX Bridge
 * @throws IllegalStateException when passed [Connection] is not connected.
 */
actual class EventApi
@Throws(IllegalStateException::class)
actual constructor(connection: Connection) : AutoCloseable {
    companion object {
        init {
            LibLoader.load()
        }
    }

    private val api: Long? = init(connection)

    @Throws(java.lang.IllegalStateException::class)
    private external fun init(connection: Connection): Long?

    @Throws(java.lang.IllegalStateException::class)
    private external fun deinit()

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
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun emitEvent(
        contextId: String,
        users: List<UserWithPubKey>,
        channelName: String,
        eventData: ByteArray
    )

    /**
     * Subscribe for the custom events on the given subscription query.
     *
     * @param subscriptionQueries list of queries
     * @return list of subscriptionIds in matching order to subscriptionQueries
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, java.lang.IllegalStateException::class)
    actual external fun subscribeFor(subscriptionQueries: List<String>): List<String>

    /**
     * Unsubscribe from events for the given subscriptionId.
     *
     * @param subscriptionIds list of subscriptionId
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, java.lang.IllegalStateException::class)
    actual external fun unsubscribeFrom(subscriptionIds: List<String>)

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    private external fun buildSubscriptionQuery(
        channelName: String,
        selectorType: Long,
        selectorId: String
    ): String

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
    @Throws(PrivmxException::class, NativeException::class, java.lang.IllegalStateException::class)
    actual fun buildSubscriptionQuery(
        channelName: String,
        selectorType: CustomEventSelectorType,
        selectorId: String
    ): String {
        return buildSubscriptionQuery(
            channelName,
            selectorType.ordinal.toLong(),
            selectorId
        )
    }

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed.
     */
    actual override fun close() {
        deinit()
    }
}