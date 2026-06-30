package com.simplito.kotlin.privmx_endpoint.model.stream.events

import com.simplito.kotlin.privmx_endpoint.model.stream.StreamInfo

/**
 * Represents data of a published stream.
 *
 * @property streamRoomId Identifier of the room in which the stream is published
 * @property stream Information about the published stream
 * @property userId Identifier of the user who published the stream
 */
data class PublishedStreamData(
    val streamRoomId: String,
    val stream: StreamInfo,
    val userId: String,
)