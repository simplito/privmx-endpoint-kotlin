package com.simplito.kotlin.privmx_endpoint.modules.core

import com.simplito.kotlin.privmx_endpoint.LibLoader
import com.simplito.kotlin.privmx_endpoint.model.Context
import com.simplito.kotlin.privmx_endpoint.model.PagingList

actual class Connection private constructor(private val api: Long?,private val connectionId: Long?) : AutoCloseable {
    actual companion object{
        init {
            LibLoader.load()
        }
        @JvmStatic
        actual external fun connect(
            userPrivKey: String,
            solutionId: String,
            bridgeUrl: String,
        ): Connection

        @JvmStatic
        actual external fun connectPublic(
            solutionId: String,
            bridgeUrl: String,
        ): Connection

        @JvmStatic
        actual external fun setCertsPath(certsPath: String)
    }

    private external fun  deinit();

    actual override fun close() {
        deinit()
    }

    @JvmOverloads
    actual external fun listContexts(
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?
    ): PagingList<Context>

    actual external fun disconnect()

    actual fun getConnectionId() = this.connectionId
}

