@file:OptIn(ExperimentalCoroutinesApi::class)

import com.simplito.kotlin.privmx_endpoint.model.stream.Key
import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import com.simplito.kotlin.privmx_endpoint.modules.stream.WebRtcInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.coroutines.CoroutineContext

class RoomJanusSession(
    val roomId: String,
    private val pcFactory: PeerConnectionFactory,
    private val onTrickle: (Long, String) -> Unit,
    private val acceptOfferOnReconfigure: (Long, SdpWithTypeModel) -> Unit = { _, _ -> }
) {
    internal val keyStore: KeyStore = createKeyStore()

    private val trackObserversByStreamId = mutableMapOf<String?, TrackObserver>()
    private val trackObserver: TrackObserver = TrackObserverImpl()
    private var onConnectionChangeCallback: (IceConnectionState) -> Unit = {}

    private val context: CoroutineContext = Dispatchers.Default.limitedParallelism(1)

    var subscriber: JanusSubscriber? = null
        private set
    var publisher: JanusPublisher? = null
        private set

    val webrtc: WebRtcInterface = WebRTCImpl()

    fun createSubscriber(observer: TrackObserver = trackObserver) = runBlockingOn(context) {
        check(subscriber?.isEnded ?: true) { "Subscriber is currently active." }
        subscriber?.close()
        subscriber = JanusSubscriber(pcFactory, keyStore, observer, onTrickle)
    }

    fun createPublisher(observer: TrackObserver? = null) = runBlockingOn(context) {
        check(publisher?.isEnded ?: true) { "Publisher is currently active." }
        publisher?.close()
        publisher = JanusPublisher(
            pcFactory, keyStore, observer, onTrickle,
            acceptOfferOnReconfigure, ::onConnectionChange
        )
    }

    fun setTrackObserver(observer: TrackObserver) = setTrackObserver(null, observer)

    fun setTrackObserver(streamId: String?, observer: TrackObserver) = runBlockingOn(context) {
        trackObserversByStreamId[streamId] = observer
    }

    fun setOnConnectionChange(onConnectionChange: (IceConnectionState) -> Unit) = runBlockingOn(context) {
        onConnectionChangeCallback = onConnectionChange
    }

    fun setFrameCryptorOptions(options: FrameCryptorOptions) {
        subscriber?.setFrameCryptorOptions(options)
        publisher?.setFrameCryptorOptions(options)
    }

    fun unpublish() = runBlockingOn(context) {
        publisher?.let {
            if (!it.isEnded) {
                it.close()
                publisher = null
            }
        }
    }

    private fun onConnectionChange(state: IceConnectionState) = onConnectionChangeCallback(state)

    private inner class WebRTCImpl : WebRtcInterface {
        override fun createOfferAndSetLocalDescription(streamRoomId: String): String =
            runBlockingOn(context) {
                (publisher ?: throw RuntimeException("Create publisher first")).createOffer()
            }

        override fun createAnswerAndSetDescriptions(
            streamRoomId: String, sdp: String, type: String
        ): String = runBlockingOn(context) {
            (subscriber ?: throw RuntimeException("Create subscriber first")).createAnswer(sdp, type)
        }

        override fun setAnswerAndSetRemoteDescription(
            streamRoomId: String, sdp: String, type: String
        ) {
            runBlockingOn(context) {
                (publisher ?: throw RuntimeException("Create publisher first")).setAnswer(sdp, type)
            }
        }

        override fun updateSessionId(streamRoomId: String, sessionId: Long, connectionType: String) {
            runBlockingOn(context) {
                when (connectionType) {
                    "subscriber" -> subscriber?.sessionId = sessionId
                    "publisher" -> publisher?.sessionId = sessionId
                }
            }
        }

        override fun close(streamRoomId: String) {
            runBlockingOn(context) {
                publisher?.close()
                subscriber?.close()
            }
        }

        override fun updateKeys(streamRoomId: String, keys: List<Key>) = runBlockingOn(context) {
            keyStore.applyKeys(keys)
        }
    }

    private inner class TrackObserverImpl : TrackObserver {
        override fun onRemoteTrack(streamId: String?, track: MediaStreamTrack) {
            runBlockingOn(context) {
                trackObserversByStreamId[streamId]?.onRemoteTrack(streamId, track)
                trackObserversByStreamId[null]?.onRemoteTrack(streamId, track)
            }
        }
    }
}