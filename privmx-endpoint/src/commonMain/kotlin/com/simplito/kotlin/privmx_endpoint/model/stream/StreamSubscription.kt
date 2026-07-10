package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * Describes a remote stream or a specific track within that stream,
 * used when subscribing to, modifying, or unsubscribing from remote streams.
 *
 * @property streamId       Unique identifier of the stream
 * @property streamTrackId  Identifier of a specific track within the stream. If not provided, the entire stream is targeted.
 */
data class StreamSubscription(
    val streamId: Long?,
    val streamTrackId: String?,
)