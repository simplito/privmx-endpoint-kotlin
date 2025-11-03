package com.simplito.kotlin.privmx_endpoint.modules

import com.simplito.kotlin.privmx_endpoint.LibLoader
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.event.EventApi

actual class StreamApi
@Throws(IllegalStateException::class)
@JvmOverloads
actual constructor(
    connection: Connection,
    eventApi: EventApi?
) : AutoCloseable {
    companion object {
        init {
            LibLoader.loadPrivmxLibraries()
        }
    }

    private var api: Long? = null

    init {
        val tmpEventApi = if (eventApi == null) EventApi(connection) else null
        api = init(
            connection,
            eventApi ?: tmpEventApi!!,
        )

        eventApi?.close()
    }


    @Throws(Exception::class)
    actual override fun close() {
        deinit()
    }

    @Throws(IllegalStateException::class)
    private external fun init(
        connection: Connection,
        eventApi: EventApi
    ): Long?

    @Throws(IllegalStateException::class)
    private external fun deinit()
}