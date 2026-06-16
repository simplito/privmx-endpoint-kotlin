@file:OptIn(ExperimentalForeignApi::class)

import WebRTCFramework.PMXFrameCryptorTransformer
import WebRTCFramework.PMXKSKey
import WebRTCFramework.PMXKSKeyTypeLOCAL
import WebRTCFramework.PMXKSKeyTypeREMOTE
import WebRTCFramework.PMXKeyStore
import WebRTCFramework.RTCConfiguration
import WebRTCFramework.RTCIceCandidate
import WebRTCFramework.RTCIceServer
import WebRTCFramework.RTCMediaConstraints
import WebRTCFramework.RTCPeerConnection
import WebRTCFramework.RTCPeerConnectionDelegateProtocol
import WebRTCFramework.RTCPeerConnectionFactory
import WebRTCFramework.RTCPeerConnectionState
import WebRTCFramework.RTCRtpSender
import WebRTCFramework.RTCSdpType
import WebRTCFramework.RTCSessionDescription
import com.simplito.kotlin.privmx_endpoint.model.stream.Key
import com.simplito.kotlin.privmx_endpoint.model.stream.KeyType
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual typealias PeerConnectionFactory = RTCPeerConnectionFactory
actual typealias PeerConnection = RTCPeerConnection
actual typealias KeyStore = PMXKeyStore
actual typealias IceServer = RTCIceServer
actual typealias IceCandidate = RTCIceCandidate
actual typealias SessionDescription = RTCSessionDescription
actual typealias RtpSender = RTCRtpSender
actual typealias FrameCryptor = PMXFrameCryptorTransformer
actual typealias Observer = RTCPeerConnectionDelegateProtocol

/** Opcje frame cryptora po stronie iOS (brak gotowego typu we frameworku). */
data class PmxFrameCryptorOptions(
    val dropFrameIfCryptionFailed: Boolean = false
)

actual typealias FrameCryptorOptions = PmxFrameCryptorOptions

private fun mediaConstraints() =
    RTCMediaConstraints(mandatoryConstraints = emptyMap<Any?, Any>(), optionalConstraints = null)

/* ── PeerConnectionFactory ── */
internal actual fun PeerConnectionFactory.createPeerConnection(observer: Observer): PeerConnection =
    // TODO(iOS): zweryfikuj sygnaturę peerConnectionWithConfiguration:constraints:delegate:
    peerConnectionWithConfiguration(RTCConfiguration(), mediaConstraints(), observer)
        ?: throw IllegalStateException("Failed to create PeerConnection")

internal actual fun PeerConnectionFactory.disposeFactory() {}

internal actual fun PeerConnectionFactory.makeVideoTrack(
    id: String,
    isScreenCast: Boolean,
    alignTimestamps: Boolean  // TODO(iOS): brak odpowiednika alignTimestamps
): VideoTrack = videoTrackWithSource(videoSourceForScreenCast(isScreenCast), id)

internal actual fun PeerConnectionFactory.makeAudioTrack(id: String): AudioTrack =
    audioTrackWithSource(audioSourceWithConstraints(mediaConstraints()), id)

internal actual fun PeerConnectionFactory.createSenderFrameCryptor(
    sender: RtpSender,
    keyStore: KeyStore
): FrameCryptor =
    // TODO(iOS): zweryfikuj inicjalizator forRtpSender (analogicznie do forRtpReceiver w PcObserver)
    PMXFrameCryptorTransformer(
        forRtpSender = sender,
        withPeerConnectionFactory = this,
        keyStore,
        null
    )

/* ── PeerConnection ── */
internal actual val PeerConnection.peerConnectionState: PeerConnectionState
    get() = when (connectionState()) {
        RTCPeerConnectionState.RTCPeerConnectionStateNew -> PeerConnectionState.NEW
        RTCPeerConnectionState.RTCPeerConnectionStateConnecting -> PeerConnectionState.CONNECTING
        RTCPeerConnectionState.RTCPeerConnectionStateConnected -> PeerConnectionState.CONNECTED
        RTCPeerConnectionState.RTCPeerConnectionStateDisconnected -> PeerConnectionState.DISCONNECTED
        RTCPeerConnectionState.RTCPeerConnectionStateFailed -> PeerConnectionState.FAILED
        RTCPeerConnectionState.RTCPeerConnectionStateClosed -> PeerConnectionState.CLOSED
        else -> PeerConnectionState.FAILED
    }

internal actual fun PeerConnection.applyIceServers(iceServers: List<IceServer>) {
    setConfiguration(RTCConfiguration().also { it.setIceServers(iceServers) })
}

internal actual fun PeerConnection.disposeConnection() = close()

internal actual fun PeerConnection.addTrack(track: MediaStreamTrack): RtpSender =
    addTrack(track, listOf(track.trackId))
        ?: throw IllegalStateException("Failed to add track — session may be closed")

internal actual fun PeerConnection.removeTrack(sender: RtpSender) {
    removeTrack(sender)
}

internal actual suspend fun PeerConnection.createOffer(): SessionDescription =
    suspendCancellableCoroutine { cont ->
        offerForConstraints(mediaConstraints()) { sdp, error ->
            if (error == null) cont.resume(sdp!!) else cont.resumeWithException(RuntimeException(error.description))
        }
    }

internal actual suspend fun PeerConnection.createAnswer(): SessionDescription =
    suspendCancellableCoroutine { cont ->
        answerForConstraints(mediaConstraints()) { sdp, error ->
            if (error == null) cont.resume(sdp!!) else cont.resumeWithException(RuntimeException(error.description))
        }
    }

internal actual suspend fun PeerConnection.setLocalDescription(sdp: SessionDescription) {
    suspendCancellableCoroutine<Unit> { cont ->
        setLocalDescription(sdp) { error ->
            if (error == null) cont.resume(Unit) else cont.resumeWithException(RuntimeException(error.description))
        }
    }
}

internal actual suspend fun PeerConnection.setRemoteDescription(sdp: SessionDescription) {
    suspendCancellableCoroutine<Unit> { cont ->
        setRemoteDescription(sdp) { error ->
            if (error == null) cont.resume(Unit) else cont.resumeWithException(RuntimeException(error.description))
        }
    }
}

internal actual val SessionDescription.sdp: String get() = this.sdp

internal actual fun sessionDescription(type: String, sdp: String): SessionDescription =
    RTCSessionDescription(type.toSdpType(), sdp)

private fun String.toSdpType(): RTCSdpType = when (this) {
    "offer" -> RTCSdpType.RTCSdpTypeOffer
    "answer" -> RTCSdpType.RTCSdpTypeAnswer
    "pranswer" -> RTCSdpType.RTCSdpTypePrAnswer
    else -> RTCSdpType.RTCSdpTypeOffer
}

/* ── FrameCryptor ── */
internal actual fun FrameCryptor.disposeCryptor() {}

internal actual fun FrameCryptor.applyOptions(options: FrameCryptorOptions) {
    setDropFramesIfCryptionFailed(options.dropFrameIfCryptionFailed)
}

internal actual fun IceCandidate.toJson(): String = buildJsonObject {
    put("sdp", sdp)
    put("sdpMid", sdpMid)
    put("sdpMLineIndex", sdpMLineIndex)
    put("serverUrl", serverUrl)
}.toString()

internal actual fun createKeyStore(): KeyStore = PMXKeyStore()

internal actual fun KeyStore.applyKeys(keys: List<Key>) {
    setKeys(keys.map { key ->
        PMXKSKey(
            key.keyId,
            key.key.usePinned { NSData.dataWithBytes(it.addressOf(0), it.get().size.toULong()) },
            if (key.type == KeyType.LOCAL) PMXKSKeyTypeLOCAL else PMXKSKeyTypeREMOTE
        )
    })
}