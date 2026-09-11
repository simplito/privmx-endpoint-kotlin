package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * Represents metadata about a single media track within a stream.
 *
 * @property type           type of the track (e.g. "audio", "video", "data")
 * @property mindex         index of the track's media line in the SDP
 * @property mid            media line ID of the track in the SDP
 * @property disabled       whether the track is currently disabled by its publisher
 * @property codec          codec used by the track, if known (e.g. "opus", "VP8")
 * @property description    track description provided by its publisher
 * @property moderated      determines whether the track has been muted by a moderator
 * @property simulcast      whether the track is sent using simulcast
 */
data class StreamTrackInfo(
    val type: String,
    val mindex: Long,
    val mid: String,
    val disabled: Boolean,
    val codec: String?,
    val description: String?,
    val moderated: Boolean,
    val simulcast: Boolean
)