package webrtc

import org.webrtc.MediaConstraints

actual class TrackFactory internal actual constructor(pcManager: PeerConnectionManager) {
    actual val factory: PeerConnectionFactory = pcManager.pcFactory

    @JvmOverloads
    actual fun createVideoTrack(
        id: String,
        isScreenCast: Boolean,
        alignTimestamps: Boolean
    ): VideoTrack {
        val source = factory.createVideoSource(isScreenCast, alignTimestamps)
        val native: org.webrtc.VideoTrack = factory.createVideoTrack(id, source)
        return native.toCommon()
    }

    actual fun createAudioTrack(id: String): AudioTrack {
        val source = factory.createAudioSource(MediaConstraints())
        val native: org.webrtc.AudioTrack = factory.createAudioTrack(id, source)
        return native.toCommon()

    }
}


class AndroidMediaStreamTrack(
    private val native: org.webrtc.MediaStreamTrack,
) : MediaStreamTrack {
    override val trackId: String
        get() = native.id()
    override val kind: String
        get() = native.kind()
    override var isEnabled: Boolean
        get() = native.enabled()
        set(value) { native.setEnabled(value) }
    override val state: TrackState
        get() = when (native.state()) {
            org.webrtc.MediaStreamTrack.State.LIVE -> TrackState.LIVE
            else -> TrackState.ENDED
        }
}

class AndroidAudioTrack(
    val native: org.webrtc.AudioTrack
) : AudioTrack {
    override val trackId: String
        get() = native.id()
    override val kind: String
        get() = native.kind()
    override var isEnabled: Boolean
        get() = native.enabled()
        set(value) { native.setEnabled(value) }
    override val state: TrackState
        get() = when (native.state()) {
            org.webrtc.MediaStreamTrack.State.LIVE -> TrackState.LIVE
            else -> TrackState.ENDED
        }

}

class AndroidVideoTrack(
    val native: org.webrtc.VideoTrack
) : VideoTrack {
    override val trackId: String get() = native.id()
    override val kind: String get() = native.kind()
    override var isEnabled: Boolean
        get() = native.enabled()
        set(value) { native.setEnabled(value) }
    override val state: TrackState
        get() = when (native.state()) {
            org.webrtc.MediaStreamTrack.State.LIVE -> TrackState.LIVE
            else -> TrackState.ENDED
        }
}


fun org.webrtc.MediaStreamTrack.toCommon() = AndroidMediaStreamTrack(this)
fun org.webrtc.AudioTrack.toCommon(): AudioTrack = AndroidAudioTrack(this)
fun org.webrtc.VideoTrack.toCommon() = AndroidVideoTrack(this)