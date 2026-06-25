@file:OptIn(ExperimentalForeignApi::class)

package webrtc

import WebRTCFramework.RTCConfiguration
import WebRTCFramework.RTCPeerConnection
import WebRTCFramework.RTCPeerConnectionState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual typealias PeerConnection = RTCPeerConnection

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
            if (error == null) cont.resume(sdp!!) else cont.resumeWithException(
                RuntimeException(
                    error.description
                )
            )
        }
    }

internal actual suspend fun PeerConnection.createAnswer(): SessionDescription =
    suspendCancellableCoroutine { cont ->
        answerForConstraints(mediaConstraints()) { sdp, error ->
            if (error == null) cont.resume(sdp!!) else cont.resumeWithException(
                RuntimeException(
                    error.description
                )
            )
        }
    }

internal actual suspend fun PeerConnection.setLocalDescription(sdp: SessionDescription) {
    suspendCancellableCoroutine<Unit> { cont ->
        setLocalDescription(sdp) { error ->
            if (error == null) cont.resume(Unit) else cont.resumeWithException(
                RuntimeException(
                    error.description
                )
            )
        }
    }
}

internal actual suspend fun PeerConnection.setRemoteDescription(sdp: SessionDescription) {
    suspendCancellableCoroutine<Unit> { cont ->
        setRemoteDescription(sdp) { error ->
            if (error == null) cont.resume(Unit) else cont.resumeWithException(
                RuntimeException(
                    error.description
                )
            )
        }
    }
}