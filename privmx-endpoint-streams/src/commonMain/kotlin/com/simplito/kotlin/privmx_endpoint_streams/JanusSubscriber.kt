package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint_streams.webrtc.KeyStore
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnectionFactory
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.SdpType
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.createAnswer
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.fromCanonicalForm
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.sdp
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.sessionDescription
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.setLocalDescription
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.setRemoteDescription
import kotlinx.coroutines.sync.withLock

internal class JanusSubscriber(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: KeyStore,
    trackObserver: TrackObserver?,
    onTrickle: (Long, String) -> Unit
) : JanusConnection(peerConnectionFactory, keyStore, trackObserver, onTrickle) {

    suspend fun createAnswer(offerSdp: String, type: String): String = configurationMutex.withLock {
        peerConnection.setRemoteDescription(sessionDescription(fromCanonicalForm(type), offerSdp))
        val answer = peerConnection.createAnswer()
        peerConnection.setLocalDescription(answer)
        answer.sdp
    }
}