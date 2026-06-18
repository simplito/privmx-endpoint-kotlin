package modules

import org.webrtc.MediaStreamTrack

interface TrackObserver {
    fun OnRemoteTrack(streamId: String, track: MediaStreamTrack)
}