package webrtc

expect class TrackFactory internal constructor(pcManager: PeerConnectionManager) {

    val factory: PeerConnectionFactory

    fun createVideoTrack(id: String, isScreenCast: Boolean, alignTimestamps: Boolean = true): VideoTrack
    fun createAudioTrack(id: String): AudioTrack
}
 interface  MediaStreamTrack{
    val trackId: String
    val kind: String
    var isEnabled: Boolean
    val state: TrackState
}

enum class TrackState { LIVE, ENDED }
interface AudioTrack : MediaStreamTrack
interface VideoTrack : MediaStreamTrack


fun ccccccc(track: MediaStreamTrack){
    val a : AudioTrack

}