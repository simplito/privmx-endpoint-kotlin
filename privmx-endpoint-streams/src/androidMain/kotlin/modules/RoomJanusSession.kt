package modules

import com.simplito.kotlin.privmx_endpoint.model.stream.Key
import com.simplito.kotlin.privmx_endpoint.model.stream.KeyType
import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import com.simplito.kotlin.privmx_endpoint.modules.stream.WebRtcInterface
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.PmxFrameCryptor
import org.webrtc.PmxFrameCryptorFactory
import org.webrtc.PmxKeyStore


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
        private val context = newSingleThreadContext("WebRTCImplThread")

        override fun createOfferAndSetLocalDescription(streamRoomId: String): String {
            return runBlocking(context) {
                val pub = publisher ?: throw RuntimeException("Create publisher first")
                pub.createOffer()
            }
        }

        override fun createAnswerAndSetDescriptions(
            streamRoomId: String,
            sdp: String,
            type: String
        ): String {
            return runBlocking(context) {
                val sub = subscriber ?: throw RuntimeException("Create subscriber first")
                sub.createAnswer(sdp, type)
            }
        }

        override fun setAnswerAndSetRemoteDescription(
            streamRoomId: String,
            sdp: String,
            type: String
        ) {
            runBlocking(context) {
                val pub = publisher ?: throw RuntimeException("Create publisher first")
                pub.setAnswer(sdp, type)
            }
        }

        override fun close(streamRoomId: String) {
            runBlocking(context) {
                publisher?.close()
                subscriber?.close()
            }
        }

        override fun updateKeys(
            streamRoomId: String,
            keys: List<Key>
        ) {
            runBlocking(context) {
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