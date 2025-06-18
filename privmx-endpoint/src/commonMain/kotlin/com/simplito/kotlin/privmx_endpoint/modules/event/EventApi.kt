package com.simplito.kotlin.privmx_endpoint.modules.event

import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection

/**
 * Manages PrivMX Bridge context custom events.
 *
 * @param connection active connection to PrivMX Bridge
 * @throws IllegalStateException when passed [Connection] is not connected.
 */
expect class EventApi
@Throws(IllegalStateException::class)
constructor(connection: Connection) : AutoCloseable {

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
    fun emitEvent(
        contextId: String,
        users: List<UserWithPubKey>,
        channelName: String,
        eventData: ByteArray
    )

    /**
     * Subscribe for the custom events on the given channel.
     *
     * @param contextId   ID of the Context
     * @param channelName name of the Channel
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun subscribeForCustomEvents(contextId: String, channelName: String)

    /**
     * Unsubscribe from the custom events on the given channel.
     *
     * @param contextId   ID of the Context
     * @param channelName name of the Channel
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun unsubscribeFromCustomEvents(contextId: String, channelName: String)

    /**
     * Frees memory.
     */
    override fun close()
}