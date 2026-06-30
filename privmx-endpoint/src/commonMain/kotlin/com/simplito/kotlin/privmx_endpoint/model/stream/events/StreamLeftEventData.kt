package com.simplito.kotlin.privmx_endpoint.model.stream.events

/**
 * Data describing event that triggers when a user leaves a StreamRoom.
 *
 * @property streamRoomId Identifier of the StreamRoom that the user left.
 * @property streamId //todo
 * @property userId Identifier of the user who left the StreamRoom
 */
data class StreamLeftEventData(
    val streamRoomId: String,
    val streamId: Long,
    val userId: String
)