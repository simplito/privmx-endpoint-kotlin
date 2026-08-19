package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * Represents a single track modification, holding the state of a track before and after the change.
 *
 * @property before state of the track before the modification, or no value if the track has just been added
 * @property after  state of the track after the modification, or no value if the track has been removed
 */
data class StreamTrackModificationPair(
    val before: StreamTrackInfo?,
    val after: StreamTrackInfo?,
)