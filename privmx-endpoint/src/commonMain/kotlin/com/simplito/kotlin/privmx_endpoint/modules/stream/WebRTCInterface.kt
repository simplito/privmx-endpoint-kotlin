package com.simplito.kotlin.privmx_endpoint.modules.stream

import com.simplito.kotlin.privmx_endpoint.model.stream.Key

interface WebRTCInterface {
    fun createOfferAndSetLocalDescription(
        streamRoomId: String,
        connectionType: String
    ): String

    fun createAnswerAndSetDescriptions(
        streamRoomId: String,
        sdp: String,
        type: String,
        connectionType: String
    ): String

    fun setAnswerAndSetRemoteDescription(
        streamRoomId: String,
        sdp: String,
        type: String,
        connectionType: String
    )

    fun updateSessionId(streamRoomId: String, sessionId: Long?, connectionType: String)

    fun close(streamRoomId: String, connectionType: String)

    fun closeAll(streamRoomId: String)

    fun updateKeys(streamRoomId: String, keys: List<Key>)
}