@file:OptIn(ExperimentalForeignApi::class)

package webrtc

import WebRTCFramework.RTCIceCandidate
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