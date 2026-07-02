package com.simplito.kotlin.privmx_endpoint.model.stream.events

/**
 * @property streamRoomId StreamRoom ID
 * @property userId User ID of the member who joined or left
 */
data class StreamRoomParticipantEventData (
    val streamRoomId: String,
    val userId: String
)