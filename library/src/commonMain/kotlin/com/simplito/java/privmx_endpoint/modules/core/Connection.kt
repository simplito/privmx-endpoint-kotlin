package com.simplito.java.privmx_endpoint.modules.core

import com.simplito.java.privmx_endpoint.model.Context
import com.simplito.java.privmx_endpoint.model.PagingList


expect class Connection: AutoCloseable{
    override fun close()
    companion object {
        fun connect(userPrivKey: String, solutionId: String, host: String): Connection
    }

    fun listContexts(
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String? = null
    ): PagingList<Context>

    fun disconnect()

}