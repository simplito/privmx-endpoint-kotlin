package com.simplito.kotlin.privmx_endpoint.model.stream.events

/**
 * Data describing the event triggered when a StreamRoom is deleted.
 *
 * @property streamRoomId Identifier of the StreamRoom that was deleted
 */
data class StreamRoomDeletedEventData(
    val streamRoomId: String
)