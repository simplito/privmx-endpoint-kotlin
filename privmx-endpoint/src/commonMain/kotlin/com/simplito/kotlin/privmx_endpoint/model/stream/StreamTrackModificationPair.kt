package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * Represents a single track modification, holding the state of a track before and after the change.
 *
 * @property before The state of the track before the modification
 * @property after  The state of the track after the modification
 */
data class StreamTrackModificationPair(
    val before: StreamTrackInfo?,
    val after: StreamTrackInfo?,
)