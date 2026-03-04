package com.simplito.java.privmx_endpoint.modules.stream

import TrackObserver
import WebRTCFramework.PMXFrameCryptorTransformer
import WebRTCFramework.PMXKeyStore
import WebRTCFramework.RTCDataChannel
import WebRTCFramework.RTCIceCandidate
import WebRTCFramework.RTCIceConnectionState
import WebRTCFramework.RTCIceGatheringState
import WebRTCFramework.RTCMediaStream
import WebRTCFramework.RTCPeerConnection
import WebRTCFramework.RTCPeerConnectionDelegateProtocol
import WebRTCFramework.RTCPeerConnectionFactory
import WebRTCFramework.RTCRtpReceiver
import WebRTCFramework.RTCSignalingState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.darwin.NSObject

//TODO: Fix warnings
@OptIn(ExperimentalForeignApi::class)
class PcObserver constructor(
    private val peerConnectionFactory: RTCPeerConnectionFactory,
    private val keyStore: PMXKeyStore,
    private val trackObserver: TrackObserver?,
    private val onIceCandidate: (candidate: RTCIceCandidate) -> Unit,
    private val onIceConnectionChange: (candidate: RTCIceConnectionState) -> Unit,
) : RTCPeerConnectionDelegateProtocol, NSObject() {
    var frameCryptorMap: MutableMap<String, PMXFrameCryptorTransformer> = mutableMapOf()
    private val streamIdsByTracks: Map<String, String> = HashMap()

//    @Override
//    fun onSignalingChange(signalingState: PeerConnection.SignalingState?) {
//    }
//
//    @Override
//    fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState?) {
//    }
//
//    @Override
//    fun onIceConnectionReceivingChange(b: Boolean) {
//    }
//
//    @Override
//    fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState?) {
//    }
//
//    @Override
//    fun onIceCandidate(iceCandidate: IceCandidate?) {
//
//    }
//
//    @Override
//    fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate?>?) {
//    }
//
//    @Override
//    fun onAddStream(mediaStream: MediaStream?) {
//    }
//
//    @Override
//    fun onRemoveStream(mediaStream: MediaStream?) {
//    }
//
//    @Override
//    fun onDataChannel(dataChannel: DataChannel?) {
//    }
//
//    @Override
//    fun onRenegotiationNeeded() {
//    }
//
//    @Override
//    fun onAddTrack(receiver: RtpReceiver, mediaStreams: Array<MediaStream?>) {
//
//    }
//
//    @Override
//    fun onTrack(transceiver: RtpTransceiver) {
//        val rtpReceiver: RtpReceiver = transceiver.getReceiver()
//        val track: MediaStreamTrack? = rtpReceiver.track()
//        if (peerConnectionFactory != null && track != null && track.id() != null) {
//            PmxFrameCryptorFactory.createPmxFrameCryptorForRtpReceiver(peerConnectionFactory, rtpReceiver, keyStore)
//            frameCryptorMap.put(
//                track.id(),
//                PmxFrameCryptorFactory.createPmxFrameCryptorForRtpReceiver(
//                    peerConnectionFactory,
//                    rtpReceiver,
//                    keyStore
//                )
//            )
//            if (trackObserver != null) {
//                val streamId = streamIdsByTracks.get(track.id())
//                if (streamId != null) {
//                    trackObserver.OnRemoteTrack(streamId, track)
//                }
//            }
//        }
//    }
//
//    @Override
//    fun onRemoveTrack(receiver: RtpReceiver) {
//        //TODO: cleanup track cryptors (?)
////        onRemoveTrack.accept(receiver.track());
//        receiver.dispose()
//    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeSignalingState: RTCSignalingState
    ) {
//        TODO("Not yet implemented")
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didAddReceiver: RTCRtpReceiver,
        streams: List<*>
    ) {

        val track = didAddReceiver.track()
        track?.let { track ->
            val cryptor = PMXFrameCryptorTransformer(
                forRtpReceiver = didAddReceiver,
                withPeerConnectionFactory = peerConnectionFactory,
                keyStore
            )
            frameCryptorMap[track.trackId()] = cryptor
            trackObserver?.onRemoteTrack(
                (streams.firstOrNull() as? RTCMediaStream)?.streamId,
                track
            )
        }
    }

    @ObjCSignatureOverride
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didAddStream: RTCMediaStream
    ) {
//        TODO("Not yet implemented")
    }

    @ObjCSignatureOverride
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveStream: RTCMediaStream
    ) {
//        TODO("Not yet implemented")
    }

    override fun peerConnectionShouldNegotiate(peerConnection: RTCPeerConnection) {
//        TODO("Not yet implemented")
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeIceConnectionState: RTCIceConnectionState
    ) {
        onIceConnectionChange(didChangeIceConnectionState)
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeIceGatheringState: RTCIceGatheringState
    ) {
//        TODO("Not yet implemented")
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didGenerateIceCandidate: RTCIceCandidate
    ) {
        onIceCandidate(didGenerateIceCandidate)
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveIceCandidates: List<*>
    ) {
//        TODO("Not yet implemented")
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didOpenDataChannel: RTCDataChannel
    ) {
//        TODO("Not yet implemented")
    }

//    fun setFrameCryptorOptions(options: Pm.PmxFrameCryptorOptions?) {
//        frameCryptorMap.forEach({ k, v -> v.setOptions(options) })
//    }
}
