package com.simplito.kotlin.privmx_endpoint.model.stream

class DecryptedDataChannelMessage(
    val statusCode: Long,
    data: ByteArray,
    seq: Long
): DataChannelMessage(data, seq)