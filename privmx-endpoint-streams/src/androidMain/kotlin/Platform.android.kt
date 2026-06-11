import com.simplito.kotlin.privmx_endpoint.model.stream.Key
import com.simplito.kotlin.privmx_endpoint.model.stream.KeyType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.webrtc.MediaConstraints
import org.webrtc.PmxFrameCryptor
import org.webrtc.PmxFrameCryptorFactory
import org.webrtc.PmxKeyStore
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/* ── natywne typy-liście ── */
actual typealias PeerConnectionFactory = org.webrtc.PeerConnectionFactory
actual typealias PeerConnection = org.webrtc.PeerConnection
actual typealias KeyStore = PmxKeyStore
actual typealias IceServer = org.webrtc.PeerConnection.IceServer
actual typealias IceCandidate = org.webrtc.IceCandidate
actual typealias SessionDescription = org.webrtc.SessionDescription
actual typealias RtpSender = org.webrtc.RtpSender
actual typealias FrameCryptor = PmxFrameCryptor
actual typealias FrameCryptorOptions = PmxFrameCryptor.PmxFrameCryptorOptions
actual typealias Observer = org.webrtc.PeerConnection.Observer

/* ── PeerConnectionFactory ── */
internal actual fun PeerConnectionFactory.createPeerConnection(observer: Observer): PeerConnection =
    createPeerConnection(
        org.webrtc.PeerConnection.RTCConfiguration(emptyList<org.webrtc.PeerConnection.IceServer>()),
        observer
    ) ?: throw IllegalStateException("Failed to create PeerConnection") // todo - zmienic message

internal actual fun PeerConnectionFactory.disposeFactory() = dispose()

internal actual fun PeerConnectionFactory.makeVideoTrack(
    id: String,
    isScreenCast: Boolean,
    alignTimestamps: Boolean
): VideoTrack = createVideoTrack(id, createVideoSource(isScreenCast, alignTimestamps)).toCommon()

internal actual fun PeerConnectionFactory.makeAudioTrack(id: String): AudioTrack =
    createAudioTrack(id, createAudioSource(MediaConstraints())).toCommon()

internal actual fun PeerConnectionFactory.createSenderFrameCryptor(
    sender: RtpSender,
    keyStore: KeyStore
): FrameCryptor =
    PmxFrameCryptorFactory.createPmxFrameCryptorFromRtpSender(
        this,
        sender,
        keyStore,
        null
    )

/* ── PeerConnection ── */
internal actual val PeerConnection.peerConnectionState: PeerConnectionState
    get() = when (connectionState()) {
        org.webrtc.PeerConnection.PeerConnectionState.NEW -> PeerConnectionState.NEW
        org.webrtc.PeerConnection.PeerConnectionState.CONNECTING -> PeerConnectionState.CONNECTING
        org.webrtc.PeerConnection.PeerConnectionState.CONNECTED -> PeerConnectionState.CONNECTED
        org.webrtc.PeerConnection.PeerConnectionState.DISCONNECTED -> PeerConnectionState.DISCONNECTED
        org.webrtc.PeerConnection.PeerConnectionState.FAILED -> PeerConnectionState.FAILED
        org.webrtc.PeerConnection.PeerConnectionState.CLOSED -> PeerConnectionState.CLOSED
        null -> PeerConnectionState.NEW
    }

internal actual fun PeerConnection.applyIceServers(iceServers: List<IceServer>) {
    setConfiguration(org.webrtc.PeerConnection.RTCConfiguration(iceServers))
}

internal actual fun PeerConnection.disposeConnection() = dispose()

internal actual fun PeerConnection.addTrack(track: MediaStreamTrack): RtpSender =
    addTrack((track as NativeTrack).native)

internal actual fun PeerConnection.removeTrack(sender: RtpSender) {
    removeTrack(sender)
}

internal actual suspend fun PeerConnection.createOffer(): SessionDescription =
    suspendCancellableCoroutine { cont -> createOffer(SdpObserver(cont), MediaConstraints()) }!!

internal actual suspend fun PeerConnection.createAnswer(): SessionDescription =
    suspendCancellableCoroutine { cont -> createAnswer(SdpObserver(cont), MediaConstraints()) }!!

internal actual suspend fun PeerConnection.setLocalDescription(sdp: SessionDescription) {
    suspendCancellableCoroutine<SessionDescription?> { cont -> setLocalDescription(SdpObserver(cont), sdp) }
}

internal actual suspend fun PeerConnection.setRemoteDescription(sdp: SessionDescription) {
    suspendCancellableCoroutine<SessionDescription?> { cont -> setRemoteDescription(SdpObserver(cont), sdp) }
}

/** Resumuje przy create - ORAZ set-success (set-success → null). */
private class SdpObserver(
    private val cont: Continuation<SessionDescription?>
) : org.webrtc.SdpObserver {
    override fun onCreateSuccess(sdp: SessionDescription) = cont.resume(sdp)
    override fun onSetSuccess() = cont.resume(null)
    override fun onCreateFailure(s: String?) = cont.resumeWithException(RuntimeException(s))
    override fun onSetFailure(s: String?) = cont.resumeWithException(RuntimeException(s))
}

/* ── SessionDescription ── */
internal actual val SessionDescription.sdp: String get() = description

internal actual fun sessionDescription(type: String, sdp: String): SessionDescription =
    SessionDescription(org.webrtc.SessionDescription.Type.fromCanonicalForm(type), sdp)

/* ── FrameCryptor ── */
internal actual fun FrameCryptor.disposeCryptor() = dispose()
internal actual fun FrameCryptor.applyOptions(options: FrameCryptorOptions) = setOptions(options)

/* ── IceCandidate ── */
internal actual fun IceCandidate.toJson(): String = buildJsonObject {
    put("sdp", sdp)
    put("adapterType", adapterType.ordinal)
    put("sdpMid", sdpMid)
    put("sdpMLineIndex", sdpMLineIndex)
    put("serverUrl", serverUrl)
}.toString()

/* ── KeyStore ── */
internal actual fun createKeyStore(): KeyStore = PmxFrameCryptorFactory.createPmxKeyStore()

internal actual fun KeyStore.applyKeys(keys: List<Key>) {
    setKeys(keys.map { key ->
        PmxKeyStore.Key(
            key.keyId,
            key.key,
            if (key.type == KeyType.LOCAL) PmxKeyStore.KeyType.LOCAL else PmxKeyStore.KeyType.REMOTE
        )
    })
}
