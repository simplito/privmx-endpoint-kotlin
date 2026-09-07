package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import org.webrtc.MediaConstraints
import org.webrtc.PmxFrameCryptorFactory

actual typealias PeerConnectionFactory = org.webrtc.PeerConnectionFactory

internal actual fun PeerConnectionFactory.createPeerConnection(observer: Observer): PeerConnection =
    createPeerConnection(
        org.webrtc.PeerConnection.RTCConfiguration(emptyList<org.webrtc.PeerConnection.IceServer>()),
        observer
    ) ?: throw IllegalStateException("Failed to create PeerConnection")

internal actual fun PeerConnectionFactory.disposeFactory() = dispose()

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

// AUDIO
internal actual fun PeerConnectionFactory.makeAudioSource(): AudioSource =
    createAudioSource(MediaConstraints())

internal actual fun PeerConnectionFactory.makeAudioTrack(
    id: String,
    audioSource: AudioSource?
): AudioTrack = if (audioSource == null) createAudioTrack(
    id,
    makeAudioSource()
) else createAudioTrack(id, audioSource)

// VIDEO
actual fun PeerConnectionFactory.makeVideoTrack(
    id: String,
    source: VideoSource
): VideoTrack = createVideoTrack(id, source)

actual fun PeerConnectionFactory.makeVideoSource(
    isScreenCast: Boolean,
    alignTimestamps: Boolean
): VideoSource = createVideoSource(isScreenCast, alignTimestamps)