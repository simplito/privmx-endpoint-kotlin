package modules

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.withLock
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory
import org.webrtc.PmxKeyStore
import org.webrtc.SessionDescription

internal class JanusSubscriber(
    pcFactory: PeerConnectionFactory,
    keyStore: PmxKeyStore,
    observer: TrackObserver?,
    onTrickle: (Long, String) -> Unit
) : JanusConnection(pcFactory, keyStore, observer, onTrickle) {
    suspend fun createAnswer(offerSdp: String, type: String): String {
        configurationMutex.withLock {
            suspendCancellableCoroutine { continuation ->
                peerConnection.setRemoteDescription(
                    SdpObserver(continuation),
                    SessionDescription(
                        SessionDescription.Type.fromCanonicalForm(type),
                        offerSdp
                    )
                )
            }

            val answer = runCatching {
                suspendCancellableCoroutine { continuation ->
                    peerConnection.createAnswer(
                        SdpObserver(continuation), MediaConstraints()
                    )
                }
            }.getOrElse { throw RuntimeException("Cannot create answer") }

            suspendCancellableCoroutine { continuation ->
                peerConnection.setLocalDescription(
                    SdpObserver(continuation),
                    answer
                )
            }

            return answer.description
        }
    }
}