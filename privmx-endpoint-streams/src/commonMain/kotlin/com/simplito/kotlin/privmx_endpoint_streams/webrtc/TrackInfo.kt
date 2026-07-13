package com.simplito.kotlin.privmx_endpoint_streams.webrtc

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