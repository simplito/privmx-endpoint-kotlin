import kotlinx.cinterop.ExperimentalForeignApi
import webrtc.MediaStreamTrack

actual fun interface TrackObserver {
    @OptIn(ExperimentalForeignApi::class)
    actual fun onRemoteTrack(streamId: String?, track: MediaStreamTrack)
}