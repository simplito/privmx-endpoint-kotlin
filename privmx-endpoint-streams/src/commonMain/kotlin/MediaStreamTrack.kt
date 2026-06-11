enum class TrackState { LIVE, ENDED }

interface MediaStreamTrack {
    val trackId: String
    val kind: String
    var isEnabled: Boolean
    val state: TrackState
}

interface AudioTrack : MediaStreamTrack
interface VideoTrack : MediaStreamTrack

data class AudioTrackInfo(
    val track: AudioTrack,
    val sender: RtpSender,
    val frameCryptor: FrameCryptor
)

data class VideoTrackInfo(
    val track: VideoTrack,
    val sender: RtpSender,
    val frameCryptor: FrameCryptor
)