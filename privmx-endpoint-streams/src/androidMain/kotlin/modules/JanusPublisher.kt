package modules

import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.function.Consumer

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
    private val executorService = Executors.newSingleThreadExecutor()

    fun addAudioTrack(audioTrack: AudioTrack) {
        synchronized(audioTracks) {
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
        synchronized(videoTracks) {
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

    fun removeAudioTrack(id: String) {
        synchronized(audioTracks) {
            val info = audioTracks.remove(id) ?: return
            peerConnection.removeTrack(info.sender)
            info.frameCryptor.dispose()
        }
    }

    fun removeVideoTrack(id: String) {
        synchronized(videoTracks) {
            val info = videoTracks.remove(id) ?: return
            peerConnection.removeTrack(info.sender)
            info.frameCryptor.dispose()
        }
    }

    fun createOffer(): String {
        val res = CompletableFuture<SessionDescription>()
        peerConnection.createOffer(SdpObserver(res), MediaConstraints())
        return runCatching {
            val offer = res.get()
            peerConnection.setLocalDescription(SdpObserver(null), offer)
            offer.description
        }.getOrElse {
            throw RuntimeException("Cannot create offer")
        }
    }

    fun setAnswer(sdp: String?, type: String) {
        peerConnection.setRemoteDescription(
            SdpObserver(null),
            SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sdp)
        )
    }


    override fun close() {
        try {
            audioTracks.values.forEach(Consumer { track: AudioTrackInfo? -> track!!.frameCryptor.dispose() })
            videoTracks.values.forEach(Consumer { track: VideoTrackInfo? -> track!!.frameCryptor.dispose() })
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
        if (sessionId > -1) {
            executorService.execute {
                setNewOfferOnReconfigure(
                    sessionId,
                    SdpWithTypeModel(createOffer(), SessionDescription.Type.OFFER.canonicalForm())
                )
            }
        }
    }

    private data class AudioTrackInfo(
        var track: AudioTrack?,
        var sender: RtpSender?,
        var frameCryptor: PmxFrameCryptor
    )

    private data class VideoTrackInfo(
        var track: VideoTrack?,
        var sender: RtpSender?,
        var frameCryptor: PmxFrameCryptor
    )
}