package com.simplito.kotlin.privmx_endpoint.modules.core

import com.simplito.kotlin.privmx_endpoint.model.Context
import com.simplito.kotlin.privmx_endpoint.model.PagingList


expect class Connection: AutoCloseable{
    override fun close()
    companion object {
        fun connect(userPrivKey: String, solutionId: String, bridgeUrl: String): Connection
        fun connectPublic(solutionId: String, bridgeUrl: String): Connection

        fun setCertsPath(certsPath: String?)
    }

    fun listContexts(
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String? = null
    ): PagingList<Context>

    fun getConnectionId(): Long?

    fun disconnect()

}