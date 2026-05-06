package modules

import com.simplito.kotlin.privmx_endpoint.model.stream.Key
import com.simplito.kotlin.privmx_endpoint.model.stream.KeyType
import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import com.simplito.kotlin.privmx_endpoint.modules.stream.WebRtcInterface
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.PmxFrameCryptor
import org.webrtc.PmxFrameCryptorFactory
import org.webrtc.PmxKeyStore
import java.util.concurrent.Executors


internal class RoomJanusSession(
    val roomID: String,
    val pcFactory: PeerConnectionFactory,
    private val onTrickle: (Long, String) -> Unit,
    private val setNewOfferOnReconfigure: (Long, SdpWithTypeModel) -> Unit
) {
    private val keyStore: PmxKeyStore = PmxFrameCryptorFactory.createPmxKeyStore()
    private val trackObserversByStreamId = mutableMapOf<String?, TrackObserver>()
    private val trackObserver: TrackObserver = TrackObserverImpl()
    private var onConnectionChangeCallback: (PeerConnection.IceConnectionState) -> Unit = {}

    var subscriber: JanusSubscriber? = null
        private set

    var publisher: JanusPublisher? = null
        private set

    val webrtc: WebRtcInterface = WebRTCImpl()

    @Synchronized
    fun createSubscriber(observer: TrackObserver = trackObserver) {
        if (subscriber != null && !subscriber!!.isEnded()) {
            throw IllegalStateException("Subscriber is currently active.")
        }

        subscriber?.close()
        subscriber = JanusSubscriber(
            pcFactory,
            keyStore,
            observer,
            onTrickle
        )
    }

    @Synchronized
    fun createPublisher(observer: TrackObserver? = null) {
        if (publisher != null && !publisher!!.isEnded()) {
            throw IllegalStateException("Publisher is currently active.")
        }

        publisher?.close()
        publisher = JanusPublisher(
            pcFactory,
            keyStore,
            observer,
            onTrickle,
            setNewOfferOnReconfigure,
            this::onConnectionChange
        )
    }

    fun setTrackObserver(observer: TrackObserver) =
        setTrackObserver(null, observer)

    fun setTrackObserver(streamId: String?, observer: TrackObserver) {
        synchronized(trackObserversByStreamId) {
            trackObserversByStreamId.put(streamId, observer)
        }
    }

    @Synchronized
    fun setOnConnectionChange(onConnectionChange: (PeerConnection.IceConnectionState) -> Unit) {
        this.onConnectionChangeCallback = onConnectionChange
    }

    fun setFrameCryptorOptions(options: PmxFrameCryptor.PmxFrameCryptorOptions) {
        subscriber?.setFrameCryptorOptions(options)
        publisher?.setFrameCryptorOptions(options)
    }

    private fun onConnectionChange(connectionState: PeerConnection.IceConnectionState) {
        onConnectionChangeCallback(connectionState)
    }

    @Synchronized
    fun unpublish() {
        publisher?.let {
            if (!it.isEnded()) {
                synchronized(it) {
                    it.close()
                    publisher = null
                }
            }
        }
    }

    inner class WebRTCImpl : WebRtcInterface {
        private val executor = Executors.newSingleThreadExecutor()

        override fun createOfferAndSetLocalDescription(streamRoomId: String): String {
            return runCatching {
                publisher?.createOffer() ?: throw RuntimeException("Create publisher first")
            }.getOrDefault("")
        }

        override fun createAnswerAndSetDescriptions(
            streamRoomId: String,
            sdp: String,
            type: String
        ): String {
            return runCatching {
                subscriber?.createAnswer(sdp, type)
                    ?: throw RuntimeException("Create subscriber first")
            }.getOrDefault("")
        }

        override fun setAnswerAndSetRemoteDescription(
            streamRoomId: String,
            sdp: String,
            type: String
        ) {
            runCatching {
                publisher?.setAnswer(sdp, type) ?: throw RuntimeException("Create publisher first")
            }
        }

        override fun close(streamRoomId: String) {
            publisher?.close()
            subscriber?.close()
        }

        override fun updateKeys(
            streamRoomId: String,
            keys: List<Key>
        ) {
            executor.execute {
                keyStore.setKeys(keys.map { key ->
                    PmxKeyStore.Key(
                        key.keyId,
                        key.key,
                        if (key.type == KeyType.LOCAL) PmxKeyStore.KeyType.LOCAL
                        else PmxKeyStore.KeyType.REMOTE
                    )
                })
            }
        }

        override fun updateSessionId(
            streamRoomId: String,
            sessionId: Long,
            connectionType: String
        ) {
            when (connectionType) {
                "subscriber" -> subscriber?.sessionId = sessionId
                "publisher" -> publisher?.sessionId = sessionId
            }
        }
    }

    private inner class TrackObserverImpl : TrackObserver {
        override fun OnRemoteTrack(
            streamId: String,
            track: MediaStreamTrack
        ) {
            synchronized(trackObserversByStreamId) {
                trackObserversByStreamId[streamId]?.OnRemoteTrack(streamId, track)
                trackObserversByStreamId[null]?.OnRemoteTrack(streamId, track)
            }
        }
    }
}