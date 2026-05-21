@file:OptIn(ExperimentalForeignApi::class)

package webrtc

import WebRTCFramework.PMXFrameCryptorTransformer
import WebRTCFramework.PMXKeyStore
import WebRTCFramework.RTCAudioTrack
import WebRTCFramework.RTCIceCandidate
import WebRTCFramework.RTCIceConnectionState
import WebRTCFramework.RTCIceServer
import WebRTCFramework.RTCPeerConnection
import WebRTCFramework.RTCPeerConnectionDelegateProtocol
import WebRTCFramework.RTCPeerConnectionFactory
import WebRTCFramework.RTCPeerConnectionState
import WebRTCFramework.RTCRtpSender
import WebRTCFramework.RTCSessionDescription
import WebRTCFramework.RTCVideoTrack
import kotlinx.cinterop.ExperimentalForeignApi

actual typealias PeerConnectionFactory = RTCPeerConnectionFactory

actual typealias PeerConnection = RTCPeerConnection

actual enum class PeerConnectionState(val state: RTCPeerConnectionState) {
    NEW(RTCPeerConnectionState.RTCPeerConnectionStateNew),
    CONNECTING(RTCPeerConnectionState.RTCPeerConnectionStateConnecting),
    CONNECTED(RTCPeerConnectionState.RTCPeerConnectionStateConnected),
    DISCONNECTED(RTCPeerConnectionState.RTCPeerConnectionStateDisconnected),
    FAILED(RTCPeerConnectionState.RTCPeerConnectionStateFailed),
    CLOSED(RTCPeerConnectionState.RTCPeerConnectionStateClosed)
}

actual typealias KeyStore = PMXKeyStore

actual typealias IceCandidate = RTCIceCandidate

actual typealias Observer = RTCPeerConnectionDelegateProtocol// todo ??

actual typealias FrameCryptorOptions = PmxFrameCryptorOptions   // todo ??

actual internal typealias PCManager = PeerConnectionManager


actual enum class IceConnectionState(val state: RTCIceConnectionState) {
    NEW(RTCIceConnectionState.RTCIceConnectionStateNew),
    CHECKING(RTCIceConnectionState.RTCIceConnectionStateChecking),
    CONNECTED(RTCIceConnectionState.RTCIceConnectionStateConnected),
    COMPLETED(RTCIceConnectionState.RTCIceConnectionStateCompleted),
    DISCONNECTED(RTCIceConnectionState.RTCIceConnectionStateDisconnected),
    CLOSED(RTCIceConnectionState.RTCIceConnectionStateClosed),
    FAILED(RTCIceConnectionState.RTCIceConnectionStateFailed)
}


class PmxFrameCryptorOptions {
    var dropFrameIfCryptionFailed: Boolean = false
}


actual typealias IceServer = RTCIceServer

actual data class AudioTrackInfo(
    var track: RTCAudioTrack,
    var sender: RTCRtpSender,
    var frameCryptor: PMXFrameCryptorTransformer
)

actual data class VideoTrackInfo(
    var track: RTCVideoTrack,
    var sender: RTCRtpSender,
    var frameCryptor: PMXFrameCryptorTransformer
)

actual typealias SessionDescription = RTCSessionDescription