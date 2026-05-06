package modules

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.PmxFrameCryptor
import org.webrtc.PmxKeyStore
import org.webrtc.SessionDescription
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal open class JanusConnection(
    protected val peerConnectionFactory: PeerConnectionFactory,
    protected val keyStore: PmxKeyStore,
    trackObserver: TrackObserver?,
    onTrickle: (Long, String) -> Unit,
    onConnectionChange: (PeerConnection.IceConnectionState) -> Unit = {}
) {
    protected val peerConnection: PeerConnection
    var sessionId: Long = -1L
    private val pcObserver: PcObserver

    init {
        this.pcObserver = PcObserver(
            peerConnectionFactory = peerConnectionFactory,
            keyStore = keyStore,
            trackObserver = trackObserver,
            iceCandidateCallback = { iceCandidate: IceCandidate ->
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
            renegotiationNeededCallback = this::onRenegotiationNeeded,
            iceConnectionChangeCallback = onConnectionChange
        )
        this.peerConnection = createPeerConnection(pcObserver)
    }

    internal class SdpObserver(
        private val continuation: Continuation<SessionDescription>?
    ) : org.webrtc.SdpObserver {
        override fun onCreateSuccess(sessionDescription: SessionDescription) {
            continuation?.resume(sessionDescription)
        }

        override fun onSetSuccess() {}
        override fun onCreateFailure(s: String?) {
            continuation?.resumeWithException(Exception(s))
        }
        override fun onSetFailure(s: String?) {}
    }

    //TODO: We need method to pass framecryptorOptions and TrackObserver
    private fun createPeerConnection(pcObserver: PcObserver): PeerConnection =
        peerConnectionFactory.createPeerConnection(
            PeerConnection.RTCConfiguration(emptyList<PeerConnection.IceServer>()),
            pcObserver
        ) ?: throw IllegalStateException("Failed to create PeerConnection")

    fun getConnectionState(): PeerConnection.PeerConnectionState =
        peerConnection.connectionState()

    open fun setFrameCryptorOptions(options: PmxFrameCryptor.PmxFrameCryptorOptions) {
        pcObserver.setFrameCryptorOptions(options)
    }

    fun isEnded(): Boolean = peerConnection.connectionState() in setOf(
        PeerConnection.PeerConnectionState.DISCONNECTED,
        PeerConnection.PeerConnectionState.CLOSED,
        PeerConnection.PeerConnectionState.FAILED
    )

    open fun close() {
        if (peerConnection.connectionState() != PeerConnection.PeerConnectionState.CLOSED) {
            peerConnection.dispose()
            pcObserver.dispose()
        }
    }

    open fun onRenegotiationNeeded() {}

    fun setRTCConfiguration(configuration: List<PeerConnection.IceServer>) {
        this.peerConnection.setConfiguration(PeerConnection.RTCConfiguration(configuration))
    }
}