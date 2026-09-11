package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint_streams.webrtc.AudioSource
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnectionFactory
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.VideoSource
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.makeAudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.makeVideoSource
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.makeVideoTrack

class TrackFactory internal constructor(pcManager: PeerConnectionManager) {
    val factory: PeerConnectionFactory = pcManager.pcFactory

    fun createVideoSource(
        isScreenCast: Boolean = false,
        alignTimestamps: Boolean = true
    ) = factory.makeVideoSource(isScreenCast, alignTimestamps)

    fun createVideoTrack(
        id: String,
        videoSource: VideoSource
    ) = factory.makeVideoTrack(id, videoSource)

    fun createAudioTrack(
        id: String,
        audioSource: AudioSource? = null
    ) = factory.makeAudioTrack(id, audioSource)
}