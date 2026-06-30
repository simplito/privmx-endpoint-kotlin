package com.simplito.kotlin.privmx_endpoint.model.stream.events


import com.simplito.kotlin.privmx_endpoint.model.stream.StreamInfo

/**
 * Data describing event that triggers when a publisher starts publishing their stream in a StreamRoom.
 *
 * @property streamRoomId Identifier of the stream room in which the stream was published
 * @property stream Detailed information about the published stream
 * @property userId  Identifier of the user who published the stream
 */
data class StreamPublishedEventData(
    val streamRoomId: String,
    val stream: StreamInfo,
    val userId: String
)