@file:
OptIn(ExperimentalForeignApi::class)

import WebRTCFramework.PMXFrameCryptorTransformer
import WebRTCFramework.RTCDataChannel
import WebRTCFramework.RTCIceGatheringState
import WebRTCFramework.RTCMediaStream
import WebRTCFramework.RTCPeerConnection
import WebRTCFramework.RTCPeerConnectionDelegateProtocol
import WebRTCFramework.RTCPeerConnectionFactory
import WebRTCFramework.RTCRtpReceiver
import WebRTCFramework.RTCRtpTransceiver
import WebRTCFramework.RTCSignalingState
import com.simplito.kotlin.privmx_endpoint.model.stream.Key
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.darwin.NSObject
import kotlin.collections.set


actual class PcObserver actual constructor(
    private val peerConnectionFactory: PeerConnectionFactory,
    private val keyStore: PMXKeyStore,
    private val trackObserver: TrackObserver?,
    private val onIceCandidate: (candidate: RTCIceCandidate) -> Unit,
    private val onIceConnectionChange: (candidate: RTCIceConnectionState) -> Unit,
): RTCPeerConnectionDelegateProtocol, NSObject(){
    var frameCryptorMap: MutableMap<String, PMXFrameCryptorTransformer> = mutableMapOf()
    private val streamIdsByTracks: Map<String, String> = HashMap<String, String>()

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeSignalingState: RTCSignalingState
    ) {
//        TODO("Not yet implemented")
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didAddReceiver: RTCRtpReceiver,
        streams: List<*>
    ) {

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
                keyStore
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

    @ObjCSignatureOverride
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didAddStream: RTCMediaStream
    ) {
//        TODO("Not yet implemented")
    }

    @ObjCSignatureOverride
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveStream: RTCMediaStream
    ) {
//        TODO("Not yet implemented")
    }

    override fun peerConnectionShouldNegotiate(peerConnection: RTCPeerConnection) {
//        TODO("Not yet implemented")
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeIceConnectionState: WebRTCFramework.RTCIceConnectionState
    ) {
        onIceConnectionChange(didChangeIceConnectionState.common())
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeIceGatheringState: RTCIceGatheringState
    ) {
//        TODO("Not yet implemented")
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didGenerateIceCandidate: WebRTCFramework.RTCIceCandidate
    ) {
        onIceCandidate(didGenerateIceCandidate)
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveIceCandidates: List<*>
    ) {
//        TODO("Not yet implemented")
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didOpenDataChannel: RTCDataChannel
    ) {
//        TODO("Not yet implemented")
    }

//    fun setFrameCryptorOptions(options: Pm.PmxFrameCryptorOptions?) {
//        frameCryptorMap.forEach({ k, v -> v.setOptions(options) })
//    }
}
fun WebRTCFramework.PMXKeyStore.setKeys(keys: List<PMXKeyStoreKey>): Boolean{
    return true
}
@OptIn(BetaInteropApi::class)
actual class PMXKeyStoreKey actual constructor(from: Key): WebRTCFramework.PMXKSKey(
    from.keyId,
    from.key.usePinned {
        NSData.create(it.addressOf(0),it.get().size.toULong())
    },
    from.type.ordinal.toLong()
)
actual class PMXKeyStore : WebRTCFramework.PMXKeyStore() {
    actual fun setKeys(keys: List<PMXKeyStoreKey>): Boolean {
        return setKeys(keys as List<*>)
    }
}
actual typealias PeerConnectionFactory = RTCPeerConnectionFactory
actual typealias RTCIceCandidate = WebRTCFramework.RTCIceCandidate
private fun WebRTCFramework.RTCIceConnectionState.common() : RTCIceConnectionState{
    return when(this){
        WebRTCFramework.RTCIceConnectionState.RTCIceConnectionStateNew -> RTCIceConnectionState.NEW
        WebRTCFramework.RTCIceConnectionState.RTCIceConnectionStateChecking -> RTCIceConnectionState.CHECKING
        WebRTCFramework.RTCIceConnectionState.RTCIceConnectionStateConnected -> RTCIceConnectionState.CONNECTED
        WebRTCFramework.RTCIceConnectionState.RTCIceConnectionStateCompleted -> RTCIceConnectionState.COMPLETED
        WebRTCFramework.RTCIceConnectionState.RTCIceConnectionStateFailed -> RTCIceConnectionState.FAILED
        WebRTCFramework.RTCIceConnectionState.RTCIceConnectionStateDisconnected -> RTCIceConnectionState.DISCONNECTED
        WebRTCFramework.RTCIceConnectionState.RTCIceConnectionStateClosed -> RTCIceConnectionState.CLOSED
        else -> RTCIceConnectionState.FAILED
    }
}