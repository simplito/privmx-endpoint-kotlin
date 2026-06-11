import org.webrtc.DataChannel
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PmxFrameCryptor
import org.webrtc.PmxFrameCryptorFactory
import org.webrtc.RtpReceiver

actual class PcObserver actual constructor(
    private val peerConnectionFactory: PeerConnectionFactory,
    private val keyStore: KeyStore,
    private val trackObserver: TrackObserver?,
    private val onIceCandidateCallback: (candidate: IceCandidate) -> Unit,
    private val onRenegotiationNeededCallback: () -> Unit,
    private val onIceConnectionChangeCallback: (state: IceConnectionState) -> Unit
) : Observer {
    private val frameCryptorMap = mutableMapOf<String, PmxFrameCryptor>()

    actual fun setFrameCryptorOptions(options: FrameCryptorOptions) {
        frameCryptorMap.values.forEach { it.setOptions(options) }
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState) =
        onIceConnectionChangeCallback(p0.toCommon())

    override fun onIceCandidate(p0: IceCandidate) = onIceCandidateCallback(p0)

    override fun onRenegotiationNeeded() = onRenegotiationNeededCallback()

    override fun onAddTrack(receiver: RtpReceiver, mediaStreams: Array<MediaStream>) {
        val track = receiver.track() ?: return
        val trackId = track.id() ?: return

        frameCryptorMap[trackId] = PmxFrameCryptorFactory.createPmxFrameCryptorForRtpReceiver(
            peerConnectionFactory, receiver, keyStore, null
        )
        trackObserver?.onRemoteTrack(mediaStreams.firstOrNull()?.id, track.toCommon())
    }

    override fun onRemoveTrack(receiver: RtpReceiver) {
        receiver.track()?.id()?.let { frameCryptorMap.remove(it)?.dispose() }
        receiver.dispose()
    }

    actual fun dispose() {
        frameCryptorMap.values.forEach { it.dispose() }
        frameCryptorMap.clear()
    }

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
    override fun onIceConnectionReceivingChange(p0: Boolean) {}
    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate?>?) {}
    override fun onAddStream(p0: MediaStream?) {}
    override fun onRemoveStream(p0: MediaStream?) {}
    override fun onDataChannel(p0: DataChannel?) {}
}

internal fun PeerConnection.IceConnectionState.toCommon(): IceConnectionState = when (this) {
    PeerConnection.IceConnectionState.NEW -> IceConnectionState.NEW
    PeerConnection.IceConnectionState.CHECKING -> IceConnectionState.CHECKING
    PeerConnection.IceConnectionState.CONNECTED -> IceConnectionState.CONNECTED
    PeerConnection.IceConnectionState.COMPLETED -> IceConnectionState.COMPLETED
    PeerConnection.IceConnectionState.DISCONNECTED -> IceConnectionState.DISCONNECTED
    PeerConnection.IceConnectionState.CLOSED -> IceConnectionState.CLOSED
    PeerConnection.IceConnectionState.FAILED -> IceConnectionState.FAILED
}
