package com.simplito.kotlin.privmx_endpoint.model.stream.events

import com.simplito.kotlin.privmx_endpoint.model.stream.StreamTrackInfo
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamTrackModificationPair

data class StreamUpdatedEventData(
    val streamRoomId: String,
    val streamId: Long,
    val userId: String,
    val tracksAdded: List<StreamTrackInfo>,
    val tracksRemoved: List<StreamTrackInfo>,
    val tracksModified: List<StreamTrackModificationPair>,
)