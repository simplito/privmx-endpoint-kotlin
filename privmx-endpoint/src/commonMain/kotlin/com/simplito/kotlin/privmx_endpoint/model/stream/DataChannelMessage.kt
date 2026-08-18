package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * Holds a message sent over a Stream's data channel.
 *
 * @property data   message's plain (unencrypted) content
 * @property seq    message's sequence number, used to detect replayed and out-of-order messages
 */
open class DataChannelMessage(
    val data: ByteArray,
    val seq: Long
)