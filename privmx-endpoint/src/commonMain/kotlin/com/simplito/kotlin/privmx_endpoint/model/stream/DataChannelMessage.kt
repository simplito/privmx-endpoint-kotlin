package com.simplito.kotlin.privmx_endpoint.model.stream

open class DataChannelMessage(
    val data: ByteArray,
    val seq: Long?
)