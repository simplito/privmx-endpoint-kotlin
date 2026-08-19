package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint_streams.webrtc.toCommon
import org.webrtc.DataChannel
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PmxFrameCryptor
import org.webrtc.PmxFrameCryptorFactory
import org.webrtc.RtpReceiver
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.IceCandidate
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.IceConnectionState
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.KeyStore
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.Observer
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnectionFactory
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PmxFrameCryptorOptions

actual class PcObserver internal actual constructor(
    private val peerConnectionFactory: PeerConnectionFactory,
    private val keyStore: KeyStore,
    private val roomId: String,
    private val dataChannelCryptoProvider: InternalDataChannelMessageCryptoProvider,
    private val remoteStreamObserver: RemoteStreamObserver?,
    private val onIceCandidateCallback: (candidate: IceCandidate) -> Unit,
    private val onRenegotiationNeededCallback: () -> Unit,
    private val onIceConnectionChangeCallback: (state: IceConnectionState) -> Unit,
) : Observer {
    private val frameCryptorMap = mutableMapOf<String, PmxFrameCryptor>()

    actual fun setFrameCryptorOptions(options: PmxFrameCryptorOptions) {
        frameCryptorMap.values.forEach { it.setOptions(options) }
    }

    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) =
        onIceConnectionChangeCallback(state.toCommon())

    override fun onIceCandidate(p0: IceCandidate) = onIceCandidateCallback(p0)

    override fun onRenegotiationNeeded() = onRenegotiationNeededCallback()

    override fun onAddTrack(receiver: RtpReceiver, mediaStreams: Array<MediaStream>) {
        val track = receiver.track() ?: return
        val trackId = track.id() ?: return

        frameCryptorMap[trackId] = PmxFrameCryptorFactory.createPmxFrameCryptorForRtpReceiver(
            peerConnectionFactory, receiver, keyStore, null
        )
        remoteStreamObserver?.onTrack(mediaStreams.firstOrNull()?.id!!, track)
    }

    override fun onRemoveTrack(receiver: RtpReceiver) {
        receiver.track()?.id()?.let { frameCryptorMap.remove(it)?.dispose() }
        receiver.dispose()
    }

    actual fun dispose() {
        frameCryptorMap.values.forEach { it.dispose() }
        frameCryptorMap.clear()
    }

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
    override fun onIceConnectionReceivingChange(p0: Boolean) {}
    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate?>?) {}
    override fun onAddStream(p0: MediaStream?) {}
    override fun onRemoveStream(p0: MediaStream?) {}
    override fun onDataChannel(p0: DataChannel?) {
        p0?.registerDataChannel(roomId,dataChannelCryptoProvider){ remoteStreamObserver }
    }
}