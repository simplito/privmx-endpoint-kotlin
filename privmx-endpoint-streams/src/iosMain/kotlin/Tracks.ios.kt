@file:OptIn(ExperimentalForeignApi::class)

import WebRTCFramework.RTCAudioTrack
import WebRTCFramework.RTCMediaStreamTrack
import WebRTCFramework.RTCMediaStreamTrackState
import WebRTCFramework.RTCVideoTrack
import kotlinx.cinterop.ExperimentalForeignApi

internal interface NativeTrack {
    val native: RTCMediaStreamTrack
}

private fun RTCMediaStreamTrack.toState(): TrackState =
    when (readyState) {
        RTCMediaStreamTrackState.RTCMediaStreamTrackStateLive -> TrackState.LIVE
        else -> TrackState.ENDED
    }

open class IosMediaStreamTrack(
    override val native: RTCMediaStreamTrack
) : MediaStreamTrack, NativeTrack {
    override val trackId: String get() = native.trackId
    override val kind: String get() = native.kind
    override var isEnabled: Boolean
        get() = native.isEnabled
        set(value) { native.isEnabled = value }
    override val state: TrackState get() = native.toState()
}

class IosAudioTrack(native: RTCAudioTrack) : IosMediaStreamTrack(native), AudioTrack
class IosVideoTrack(native: RTCVideoTrack) : IosMediaStreamTrack(native), VideoTrack

fun RTCMediaStreamTrack.toCommon(): MediaStreamTrack = IosMediaStreamTrack(this)
fun RTCAudioTrack.toCommon(): AudioTrack = IosAudioTrack(this)
fun RTCVideoTrack.toCommon(): VideoTrack = IosVideoTrack(this)
