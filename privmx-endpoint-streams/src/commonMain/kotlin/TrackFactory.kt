import webrtc.AudioTrack
import webrtc.PeerConnectionFactory
import webrtc.VideoTrack
import webrtc.makeAudioTrack
import webrtc.makeVideoTrack

class TrackFactory internal constructor(pcManager: PeerConnectionManager) {

    val factory: PeerConnectionFactory = pcManager.pcFactory

    fun createVideoTrack(
        id: String,
        isScreenCast: Boolean = false,
        alignTimestamps: Boolean = true
    ): VideoTrack = factory.makeVideoTrack(id, isScreenCast, alignTimestamps)

    fun createAudioTrack(id: String): AudioTrack = factory.makeAudioTrack(id)
}
