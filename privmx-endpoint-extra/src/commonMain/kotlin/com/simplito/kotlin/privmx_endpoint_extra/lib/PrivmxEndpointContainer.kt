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
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.core.EventQueue
import com.simplito.kotlin.privmx_endpoint.modules.crypto.CryptoApi
import com.simplito.kotlin.privmx_endpoint_extra.events.EventType
import com.simplito.kotlin.privmx_endpoint_extra.model.Modules
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.collections.get

/**
 * Manages certificates, Platform sessions, and active connections.
 * Implements event loop that can be started using [.startListening].
 * Contains instance of [CryptoApi].
 *
 * @category core
 */
class PrivmxEndpointContainer : AutoCloseable {
    private val privmxEndpoints = mutableMapOf<Long, PrivmxEndpoint>()


    /**
     * Instance of [CryptoApi].
     */
    val cryptoApi: CryptoApi = CryptoApi()
    private val containerScope =
        CoroutineScope(Dispatchers.Default + CoroutineName("EndpointContainer") + CoroutineExceptionHandler { context, exception ->
            println("${exception.message}")
        })

    private val eventLoopContext =
        Dispatchers.Default + CoroutineName("Event-loop-scope") + CoroutineExceptionHandler { context, exception ->
            println("${exception.message}")
        }
    private val connectionsMutex = Mutex()

    private val eventLoopJob = containerScope.launch(eventLoopContext, CoroutineStart.LAZY) {
        while (isActive) {
            try {
                val event = EventQueue.waitEvent()
                onNewEvent(event!! as Event<out Any>)
            } catch (e: Exception) {
                println("Catch event exception: " + e.message)
            }
        }
    }

    /**
     * Stops event loop.
     */
    fun stopListening() {
        eventLoopJob.cancel()
        EventQueue.emitBreakEvent()
    }

    /**
     * Returns connection matching given `connectionId`.
     *
     * @param connectionId Id of connection
     * @return Active connection
     * @throws IllegalStateException if certificate is not set successfully
     */
    fun getEndpoint(connectionId: Long?): PrivmxEndpoint {
        return privmxEndpoints.get(connectionId)!!
    }

    val endpointIDs: Set<Long?>
        /**
         * Returns set of all active connection's IDs.
         *
         * @return set of all active connection's IDs
         * @throws IllegalStateException if certificate is not set successfully
         */
        get() = privmxEndpoints.keys

    /**
     * Sets path to the certificate used to create a secure connection to PrivMX Bridge.
     * It checks whether a .pem file with certificate exists in `certsPath` and uses it if it does.
     *
     * @param certsPath path to file with .pem certificate
     * @throws PrivmxException if there is an error while setting `certsPath`
     * @throws NativeException if there is an unknown error during set `certsPath`
     */
    @Throws(IllegalArgumentException::class, PrivmxException::class, NativeException::class)
    fun setCertsPath(certsPath: String) {
        val path = Path(certsPath)
        require(SystemFileSystem.exists(path)) { "Certs file does not exists" }
        require(SystemFileSystem.metadataOrNull(path)?.isRegularFile == true) { "Invalid file path" }
        Connection.setCertsPath(certsPath)
    }

    /**
     * Creates a new connection.
     *
     * @param enableModule   set of modules to initialize
     * @param bridgeUrl      Bridge's Endpoint URL
     * @param solutionId     `SolutionId` of the current project
     * @param userPrivateKey user private key used to authorize; generated from:
     * [CryptoApi.generatePrivateKey] or
     * [CryptoApi.derivePrivateKey]
     * @return Created connection
     * @throws IllegalStateException when certPath is not set up
     * @throws PrivmxException       if there is a problem during login
     * @throws NativeException       if there is an unknown problem during login
     */
    @Throws(PrivmxException::class, NativeException::class)
    fun connect(
        enableModule: Set<Modules>,
        userPrivateKey: String,
        solutionId: String,
        bridgeUrl: String
    ): PrivmxEndpoint {
        val privmxEndpoint = PrivmxEndpoint(
            enableModule,
            userPrivateKey,
            solutionId,
            bridgeUrl
        )
        containerScope.launch {
            connectionsMutex.withLock {
                privmxEndpoints[privmxEndpoint.connection.getConnectionId()!!] = privmxEndpoint
            }
        }
        return privmxEndpoint
    }

    /**
     * Disconnects connection matching given `connectionId` and removes it from the container.
     * This method is recommended for disconnecting connections by their ID from the container.
     *
     * @param connectionId ID of the connection
     */
    fun disconnect(connectionId: Long?) {
        val endpoint: PrivmxEndpoint = privmxEndpoints.get(connectionId)!!
        checkNotNull(endpoint) { "No connection with specified id" }
        try {
            endpoint.close()
        } catch (ignored: Exception) {
        }
    }

    /**
     * Disconnects all connections and removes them from the container.
     */
    fun disconnectAll() {
        containerScope.launch {
            connectionsMutex.withLock {
                privmxEndpoints.values.forEach { endpoint ->
                    try {
                        endpoint.close()
                    } catch (ignored: Exception) {
                    }
                }
                privmxEndpoints.clear()
            }
        }
    }

    /**
     * Starts event handling Thread.
     */
    fun startListening() {
        if (!eventLoopJob.isActive) {
            eventLoopJob.start()
        }
    }


    private suspend fun onNewEvent(event: Event<out Any>) {
        if (event.type.equals("libPlatformDisconnected")) {
            return
        }
        if (event.connectionId != null && event.connectionId != -1L) {
            connectionsMutex.withLock {
                privmxEndpoints[event.connectionId]?.let { endpoint ->
                    endpoint.handleEvent(event)
                    if (event.type == EventType.DisconnectedEvent.eventType) {
                        privmxEndpoints.remove(event.connectionId).run {
                            try {
                                close()
                            } catch (_: Exception) {
                            }
                        }
                    }
                }
            }
        }

        if (event.type == EventType.LibBreakEvent.eventType) {
            eventLoopJob.cancel()
            return
        }
    }

    /**
     * Closes event loop.
     */
//    @Throws(Exception::class)
    //TODO change to not use runBlocking
    override fun close() = runBlocking {
        stopListening()
        connectionsMutex.withLock {
            privmxEndpoints.forEach { (_, value) ->
                try {
                    value.close()
                } catch (_: Exception) {
                }
            }
            privmxEndpoints.clear()
        }
        try {
            containerScope.cancel()
        } catch (_: Exception) {
        }
    }

    companion object {
        private val TAG = "[PrivmxEndpointContainer]"
    }
}