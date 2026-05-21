import webrtc.MediaStreamTrack

actual fun interface TrackObserver {
    actual fun onRemoteTrack(streamId: String?, track: org.webrtc.MediaStreamTrack)
}