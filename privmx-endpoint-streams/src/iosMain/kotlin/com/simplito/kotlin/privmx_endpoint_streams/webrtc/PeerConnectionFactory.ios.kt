@file:OptIn(ExperimentalForeignApi::class)

package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import WebRTCFramework.PMXAudioLevelAnalyzer
import WebRTCFramework.PMXFrameCryptorTransformer
import WebRTCFramework.RTCAudioSource
import WebRTCFramework.RTCConfiguration
import WebRTCFramework.RTCMediaConstraints
import WebRTCFramework.RTCPeerConnectionFactory
import WebRTCFramework.RTCVideoSource
import kotlinx.cinterop.ExperimentalForeignApi

actual typealias PeerConnectionFactory = RTCPeerConnectionFactory

internal actual fun PeerConnectionFactory.createPeerConnection(observer: Observer): PeerConnection =
    peerConnectionWithConfiguration(RTCConfiguration(), mediaConstraints(), observer)
        ?: throw IllegalStateException("Failed to create PeerConnection")

internal actual fun PeerConnectionFactory.disposeFactory() {}

internal actual fun PeerConnectionFactory.makeVideoTrack(
    id: String,
    isScreenCast: Boolean,
    alignTimestamps: Boolean
): VideoTrack = videoTrackWithSource(makeVideoSource(isScreenCast), id)

internal actual fun PeerConnectionFactory.makeAudioTrack(id: String): AudioTrack =
    audioTrackWithSource(makeAudioSource(), id)

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

internal fun PeerConnectionFactory.makeVideoTrack(
    id: String,
    videoSource: RTCVideoSource
): VideoTrack = videoTrackWithSource(videoSource, id)

internal fun PeerConnectionFactory.makeAudioTrack(
    id: String,
    audioSource: RTCAudioSource
): AudioTrack =
    audioTrackWithSource(audioSource, id)

internal fun PeerConnectionFactory.makeAudioSource(
    mediaConstant: RTCMediaConstraints = mediaConstraints()
): RTCAudioSource = audioSourceWithConstraints(mediaConstant)

internal fun PeerConnectionFactory.makeVideoSource(
    isScreenCast: Boolean
): RTCVideoSource = videoSourceForScreenCast(isScreenCast)
