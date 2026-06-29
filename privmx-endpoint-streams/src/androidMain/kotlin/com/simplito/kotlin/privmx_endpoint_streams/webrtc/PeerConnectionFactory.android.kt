package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import org.webrtc.MediaConstraints
import org.webrtc.PmxFrameCryptorFactory


actual typealias PeerConnectionFactory = org.webrtc.PeerConnectionFactory

internal actual fun PeerConnectionFactory.createPeerConnection(observer: Observer): PeerConnection =
    createPeerConnection(
        org.webrtc.PeerConnection.RTCConfiguration(emptyList<org.webrtc.PeerConnection.IceServer>()),
        observer
    ) ?: throw IllegalStateException("Failed to create PeerConnection") // todo - zmienic message

internal actual fun PeerConnectionFactory.disposeFactory() = dispose()

internal actual fun PeerConnectionFactory.makeVideoTrack(
    id: String,
    isScreenCast: Boolean,
    alignTimestamps: Boolean
): VideoTrack = createVideoTrack(id, createVideoSource(isScreenCast, alignTimestamps))

internal actual fun PeerConnectionFactory.makeAudioTrack(id: String): AudioTrack =
    createAudioTrack(id, createAudioSource(MediaConstraints()))

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