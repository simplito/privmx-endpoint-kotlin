package webrtc

import TrackObserver
import WebRTCFramework.RTCMediaConstraints
import WebRTCFramework.RTCSdpType
import WebRTCFramework.RTCSessionDescription
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalForeignApi::class)
actual class JanusSubscriber
actual constructor(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: KeyStore,
    trackObserver: TrackObserver?,
    onTrickle: (Long, String) -> Unit
) : JanusConnection(peerConnectionFactory, keyStore, trackObserver, onTrickle) {
    actual suspend fun createAnswer(offerSdp: String, type: String): String {
        suspendCancellableCoroutine { continuation ->
            peerConnection.setRemoteDescription(
                RTCSessionDescription(RTCSdpType.RTCSdpTypeOffer, offerSdp)
            ){
                if(it == null) continuation.resume(Unit)
                else continuation.resumeWithException(RuntimeException(it.description))
            }
        }
        return suspendCancellableCoroutine { continuation ->
            peerConnection.answerForConstraints(RTCMediaConstraints()) { sdp, error ->
                if(error == null) continuation.resume(sdp!!)
                else continuation.resumeWithException(RuntimeException(error.description))
            }
        }.run {
            peerConnection.setLocalDescription(this){}
            description!!
        }
    }
}