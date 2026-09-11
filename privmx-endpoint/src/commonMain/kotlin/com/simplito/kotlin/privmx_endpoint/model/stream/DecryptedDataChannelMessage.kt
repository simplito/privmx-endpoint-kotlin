package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * Holds a decrypted message received over a Stream's data channel.
 *
 * @property statusCode     status code of decryption of the message.
 *                          A value other than 0 means that the message could not be decrypted or
 *                          that its data integrity has been violated
 * @param data              message's plain (unencrypted) content
 * @param seq               message's sequence number, used to detect replayed and out-of-order messages
 */
class DecryptedDataChannelMessage(
    val statusCode: Long,
    data: ByteArray,
    seq: Long
) : DataChannelMessage(data, seq)