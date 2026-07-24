package com.simplito.kotlin.privmx_endpoint_streams.webrtc

expect class PeerConnection

expect enum class PeerConnectionState {
    NEW, CONNECTING, CONNECTED, DISCONNECTED, FAILED, CLOSED
}

internal expect val PeerConnection.peerConnectionState: PeerConnectionState
internal expect fun PeerConnection.applyIceServers(iceServers: List<IceServer>)
internal expect fun PeerConnection.disposeConnection()
internal expect fun PeerConnection.addTrack(track: MediaStreamTrack): RtpSender
internal expect fun PeerConnection.removeTrack(sender: RtpSender)
internal expect suspend fun PeerConnection.createOffer(): SessionDescription
internal expect suspend fun PeerConnection.createAnswer(): SessionDescription
internal expect suspend fun PeerConnection.setLocalDescription(sdp: SessionDescription)
internal expect suspend fun PeerConnection.setRemoteDescription(sdp: SessionDescription)
internal expect suspend fun PeerConnection.getStats(): StatisticsReport
