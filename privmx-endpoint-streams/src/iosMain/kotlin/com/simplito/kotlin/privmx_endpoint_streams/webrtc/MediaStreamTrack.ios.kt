@file:OptIn(ExperimentalForeignApi::class)

package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import WebRTCFramework.RTCAudioTrack
import WebRTCFramework.RTCMediaStreamTrack
import WebRTCFramework.RTCMediaStreamTrackState
import WebRTCFramework.RTCVideoTrack
import kotlinx.cinterop.ExperimentalForeignApi

actual typealias MediaStreamTrack = RTCMediaStreamTrack
actual typealias VideoTrack = RTCVideoTrack
actual typealias AudioTrack = RTCAudioTrack

actual val MediaStreamTrack.trackId: String get() = trackId
actual val MediaStreamTrack.kind: String get() = kind
actual var MediaStreamTrack.isEnabled: Boolean
    set(value) {
        isEnabled = value
    }
    get() = isEnabled

actual val MediaStreamTrack.state: TrackState
    get() = when (readyState) {
        RTCMediaStreamTrackState.RTCMediaStreamTrackStateLive -> TrackState.LIVE
        else -> TrackState.ENDED
    }