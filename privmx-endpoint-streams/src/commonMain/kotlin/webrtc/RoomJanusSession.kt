package webrtc

import TrackObserver
import com.simplito.kotlin.privmx_endpoint.modules.stream.WebRtcInterface

expect class RoomJanusSession(
    streamRoomId: String,
    pcFactory: PeerConnectionFactory,
    onTrickle: (Long, String) -> Unit,
//    setNewOfferOnReconfigure: (Long, SdpWithTypeModel) -> Unit
) {
    ////    private val keyStore: PMXKeyStore = // PmxFrameCryptorFactory.createPmxKeyStore()
//    private val trackObserversByStreamId = mutableMapOf<String?, TrackObserver>()
//    private val trackObserver: TrackObserver = TrackObserverImpl()
//    private var onConnectionChangeCallback: (PeerConnection.IceConnectionState) -> Unit = {}
//
    val webrtc: WebRtcInterface
    internal val keyStore: KeyStore

    var subscriber: JanusPublisher?
        private set
    var publisher: JanusPublisher?
        private set

    fun createPublisher()


    fun setTrackObserver(observer: TrackObserver)

    fun setTrackObserver(streamId: String?, observer: TrackObserver)

    fun unpublish()
    fun createSubscriber()


    //    private val context = newSingleThreadContext("RoomJanusSessionThread")
//
//
//    fun createSubscriber(observer: TrackObserver = trackObserver) {
//        runBlocking(context) {
//            if (!(subscriber?.isEnded() ?: true)) {
//                throw IllegalStateException("Subscriber is currently active.")
//            }
//
//            subscriber?.close()
//            subscriber = JanusSubscriber(
//                pcFactory,
//                keyStore,
//                observer,
//                onTrickle
//            )
//        }
//    }
//
//    fun createPublisher(observer: TrackObserver? = null) {
//        runBlocking(context)
//        {
//            if (!(publisher?.isEnded() ?: true)) {
//                throw IllegalStateException("Publisher is currently active.")
//            }
//
//            publisher?.close()
//            publisher = JanusPublisher(
//                pcFactory,
//                keyStore,
//                observer,
//                onTrickle,
//                setNewOfferOnReconfigure,
//                this@RoomJanusSession::onConnectionChange
//            )
//        }
//    }
//
//    fun setTrackObserver(observer: TrackObserver) =
//        setTrackObserver(null, observer)
//
//    fun setTrackObserver(streamId: String?, observer: TrackObserver) {
//        runBlocking(context) {
//            trackObserversByStreamId.put(streamId, observer)
//        }
//    }
//
//    fun setOnConnectionChange(onConnectionChange: (PeerConnection.IceConnectionState) -> Unit) {
//        runBlocking(context) {
//            onConnectionChangeCallback = onConnectionChange
//        }
//    }
//
//    fun setFrameCryptorOptions(options: PmxFrameCryptor.PmxFrameCryptorOptions) {
//        subscriber?.setFrameCryptorOptions(options)
//        publisher?.setFrameCryptorOptions(options)
//    }
//
//    private fun onConnectionChange(connectionState: PeerConnection.IceConnectionState) {
//        onConnectionChangeCallback(connectionState)
//    }
//
//    fun unpublish() {
//        runBlocking(context) {
//            publisher?.let {
//                if (!it.isEnded()) {
//                    it.close()
//                    publisher = null
//                }
//            }
//        }
//    }
//
    inner class WebRTCImpl
//{
//
//        override fun createOfferAndSetLocalDescription(streamRoomId: String): String {
//            return runBlocking(context) {
//                val pub = publisher ?: throw RuntimeException("Create publisher first")
//                pub.createOffer()
//            }
//        }
//
//        override fun createAnswerAndSetDescriptions(
//            streamRoomId: String,
//            sdp: String,
//            type: String
//        ): String {
//            return runBlocking(context) {
//                val sub = subscriber ?: throw RuntimeException("Create subscriber first")
//                sub.createAnswer(sdp, type)
//            }
//        }
//
//        override fun setAnswerAndSetRemoteDescription(
//            streamRoomId: String,
//            sdp: String,
//            type: String
//        ) {
//            runBlocking(context) {
//                val pub = publisher ?: throw RuntimeException("Create publisher first")
//                pub.setAnswer(sdp, type)
//            }
//        }
//
//        override fun close(streamRoomId: String) {
//            runBlocking(context) {
//                publisher?.close()
//                subscriber?.close()
//            }
//        }
//
//        override fun updateKeys(
//            streamRoomId: String,
//            keys: List<Key>
//        ) {
//            runBlocking(context) {
//                keyStore.setKeys(keys.map { key ->
//                    PmxKeyStore.Key(
//                        key.keyId,
//                        key.key,
//                        if (key.type == KeyType.LOCAL) PmxKeyStore.KeyType.LOCAL
//                        else PmxKeyStore.KeyType.REMOTE
//                    )
//                })
//            }
//        }
//
//        override fun updateSessionId(
//            streamRoomId: String,
//            sessionId: Long,
//            connectionType: String
//        ) {
//            runBlocking(context) {
//                when (connectionType) {
//                    "subscriber" -> subscriber?.sessionId = sessionId
//                    "publisher" -> publisher?.sessionId = sessionId
//                }
//            }
//        }
//    }
//
//    private inner class TrackObserverImpl : TrackObserver {
//        override fun OnRemoteTrack(
//            streamId: String,
//            track: MediaStreamTrack
//        ) {
//            runBlocking(context) {
//                trackObserversByStreamId[streamId]?.OnRemoteTrack(streamId, track)
//                trackObserversByStreamId[null]?.OnRemoteTrack(streamId, track)
//            }
//        }
//    }
}

