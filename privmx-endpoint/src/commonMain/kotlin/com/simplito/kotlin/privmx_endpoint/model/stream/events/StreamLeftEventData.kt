package com.simplito.kotlin.privmx_endpoint.model.stream.events

data class StreamLeftEventData(
    val streamRoomId: String,
    val streamId: Long,
    val userId: String
)