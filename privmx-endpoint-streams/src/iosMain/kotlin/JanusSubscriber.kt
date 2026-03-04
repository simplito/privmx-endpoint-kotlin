package com.simplito.java.privmx_endpoint.modules.stream

import JanusConnection
import TrackObserver
import WebRTCFramework.PMXKeyStore
import WebRTCFramework.RTCCreateSessionDescriptionCompletionHandler
import WebRTCFramework.RTCMediaConstraints
import WebRTCFramework.RTCPeerConnectionFactory
import WebRTCFramework.RTCSdpType
import WebRTCFramework.RTCSessionDescription
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

//import com.simplito.java.privmx_endpoint.model.ConnectionType
@OptIn(ExperimentalForeignApi::class)
class JanusSubscriber(
    pcFactory: RTCPeerConnectionFactory,
    keyStore: PMXKeyStore,
    observer: TrackObserver?,
    onTrickle: (Long, String) -> Unit
) : JanusConnection(pcFactory, keyStore, observer, onTrickle) {

    @Throws(RuntimeException::class)
    suspend fun createAnswer(offerSdp: String): String {
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
            sdp
        }
    }

}
