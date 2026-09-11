package com.simplito.kotlin.privmx_endpoint.model.stream.events

/**
 * Data describing the event triggered when a publisher stops publishing their stream in a StreamRoom.
 *
 * @property streamRoomId Identifier of the StreamRoom in which the stream was unpublished
 * @property streamId     Identifier of the stream that was unpublished
 */
data class StreamUnpublishedEventData(
    val streamRoomId: String,
    val streamId: Long?
)