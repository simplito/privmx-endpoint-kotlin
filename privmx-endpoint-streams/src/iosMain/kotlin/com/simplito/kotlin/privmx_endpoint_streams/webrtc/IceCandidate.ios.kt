@file:OptIn(ExperimentalForeignApi::class)

package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import WebRTCFramework.RTCIceCandidate
import WebRTCFramework.RTCIceConnectionState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

actual typealias IceCandidate = RTCIceCandidate

internal actual fun IceCandidate.toJson(): String = buildJsonObject {
    put("sdp", sdp)
    put("sdpMid", sdpMid)
    put("sdpMLineIndex", sdpMLineIndex)
    put("serverUrl", serverUrl)
}.toString()

actual enum class IceConnectionState (internal val  state: RTCIceConnectionState){
    NEW(RTCIceConnectionState.RTCIceConnectionStateNew),
    CHECKING(RTCIceConnectionState.RTCIceConnectionStateChecking),
    CONNECTED(RTCIceConnectionState.RTCIceConnectionStateConnected),
    COMPLETED(RTCIceConnectionState.RTCIceConnectionStateCompleted),
    DISCONNECTED( RTCIceConnectionState.RTCIceConnectionStateDisconnected ),
    CLOSED(RTCIceConnectionState.RTCIceConnectionStateClosed),
    FAILED (RTCIceConnectionState.RTCIceConnectionStateFailed)
}

internal fun RTCIceConnectionState.toCommon(): IceConnectionState =
    IceConnectionState.entries.first { it.state == this }