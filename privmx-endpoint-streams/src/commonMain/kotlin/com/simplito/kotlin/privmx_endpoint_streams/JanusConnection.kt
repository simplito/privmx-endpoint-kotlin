package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint_streams.webrtc.IceConnectionState
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.IceServer
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.KeyStore
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnection
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnectionFactory
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnectionState
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PmxFrameCryptorOptions
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.applyIceServers
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.createPeerConnection
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.disposeConnection
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.getStats
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.peerConnectionState
import kotlinx.coroutines.sync.Mutex

internal open class JanusConnection(
    protected val peerConnectionFactory: PeerConnectionFactory,
    protected val keyStore: KeyStore,
    trackObserver: TrackObserver?,
    private val onTrickle: (Long, String) -> Unit,
    onConnectionChange: (IceConnectionState) -> Unit = {}
) : AutoCloseable {

    var sessionId: Long = -1L
    protected val pcObserver: PcObserver = PcObserver(
        peerConnectionFactory = peerConnectionFactory,
        keyStore = keyStore,
        trackObserver = trackObserver,
        onIceCandidateCallback = { candidate ->
            //TODO: Uncomment when trickle working
//            if (sessionId > -1) onTrickle(sessionId, candidate.toJson())
        },
        onRenegotiationNeededCallback = ::onRenegotiationNeeded,
        onIceConnectionChangeCallback = onConnectionChange
    )

    protected val peerConnection: PeerConnection =
        peerConnectionFactory.createPeerConnection(pcObserver)

    protected val configurationMutex = Mutex()

    val connectionState: PeerConnectionState
        get() = peerConnection.peerConnectionState

    val isEnded: Boolean
        get() = connectionState in setOf(
            PeerConnectionState.DISCONNECTED,
            PeerConnectionState.CLOSED,
            PeerConnectionState.FAILED
        )

    open fun setFrameCryptorOptions(options: PmxFrameCryptorOptions) {
        pcObserver.setFrameCryptorOptions(options)
    }

    fun setRTCConfiguration(configuration: List<IceServer>) {
        peerConnection.applyIceServers(configuration)
    }

    open fun onRenegotiationNeeded() {}

    suspend fun getStats() = peerConnection.getStats()

    override fun close() {
        if (connectionState != PeerConnectionState.CLOSED) {
            peerConnection.disposeConnection()
            pcObserver.dispose()
        }
    }
}