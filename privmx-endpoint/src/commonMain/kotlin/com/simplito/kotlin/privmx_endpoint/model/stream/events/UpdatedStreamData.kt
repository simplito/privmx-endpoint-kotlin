package com.simplito.kotlin.privmx_endpoint.model.stream.events

data class UpdatedStreamData(
    val active: Boolean,
    val type: String,
    val codec: String?,
    val streamId: Long?,
    val streamMid: String?,
    val streamDisplay: String?,
    val mindex: Long,
    val mid: String,
    val send: Boolean,
    val ready: Boolean,
)