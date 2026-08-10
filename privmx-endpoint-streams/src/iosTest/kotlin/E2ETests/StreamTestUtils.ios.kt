@file:OptIn(ExperimentalForeignApi::class)

package E2ETests

import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import com.simplito.kotlin.privmx_endpoint_streams.StreamApi
import com.simplito.kotlin.privmx_endpoint_streams.StreamApiInit
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.AudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.VideoTrack
import kotlinx.cinterop.ExperimentalForeignApi

actual fun createStreamApiInit(): StreamApiInit {
    return StreamApiInit()
}

actual fun addtrackvideo(
    streamApi: StreamApi,
    streamHandle: StreamHandle
) {
    TODO("Not yet implemented")
}

actual fun addFakeVideoTrackToStream(
    streamApi: StreamApi,
    streamHandle: StreamHandle
): VideoTrack {
    TODO("Not yet implemented")
}

actual fun addFakeAudioTrackToStream(
    streamApi: StreamApi,
    streamHandle: StreamHandle
): AudioTrack {
    TODO("Not yet implemented")
}