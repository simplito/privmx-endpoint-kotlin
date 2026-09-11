package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.webrtc.PeerConnection

actual typealias IceCandidate = org.webrtc.IceCandidate

internal actual fun IceCandidate.toJson(): String = buildJsonObject {
    put("sdp", sdp)
    put("adapterType", adapterType.ordinal)
    put("sdpMid", sdpMid)
    put("sdpMLineIndex", sdpMLineIndex)
    put("serverUrl", serverUrl)
}.toString()

actual enum class IceConnectionState(internal val state: PeerConnection.IceConnectionState) {
    NEW(PeerConnection.IceConnectionState.NEW),
    CHECKING(PeerConnection.IceConnectionState.CHECKING),
    CONNECTED(PeerConnection.IceConnectionState.CONNECTED),
    COMPLETED(PeerConnection.IceConnectionState.COMPLETED),
    DISCONNECTED(PeerConnection.IceConnectionState.DISCONNECTED),
    CLOSED(PeerConnection.IceConnectionState.CLOSED),
    FAILED(PeerConnection.IceConnectionState.FAILED)
}

internal fun PeerConnection.IceConnectionState.toCommon(): IceConnectionState =
    IceConnectionState.entries.first { it.state == this }