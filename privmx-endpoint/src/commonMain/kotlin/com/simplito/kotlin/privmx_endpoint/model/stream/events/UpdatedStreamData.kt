package com.simplito.kotlin.privmx_endpoint.model.stream.events

/**
 * Current state of a single publisher stream, received as part of [StreamsUpdatedData].
 *
 * @property active         //todo
 * @property type           Track type (e.g. "audio", "video")
 * @property codec          Codec used by the stream (e.g. "opus", "VP8")
 * @property streamId       Identifier of the publisher stream
 * @property streamMid      //todo
 * @property streamDisplay  //todo
 * @property mindex         //todo
 * @property mid            //todo
 * @property send           //todo
 * @property ready          //todo
 */
data class UpdatedStreamData(
    val active: Boolean,
    val type: String,
    val codec: String?,
    val streamId: Long?,
    val streamMid: String?,
    val streamDisplay: String?,
    val mindex: Long,
    val mid: String,
    val send: Boolean,
    val ready: Boolean,
)