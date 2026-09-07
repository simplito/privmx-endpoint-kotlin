package com.simplito.kotlin.privmx_endpoint_streams.webrtc

expect class PeerConnectionFactory

internal expect fun PeerConnectionFactory.createPeerConnection(observer: Observer): PeerConnection
internal expect fun PeerConnectionFactory.disposeFactory()
internal expect fun PeerConnectionFactory.createSenderFrameCryptor(
    sender: RtpSender,
    keyStore: KeyStore
): FrameCryptor

internal expect fun PeerConnectionFactory.makeAudioSource(): AudioSource
internal expect fun PeerConnectionFactory.makeAudioTrack(
    id: String,
    audioSource: AudioSource?
): AudioTrack

internal expect fun PeerConnectionFactory.makeVideoSource(
    isScreenCast: Boolean = false,
    alignTimestamps: Boolean = true
): VideoSource

internal expect fun PeerConnectionFactory.makeVideoTrack(
    id: String,
    source: VideoSource,
): VideoTrack


