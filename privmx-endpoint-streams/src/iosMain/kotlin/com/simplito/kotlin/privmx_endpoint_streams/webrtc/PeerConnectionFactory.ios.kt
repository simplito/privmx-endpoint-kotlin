@file:OptIn(ExperimentalForeignApi::class)

package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import WebRTCFramework.PMXAudioLevelAnalyzer
import WebRTCFramework.PMXFrameCryptorTransformer
import WebRTCFramework.RTCConfiguration
import WebRTCFramework.RTCPeerConnectionFactory
import kotlinx.cinterop.ExperimentalForeignApi

actual typealias PeerConnectionFactory = RTCPeerConnectionFactory

internal actual fun PeerConnectionFactory.createPeerConnection(observer: Observer): PeerConnection =
    peerConnectionWithConfiguration(RTCConfiguration(), mediaConstraints(), observer)
        ?: throw IllegalStateException("Failed to create PeerConnection")

internal actual fun PeerConnectionFactory.disposeFactory() {}

internal actual fun PeerConnectionFactory.createSenderFrameCryptor(
    sender: RtpSender,
    keyStore: KeyStore
): FrameCryptor =
    PMXFrameCryptorTransformer(
        forRtpSender = sender,
        withPeerConnectionFactory = this,
        keyStore,
        PMXAudioLevelAnalyzer()
    )

// AUDIO
internal actual fun PeerConnectionFactory.makeAudioSource(): AudioSource =
    audioSourceWithConstraints(mediaConstraints())

internal actual fun PeerConnectionFactory.makeAudioTrack(
    id: String,
    audioSource: AudioSource?
): AudioTrack = if (audioSource == null) audioTrackWithSource(
    makeAudioSource(),
    id
) else audioTrackWithSource(audioSource, id)

// VIDEO
internal actual fun PeerConnectionFactory.makeVideoTrack(
    id: String,
    source: VideoSource
): VideoTrack = videoTrackWithSource(source, id)

internal actual fun PeerConnectionFactory.makeVideoSource(
    isScreenCast: Boolean,
    alignTimestamps: Boolean
): VideoSource = videoSourceForScreenCast(isScreenCast)

