package com.simplito.kotlin.privmx_endpoint.model.stream

data class StreamInfo(
    val id: Long,
    val userId: String,
    val metadata: String?,
    val dummy: Boolean?,
    val tracks: List<StreamTrackInfo>
)