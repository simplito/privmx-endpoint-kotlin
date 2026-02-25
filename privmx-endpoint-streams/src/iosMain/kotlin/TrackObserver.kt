import WebRTCFramework.RTCMediaStreamTrack
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
fun interface TrackObserver{
    fun onRemoteTrack(streamId: String?, track: RTCMediaStreamTrack)
}