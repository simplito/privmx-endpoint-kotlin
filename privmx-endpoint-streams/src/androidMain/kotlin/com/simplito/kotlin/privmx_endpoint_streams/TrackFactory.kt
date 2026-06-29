package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint_streams.webrtc.makeAudioSource
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.makeAudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.makeVideoSource
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.makeVideoTrack
import org.webrtc.AudioSource
import org.webrtc.VideoSource

fun TrackFactory.createVideoSource(
    isScreenCast: Boolean,
    alignTimestamps: Boolean
): VideoSource = factory.makeVideoSource(isScreenCast, alignTimestamps)

fun TrackFactory.createAudioSource() : AudioSource = factory.makeAudioSource()

fun TrackFactory.createVideoTrack(
    id: String,
    videoSource: VideoSource
) = factory.makeVideoTrack(id, videoSource)

fun TrackFactory.createAudioTrack(
    id: String,
    audioSource: AudioSource
) = factory.makeAudioTrack(id, audioSource)