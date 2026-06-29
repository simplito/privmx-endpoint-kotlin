package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.AudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.AudioTrackInfo
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.IceConnectionState
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.KeyStore
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnectionFactory
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PmxFrameCryptorOptions
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.SessionDescription
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.VideoTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.VideoTrackInfo
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.addTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.applyOptions
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.createOffer
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.createSenderFrameCryptor
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.disposeCryptor
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.fromCanonicalForm
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.removeTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.sdp
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.sessionDescription
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.setLocalDescription
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.setRemoteDescription
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.trackId
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import webrtc.AudioTrack
import webrtc.AudioTrackInfo
import webrtc.IceConnectionState
import webrtc.KeyStore
import webrtc.PeerConnectionFactory
import webrtc.PmxFrameCryptorOptions
import webrtc.SessionDescription
import webrtc.VideoTrack
import webrtc.VideoTrackInfo
import webrtc.addTrack
import webrtc.applyOptions
import webrtc.createOffer
import webrtc.createSenderFrameCryptor
import webrtc.disposeCryptor
import webrtc.removeTrack
import webrtc.sdp
import webrtc.sessionDescription
import webrtc.setLocalDescription
import webrtc.setRemoteDescription
import webrtc.trackId
import kotlin.coroutines.EmptyCoroutineContext

class JanusPublisher(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: KeyStore,
    trackObserver: TrackObserver?,
    onTrickle: (Long, String) -> Unit,
    private val acceptOfferOnReconfigure: (Long, SdpWithTypeModel) -> Unit = { _, _ -> },
    onConnectionChange: (IceConnectionState) -> Unit = {}
) : JanusConnection(peerConnectionFactory, keyStore, trackObserver, onTrickle, onConnectionChange) {

    val audioTracks: MutableMap<String, AudioTrackInfo> = mutableMapOf()
    val videoTracks: MutableMap<String, VideoTrackInfo> = mutableMapOf()

    fun addAudioTrack(audioTrack: AudioTrack) {
        val sender = peerConnection.addTrack(audioTrack)
        val cryptor = peerConnectionFactory.createSenderFrameCryptor(sender, keyStore)
        audioTracks[audioTrack.trackId] = AudioTrackInfo(audioTrack, sender, cryptor)
    }

    fun addVideoTrack(videoTrack: VideoTrack) {
        val sender = peerConnection.addTrack(videoTrack)
        val cryptor = peerConnectionFactory.createSenderFrameCryptor(sender, keyStore)
        videoTracks[videoTrack.trackId] = VideoTrackInfo(videoTrack, sender, cryptor)
    }

    fun removeAudioTrack(id: String) {
        val info = audioTracks.remove(id) ?: return
        peerConnection.removeTrack(info.sender)
        info.frameCryptor.disposeCryptor()
    }

    fun removeVideoTrack(id: String) {
        val info = videoTracks.remove(id) ?: return
        peerConnection.removeTrack(info.sender)
        info.frameCryptor.disposeCryptor()
    }

    suspend fun createOffer(): String = configurationMutex.withLock {
        val offer = peerConnection.createOffer()
        peerConnection.setLocalDescription(offer)
        offer.sdp
    }

    suspend fun setAnswer(sdp: String?, type: String): SessionDescription =
        configurationMutex.withLock {
            sessionDescription(type, sdp ?: "").also { peerConnection.setRemoteDescription(it) }
        }

    override fun setFrameCryptorOptions(options: PmxFrameCryptorOptions) {
        audioTracks.values.forEach { it.frameCryptor.applyOptions(options) }
        videoTracks.values.forEach { it.frameCryptor.applyOptions(options) }
    }

    override fun onRenegotiationNeeded() {
        if (sessionId > -1) {
            val offer = runBlocking(EmptyCoroutineContext) { createOffer() }
            acceptOfferOnReconfigure(sessionId, SdpWithTypeModel(offer, "offer"))


        }
    }

    override fun close() {
        runCatching {
            audioTracks.values.forEach { it.frameCryptor.disposeCryptor() }
            videoTracks.values.forEach { it.frameCryptor.disposeCryptor() }
        }
        audioTracks.clear()
        videoTracks.clear()
        super.close()
    }
}