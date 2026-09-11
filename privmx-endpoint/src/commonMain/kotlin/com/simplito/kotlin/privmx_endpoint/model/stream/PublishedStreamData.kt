package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * Represents data of a published stream.
 *
 * @property streamRoomId   StreamRoom ID
 * @property stream         information about the published stream
 * @property userId         ID of the user who published the Stream
 */
data class PublishedStreamData(
    val streamRoomId: String,
    val stream: StreamInfo,
    val userId: String,
)