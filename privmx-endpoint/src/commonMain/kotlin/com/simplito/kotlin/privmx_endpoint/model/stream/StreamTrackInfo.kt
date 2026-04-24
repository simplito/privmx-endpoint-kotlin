package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * kotlin equivalent of privmx::endpoint::stream::StreamTrackInfo
 */
data class StreamTrackInfo(
    val type: String,
    val mindex: Long,
    val mid: String,
    val disabled: Boolean?,
    val codec: String?,
    val description: String?,
    val moderated: Boolean?,
    val simulcast: Boolean?
)