package webrtc

import TrackObserver
import com.simplito.kotlin.privmx_endpoint.model.stream.Key

expect class PcObserver(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: KeyStore,
    trackObserver: TrackObserver?,
    onIceCandidateCallback: (candidate: IceCandidate) -> Unit,
    onRenegotiationNeededCallback: () -> Unit = {},
    onIceConnectionChangeCallback: (candidate: IceConnectionState) -> Unit = {}
): Observer{
    fun setFrameCryptorOptions(options: FrameCryptorOptions)
    fun dispose()
}

fun cwff(){

}




//expect class PMXKeyStore{
//    fun setKeys(keys: List<webrtc.PMXKeyStoreKey>): Boolean
//}
//expect class PMXKeyStoreKey(from: Key)
//expect class PeerConnectionFactory
//expect class RTCIceCandidate
//enum class RTCIceConnectionState {
//    NEW,
//    CHECKING,
//    CONNECTED,
//    COMPLETED,
//    FAILED,
//    DISCONNECTED,
//    CLOSED;
//}

