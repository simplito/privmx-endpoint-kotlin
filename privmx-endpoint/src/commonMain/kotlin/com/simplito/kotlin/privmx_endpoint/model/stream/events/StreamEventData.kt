package com.simplito.kotlin.privmx_endpoint.model.stream.events

data class StreamEventData(
    val streamRoomId: String,
    val streamIds: List<Long>,
    val userId: String?
)
