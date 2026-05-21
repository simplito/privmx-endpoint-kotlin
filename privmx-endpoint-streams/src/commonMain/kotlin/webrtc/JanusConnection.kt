package webrtc

import TrackObserver
import kotlinx.coroutines.sync.Mutex


expect open class JanusConnection(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: KeyStore,
    trackObserver: TrackObserver?,
    onTrickle: (Long, String) -> Unit,
    onConnectionChange: (IceConnectionState) -> Unit = {}
) : AutoCloseable {

    var sessionId: Long
    protected var pcObserver: Observer
    protected var peerConnection: PeerConnection
    protected val configurationMutex : Mutex
    protected fun createPeerConnection(pcObserver: Observer): PeerConnection

    val connectionState: PeerConnectionState
    fun setFrameCryptorOptions(options: FrameCryptorOptions)

    fun setRTCConfiguration(configuration: List<IceServer>)

    val isEnded: Boolean
    override fun close()


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

//    override fun close()
//    {
//        if (peerConnection.connectionState() !== RTCPeerConnectionState.RTCPeerConnectionStateClosed) {
//            peerConnection.close()
//        }
//    }
}