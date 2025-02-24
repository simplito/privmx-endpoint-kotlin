package com.simplito.java.privmx_endpoint.modules.core

import com.simplito.java.privmx_endpoint.model.Context
import com.simplito.java.privmx_endpoint.model.PagingList

actual class Connection(private val api: Long?,private val connectionId: Long?) : AutoCloseable {
    init{
        println("Connection class name: ${Connection::class.java.name}")
    }
    actual companion object{
        init {
            System.loadLibrary("privmx-endpoint-java")
            System.loadLibrary("crypto")
            System.loadLibrary("ssl")
        }

        @JvmStatic
        actual external fun connect(
            userPrivKey: String,
            solutionId: String,
            host: String,
        ): Connection
    }

//    private fun init();

//    actual companion object {

//    }

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

