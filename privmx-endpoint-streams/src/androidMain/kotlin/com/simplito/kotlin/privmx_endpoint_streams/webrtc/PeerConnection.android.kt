package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import kotlinx.coroutines.suspendCancellableCoroutine
import org.webrtc.MediaConstraints
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


actual typealias PeerConnection = org.webrtc.PeerConnection

actual enum class PeerConnectionState(internal val platform: org.webrtc.PeerConnection.PeerConnectionState) {
    NEW(org.webrtc.PeerConnection.PeerConnectionState.NEW),
    CONNECTING(org.webrtc.PeerConnection.PeerConnectionState.CONNECTING),
    CONNECTED(org.webrtc.PeerConnection.PeerConnectionState.CONNECTED),
    DISCONNECTED(org.webrtc.PeerConnection.PeerConnectionState.DISCONNECTED),
    FAILED(org.webrtc.PeerConnection.PeerConnectionState.FAILED),
    CLOSED(org.webrtc.PeerConnection.PeerConnectionState.CLOSED)
}

internal fun org.webrtc.PeerConnection.PeerConnectionState.toCommon(): PeerConnectionState =
    PeerConnectionState.entries.first { it.platform == this }

internal actual val PeerConnection.peerConnectionState: PeerConnectionState
    get() = connectionState().toCommon()

internal actual fun PeerConnection.applyIceServers(iceServers: List<IceServer>) {
    setConfiguration(org.webrtc.PeerConnection.RTCConfiguration(iceServers))
}

internal actual fun PeerConnection.disposeConnection() = dispose()

internal actual fun PeerConnection.addTrack(track: MediaStreamTrack): RtpSender = addTrack(track)

internal actual fun PeerConnection.removeTrack(sender: RtpSender) {
    removeTrack(sender)
}

internal actual suspend fun PeerConnection.createOffer(): SessionDescription =
    suspendCancellableCoroutine { cont -> createOffer(SdpObserver(cont), MediaConstraints()) }!!

internal actual suspend fun PeerConnection.createAnswer(): SessionDescription =
    suspendCancellableCoroutine { cont -> createAnswer(SdpObserver(cont), MediaConstraints()) }!!

internal actual suspend fun PeerConnection.setLocalDescription(sdp: SessionDescription) {
    suspendCancellableCoroutine<SessionDescription?> { cont ->
        setLocalDescription(
            SdpObserver(cont),
            sdp
        )
    }
}

internal actual suspend fun PeerConnection.setRemoteDescription(sdp: SessionDescription) {
    suspendCancellableCoroutine<SessionDescription?> { cont ->
        setRemoteDescription(
            SdpObserver(cont),
            sdp
        )
    }
}

private class SdpObserver(
    private val cont: Continuation<SessionDescription?>
) : org.webrtc.SdpObserver {
    override fun onCreateSuccess(sdp: SessionDescription) = cont.resume(sdp)
    override fun onSetSuccess() = cont.resume(null)
    override fun onCreateFailure(s: String?) = cont.resumeWithException(RuntimeException(s))
    override fun onSetFailure(s: String?) = cont.resumeWithException(RuntimeException(s))
}

internal actual suspend fun PeerConnection.getStats(): StatisticsReport = suspendCancellableCoroutine { continuation ->
    getStats{
        if (it == null) continuation.resumeWithException(RuntimeException("Unknown exception during getting stats"))
        else continuation.resume(it)
    }
}
