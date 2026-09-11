package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * Describes a remote stream or a specific track within that stream,
 * used when subscribing to, modifying, or unsubscribing from remote streams.
 *
 * @property streamId       ID of the remote Stream to subscribe to
 * @property streamTrackId  ID of the track to subscribe to, or no value to subscribe to the whole Stream
 */
data class StreamSubscription(
    val streamId: Long?,
    val streamTrackId: String?,
)