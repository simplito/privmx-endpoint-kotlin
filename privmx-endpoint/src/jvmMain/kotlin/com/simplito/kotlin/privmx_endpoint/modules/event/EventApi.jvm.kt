package com.simplito.kotlin.privmx_endpoint.modules.event

import com.simplito.kotlin.privmx_endpoint.LibLoader
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
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
    actual external fun subscribeForCustomEvents(contextId: String, channelName: String)

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
    actual external fun unsubscribeFromCustomEvents(contextId: String, channelName: String)

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed.
     */
    actual override fun close() {
        deinit()
    }
}