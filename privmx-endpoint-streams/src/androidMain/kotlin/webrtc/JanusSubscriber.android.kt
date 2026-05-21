package webrtc

import TrackObserver
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.withLock
import org.webrtc.MediaConstraints

actual class JanusSubscriber actual constructor(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: KeyStore,
    trackObserver: TrackObserver?,
    onTrickle: (Long, String) -> Unit
) : JanusConnection(peerConnectionFactory, keyStore, trackObserver,onTrickle) {
    actual suspend fun createAnswer(offerSdp: String, type: String): String {
        configurationMutex.withLock {
            suspendCancellableCoroutine { continuation ->
                peerConnection.setRemoteDescription(
                    SdpObserver(continuation),
                    SessionDescription(
                        org.webrtc.SessionDescription.Type.fromCanonicalForm(type),
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