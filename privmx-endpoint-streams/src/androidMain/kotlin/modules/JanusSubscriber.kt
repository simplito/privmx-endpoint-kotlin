package modules

import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory
import org.webrtc.PmxKeyStore
import org.webrtc.SessionDescription
import java.util.concurrent.CompletableFuture

internal class JanusSubscriber(
    pcFactory: PeerConnectionFactory,
    keyStore: PmxKeyStore,
    observer: TrackObserver?,
    onTrickle: (Long, String) -> Unit
) : JanusConnection(pcFactory, keyStore, observer, onTrickle) {
    fun createAnswer(offerSdp: String, type: String): String {
        val res = CompletableFuture<SessionDescription>()
        peerConnection.setRemoteDescription(
            SdpObserver(null),
            SessionDescription(SessionDescription.Type.fromCanonicalForm(type), offerSdp)
        )
        peerConnection.createAnswer(SdpObserver(res), MediaConstraints())

        return runCatching {
            val answer = res.get()
            peerConnection.setLocalDescription(SdpObserver(null), answer)
            answer.description
        }.getOrElse {
            throw RuntimeException("Cannot create answer")
        }
    }
}