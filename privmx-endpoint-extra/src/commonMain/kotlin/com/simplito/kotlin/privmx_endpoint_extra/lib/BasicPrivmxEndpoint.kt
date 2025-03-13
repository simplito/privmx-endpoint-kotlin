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

import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.inbox.InboxApi
import com.simplito.kotlin.privmx_endpoint.modules.store.StoreApi
import com.simplito.kotlin.privmx_endpoint.modules.thread.ThreadApi
import com.simplito.kotlin.privmx_endpoint_extra.model.Modules

/**
 * A collection of all PrivMX Endpoint modules. It represents a single connection to PrivMX Bridge.
 *
 * @category core
 */
open class BasicPrivmxEndpoint(
    enableModule: Set<Modules>,
    userPrivateKey: String,
    solutionId: String,
    bridgeUrl: String
) : AutoCloseable {
    /**
     * Reference to Store module.
     */
    val storeApi: StoreApi?

    /**
     * Reference to Thread module.
     */
    val threadApi: ThreadApi?

    /**
     * Reference to Inbox module.
     */
    val inboxApi: InboxApi?

    /**
     * Reference to Connection module.
     */
    val connection: Connection


    /**
     * Initializes modules and connects to PrivMX Bridge server using given parameters.
     *
     * @param enableModule   set of modules to initialize; should contain [Modules.THREAD]
     * to enable Thread module or [Modules.STORE] to enable Store module
     * @param bridgeUrl      Bridge's Endpoint URL
     * @param solutionId     `SolutionId` of the current project
     * @param userPrivateKey user private key used to authorize; generated from:
     * [CryptoApi.generatePrivateKey] or
     * [CryptoApi.derivePrivateKey]
     * @throws IllegalStateException thrown if there is an exception during init modules
     * @throws PrivmxException       thrown if there is a problem during login
     * @throws NativeException       thrown if there is an **unknown** problem during login
     */
    init {
        connection = Connection.connect(userPrivateKey, solutionId, bridgeUrl)
        storeApi = if (enableModule.contains(Modules.STORE)) StoreApi(connection) else null
        threadApi = if (enableModule.contains(Modules.THREAD)) ThreadApi(connection) else null
        inboxApi = if (enableModule.contains(Modules.INBOX)) InboxApi(
            connection,
            threadApi,
            storeApi
        ) else null
    }

    /**
     * Disconnects from PrivMX Bridge and frees memory.
     *
     * @throws Exception when instance is currently closed
     */
    @Throws(Exception::class)
    override fun close() {
        threadApi?.close()
        storeApi?.close()
        inboxApi?.close()
        connection.close()
    }
}
