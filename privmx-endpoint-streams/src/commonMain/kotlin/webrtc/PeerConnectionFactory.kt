package webrtc

expect class PeerConnectionFactory

internal expect fun PeerConnectionFactory.createPeerConnection(observer: Observer): PeerConnection
internal expect fun PeerConnectionFactory.disposeFactory()
internal expect fun PeerConnectionFactory.makeVideoTrack(
    id: String,
    isScreenCast: Boolean,
    alignTimestamps: Boolean = true
): VideoTrack

internal expect fun PeerConnectionFactory.makeAudioTrack(id: String): AudioTrack
internal expect fun PeerConnectionFactory.createSenderFrameCryptor(
    sender: RtpSender,
    keyStore: KeyStore
): FrameCryptor