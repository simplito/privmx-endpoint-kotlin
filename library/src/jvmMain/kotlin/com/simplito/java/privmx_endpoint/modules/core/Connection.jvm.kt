package com.simplito.java.privmx_endpoint.modules.core

import com.simplito.java.privmx_endpoint.model.Context
import com.simplito.java.privmx_endpoint.model.PagingList

actual class Connection(private val api: Long?,private val connectionId: Long?) : AutoCloseable {
    actual companion object{
        init {
            System.loadLibrary("privmx-endpoint-java")
        }

        @JvmStatic
        actual external fun connect(
            userPrivKey: String,
            solutionId: String,
            host: String,
        ): Connection
    }

    private external fun  deinit();

    actual override fun close() {
        deinit()
    }

    actual external fun listContexts(
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?
    ): PagingList<Context>

    actual external fun disconnect()
}

