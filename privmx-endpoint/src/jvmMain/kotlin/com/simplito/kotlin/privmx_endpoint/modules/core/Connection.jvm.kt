package com.simplito.kotlin.privmx_endpoint.modules.core

import com.simplito.kotlin.privmx_endpoint.LibLoader
import com.simplito.kotlin.privmx_endpoint.model.Context
import com.simplito.kotlin.privmx_endpoint.model.PagingList

/**
 * Manages a connection between the Endpoint and the Bridge server.
 *
 * @category core
 */
actual class Connection private constructor(private val api: Long?,private val connectionId: Long?) : AutoCloseable {
    actual companion object{
        init {
            LibLoader.load()
        }

        /**
         * Connects to PrivMX Bridge server.
         *
         * @param userPrivKey user's private key
         * @param solutionId  ID of the Solution
         * @param bridgeUrl   Bridge's Endpoint URL
         * @return Connection object
         * @throws PrivmxException thrown when method encounters an exception.
         * @throws NativeException thrown when method encounters an unknown exception.
         * @event type: libConnected
         * channel: -
         * @payload: [Void]
         */
        @JvmStatic
        actual external fun connect(
            userPrivKey: String,
            solutionId: String,
            bridgeUrl: String,
        ): Connection

        /**
         * Connects to PrivMX Bridge server as a guest user.
         *
         * @param solutionId ID of the Solution
         * @param bridgeUrl  Bridge's Endpoint URL
         * @return Connection object
         * @throws PrivmxException thrown when method encounters an exception.
         * @throws NativeException thrown when method encounters an unknown exception.
         * @event type: libConnected
         * channel: -
         * payload: [Void]
         */
        @JvmStatic
        actual external fun connectPublic(
            solutionId: String,
            bridgeUrl: String,
        ): Connection

        /**
         * Allows to set path to the SSL certificate file.
         *
         * @param certsPath path to file
         * @throws PrivmxException thrown when method encounters an exception.
         * @throws NativeException thrown when method encounters an unknown exception.
         */
        @JvmStatic
        actual external fun setCertsPath(certsPath: String)
    }

    /**
     * Gets the ID of the current connection.
     *
     * @return ID of the connection
     */
    actual fun getConnectionId() = this.connectionId

    /**
     * If there is an active connection then it
     * disconnects from PrivMX Bridge and frees memory making this instance not reusable.
     */
    actual override fun close() {
        deinit()
    }

    /**
     * Gets a list of Contexts available for the user.
     *
     * @param skip      skip number of elements to skip from result
     * @param limit     limit of elements to return for query
     * @param sortOrder order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId    ID of the element from which query results should start
     * @return list of Contexts
     * @throws IllegalStateException thrown when instance is not connected.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @JvmOverloads
    actual external fun listContexts(
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?
    ): PagingList<Context>

    /**
     * Disconnects from PrivMX Bridge server.
     *
     * @throws IllegalStateException thrown when instance is not connected or closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @event type: libDisconnected
     * channel: -
     * payload: [Void]
     * @event type: libPlatformDisconnected
     * channel: -
     * payload: [Void]
     */
    actual external fun disconnect()

    private external fun  deinit();
}

