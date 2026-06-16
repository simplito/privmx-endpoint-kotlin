actual typealias MediaStreamTrack = org.webrtc.MediaStreamTrack
actual typealias VideoTrack = org.webrtc.VideoTrack
actual typealias AudioTrack = org.webrtc.AudioTrack

actual val MediaStreamTrack.trackId: String get() = id()
actual val MediaStreamTrack.kind: String get() = kind()
actual var MediaStreamTrack.isEnabled: Boolean
    set(value){
        setEnabled(value)
    }
    get() = enabled()

actual val MediaStreamTrack.state: TrackState get() = when (state()) {
    org.webrtc.MediaStreamTrack.State.LIVE -> TrackState.LIVE
    else -> TrackState.ENDED
}