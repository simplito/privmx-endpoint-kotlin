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

package com.simplito.kotlin.privmx_endpoint.modules.core

import com.simplito.kotlin.privmx_endpoint.model.Context
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException

/**
 * Manages a connection between the Endpoint and the Bridge server.
 *
 * @category core
 */
expect class Connection : AutoCloseable {
    companion object {
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
         * @payload: [Unit]
         */
        fun connect(userPrivKey: String, solutionId: String, bridgeUrl: String): Connection

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
         * payload: [Unit]
         */
        fun connectPublic(solutionId: String, bridgeUrl: String): Connection

        /**
         * Allows to set path to the SSL certificate file.
         *
         * @param certsPath path to file
         * @throws PrivmxException thrown when method encounters an exception.
         * @throws NativeException thrown when method encounters an unknown exception.
         */
        fun setCertsPath(certsPath: String)
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
    fun listContexts(
        skip: Long, limit: Long, sortOrder: String = "desc", lastId: String? = null
    ): PagingList<Context>

    /**
     * Gets the ID of the current connection.
     *
     * @return ID of the connection
     */
    fun getConnectionId(): Long?

    /**
     * Disconnects from PrivMX Bridge server.
     *
     * @throws IllegalStateException thrown when instance is not connected or closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @event type: libDisconnected
     * channel: -
     * payload: [Unit]
     * @event type: libPlatformDisconnected
     * channel: -
     * payload: [Unit]
     */
    fun disconnect()

    /**
     * If there is an active connection then it
     * disconnects from PrivMX Bridge and frees memory making this instance not reusable.
     */
    override fun close()
}