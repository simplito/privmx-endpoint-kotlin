package webrtc

expect class PeerConnectionFactory
expect class PeerConnection
expect class KeyStore
expect class FrameCryptorOptions
expect interface Observer
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

expect internal class PCManager
expect enum class PeerConnectionState {
    NEW,
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    FAILED,
    CLOSED
}

expect class IceServer

expect class AudioTrackInfo  //todo - czy to jest ok? czy lepiej nie robić expect?
expect class VideoTrackInfo // todo - czy to jest ok? czy lepiej nie robić expect?

expect class SessionDescription