package com.simplito.kotlin.privmx_endpoint.modules

import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.event.EventApi

expect class StreamApi
@Throws(IllegalStateException::class)
constructor(
    connection: Connection,
    eventApi: EventApi? = null
) : AutoCloseable {

    @Throws(Exception::class)
    override fun close()
}