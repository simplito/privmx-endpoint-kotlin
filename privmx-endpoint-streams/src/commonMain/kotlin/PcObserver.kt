import com.simplito.kotlin.privmx_endpoint.model.stream.Key

expect class PcObserver(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: PMXKeyStore,
    trackObserver: TrackObserver?,
    onIceCandidate: (candidate: RTCIceCandidate) -> Unit,
    onIceConnectionChange: (candidate: RTCIceConnectionState) -> Unit,
)


expect class PMXKeyStore{
    fun setKeys(keys: List<PMXKeyStoreKey>): Boolean
}
expect class PMXKeyStoreKey(from: Key)
expect class PeerConnectionFactory
expect class RTCIceCandidate
enum class RTCIceConnectionState {
    NEW,
    CHECKING,
    CONNECTED,
    COMPLETED,
    FAILED,
    DISCONNECTED,
    CLOSED;
}