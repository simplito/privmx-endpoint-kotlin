package webrtc

import TrackObserver
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PmxFrameCryptor
import org.webrtc.PmxFrameCryptorFactory
import org.webrtc.RtpReceiver

actual class PcObserver actual constructor(
    val peerConnectionFactory: PeerConnectionFactory,
    val keyStore: KeyStore,
    val trackObserver: TrackObserver?,
    val onIceCandidateCallback: (candidate: webrtc.IceCandidate) -> Unit,
    val onRenegotiationNeededCallback: () -> Unit,
    val onIceConnectionChangeCallback: (candidate: IceConnectionState) -> Unit
) : Observer {
    private val frameCryptorMap = mutableMapOf<String, PmxFrameCryptor>()

    actual fun setFrameCryptorOptions(options: FrameCryptorOptions) {
        frameCryptorMap.values.forEach { it.setOptions(options) }
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState) = onIceConnectionChangeCallback(p0)

    override fun onIceCandidate(p0: IceCandidate) = onIceCandidateCallback(p0)

    override fun onRenegotiationNeeded() = onRenegotiationNeededCallback()

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}

    override fun onIceConnectionReceivingChange(p0: Boolean) {}

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate?>?) {}

    override fun onAddStream(p0: MediaStream?) {}

    override fun onRemoveStream(p0: MediaStream?) {}

    override fun onDataChannel(p0: DataChannel?) {}

    override fun onAddTrack(receiver: RtpReceiver, mediaStreams: Array<MediaStream>) {
        val track = receiver.track() ?: return
        val trackId = track.id() ?: return

        frameCryptorMap[trackId] = PmxFrameCryptorFactory.createPmxFrameCryptorForRtpReceiver(
            peerConnectionFactory,
            receiver,
            keyStore,
            null
        )

        trackObserver?.let { observer ->
            mediaStreams.firstOrNull()?.id.let { streamId ->
                observer.onRemoteTrack(streamId, track)
            }
        }
    }

    override fun onRemoveTrack(receiver: RtpReceiver) {
        receiver.track()?.id()?.let { trackId ->
            frameCryptorMap.remove(trackId)?.dispose()
        }
        receiver.dispose()
    }

    actual fun dispose() {
        frameCryptorMap.values.forEach { it.dispose() }
        frameCryptorMap.clear()
    }


}


//actual typealias webrtc.PMXKeyStoreKey = PmxKeyStore.Key
//actual typealias PMXKeyStore = PmxKeyStore
//actual typealias PeerConnectionFactory = org.webrtc.PeerConnectionFactory
//actual typealias RTCIceCandidate = IceCandidate
//actual typealias RTCIceConnectionState = PeerConnection.IceConnectionState
//actual class PMXKeyStoreKey actual constructor(from: Key)