package webrtc

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

actual typealias IceCandidate = org.webrtc.IceCandidate

internal actual fun IceCandidate.toJson(): String = buildJsonObject {
    put("sdp", sdp)
    put("adapterType", adapterType.ordinal)
    put("sdpMid", sdpMid)
    put("sdpMLineIndex", sdpMLineIndex)
    put("serverUrl", serverUrl)
}.toString()