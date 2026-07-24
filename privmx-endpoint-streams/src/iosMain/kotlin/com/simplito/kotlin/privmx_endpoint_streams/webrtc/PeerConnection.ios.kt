@file:OptIn(ExperimentalForeignApi::class)

package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import WebRTCFramework.RTCConfiguration
import WebRTCFramework.RTCPeerConnection
import WebRTCFramework.RTCPeerConnectionState
import WebRTCFramework.statisticsWithCompletionHandler
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual typealias PeerConnection = RTCPeerConnection

actual enum class PeerConnectionState(internal val platform: RTCPeerConnectionState) {
    NEW(RTCPeerConnectionState.RTCPeerConnectionStateNew),
    CONNECTING(RTCPeerConnectionState.RTCPeerConnectionStateConnecting),
    CONNECTED(RTCPeerConnectionState.RTCPeerConnectionStateConnected),
    DISCONNECTED(RTCPeerConnectionState.RTCPeerConnectionStateDisconnected),
    FAILED(RTCPeerConnectionState.RTCPeerConnectionStateFailed),
    CLOSED(RTCPeerConnectionState.RTCPeerConnectionStateClosed)
}

internal fun RTCPeerConnectionState.toCommon(): PeerConnectionState =
    PeerConnectionState.entries.first { it.platform == this }

internal actual val PeerConnection.peerConnectionState: PeerConnectionState
    get() = connectionState().toCommon()

internal actual fun PeerConnection.applyIceServers(iceServers: List<IceServer>) {
    setConfiguration(RTCConfiguration().also { it.setIceServers(iceServers) })
}

internal actual fun PeerConnection.disposeConnection() = close()

internal actual fun PeerConnection.addTrack(track: MediaStreamTrack): RtpSender =
    addTrack(track, emptyList<Any>())
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

internal actual suspend fun PeerConnection.getStats(): StatisticsReport = suspendCancellableCoroutine { continuation ->
    statisticsWithCompletionHandler {
        if (it == null) continuation.resumeWithException(RuntimeException("Unknown exception during getting stats"))
        else continuation.resume(it)
    }
}