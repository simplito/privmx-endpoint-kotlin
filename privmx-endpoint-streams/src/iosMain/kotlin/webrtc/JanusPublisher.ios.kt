package webrtc

import TrackObserver
import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalForeignApi::class)

actual class JanusPublisher(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: KeyStore,
    trackObserver: TrackObserver?,
    onTrickle: (Long, String) -> Unit,
) : JanusConnection(peerConnectionFactory, keyStore, trackObserver, onTrickle){

    actual val audioTracks: MutableMap<String, AudioTrackInfo> = mutableMapOf()
    actual val videoTracks: MutableMap<String, VideoTrackInfo> = mutableMapOf()

    actual fun addAudioTrack(audioTrack: AudioTrack) {

    }

    actual fun addVideoTrack(videoTrack: VideoTrack) {
    }

    actual  fun removeAudioTrack(id: String) {
    }

    actual  fun removeVideoTrack(id: String) {
    }

    actual suspend fun createOffer(): String {
        TODO("Not yet implemented")
    }

    actual suspend fun setAnswer(
        sdp: String?,
        type: String
    ): SessionDescription {
        TODO("Not yet implemented")
    }
}