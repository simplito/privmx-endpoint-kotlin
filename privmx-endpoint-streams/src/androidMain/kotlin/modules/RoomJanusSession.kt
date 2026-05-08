package modules

import com.simplito.kotlin.privmx_endpoint.model.stream.Key
import com.simplito.kotlin.privmx_endpoint.model.stream.KeyType
import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import com.simplito.kotlin.privmx_endpoint.modules.stream.WebRtcInterface
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.PmxFrameCryptor
import org.webrtc.PmxFrameCryptorFactory
import org.webrtc.PmxKeyStore

@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
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
    private val context = newSingleThreadContext("RoomJanusSessionThread")

    fun createSubscriber(observer: TrackObserver = trackObserver) {
        runBlocking(context) {
            if (!(subscriber?.isEnded() ?: true)) {
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
    }

    fun createPublisher(observer: TrackObserver? = null) {
        runBlocking(context)
        {
            if (!(publisher?.isEnded() ?: true)) {
                throw IllegalStateException("Publisher is currently active.")
            }

            publisher?.close()
            publisher = JanusPublisher(
                pcFactory,
                keyStore,
                observer,
                onTrickle,
                setNewOfferOnReconfigure,
                this@RoomJanusSession::onConnectionChange
            )
        }
    }

    fun setTrackObserver(observer: TrackObserver) =
        setTrackObserver(null, observer)

    fun setTrackObserver(streamId: String?, observer: TrackObserver) {
        runBlocking(context) {
            trackObserversByStreamId.put(streamId, observer)
        }
    }

    fun setOnConnectionChange(onConnectionChange: (PeerConnection.IceConnectionState) -> Unit) {
        runBlocking(context) {
            onConnectionChangeCallback = onConnectionChange
        }
    }

    fun setFrameCryptorOptions(options: PmxFrameCryptor.PmxFrameCryptorOptions) {
        subscriber?.setFrameCryptorOptions(options)
        publisher?.setFrameCryptorOptions(options)
    }

    private fun onConnectionChange(connectionState: PeerConnection.IceConnectionState) {
        onConnectionChangeCallback(connectionState)
    }

    fun unpublish() {
        runBlocking(context) {
            publisher?.let {
                if (!it.isEnded()) {
                    it.close()
                    publisher = null
                }
            }
        }
    }

    inner class WebRTCImpl : WebRtcInterface {

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
            runBlocking(context) {
                when (connectionType) {
                    "subscriber" -> subscriber?.sessionId = sessionId
                    "publisher" -> publisher?.sessionId = sessionId
                }
            }
        }
    }

    private inner class TrackObserverImpl : TrackObserver {
        override fun OnRemoteTrack(
            streamId: String,
            track: MediaStreamTrack
        ) {
            runBlocking(context) {
                trackObserversByStreamId[streamId]?.OnRemoteTrack(streamId, track)
                trackObserversByStreamId[null]?.OnRemoteTrack(streamId, track)
            }
        }
    }
}