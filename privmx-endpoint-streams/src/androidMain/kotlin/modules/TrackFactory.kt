package modules

import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

class TrackFactory internal constructor(
    pcManager: PeerConnectionManager
) {
    private val factory: PeerConnectionFactory = pcManager.pcFactory

    fun createVideoSource(isScreenCast: Boolean): VideoSource =
        factory.createVideoSource(isScreenCast)

    fun createVideoSource(isScreenCast: Boolean, alignTimestamps: Boolean): VideoSource =
        factory.createVideoSource(isScreenCast, alignTimestamps)

    fun createAudioSource(): AudioSource =
        factory.createAudioSource(MediaConstraints())

    fun createVideoTrack(id: String, videoSource: VideoSource): VideoTrack =
        factory.createVideoTrack(id, videoSource)

    fun createAudioTrack(id: String, audioSource: AudioSource): AudioTrack =
        factory.createAudioTrack(id, audioSource)
}