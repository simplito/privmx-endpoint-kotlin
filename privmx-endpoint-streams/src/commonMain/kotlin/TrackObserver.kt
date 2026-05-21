import webrtc.MediaStreamTrack

expect fun interface TrackObserver{
    fun onRemoteTrack(streamId: String?, track: MediaStreamTrack)
}

