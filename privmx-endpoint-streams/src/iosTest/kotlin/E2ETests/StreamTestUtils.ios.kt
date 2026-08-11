@file:OptIn(ExperimentalForeignApi::class)

package E2ETests

import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import com.simplito.kotlin.privmx_endpoint_streams.StreamApi
import com.simplito.kotlin.privmx_endpoint_streams.StreamApiInit
import com.simplito.kotlin.privmx_endpoint_streams.createAudioSource
import com.simplito.kotlin.privmx_endpoint_streams.createAudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.createVideoSource
import com.simplito.kotlin.privmx_endpoint_streams.createVideoTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.AudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.VideoTrack
import kotlinx.cinterop.ExperimentalForeignApi

actual fun createStreamApiInit(): StreamApiInit = StreamApiInit()

actual fun addFakeVideoTrackToStream(
    streamApi: StreamApi,
    streamHandle: StreamHandle
): VideoTrack {
    val trackFactory = streamApi.trackFactory
    val videoSource = trackFactory.createVideoSource(isScreenCast = false)
    val videoTrack = trackFactory.createVideoTrack("fake_video_track", videoSource)
    videoTrack.isEnabled = true

    streamApi.addTrack(streamHandle, videoTrack)
    return videoTrack
}

actual fun addFakeAudioTrackToStream(
    streamApi: StreamApi,
    streamHandle: StreamHandle
): AudioTrack {
    val trackFactory = streamApi.trackFactory
    val audioSource = trackFactory.createAudioSource()
    val audioTrack = trackFactory.createAudioTrack("fake_audio_track", audioSource)
    audioTrack.isEnabled = true

    streamApi.addTrack(streamHandle, audioTrack)
    return audioTrack
}