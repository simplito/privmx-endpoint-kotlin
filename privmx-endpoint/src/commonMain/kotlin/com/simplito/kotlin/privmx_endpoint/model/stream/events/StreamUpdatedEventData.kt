package com.simplito.kotlin.privmx_endpoint.model.stream.events

import com.simplito.kotlin.privmx_endpoint.model.stream.StreamInfo
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamTrackModification

data class StreamUpdatedEventData(
    val streamRoomId: String,
    val streamsAdded: List<StreamInfo>,
    val streamsRemoved: List<StreamInfo>,
    val streamsModified: List<StreamTrackModification>,
)