package com.simplito.kotlin.privmx_endpoint_streams.webrtc

expect class SessionDescription

internal expect val SessionDescription.sdp: String
internal expect fun fromCanonicalForm(canonical: String): SdpType

internal expect fun sessionDescription(type: SdpType, sdp: String): SessionDescription
expect enum class SdpType {
    OFFER,
    ANSWER,
    PRANSWER,
    ROLLBACK
}