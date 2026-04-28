package com.simplito.kotlin.privmx_endpoint.model.stream.events

data class StreamsUpdatedData(
    val room: String,
    val streams: List<UpdatedStreamData>,
)