package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * kotlin equivalent of privmx::endpoint::stream::StreamSubscription
 */
data class StreamSubscription(
    val streamId: Long,
    val streamTrackId: String?,
)