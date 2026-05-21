package com.simplito.kotlin.privmx_endpoint.model.stream.events


import com.simplito.kotlin.privmx_endpoint.model.stream.StreamInfo

data class NewStreams(
    val room: String,
    val streams: List<StreamInfo>
)
