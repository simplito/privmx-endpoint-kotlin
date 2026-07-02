package com.simplito.kotlin.privmx_endpoint.model.stream.events

import com.simplito.kotlin.privmx_endpoint.model.stream.StreamTrackInfo
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamTrackModificationPair

/**
 * Data describing changes that occurred within a stream room during an active session.
 * Received when an existing publisher adds, removes, or modifies their tracks.
 *
 * @property streamRoomId Identifier of the stream room in which the update occurred
 * @property streamsAdded  List of streams that were added to the stream room
 * @property streamsRemoved List of streams that were removed from the stream room
 * @property streamsModified  List of streams that were modified in the stream room
 */
data class StreamUpdatedEventData(
    val streamRoomId: String,
    val streamId: Long,
    val userId: String,
    val tracksAdded: List<StreamTrackInfo>,
    val tracksRemoved: List<StreamTrackInfo>,
    val tracksModified: List<StreamTrackModificationPair>,
)