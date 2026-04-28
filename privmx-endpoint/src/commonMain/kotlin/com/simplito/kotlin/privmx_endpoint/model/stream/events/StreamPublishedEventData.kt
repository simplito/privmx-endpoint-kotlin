package com.simplito.kotlin.privmx_endpoint.model.stream.events


import com.simplito.kotlin.privmx_endpoint.model.stream.StreamInfo

data class StreamPublishedEventData(
    val streamRoomId: String,
    val stream: StreamInfo,
    val userId: String
)