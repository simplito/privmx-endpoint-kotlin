package webrtc

import TrackObserver
import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.withLock
import org.webrtc.MediaConstraints
import org.webrtc.PmxFrameCryptor
import org.webrtc.PmxFrameCryptorFactory

actual class JanusPublisher(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: KeyStore,
    trackObserver: TrackObserver?,
    onTrickle: (Long, String) -> Unit,
    private val setNewOfferOnReconfigure: (Long, SdpWithTypeModel) -> Unit,
    onConnectionChange: (IceConnectionState) -> Unit
) : JanusConnection(peerConnectionFactory, keyStore, trackObserver, onTrickle) {

    actual val audioTracks: MutableMap<String, AudioTrackInfo> = mutableMapOf()
    actual val videoTracks: MutableMap<String, VideoTrackInfo> = mutableMapOf()

    actual fun addAudioTrack(audioTrack: AudioTrack) {
        runBlocking {
            val rtpSender = peerConnection.addTrack((audioTrack as AndroidAudioTrack).native)
            val frameCryptor = PmxFrameCryptorFactory.createPmxFrameCryptorFromRtpSender(
                peerConnectionFactory,
                rtpSender,
                keyStore,
                null
            )
            audioTracks.put(
                audioTrack.trackId,
                AudioTrackInfo(audioTrack, rtpSender, frameCryptor)
            )
        }
    }

    actual fun addVideoTrack(videoTrack: VideoTrack) {
        runBlocking {
            val rtpSender = peerConnection.addTrack((videoTrack as AndroidVideoTrack).native)
            val frameCryptor = PmxFrameCryptorFactory.createPmxFrameCryptorFromRtpSender(
                peerConnectionFactory,
                rtpSender,
                keyStore,
                null
            )
            videoTracks.put(
                videoTrack.trackId,
                VideoTrackInfo(videoTrack, rtpSender, frameCryptor)
            )
        }
    }

    actual suspend fun removeAudioTrack(id: String) {
        configurationMutex.withLock {
            val info = audioTracks.remove(id) ?: return
            peerConnection.removeTrack(info.sender)
            info.frameCryptor.dispose()
        }
    }

    actual suspend fun removeVideoTrack(id: String) {
        configurationMutex.withLock {
            val info = videoTracks.remove(id) ?: return
            peerConnection.removeTrack(info.sender)
            info.frameCryptor.dispose()
        }
    }

    actual suspend fun createOffer(): String {
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

    actual suspend fun setAnswer(sdp: String?, type: String): SessionDescription {
        return configurationMutex.withLock {
            suspendCancellableCoroutine { continuation ->
                peerConnection.setRemoteDescription(
                    SdpObserver(continuation),
                    SessionDescription(
                        org.webrtc.SessionDescription.Type.fromCanonicalForm(type),
                        sdp
                    )
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
                    SdpWithTypeModel(createOffer(), org.webrtc.SessionDescription.Type.OFFER.canonicalForm())
                )
            }
        }
    }


}