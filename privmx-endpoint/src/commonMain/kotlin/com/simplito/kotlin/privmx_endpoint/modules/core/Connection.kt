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
import com.simplito.kotlin.privmx_endpoint.model.PKIVerificationOptions
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.UserInfo
import com.simplito.kotlin.privmx_endpoint.modules.core.UserVerifierInterface
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException

/**
 * Manages a connection between the PrivMX Endpoint and PrivMX Bridge server.
 */
expect class Connection : AutoCloseable {
    companion object {
        /**
         * Connects to PrivMX Bridge server.
         *
         * @param userPrivKey user's private key
         * @param solutionId  ID of the Solution
         * @param bridgeUrl   PrivMX Bridge server URL
         * @param verificationOptions PrivMX Bridge server instance verification options using a PKI server
         * @return Connection object
         * @throws PrivmxException thrown when method encounters an exception
         * @throws NativeException thrown when method encounters an unknown exception
         */
        @Throws(PrivmxException::class, NativeException::class)
        fun connect(
            userPrivKey: String,
            solutionId: String,
            bridgeUrl: String,
            verificationOptions: PKIVerificationOptions? = null
        ): Connection

        /**
         * Connects to PrivMX Bridge server as a guest user.
         *
         * @param solutionId ID of the Solution
         * @param bridgeUrl  PrivMX Bridge server URL
         * @param verificationOptions PrivMX Bridge server instance verification options using a PKI server
         * @return Connection object
         * @throws PrivmxException thrown when method encounters an exception
         * @throws NativeException thrown when method encounters an unknown exception
         */
        @Throws(PrivmxException::class, NativeException::class)
        fun connectPublic(
            solutionId: String,
            bridgeUrl: String,
            verificationOptions: PKIVerificationOptions? = null
        ): Connection

        /**
         * Allows to set path to the SSL certificate file.
         *
         * @param certsPath path to file
         * @throws PrivmxException thrown when method encounters an exception
         * @throws NativeException thrown when method encounters an unknown exception
         */
        @Throws(PrivmxException::class, NativeException::class)
        fun setCertsPath(certsPath: String)
    }

    /**
     * Gets a list of Contexts available for the user.
     *
     * @param skip      skip number of elements to skip from result
     * @param limit     limit of elements to return for query
     * @param sortOrder order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId    ID of the element from which query results should start
     * @param queryAsJson stringified JSON object with a custom field to filter result
     * @param sortBy      field name to sort elements by
     * @return list of Contexts
     * @throws IllegalStateException thrown when instance is not connected
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun listContexts(
        skip: Long, limit: Long,
        sortOrder: String = "desc",
        lastId: String? = null,
        queryAsJson: String? = null,
        sortBy: String? = null
    ): PagingList<Context>

    /**
     * Gets the ID of the current connection.
     *
     * @return ID of the connection
     */
    fun getConnectionId(): Long?

    /**
     * Sets user's custom verification callback.
     *
     * The feature allows the developer to set up a callback for user verification.
     * A developer can implement an interface and pass the implementation to the function.
     * Each time data is read from the container, a callback will be triggered, allowing the developer to validate the sender in an external service,
     * e.g. Developer's Application Server or PKI Server.
     *
     * @param userVerifier an implementation of the [UserVerifierInterface]
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is not connected.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun setUserVerifier(userVerifier: UserVerifierInterface): Unit

    /**
     * Gets a list of users of given context.
     *
     * @param contextId ID of the context
     * @return list of users Info
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is not connected.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun getContextUsers(contextId: String): List<UserInfo>

    /**
     * Disconnects from PrivMX Bridge server.
     *
     * @throws IllegalStateException thrown when instance is not connected or closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun disconnect()

    /**
     * If there is an active connection then it
     * disconnects from PrivMX Bridge and frees memory making this instance not reusable.
     */
    override fun close()
}