package webrtc

actual typealias SessionDescription = org.webrtc.SessionDescription

internal actual val SessionDescription.sdp: String get() = description

internal actual fun sessionDescription(type: String, sdp: String): SessionDescription =
    SessionDescription(org.webrtc.SessionDescription.Type.fromCanonicalForm(type), sdp)