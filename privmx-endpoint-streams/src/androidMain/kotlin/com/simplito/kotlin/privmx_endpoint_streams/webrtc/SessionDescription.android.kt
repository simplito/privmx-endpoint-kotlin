package com.simplito.kotlin.privmx_endpoint_streams.webrtc

actual typealias SessionDescription = org.webrtc.SessionDescription

internal actual val SessionDescription.sdp: String get() = description

internal actual fun sessionDescription(
    type: SdpType,
    sdp: String
): SessionDescription = SessionDescription(type.type, sdp)

actual enum class SdpType(internal val type: org.webrtc.SessionDescription.Type) {
    OFFER(org.webrtc.SessionDescription.Type.OFFER),
    ANSWER(org.webrtc.SessionDescription.Type.ANSWER),
    PRANSWER(org.webrtc.SessionDescription.Type.PRANSWER),
    ROLLBACK(org.webrtc.SessionDescription.Type.ROLLBACK)
}

internal fun org.webrtc.SessionDescription.Type.toCommon(): SdpType =
    SdpType.entries.first { it.type == this }

internal actual fun fromCanonicalForm(canonical: String): SdpType =
    org.webrtc.SessionDescription.Type.fromCanonicalForm(canonical).toCommon()