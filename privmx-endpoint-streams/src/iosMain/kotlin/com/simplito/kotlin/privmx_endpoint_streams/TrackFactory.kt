@file:OptIn(ExperimentalForeignApi::class)

package com.simplito.kotlin.privmx_endpoint_streams

import WebRTCFramework.RTCAudioSource
import WebRTCFramework.RTCVideoSource
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.makeAudioSource
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.makeAudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.makeVideoSource
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.makeVideoTrack
import kotlinx.cinterop.ExperimentalForeignApi

fun TrackFactory.createVideoSource(
    isScreenCast: Boolean
): RTCVideoSource = factory.makeVideoSource(isScreenCast)

fun TrackFactory.createAudioSource() : RTCAudioSource = factory.makeAudioSource()

fun TrackFactory.createVideoTrack(
    id: String,
    videoSource: RTCVideoSource
) = factory.makeVideoTrack(id, videoSource)

fun TrackFactory.createAudioTrack(
    id: String,
    audioSource: RTCAudioSource
) = factory.makeAudioTrack(id, audioSource)