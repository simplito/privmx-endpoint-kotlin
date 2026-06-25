package webrtc

expect class SessionDescription

internal expect val SessionDescription.sdp: String
internal expect fun sessionDescription(type: String, sdp: String): SessionDescription