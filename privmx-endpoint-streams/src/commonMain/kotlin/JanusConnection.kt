import kotlinx.coroutines.sync.Mutex

open class JanusConnection(
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
            if (sessionId > -1) onTrickle(sessionId, candidate.toJson())
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

    open fun setFrameCryptorOptions(options: FrameCryptorOptions) {
        pcObserver.setFrameCryptorOptions(options)
    }

    fun setRTCConfiguration(configuration: List<IceServer>) {
        peerConnection.applyIceServers(configuration)
    }

    open fun onRenegotiationNeeded() {}

    override fun close() {
        if (connectionState != PeerConnectionState.CLOSED) {
            peerConnection.disposeConnection()
            pcObserver.dispose()
        }
    }
}