import WebRTCFramework.PMXFrameCryptorTransformer
import WebRTCFramework.RTCPeerConnection
import WebRTCFramework.RTCPeerConnectionFactory
import WebRTCFramework.PMXKeyStore
import WebRTCFramework.RTCConfiguration
import WebRTCFramework.RTCDataChannel
import WebRTCFramework.RTCIceCandidate
import WebRTCFramework.RTCIceConnectionState
import WebRTCFramework.RTCIceGatheringState
import WebRTCFramework.RTCMediaConstraints
import WebRTCFramework.RTCMediaStream
import WebRTCFramework.RTCPeerConnectionDelegateProtocol
import WebRTCFramework.RTCPeerConnectionState
import WebRTCFramework.RTCSessionDescription
import WebRTCFramework.RTCSignalingState
import com.simplito.java.privmx_endpoint.modules.stream.PcObserver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import platform.darwin.NSObject
import kotlin.collections.emptyList
import kotlin.coroutines.Continuation


@OptIn(ExperimentalForeignApi::class)
open class JanusConnection(
    protected val peerConnectionFactory: RTCPeerConnectionFactory,
    protected val keyStore: PMXKeyStore,
    protected val trackObserver: TrackObserver?,
    protected val onTrickle: (Long, String) -> Unit
): AutoCloseable {

    var sessionId: Long = -1L
        private set
    protected val pcObserver: PcObserver
    protected val peerConnection: RTCPeerConnection

    init {
        this.pcObserver = PcObserver(
            peerConnectionFactory,
            keyStore,
            trackObserver,
            { iceCandidate ->
                if (sessionId > -1) {
                    val obj = buildJsonObject {
                        put("sdp", iceCandidate.sdp)
                        put("sdpMid", iceCandidate.sdpMid)
                        put("sdpMLineIndex", iceCandidate.sdpMLineIndex)
                        put("serverUrl", iceCandidate.serverUrl)
                    }
                    //TODO: Uncomment trickle when update privmx streams compilation
//                        onTrickle.accept(sessionId,obj.toString());
//                        println("Trickle executed");
                }
            },
            {
                    //TODO: Add observing ice connections state changes
            }
        )
        this.peerConnection = createPeerConnection(pcObserver)
    }

//    internal class SdpObserver(res: Continuation<RTCSessionDescription>) : RTCSess {
//        private val res: CompletableFuture<SessionDescription?>
//
//        init {
//            this.res = res
//        }
//
//        @Override
//        fun onCreateSuccess(sessionDescription: SessionDescription?) {
//            res.complete(sessionDescription)
//        }
//
//        @Override
//        fun onSetSuccess() {
//        }
//
//        @Override
//        fun onCreateFailure(s: String?) {
//        }
//
//        @Override
//        fun onSetFailure(s: String?) {
//        }
//    }

    //TODO: We need method to pass framecryptorOptions and TrackObserver
    private fun createPeerConnection(pcObserver: PcObserver): RTCPeerConnection {
        return peerConnectionFactory.peerConnectionWithConfiguration(
            RTCConfiguration(),
            RTCMediaConstraints(),
            pcObserver
        )!!
    }

    val connectionState: RTCPeerConnectionState
        get() = peerConnection.connectionState()

    fun updateSessionId(sessionId: Long) {
        this.sessionId = sessionId
    }


//    fun setFrameCryptorOptions(options: PMXOp) {
//        pcObserver.setFrameCryptorOptions(options)
//    }

    val isEnded: Boolean
        get() = peerConnection.connectionState() === RTCPeerConnectionState.RTCPeerConnectionStateDisconnected || peerConnection.connectionState() === RTCPeerConnectionState.RTCPeerConnectionStateClosed || peerConnection.connectionState() === RTCPeerConnectionState.RTCPeerConnectionStateFailed

    override fun close() {
        if (peerConnection.connectionState() !== RTCPeerConnectionState.RTCPeerConnectionStateClosed) {
            peerConnection.close()
        }
    }
}