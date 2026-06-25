@file:OptIn(ExperimentalForeignApi::class)

package webrtc

import WebRTCFramework.PMXFrameCryptorTransformer
import WebRTCFramework.RTCConfiguration
import WebRTCFramework.RTCPeerConnectionFactory
import kotlinx.cinterop.ExperimentalForeignApi

actual typealias PeerConnectionFactory = RTCPeerConnectionFactory

internal actual fun PeerConnectionFactory.createPeerConnection(observer: Observer): PeerConnection =
    // TODO(iOS): zweryfikuj sygnaturę peerConnectionWithConfiguration:constraints:delegate:
    peerConnectionWithConfiguration(RTCConfiguration(), mediaConstraints(), observer)
        ?: throw IllegalStateException("Failed to create PeerConnection")

internal actual fun PeerConnectionFactory.disposeFactory() {}

internal actual fun PeerConnectionFactory.makeVideoTrack(
    id: String,
    isScreenCast: Boolean,
    alignTimestamps: Boolean  // TODO(iOS): brak odpowiednika alignTimestamps
): VideoTrack = videoTrackWithSource(videoSourceForScreenCast(isScreenCast), id)

internal actual fun PeerConnectionFactory.makeAudioTrack(id: String): AudioTrack =
    audioTrackWithSource(audioSourceWithConstraints(mediaConstraints()), id)

internal actual fun PeerConnectionFactory.createSenderFrameCryptor(
    sender: RtpSender,
    keyStore: KeyStore
): FrameCryptor =
    // TODO(iOS): zweryfikuj inicjalizator forRtpSender (analogicznie do forRtpReceiver w PcObserver)
    PMXFrameCryptorTransformer(
        forRtpSender = sender,
        withPeerConnectionFactory = this,
        keyStore,
        null
    )