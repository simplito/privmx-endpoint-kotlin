package webrtc

import PeerConnectionManager
import org.webrtc.PeerConnection
import org.webrtc.PmxFrameCryptor
import org.webrtc.RtpSender
import org.webrtc.SessionDescription

actual typealias PeerConnectionFactory = org.webrtc.PeerConnectionFactory

actual typealias PeerConnection = org.webrtc.PeerConnection

actual enum class IceConnectionState(new: PeerConnection.IceConnectionState) {
    NEW(PeerConnection.IceConnectionState.NEW),
    CHECKING(PeerConnection.IceConnectionState.CHECKING),
    CONNECTED(PeerConnection.IceConnectionState.CONNECTED),
    COMPLETED(PeerConnection.IceConnectionState.COMPLETED),
    DISCONNECTED(PeerConnection.IceConnectionState.DISCONNECTED),
    CLOSED(PeerConnection.IceConnectionState.CLOSED),
    FAILED(PeerConnection.IceConnectionState.FAILED)
}

actual typealias KeyStore = org.webrtc.PmxKeyStore

actual typealias IceCandidate = org.webrtc.IceCandidate

actual typealias Observer = org.webrtc.PeerConnection.Observer

actual typealias FrameCryptorOptions = org.webrtc.PmxFrameCryptor.PmxFrameCryptorOptions

actual internal typealias PCManager = PeerConnectionManager

actual enum class PeerConnectionState(val state: PeerConnection.PeerConnectionState){
    NEW(PeerConnection.PeerConnectionState.NEW),
    CONNECTING(PeerConnection.PeerConnectionState.CONNECTING),
    CONNECTED(PeerConnection.PeerConnectionState.CONNECTED),
    DISCONNECTED(PeerConnection.PeerConnectionState.DISCONNECTED),
    FAILED(PeerConnection.PeerConnectionState.FAILED),
    CLOSED(PeerConnection.PeerConnectionState.CLOSED)
}

actual typealias IceServer = PeerConnection.IceServer

actual data class AudioTrackInfo(
    var track: AudioTrack,
    var sender: RtpSender,
    var frameCryptor: PmxFrameCryptor
)

actual data class VideoTrackInfo(
    var track: VideoTrack,
    var sender: RtpSender,
    var frameCryptor: PmxFrameCryptor
)

actual typealias SessionDescription = SessionDescription