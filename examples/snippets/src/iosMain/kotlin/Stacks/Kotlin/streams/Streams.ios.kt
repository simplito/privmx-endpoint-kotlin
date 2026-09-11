@file:OptIn(ExperimentalForeignApi::class)

package Stacks.Kotlin.streams

import com.simplito.kotlin.privmx_endpoint_streams.createAudioSource
import com.simplito.kotlin.privmx_endpoint_streams.createAudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.createVideoSource
import com.simplito.kotlin.privmx_endpoint_streams.createVideoTrack
import kotlinx.cinterop.ExperimentalForeignApi

actual fun createVideoTrack() {
    val videoSource = streamApi.trackFactory.createVideoSource(isScreenCast = false)

    // Set up a camera capturer with stock WebRTC (WebRTCFramework):
    //  1. Create an RTCCameraVideoCapturer with videoSource as its delegate,
    //     this is what feeds captured frames into the source.
    //  2. Pick a camera (e.g. using RTCCameraVideoCapturer.captureDevices())
    //  3. Start capturing.
    // Keep a reference to the capturer so you can stop and release it later.

    val videoTrack = streamApi.trackFactory.createVideoTrack("video0", videoSource)
}

actual fun createAudioTrack() {
    // No manual capturer needed here – WebRTC captures from the default microphone.
    // Selecting a specific input device is possible, but not needed for this minimal setup.
    val audioSource = streamApi.trackFactory.createAudioSource()
    val audioTrack = streamApi.trackFactory.createAudioTrack("audio0", audioSource)
}