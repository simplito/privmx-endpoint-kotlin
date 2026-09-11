package Stacks.Kotlin.streams

import com.simplito.kotlin.privmx_endpoint_streams.createAudioSource
import com.simplito.kotlin.privmx_endpoint_streams.createAudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.createVideoSource
import com.simplito.kotlin.privmx_endpoint_streams.createVideoTrack

actual fun createVideoTrack() {
    val videoSource = streamApi.trackFactory.createVideoSource(
        isScreenCast = false,
        alignTimestamps = true
    )

    // Set up a camera capturer with stock WebRTC (org.webrtc):
    //  1. Pick a camera (e.g. using Camera2Enumerator.deviceNames)
    //  2. Create a VideoCapturer for that camera.
    //  3. Create a SurfaceTextureHelper on eglBase context.
    //  4. Initialize the capturer with videoSource.capturerObserver,
    //     this feeds frames into the source.
    //  5. Start capturing.
    // Keep references to the capturer and helper so you can release them later.

    val videoTrack = streamApi.trackFactory.createVideoTrack("video0", videoSource)
}

actual fun createAudioTrack() {
    // No manual capturer needed here - WebRTC captures from the default microphone.
    // Selecting a specific input device is possible, but not needed for this minimal setup.
    val audioSource = streamApi.trackFactory.createAudioSource()
    val audioTrack = streamApi.trackFactory.createAudioTrack("audio0", audioSource)
}