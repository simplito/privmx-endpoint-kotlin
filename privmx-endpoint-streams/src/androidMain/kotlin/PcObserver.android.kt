import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PmxKeyStore

actual class PcObserver actual constructor(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: PMXKeyStore,
    trackObserver: TrackObserver?,
    onIceCandidate: (candidate: RTCIceCandidate) -> Unit,
    onIceConnectionChange: (candidate: RTCIceConnectionState) -> Unit
): org.webrtc.PeerConnection.Observer{
    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        TODO("Not yet implemented")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        TODO("Not yet implemented")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        TODO("Not yet implemented")
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        TODO("Not yet implemented")
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate?>?) {
        TODO("Not yet implemented")
    }

    override fun onAddStream(p0: MediaStream?) {
        TODO("Not yet implemented")
    }

    override fun onRemoveStream(p0: MediaStream?) {
        TODO("Not yet implemented")
    }

    override fun onDataChannel(p0: DataChannel?) {
        TODO("Not yet implemented")
    }

    override fun onRenegotiationNeeded() {
        TODO("Not yet implemented")
    }

}
actual typealias PMXKeyStoreKey = PmxKeyStore.Key
actual typealias PMXKeyStore = PmxKeyStore
actual typealias PeerConnectionFactory = org.webrtc.PeerConnectionFactory
actual typealias RTCIceCandidate = IceCandidate
actual typealias RTCIceConnectionState = PeerConnection.IceConnectionState