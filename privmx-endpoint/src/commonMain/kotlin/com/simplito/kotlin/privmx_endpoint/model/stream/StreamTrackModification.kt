package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * Represents a set of track modifications applied to a specific stream.
 *
 * @property streamId Identifier of the stream for which tracks were modified
 * @property tracks   List of track modifications, each containing the state of a track before and after the change
 */
data class StreamTrackModification(
    val streamId: Long,
    val tracks: List<StreamTrackModificationPair>,
)