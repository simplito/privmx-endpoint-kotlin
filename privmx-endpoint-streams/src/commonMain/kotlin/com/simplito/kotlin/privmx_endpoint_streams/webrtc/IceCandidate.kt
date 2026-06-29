package com.simplito.kotlin.privmx_endpoint_streams.webrtc

expect class IceCandidate

expect enum class IceConnectionState {
    NEW,
    CHECKING,
    CONNECTED,
    COMPLETED,
    DISCONNECTED,
    CLOSED,
    FAILED
}

internal expect fun IceCandidate.toJson(): String