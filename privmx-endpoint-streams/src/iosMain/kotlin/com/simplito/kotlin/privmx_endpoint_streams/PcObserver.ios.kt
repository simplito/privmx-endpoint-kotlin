@file:OptIn(ExperimentalForeignApi::class)

package com.simplito.kotlin.privmx_endpoint_streams

import WebRTCFramework.PMXAudioLevelAnalyzer
import WebRTCFramework.PMXFrameCryptorTransformer
import WebRTCFramework.RTCDataChannel
import WebRTCFramework.RTCIceCandidate
import WebRTCFramework.RTCIceConnectionState
import WebRTCFramework.RTCIceGatheringState
import WebRTCFramework.RTCMediaStream
import WebRTCFramework.RTCPeerConnection
import WebRTCFramework.RTCRtpReceiver
import WebRTCFramework.RTCRtpTransceiver
import WebRTCFramework.RTCSignalingState
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.IceCandidate
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.IceConnectionState
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.KeyStore
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.Observer
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnectionFactory
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PmxFrameCryptorOptions
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.toCommon
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.darwin.NSObject

actual class PcObserver internal actual constructor(
    private val peerConnectionFactory: PeerConnectionFactory,
    private val keyStore: KeyStore,
    private val roomId: String,
    private val dataChannelCryptoProvider: InternalDataChannelMessageCryptoProvider,
    private val remoteStreamObserver: RemoteStreamObserver?,
    private val onIceCandidateCallback: (candidate: IceCandidate) -> Unit,
    private val onRenegotiationNeededCallback: () -> Unit,
    private val onIceConnectionChangeCallback: (state: IceConnectionState) -> Unit,
) : Observer, NSObject() {
    private val frameCryptorMap = mutableMapOf<String, PMXFrameCryptorTransformer>()

    actual fun setFrameCryptorOptions(options: PmxFrameCryptorOptions) {
        frameCryptorMap.values.forEach { it.setDropFramesIfCryptionFailed(options.dropFrameIfCryptionFailed) }
    }

    actual fun dispose() {
        frameCryptorMap.clear()
    }

    override fun peerConnectionShouldNegotiate(peerConnection: RTCPeerConnection) =
        onRenegotiationNeededCallback()

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeIceConnectionState: RTCIceConnectionState
    ) = onIceConnectionChangeCallback(didChangeIceConnectionState.toCommon())

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didGenerateIceCandidate: RTCIceCandidate
    ) = onIceCandidateCallback(didGenerateIceCandidate)

    override fun peerConnection(peerConnection: RTCPeerConnection, didAddReceiver: RTCRtpReceiver, streams: List<*>) {
        val track = didAddReceiver.track() ?: return
        frameCryptorMap[track.trackId] = PMXFrameCryptorTransformer(
            forRtpReceiver = didAddReceiver,
            withPeerConnectionFactory = peerConnectionFactory,
            keyStore,
            PMXAudioLevelAnalyzer()
        )
        val streamId = (streams.firstOrNull() as? RTCMediaStream)?.streamId
        remoteStreamObserver?.onTrack(streamId, track)
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didStartReceivingOnTransceiver: RTCRtpTransceiver){}

    override fun peerConnection(peerConnection: RTCPeerConnection, didChangeSignalingState: RTCSignalingState) {}

    @ObjCSignatureOverride
    override fun peerConnection(peerConnection: RTCPeerConnection, didAddStream: RTCMediaStream) {}

    @ObjCSignatureOverride
    override fun peerConnection(peerConnection: RTCPeerConnection, didRemoveStream: RTCMediaStream) {}

    override fun peerConnection(peerConnection: RTCPeerConnection, didChangeIceGatheringState: RTCIceGatheringState) {}
    override fun peerConnection(peerConnection: RTCPeerConnection, didRemoveIceCandidates: List<*>) {}
    override fun peerConnection(peerConnection: RTCPeerConnection, didOpenDataChannel: RTCDataChannel) {
        didOpenDataChannel.registerDataChannel(roomId,dataChannelCryptoProvider){ remoteStreamObserver }
    }
}