package modules

import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.PmxFrameCryptor
import org.webrtc.PmxFrameCryptorFactory
import org.webrtc.PmxKeyStore
import org.webrtc.RtpReceiver

class PcObserver(
    private val peerConnectionFactory: PeerConnectionFactory?,
    private val keyStore: PmxKeyStore,
    private val trackObserver: TrackObserver?,
    private val iceCandidateCallback: (IceCandidate) -> Unit,
    private val renegotiationNeededCallback: () -> Unit = {},
    private val iceConnectionChangeCallback: (PeerConnection.IceConnectionState) -> Unit = {}
) : PeerConnection.Observer {
    private val frameCryptorMap = mutableMapOf<String, PmxFrameCryptor>()

    override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {}

    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
        iceConnectionChangeCallback(iceConnectionState)
    }

    override fun onIceConnectionReceivingChange(b: Boolean) {}

    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {}

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        iceCandidateCallback(iceCandidate)
    }

    override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {}

    override fun onAddStream(mediaStream: MediaStream) {}

    override fun onRemoveStream(mediaStream: MediaStream) {}

    override fun onDataChannel(dataChannel: DataChannel) {}

    override fun onRenegotiationNeeded() {
        renegotiationNeededCallback()
    }

    override fun onAddTrack(receiver: RtpReceiver, mediaStreams: Array<MediaStream>) {
        val track = receiver.track() ?: return
        val trackId = track.id() ?: return

        if (peerConnectionFactory != null) {
            frameCryptorMap.put(
                trackId,
                PmxFrameCryptorFactory.createPmxFrameCryptorForRtpReceiver(
                    peerConnectionFactory,
                    receiver,
                    keyStore
                )
            )
        }

        trackObserver?.let { observer ->
            mediaStreams.firstOrNull()?.id?.let { streamId ->
                observer.OnRemoteTrack(streamId, track)
            }
        }
    }

    override fun onRemoveTrack(receiver: RtpReceiver) {
        receiver.track()?.id()?.let { trackId ->
            frameCryptorMap.remove(trackId)?.dispose()
        }
        receiver.dispose()
    }

    fun setFrameCryptorOptions(options: PmxFrameCryptor.PmxFrameCryptorOptions) {
        frameCryptorMap.values.forEach { it.setOptions(options) }
    }

    fun dispose() {
        frameCryptorMap.values.forEach { it.dispose() }
        frameCryptorMap.clear()
    }
}