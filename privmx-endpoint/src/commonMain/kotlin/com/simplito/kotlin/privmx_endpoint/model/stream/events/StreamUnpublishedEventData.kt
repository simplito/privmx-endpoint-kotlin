package com.simplito.kotlin.privmx_endpoint.model.stream.events

data class StreamUnpublishedEventData(
    val streamRoomId: String,
    val streamId: Long
)