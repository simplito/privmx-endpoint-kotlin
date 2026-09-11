package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint_streams.webrtc.*
import kotlinx.coroutines.sync.withLock

internal class JanusSubscriber(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: KeyStore,
    roomId: String,
    dataChannelCryptoProvider: InternalDataChannelMessageCryptoProvider,
    remoteStreamObserver: RemoteStreamObserver?,
    onTrickle: (Long, String) -> Unit
) : JanusConnection(peerConnectionFactory, keyStore, roomId, dataChannelCryptoProvider,remoteStreamObserver, onTrickle) {

    private var bootstrapDataChannel: DataChannel? = null

    suspend fun createAnswer(offerSdp: String, type: String): String = configurationMutex.withLock {
        if (bootstrapDataChannel == null || bootstrapDataChannel?.state == DataChannelState.CLOSED) {
            bootstrapDataChannel = peerConnection.createDataChannel("JanusDataChannel", getDataChannelInit())
        }
        peerConnection.setRemoteDescription(sessionDescription(fromCanonicalForm( type), offerSdp))
        val answer = peerConnection.createAnswer()
        peerConnection.setLocalDescription(answer)
        answer.sdp
    }
}