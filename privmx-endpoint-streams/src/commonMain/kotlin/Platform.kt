import com.simplito.kotlin.privmx_endpoint.model.stream.Key
import kotlin.coroutines.CoroutineContext

expect class PeerConnectionFactory
expect class PeerConnection
expect class KeyStore
expect class IceServer
expect class IceCandidate
expect class SessionDescription
expect class RtpSender
expect class FrameCryptor
expect class FrameCryptorOptions

expect interface Observer

enum class PeerConnectionState {
    NEW, CONNECTING, CONNECTED, DISCONNECTED, FAILED, CLOSED
}

enum class IceConnectionState {
    NEW, CHECKING, CONNECTED, COMPLETED, DISCONNECTED, CLOSED, FAILED
}

// — PeerConnectionFactory —
internal expect fun PeerConnectionFactory.createPeerConnection(observer: Observer): PeerConnection
internal expect fun PeerConnectionFactory.disposeFactory()
internal expect fun PeerConnectionFactory.makeVideoTrack(
    id: String,
    isScreenCast: Boolean,
    alignTimestamps: Boolean = true
): VideoTrack
internal expect fun PeerConnectionFactory.makeAudioTrack(id: String): AudioTrack
internal expect fun PeerConnectionFactory.createSenderFrameCryptor(
    sender: RtpSender,
    keyStore: KeyStore
): FrameCryptor

// — PeerConnection —
internal expect val PeerConnection.peerConnectionState: PeerConnectionState
internal expect fun PeerConnection.applyIceServers(iceServers: List<IceServer>)
internal expect fun PeerConnection.disposeConnection()
internal expect fun PeerConnection.addTrack(track: MediaStreamTrack): RtpSender
internal expect fun PeerConnection.removeTrack(sender: RtpSender)
internal expect suspend fun PeerConnection.createOffer(): SessionDescription
internal expect suspend fun PeerConnection.createAnswer(): SessionDescription
internal expect suspend fun PeerConnection.setLocalDescription(sdp: SessionDescription)
internal expect suspend fun PeerConnection.setRemoteDescription(sdp: SessionDescription)

// — SessionDescription —
internal expect val SessionDescription.sdp: String
internal expect fun sessionDescription(type: String, sdp: String): SessionDescription

// — FrameCryptor —
internal expect fun FrameCryptor.disposeCryptor()
internal expect fun FrameCryptor.applyOptions(options: FrameCryptorOptions)

// — IceCandidate —
internal expect fun IceCandidate.toJson(): String

// — KeyStore —
internal expect fun createKeyStore(): KeyStore
internal expect fun KeyStore.applyKeys(keys: List<Key>)

// — coroutines (brak runBlocking w commonMain) —
internal expect fun <T> runBlockingOn(context: CoroutineContext, block: suspend () -> T): T