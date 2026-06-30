package com.simplito.kotlin.privmx_endpoint.model.stream.events

/**
 * Data describing event that triggers when a participant joins a StreamRoom.
 *
 * @property streamRoomId Identifier of the stream room that was joined
 * @property streamIds // todo
 * @property userId Identifier of the user who joined the StreamRoom
 */
data class StreamEventData(
    val streamRoomId: String,
    val streamIds: List<Long>,
    val userId: String
)
