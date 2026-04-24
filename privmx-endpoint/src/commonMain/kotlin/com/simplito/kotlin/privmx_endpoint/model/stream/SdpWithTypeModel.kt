package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * kotlin equivalent of privmx::endpoint::stream::SdpWithTypeModel
 */
data class SdpWithTypeModel(
    val sdp: String,
    val type: String,
)