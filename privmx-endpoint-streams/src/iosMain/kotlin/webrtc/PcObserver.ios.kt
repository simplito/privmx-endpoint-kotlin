@file:
OptIn(ExperimentalForeignApi::class)

package webrtc

import TrackObserver
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
import kotlin.collections.set

actual class PcObserver actual constructor(
    val peerConnectionFactory: PeerConnectionFactory,
    val keyStore: KeyStore,
    val trackObserver: TrackObserver?,
    val onIceCandidateCallback: (candidate: IceCandidate) -> Unit,
    val onRenegotiationNeededCallback: () -> Unit,
    val onIceConnectionChangeCallback: (candidate: IceConnectionState) -> Unit
): Observer, NSObject()  {
    var frameCryptorMap: MutableMap<String, PMXFrameCryptorTransformer> = mutableMapOf()
    private val streamIdsByTracks: Map<String, String> = HashMap<String, String>()


    actual fun setFrameCryptorOptions(options: FrameCryptorOptions){
        frameCryptorMap.values.forEach {
            it.setDropFramesIfCryptionFailed(options.dropFrameIfCryptionFailed)
        }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didStartReceivingOnTransceiver: RTCRtpTransceiver
    ) {
        val track = didStartReceivingOnTransceiver.receiver.track()
        track?.let { track ->
            val cryptor = PMXFrameCryptorTransformer(
                forRtpReceiver = didStartReceivingOnTransceiver.receiver,
                withPeerConnectionFactory = peerConnectionFactory,
                keyStore,
                null        // todo - czy powinno sie zezwalać na null? w androidzie można
            )
            frameCryptorMap[track.trackId()] = cryptor
            trackObserver?.onRemoteTrack(
                //TODO: Pass correct streamId for this track
//                (streams.firstOrNull() as? RTCMediaStream)?.streamId,
                null,
                track
            )
        }
    }

    override fun peerConnectionShouldNegotiate(peerConnection: RTCPeerConnection) =   onRenegotiationNeededCallback()

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeIceConnectionState: RTCIceConnectionState
    ) = onIceConnectionChangeCallback(didChangeIceConnectionState)

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didGenerateIceCandidate: RTCIceCandidate
    ) = onIceCandidateCallback(didGenerateIceCandidate)



    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeSignalingState: RTCSignalingState
    ) {}

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didAddReceiver: RTCRtpReceiver,
        streams: List<*>
    ) {}


    @ObjCSignatureOverride
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didAddStream: RTCMediaStream
    ) {}

    @ObjCSignatureOverride
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveStream: RTCMediaStream
    ) {  }


    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeIceGatheringState: RTCIceGatheringState
    ) {  }


    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveIceCandidates: List<*>
    ) {

    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didOpenDataChannel: RTCDataChannel
    ) {}

    actual fun dispose() {
//        frameCryptorMap.values.forEach { it.dispose() }  // todo - jest odpowiednik?
            frameCryptorMap.clear()

    }
}