package E2ETests


import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import com.simplito.kotlin.privmx_endpoint_streams.StreamApi
import com.simplito.kotlin.privmx_endpoint_streams.StreamApiInit
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.AudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.VideoTrack
import org.webrtc.AudioSource
import org.webrtc.Camera2Enumerator
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import kotlin.test.fail

val appContext: Context = ApplicationProvider.getApplicationContext()

val eglBase: EglBase by lazy {
    EglBase.create()
}

/** Resources created per fake video track, released in [releaseFakeTracks]. */
private class FakeVideoTrackResources(
    val capturer: VideoCapturer,
    val helper: SurfaceTextureHelper,
    val source: VideoSource,
    val track: VideoTrack,
)

/** Resources created per fake audio track, released in [releaseFakeTracks]. */
private class FakeAudioTrackResources(
    val source: AudioSource,
    val track: AudioTrack,
)

private val fakeVideoTracks = mutableListOf<FakeVideoTrackResources>()
private val fakeAudioTracks = mutableListOf<FakeAudioTrackResources>()

actual fun createStreamApiInit(): StreamApiInit {
    return StreamApiInit(
        appContext,
        eglBase
    )
}

actual fun addFakeAudioTrackToStream(
    streamApi: StreamApi,
    streamHandle: StreamHandle,
): AudioTrack {
    val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "false"))
        mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "false"))
        mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "false"))
        mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "false"))
    }

    val factory = streamApi.trackFactory.factory
    val audioSource = factory.createAudioSource(constraints)
    val audioTrack = factory.createAudioTrack("fake_audio_track", audioSource)
    audioTrack.setEnabled(true)
    fakeAudioTracks += FakeAudioTrackResources(audioSource, audioTrack)

    streamApi.addTrack(streamHandle, audioTrack)
    return audioTrack
}

actual fun addFakeVideoTrackToStream(
    streamApi: StreamApi,
    streamHandle: StreamHandle,
): VideoTrack {
    val factory = streamApi.trackFactory.factory
    val enumerator = Camera2Enumerator(appContext)
    val deviceNames = enumerator.deviceNames
    if (deviceNames.isEmpty()) fail("No camera available on this device")
    val capturer = enumerator.createCapturer(deviceNames[0], null)
    val helper = SurfaceTextureHelper.create(
        "FakeCaptureThread",
        eglBase.eglBaseContext,
    )
    val videoSource = factory.createVideoSource(capturer.isScreencast)
    capturer.initialize(helper, appContext, videoSource.capturerObserver)
    capturer.startCapture(640, 480, 15)

    val videoTrack = factory.createVideoTrack("fake_video_track", videoSource)
    videoTrack.setEnabled(true)
    fakeVideoTracks += FakeVideoTrackResources(capturer, helper, videoSource, videoTrack)

    streamApi.addTrack(streamHandle, videoTrack)
    return videoTrack
}

actual fun releaseFakeTracks() {
    fakeVideoTracks.forEach { resources ->
        runCatching { resources.capturer.stopCapture() }
        runCatching { resources.capturer.dispose() }
        runCatching { resources.helper.dispose() }
        runCatching { resources.track.dispose() }
        runCatching { resources.source.dispose() }
    }
    fakeVideoTracks.clear()

    fakeAudioTracks.forEach { resources ->
        runCatching { resources.track.dispose() }
        runCatching { resources.source.dispose() }
    }
    fakeAudioTracks.clear()
}
