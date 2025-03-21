package com.simplito.java.privmx_endpoint.modules.core

import com.simplito.java.privmx_endpoint.model.Context
import com.simplito.java.privmx_endpoint.model.PagingList


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