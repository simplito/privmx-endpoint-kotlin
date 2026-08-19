package com.simplito.kotlin.privmx_endpoint.model.stream.events

/**
 * Data describing the event triggered when a participant joins or leaves a Stream Room.
 *
 * @property streamRoomId   StreamRoom ID
 * @property userId         User ID of the member who joined or left
 */
data class StreamRoomParticipantEventData(
    val streamRoomId: String,
    val userId: String
)