package modules

import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.withLock
import org.webrtc.AudioTrack
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.PmxFrameCryptor
import org.webrtc.PmxFrameCryptorFactory
import org.webrtc.PmxKeyStore
import org.webrtc.RtpSender
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack

@OptIn(ExperimentalCoroutinesApi::class)
internal class JanusPublisher(
    pcFactory: PeerConnectionFactory,
    keyStore: PmxKeyStore,
    observer: TrackObserver?,
    onTrickle: (Long, String) -> Unit,
    private val setNewOfferOnReconfigure: (Long, SdpWithTypeModel) -> Unit,
    onConnectionChange: (PeerConnection.IceConnectionState) -> Unit
) : JanusConnection(pcFactory, keyStore, observer, onTrickle, onConnectionChange) {

    private val audioTracks = mutableMapOf<String, AudioTrackInfo>()
    private val videoTracks = mutableMapOf<String, VideoTrackInfo>()

    fun addAudioTrack(audioTrack: AudioTrack) {
        runBlocking {
            val rtpSender = peerConnection.addTrack(audioTrack)
            val frameCryptor = PmxFrameCryptorFactory.createPmxFrameCryptorFromRtpSender(
                peerConnectionFactory,
                rtpSender,
                keyStore,
                null
            )
            audioTracks.put(
                audioTrack.id(),
                AudioTrackInfo(audioTrack, rtpSender, frameCryptor)
            )
        }
    }

    fun addVideoTrack(videoTrack: VideoTrack) {
        runBlocking {
            val rtpSender = peerConnection.addTrack(videoTrack)
            val frameCryptor = PmxFrameCryptorFactory.createPmxFrameCryptorFromRtpSender(
                peerConnectionFactory,
                rtpSender,
                keyStore,
                null
            )
            videoTracks.put(
                videoTrack.id(),
                VideoTrackInfo(videoTrack, rtpSender, frameCryptor)
            )
        }
    }

    suspend fun removeAudioTrack(id: String) {
        configurationMutex.withLock {
            val info = audioTracks.remove(id) ?: return
            peerConnection.removeTrack(info.sender)
            info.frameCryptor.dispose()
        }
    }

    suspend fun removeVideoTrack(id: String) {
        configurationMutex.withLock {
            val info = videoTracks.remove(id) ?: return
            peerConnection.removeTrack(info.sender)
            info.frameCryptor.dispose()
        }
    }

    suspend fun createOffer(): String {
        configurationMutex.withLock {
            val sdp = runCatching {
                suspendCancellableCoroutine { continuation ->
                    peerConnection.createOffer(
                        SdpObserver(continuation),
                        MediaConstraints()
                    )
                }
            }.getOrElse { throw RuntimeException("Cannot create offer") }

            suspendCancellableCoroutine { continuation ->
                peerConnection.setLocalDescription(
                    SdpObserver(continuation),
                    sdp
                )
            }
            return sdp.description
        }
    }

    suspend fun setAnswer(sdp: String?, type: String): SessionDescription {
        return configurationMutex.withLock {
            suspendCancellableCoroutine { continuation ->
                peerConnection.setRemoteDescription(
                    SdpObserver(continuation),
                    SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sdp)
                )
            }
        }
    }

    override fun close() {
        try {
            audioTracks.values.forEach { track: AudioTrackInfo? -> track!!.frameCryptor.dispose() }
            videoTracks.values.forEach { track: VideoTrackInfo? -> track!!.frameCryptor.dispose() }
        } catch (ignored: IllegalStateException) {
        }
        audioTracks.clear()
        videoTracks.clear()

        super.close()
    }

    override fun setFrameCryptorOptions(options: PmxFrameCryptor.PmxFrameCryptorOptions) {
        audioTracks.values.forEach { it.frameCryptor.setOptions(options) }
        videoTracks.values.forEach { it.frameCryptor.setOptions(options) }
    }

    override fun onRenegotiationNeeded() {
        runBlocking {
            if (sessionId > -1) {
                setNewOfferOnReconfigure(
                    sessionId,
                    SdpWithTypeModel(createOffer(), SessionDescription.Type.OFFER.canonicalForm())
                )
            }
        }
    }

    private data class AudioTrackInfo(
        var track: AudioTrack,
        var sender: RtpSender,
        var frameCryptor: PmxFrameCryptor
    )

    private data class VideoTrackInfo(
        var track: VideoTrack,
        var sender: RtpSender,
        var frameCryptor: PmxFrameCryptor
    )
}