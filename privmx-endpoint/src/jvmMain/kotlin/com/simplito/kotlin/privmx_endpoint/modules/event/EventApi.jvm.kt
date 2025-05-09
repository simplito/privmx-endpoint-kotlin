package com.simplito.kotlin.privmx_endpoint.modules.event

import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection

/**
 * Manages PrivMX Bridge custom events.
 */
actual class EventApi : AutoCloseable {

    @Throws(IllegalStateException::class)
    actual constructor(connection: Connection)

    /**
     * Emits the custom event on the given Context and channel.
     *
     * @param contextId   ID of the Context
     * @param users       list of [UserWithPubKey] objects which defines the recipeints of the event
     * @param channelName name of the Channel
     * @param eventData   event's data
     * @throws IllegalStateException thrown when instance is closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun emitEvent(
        contextId: String,
        users: MutableList<UserWithPubKey>,
        channelName: String,
        eventData: ByteArray
    )

    /**
     * Subscribe for the custom events on the given channel.
     *
     * @param contextId   ID of the Context
     * @param channelName name of the Channel
     * @throws IllegalStateException thrown when instance is closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun subscribeForCustomEvents(contextId: String, channelName: String)

    /**
     * Unsubscribe from the custom events on the given channel.
     *
     * @param contextId   ID of the Context
     * @param channelName name of the Channel
     * @throws IllegalStateException thrown when instance is closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun unsubscribeFromCustomEvents(contextId: String, channelName: String)

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed.
     */
    actual external override fun close()
}