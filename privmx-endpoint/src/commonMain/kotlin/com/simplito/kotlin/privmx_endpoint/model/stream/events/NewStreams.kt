package com.simplito.kotlin.privmx_endpoint.model.stream.events


import com.simplito.kotlin.privmx_endpoint.model.stream.StreamInfo

/**
 * Data describing event triggered when there are new streams available in stream room.
 *
 * @property room Identifier of the stream room
 * @property streams List of new streams that can be subscribed
 */
data class NewStreams(
    val room: String,
    val streams: List<StreamInfo>
)
