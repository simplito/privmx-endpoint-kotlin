package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * Contains detailed information about a stream and its associated tracks.
 *
 * @property id       Unique identifier of the stream
 * @property userId   Identifier of the user who published the stream
 * @property metadata Additional metadata attached to the stream as a JSON string
 * @property dummy    //todo
 * @property tracks   Information about the tracks within the stream
 */
data class StreamInfo(
    val id: Long?,
    val userId: String,
    val metadata: String?,
    val dummy: Boolean,
    val tracks: List<StreamTrackInfo>
)