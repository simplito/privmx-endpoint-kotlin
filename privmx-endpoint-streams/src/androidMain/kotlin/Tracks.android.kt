internal interface NativeTrack {
    val native: org.webrtc.MediaStreamTrack
}

private fun org.webrtc.MediaStreamTrack.toState(): TrackState =
    when (state()) {
        org.webrtc.MediaStreamTrack.State.LIVE -> TrackState.LIVE
        else -> TrackState.ENDED
    }

class AndroidMediaStreamTrack(
    override val native: org.webrtc.MediaStreamTrack
) : MediaStreamTrack, NativeTrack {
    override val trackId: String get() = native.id()
    override val kind: String get() = native.kind()
    override var isEnabled: Boolean
        get() = native.enabled()
        set(value) { native.setEnabled(value) }
    override val state: TrackState get() = native.toState()
}

class AndroidAudioTrack(
    override val native: org.webrtc.AudioTrack
) : AudioTrack, NativeTrack {
    override val trackId: String get() = native.id()
    override val kind: String get() = native.kind()
    override var isEnabled: Boolean
        get() = native.enabled()
        set(value) { native.setEnabled(value) }
    override val state: TrackState get() = native.toState()
}

class AndroidVideoTrack(
    override val native: org.webrtc.VideoTrack
) : VideoTrack, NativeTrack {
    override val trackId: String get() = native.id()
    override val kind: String get() = native.kind()
    override var isEnabled: Boolean
        get() = native.enabled()
        set(value) { native.setEnabled(value) }
    override val state: TrackState get() = native.toState()
}

fun org.webrtc.MediaStreamTrack.toCommon(): MediaStreamTrack = AndroidMediaStreamTrack(this)
fun org.webrtc.AudioTrack.toCommon(): AudioTrack = AndroidAudioTrack(this)
fun org.webrtc.VideoTrack.toCommon(): VideoTrack = AndroidVideoTrack(this)
