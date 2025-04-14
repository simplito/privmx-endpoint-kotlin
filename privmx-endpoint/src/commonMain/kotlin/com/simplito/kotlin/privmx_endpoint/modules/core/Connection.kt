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


expect class Connection: AutoCloseable{
    companion object {
        fun connect(userPrivKey: String, solutionId: String, bridgeUrl: String): Connection
        fun connectPublic(solutionId: String, bridgeUrl: String): Connection

        fun setCertsPath(certsPath: String)
    }

    fun listContexts(
        skip: Long,
        limit: Long,
        sortOrder: String = "desc",
        lastId: String? = null
    ): PagingList<Context>

    fun getConnectionId(): Long?

    fun disconnect()

    override fun close()
}