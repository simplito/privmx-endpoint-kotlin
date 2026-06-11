import kotlinx.coroutines.sync.withLock

class JanusSubscriber(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: KeyStore,
    trackObserver: TrackObserver?,
    onTrickle: (Long, String) -> Unit
) : JanusConnection(peerConnectionFactory, keyStore, trackObserver, onTrickle) {

    suspend fun createAnswer(offerSdp: String, type: String): String = configurationMutex.withLock {
        peerConnection.setRemoteDescription(sessionDescription(type, offerSdp))
        val answer = peerConnection.createAnswer()
        peerConnection.setLocalDescription(answer)
        answer.sdp
    }
}