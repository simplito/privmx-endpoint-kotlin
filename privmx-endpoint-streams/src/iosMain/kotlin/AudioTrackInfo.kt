import WebRTCFramework.PMXFrameCryptorTransformer
import WebRTCFramework.RTCAudioTrack
import WebRTCFramework.RTCRtpSender
import kotlinx.cinterop.ExperimentalForeignApi


@OptIn(ExperimentalForeignApi::class)
data class AudioTrackInfo(
    var track: RTCAudioTrack,
    var sender: RTCRtpSender,
    var frameCryptor: PMXFrameCryptorTransformer
)