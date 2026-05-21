package webrtc

import TrackObserver

expect class JanusSubscriber(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: KeyStore,
    trackObserver: TrackObserver?,
    onTrickle: (Long, String) -> Unit
): JanusConnection {
    suspend fun createAnswer(offerSdp: String, type: String): String
}