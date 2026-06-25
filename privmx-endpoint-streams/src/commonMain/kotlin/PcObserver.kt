import webrtc.IceCandidate
import webrtc.IceConnectionState
import webrtc.KeyStore
import webrtc.Observer
import webrtc.PeerConnectionFactory
import webrtc.PmxFrameCryptorOptions

expect class PcObserver(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: KeyStore,
    trackObserver: TrackObserver?,
    onIceCandidateCallback: (candidate: IceCandidate) -> Unit,
    onRenegotiationNeededCallback: () -> Unit = {},
    onIceConnectionChangeCallback: (candidate: IceConnectionState) -> Unit = {}
): Observer{
    fun setFrameCryptorOptions(options: PmxFrameCryptorOptions)
    fun dispose()
}
