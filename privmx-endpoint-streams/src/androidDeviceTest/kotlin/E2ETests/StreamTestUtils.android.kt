package E2ETests


import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import com.simplito.kotlin.privmx_endpoint_streams.StreamApi
import com.simplito.kotlin.privmx_endpoint_streams.StreamApiInit
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.AudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.VideoTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.SurfaceTextureHelper

val appContext: Context = ApplicationProvider.getApplicationContext()

val eglBase: EglBase by lazy {
    EglBase.create()
}

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
    require(deviceNames.isNotEmpty()) { "No camera" }
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

    streamApi.addTrack(streamHandle, videoTrack)
    return videoTrack
}