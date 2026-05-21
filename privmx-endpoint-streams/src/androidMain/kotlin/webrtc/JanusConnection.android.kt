package webrtc

import TrackObserver
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection.RTCConfiguration
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual open class JanusConnection actual constructor(
    val peerConnectionFactory: PeerConnectionFactory,
    val keyStore: KeyStore,
    val trackObserver: TrackObserver?,
    val onTrickle: (Long, String) -> Unit,
    val onConnectionChange: (IceConnectionState) -> Unit
) : AutoCloseable {
    actual var sessionId: Long = -1L

    protected actual var pcObserver: Observer
    protected actual var peerConnection: PeerConnection

    init {
        this.pcObserver = PcObserver(
            peerConnectionFactory = peerConnectionFactory,
            keyStore = keyStore,
            trackObserver = trackObserver,
            onIceCandidateCallback = { iceCandidate: IceCandidate ->
                if (sessionId > -1) {
                    val obj = buildJsonObject {
                        runCatching {
                            put("sdp", iceCandidate.sdp)
                            put("adapterType", iceCandidate.adapterType.ordinal)
                            put("sdpMid", iceCandidate.sdpMid)
                            put("sdpMLineIndex", iceCandidate.sdpMLineIndex)
                            put("serverUrl", iceCandidate.serverUrl)
                        }.onFailure {
                            println("Error with obj json")
                        }
                    }
                    // TODO: Uncomment trickle when update privmx streams compilation
                    // onTrickle(sessionId, obj.toString())
                    // println("Trickle executed")
                }
            },
            onRenegotiationNeededCallback = this::onRenegotiationNeeded,
            onIceConnectionChangeCallback = onConnectionChange
        )
        this.peerConnection = createPeerConnection(pcObserver)
    }

    protected actual fun createPeerConnection(pcObserver: Observer): PeerConnection =
        peerConnectionFactory.createPeerConnection(
            RTCConfiguration(emptyList<org.webrtc.PeerConnection.IceServer>()),
            pcObserver
        ) ?: throw IllegalStateException("Failed to create PeerConnection")


    actual val connectionState: PeerConnectionState
        get() = peerConnection.connectionState()

    actual fun setFrameCryptorOptions(options: FrameCryptorOptions){
         (pcObserver as PcObserver).setFrameCryptorOptions(options)
    }

//    actual fun updateSessionId(sessionId: Long) {
//    }
    open fun onRenegotiationNeeded() {}

    actual val isEnded: Boolean
        get() = peerConnection.connectionState() in setOf(
            PeerConnectionState.DISCONNECTED.state,
            PeerConnectionState.CLOSED.state,
            PeerConnectionState.FAILED.state
        )


    actual override fun close() {
    }

    actual open fun setRTCConfiguration(configuration: List<IceServer>) {
        this.peerConnection.setConfiguration(RTCConfiguration(configuration))
    }

    protected actual val configurationMutex: Mutex = Mutex()

    internal class SdpObserver(
        private val continuation: Continuation<SessionDescription>
    ) : org.webrtc.SdpObserver {
        override fun onCreateSuccess(sessionDescription: SessionDescription) {
            continuation.resume(sessionDescription)
        }

        override fun onSetSuccess() {}
        override fun onCreateFailure(s: String?) {
            continuation.resumeWithException(Exception(s))
        }
        override fun onSetFailure(s: String?) {}
    }

}