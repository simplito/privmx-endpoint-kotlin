package com.simplito.kotlin.privmx_endpoint.model.stream.events

import com.simplito.kotlin.privmx_endpoint.model.stream.StreamInfo

data class PublishedStreamData(
    val streamRoomId: String,
    val stream: StreamInfo,
    val userId: String,
)