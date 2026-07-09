enum class TrackState { LIVE, ENDED }

expect open class MediaStreamTrack
expect val MediaStreamTrack.trackId: String
expect val MediaStreamTrack.kind: String
expect var MediaStreamTrack.isEnabled: Boolean
expect val MediaStreamTrack.state: TrackState

expect class AudioTrack : MediaStreamTrack
expect class VideoTrack : MediaStreamTrack

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