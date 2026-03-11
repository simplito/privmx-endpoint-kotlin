import WebRTCFramework.PMXFrameCryptorTransformer
import WebRTCFramework.RTCRtpSender
import WebRTCFramework.RTCVideoTrack
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
data class VideoTrackInfo(
    var track: RTCVideoTrack,
    var sender: RTCRtpSender,
    var frameCryptor: PMXFrameCryptorTransformer
)