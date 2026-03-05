package com.simplito.java.privmx_endpoint.modules.stream

import PeerConnectionManager
import WebRTCFramework.RTCAudioSource
import WebRTCFramework.RTCAudioTrack
import WebRTCFramework.RTCCameraVideoCapturer
import WebRTCFramework.RTCMediaConstraints
import WebRTCFramework.RTCMediaStreamTrack
import WebRTCFramework.RTCPeerConnectionFactory
import WebRTCFramework.RTCVideoSource
import WebRTCFramework.RTCVideoTrack
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.collections.emptyMap

@OptIn(ExperimentalForeignApi::class)
class TrackFactory internal constructor(pcManager: PeerConnectionManager) {

    private val factory: RTCPeerConnectionFactory = pcManager.pcFactory

    //TODO: Maybe creating sources should be hidden
    fun createVideoSource(isScreenCast: Boolean): RTCVideoSource {
        return factory.videoSourceForScreenCast(isScreenCast)
    }

//    //TODO: Maybe creating sources should be hidden
//    fun createVideoSource(isScreenCast: Boolean, alignTimestamps: Boolean): VideoSource {
//        return factory.createVideoSource(isScreenCast, alignTimestamps)
//    }

    //TODO: Maybe creating sources should be hidden
    fun createAudioSource(): RTCAudioSource {
        return factory.audioSourceWithConstraints(RTCMediaConstraints(mandatoryConstraints = emptyMap<Any?,Any>(),null))
    }

    fun createVideoTrack(
        id: String,
    ): Pair<RTCCameraVideoCapturer,RTCVideoTrack> {
        val videoSource = createVideoSource(false)
        val capturer = RTCCameraVideoCapturer(videoSource)
        return capturer to factory.videoTrackWithSource(videoSource,"video-track")
    }

    fun createAudioTrack(
        id: String,
    ): RTCAudioTrack {
        return factory.audioTrackWithSource(createAudioSource(), trackId = id)
        RTCMediaStreamTrack
    }
}