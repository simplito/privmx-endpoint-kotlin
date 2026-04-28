package com.simplito.kotlin.privmx_endpoint.model.stream

data class StreamTrackModification(
    val streamId: Long,
    val tracks: List<StreamTrackModificationPair>,
)