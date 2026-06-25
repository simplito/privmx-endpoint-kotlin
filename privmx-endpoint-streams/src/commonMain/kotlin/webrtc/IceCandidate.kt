package webrtc

expect class IceCandidate

enum class IceConnectionState {
    NEW, CHECKING, CONNECTED, COMPLETED, DISCONNECTED, CLOSED, FAILED
}

internal expect fun IceCandidate.toJson(): String