@file:
OptIn(ExperimentalForeignApi::class)

package webrtc

import WebRTCFramework.RTCAudioTrack
import WebRTCFramework.RTCCameraVideoCapturer
import WebRTCFramework.RTCMediaConstraints
import WebRTCFramework.RTCMediaStreamTrack
import WebRTCFramework.RTCMediaStreamTrackState
import WebRTCFramework.RTCVideoTrack
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual class TrackFactory internal actual constructor(pcManager: PeerConnectionManager) {

    actual val factory: PeerConnectionFactory = pcManager.pcFactory

    actual fun createVideoTrack(
        id: String,
        isScreenCast: Boolean,
        alignTimestamps: Boolean        // todo - cos z tym trzeba zrobic w ios
    ): VideoTrack {
        val source = factory.videoSourceForScreenCast(isScreenCast)
        val track = factory.videoTrackWithSource(source, id)
        return track.toCommon()
    }

    fun createVideoTrack(
        id: String,
        isScreenCast: Boolean
    ): Pair<RTCCameraVideoCapturer, RTCVideoTrack> {
        val videoSource = factory.videoSourceForScreenCast(isScreenCast)
        val capturer = RTCCameraVideoCapturer(videoSource)
        return capturer to factory.videoTrackWithSource(videoSource, "video-track")
    }

    actual fun createAudioTrack(id: String): AudioTrack {
        val source = factory.audioSourceWithConstraints(
            RTCMediaConstraints(
                mandatoryConstraints = emptyMap<Any?, Any>(),
                null
            )
        )
        val track = factory.audioTrackWithSource(source, id)
        return track.toCommon()
    }

}

open class IosMediaStreamTrack(
    private val native: RTCMediaStreamTrack,
) : MediaStreamTrack {
    override val trackId: String get() = native.trackId
    override val kind: String get() = native.kind
    override var isEnabled: Boolean
        get() = native.isEnabled
        set(value) {
            native.isEnabled = value
        }
    override val state: TrackState
        get() = when (native.readyState) {
            RTCMediaStreamTrackState.RTCMediaStreamTrackStateLive -> TrackState.LIVE
            else -> TrackState.ENDED
        }
}

class IosAudioTrack(
    val native: RTCAudioTrack
) : IosMediaStreamTrack(native), AudioTrack

class IosVideoTrack(
    val native: RTCVideoTrack
) : VideoTrack, IosMediaStreamTrack(native)


fun cc(i: IosVideoTrack){

}


//
//{
//    override val trackId: String get() = native.trackId
//    override val kind: String get() = native.kind
//    override var isEnabled: Boolean
//        get() = native.isEnabled
//        set(value) { native.isEnabled = value }
//    override val state: TrackState
//        get() = when (native.readyState) {
//            RTCMediaStreamTrackState.RTCMediaStreamTrackStateLive -> TrackState.LIVE
//            else -> TrackState.ENDED
//        }
//}

//class IosVideoTrack(
//    private val native: RTCVideoTrack
//) : VideoTrack{
//    override val trackId: String get() = native.trackId
//    override val kind: String get() = native.kind
//    override var isEnabled: Boolean
//        get() = native.isEnabled
//        set(value) { native.setIsEnabled(value) }
//    override val state: TrackState
//        get() = when (native.readyState) {
//            RTCMediaStreamTrackState.RTCMediaStreamTrackStateLive -> TrackState.LIVE
//            else -> TrackState.ENDED
//        }
//}


fun RTCMediaStreamTrack.toCommon() = IosMediaStreamTrack(this)
fun RTCAudioTrack.toCommon(): AudioTrack = IosAudioTrack(this)
fun RTCVideoTrack.toCommon(): VideoTrack = IosVideoTrack(this)