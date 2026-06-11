@file:OptIn(ExperimentalForeignApi::class)

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
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.darwin.NSObject

actual class PcObserver actual constructor(
    private val peerConnectionFactory: PeerConnectionFactory,
    private val keyStore: KeyStore,
    private val trackObserver: TrackObserver?,
    private val onIceCandidateCallback: (candidate: IceCandidate) -> Unit,
    private val onRenegotiationNeededCallback: () -> Unit,
    private val onIceConnectionChangeCallback: (state: IceConnectionState) -> Unit
) : Observer, NSObject() {
    private val frameCryptorMap = mutableMapOf<String, PMXFrameCryptorTransformer>()

    actual fun setFrameCryptorOptions(options: FrameCryptorOptions) {
        frameCryptorMap.values.forEach { it.setDropFramesIfCryptionFailed(options.dropFrameIfCryptionFailed) }
    }

    actual fun dispose() {
        // TODO(iOS): brak dispose() na PMXFrameCryptorTransformer?
        frameCryptorMap.clear()
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didStartReceivingOnTransceiver: RTCRtpTransceiver
    ) {
        val track = didStartReceivingOnTransceiver.receiver.track() ?: return
        frameCryptorMap[track.trackId] = PMXFrameCryptorTransformer(
            forRtpReceiver = didStartReceivingOnTransceiver.receiver,
            withPeerConnectionFactory = peerConnectionFactory,
            keyStore,
            null
        )

        //TODO: Pass correct streamId for this track
        // (streams.firstOrNull() as? RTCMediaStream)?.streamId,
        trackObserver?.onRemoteTrack(null, track.toCommon())
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

    override fun peerConnection(peerConnection: RTCPeerConnection, didChangeSignalingState: RTCSignalingState) {}
    override fun peerConnection(peerConnection: RTCPeerConnection, didAddReceiver: RTCRtpReceiver, streams: List<*>) {}

    @ObjCSignatureOverride
    override fun peerConnection(peerConnection: RTCPeerConnection, didAddStream: RTCMediaStream) {}

    @ObjCSignatureOverride
    override fun peerConnection(peerConnection: RTCPeerConnection, didRemoveStream: RTCMediaStream) {}

    override fun peerConnection(peerConnection: RTCPeerConnection, didChangeIceGatheringState: RTCIceGatheringState) {}
    override fun peerConnection(peerConnection: RTCPeerConnection, didRemoveIceCandidates: List<*>) {}
    override fun peerConnection(peerConnection: RTCPeerConnection, didOpenDataChannel: RTCDataChannel) {}
}

internal fun RTCIceConnectionState.toCommon(): IceConnectionState = when (this) {
    RTCIceConnectionState.RTCIceConnectionStateNew -> IceConnectionState.NEW
    RTCIceConnectionState.RTCIceConnectionStateChecking -> IceConnectionState.CHECKING
    RTCIceConnectionState.RTCIceConnectionStateConnected -> IceConnectionState.CONNECTED
    RTCIceConnectionState.RTCIceConnectionStateCompleted -> IceConnectionState.COMPLETED
    RTCIceConnectionState.RTCIceConnectionStateDisconnected -> IceConnectionState.DISCONNECTED
    RTCIceConnectionState.RTCIceConnectionStateClosed -> IceConnectionState.CLOSED
    RTCIceConnectionState.RTCIceConnectionStateFailed -> IceConnectionState.FAILED
    else -> IceConnectionState.FAILED   // todo - new / failed ?
}