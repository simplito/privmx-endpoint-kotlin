package com.simplito.kotlin.privmx_endpoint.model.stream.events

import com.simplito.kotlin.privmx_endpoint.model.stream.StreamTrackInfo
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamTrackModificationPair

/**
 * Data describing changes that occurred within a Stream Room during an active session.
 * Received when an existing publisher adds, removes, or modifies their tracks.
 *
 * @property streamRoomId       ID of the StreamRoom the event occurred in
 * @property streamId           publisher Stream ID that changed
 * @property userId             ID of the user who publishes the Stream
 * @property tracksAdded        list of tracks added to the Stream
 * @property tracksRemoved      list of tracks removed from the Stream
 * @property tracksModified     list of tracks modified in the Stream,
 *                              with their state before and after the change
 */
data class StreamUpdatedEventData(
    val streamRoomId: String,
    val streamId: Long,
    val userId: String,
    val tracksAdded: List<StreamTrackInfo>,
    val tracksRemoved: List<StreamTrackInfo>,
    val tracksModified: List<StreamTrackModificationPair>,
)