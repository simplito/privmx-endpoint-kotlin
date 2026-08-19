package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * Contains detailed information about a stream and its associated tracks.
 *
 * @property id         ID of the Stream
 * @property userId     ID of the user who published the stream
 * @property metadata   additional metadata attached to the stream as a JSON string
 * @property dummy      determines whether the Stream is a dummy (placeholder) Stream
 * @property tracks     information about the tracks within the stream
 */
data class StreamInfo(
    val id: Long?,
    val userId: String,
    val metadata: String?,
    val dummy: Boolean,
    val tracks: List<StreamTrackInfo>
)