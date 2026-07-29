package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * Represents metadata about a single media track within a stream.
 *
 * @property type           Type of the track (e.g. "audio", "video")
 * @property mindex         //todo
 * @property mid            //todo
 * @property disabled       Whether the track is disabled
 * @property codec          Codec used by the track (e.g. "opus", "VP8")
 * @property description    Description of the track
 * @property moderated      //todo
 * @property simulcast      Whether simulcast is enabled for the track
 */
data class StreamTrackInfo(
    val type: String,
    val mindex: Long?,
    val mid: String,
    val disabled: Boolean,
    val codec: String?,
    val description: String?,
    val moderated: Boolean,
    val simulcast: Boolean
)