package webrtc

import TrackObserver
import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine


expect class JanusPublisher : JanusConnection {

     val audioTracks : MutableMap<String, AudioTrackInfo>
     val videoTracks : MutableMap<String, VideoTrackInfo>

    fun addAudioTrack(audioTrack: AudioTrack)
    fun addVideoTrack(videoTrack: VideoTrack)
    fun removeAudioTrack(id: String)
    fun removeVideoTrack(id: String)

    suspend fun createOffer(): String

    suspend fun setAnswer(sdp: String?, type: String): SessionDescription
}
