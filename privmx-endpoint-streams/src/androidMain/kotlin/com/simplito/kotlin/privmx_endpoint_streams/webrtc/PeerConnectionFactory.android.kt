package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import org.webrtc.AudioSource
import org.webrtc.MediaConstraints
import org.webrtc.PmxFrameCryptorFactory
import org.webrtc.VideoSource

actual typealias PeerConnectionFactory = org.webrtc.PeerConnectionFactory

internal actual fun PeerConnectionFactory.createPeerConnection(observer: Observer): PeerConnection =
    createPeerConnection(
        org.webrtc.PeerConnection.RTCConfiguration(emptyList<org.webrtc.PeerConnection.IceServer>()),
        observer
    ) ?: throw IllegalStateException("Failed to create PeerConnection")

internal actual fun PeerConnectionFactory.disposeFactory() = dispose()

internal actual fun PeerConnectionFactory.makeVideoTrack(
    id: String,
    isScreenCast: Boolean,
    alignTimestamps: Boolean
): VideoTrack = createVideoTrack(id, makeVideoSource(isScreenCast, alignTimestamps))

internal actual fun PeerConnectionFactory.makeAudioTrack(id: String): AudioTrack =
    createAudioTrack(id, makeAudioSource())

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

internal fun PeerConnectionFactory.makeAudioSource(
): AudioSource = createAudioSource(MediaConstraints())

internal fun PeerConnectionFactory.makeVideoSource(
    isScreenCast: Boolean,
    alignTimestamps: Boolean
): VideoSource = createVideoSource(isScreenCast, alignTimestamps)

internal fun PeerConnectionFactory.makeVideoTrack(
    id: String,
    videoSource: VideoSource
): VideoTrack = createVideoTrack(id, videoSource)

internal fun PeerConnectionFactory.makeAudioTrack(
    id: String,
    audioSource: AudioSource
): AudioTrack =
    createAudioTrack(id, audioSource)