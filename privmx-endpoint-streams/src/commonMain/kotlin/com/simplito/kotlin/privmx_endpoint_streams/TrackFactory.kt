package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint_streams.webrtc.AudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnectionFactory
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.VideoTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.makeAudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.makeVideoTrack

class TrackFactory internal constructor(pcManager: PeerConnectionManager) {

    val factory: PeerConnectionFactory = pcManager.pcFactory

    fun createVideoTrack(
        id: String,
        isScreenCast: Boolean = false,
        alignTimestamps: Boolean = true
    ): VideoTrack = factory.makeVideoTrack(id, isScreenCast, alignTimestamps)

    fun createAudioTrack(id: String): AudioTrack = factory.makeAudioTrack(id)
}
