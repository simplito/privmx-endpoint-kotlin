package webrtc

import TrackObserver
import com.simplito.kotlin.privmx_endpoint.model.stream.Key
import com.simplito.kotlin.privmx_endpoint.model.stream.KeyType
import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import com.simplito.kotlin.privmx_endpoint.modules.stream.WebRtcInterface
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import org.webrtc.PmxFrameCryptor
import org.webrtc.PmxKeyStore

// actual internal typealias PeerConnectionManager = PeerConnectionManager
actual class RoomJanusSession actual constructor(
    streamRoomId: String,
    pcFactory: PeerConnectionFactory,
    onTrickle: (Long, String) -> Unit,
){
    lateinit var setNewOfferOnReconfigure :(Long, SdpWithTypeModel) -> Unit
    private val context = newSingleThreadContext("RoomJanusSessionThread")

    constructor(
        streamRoomId: String,
        pcFactory: PeerConnectionFactory,
        onTrickle: (Long, String) -> Unit,
        setNewOfferOnReconfigure: (Long, SdpWithTypeModel) -> Unit
    ) : this(streamRoomId, pcFactory, onTrickle){
        this.setNewOfferOnReconfigure = setNewOfferOnReconfigure
    }

    actual var subscriber: JanusPublisher?
        get() = TODO("Not yet implemented")
        set(value) {}
    actual var publisher: JanusPublisher?
        get() = TODO("Not yet implemented")
        set(value) {}

    actual fun createPublisher() {
    }

    actual fun setTrackObserver(observer: TrackObserver) {
    }

    actual fun setTrackObserver(streamId: String?, observer: TrackObserver) {
    }

    actual val webrtc: WebRtcInterface
        get() = TODO("Not yet implemented")

    actual fun unpublish() {
    }

    actual fun createSubscriber() {
    }


    open fun setFrameCryptorOptions(options: PmxFrameCryptor.PmxFrameCryptorOptions) {
        subscriber?.setFrameCryptorOptions(options)
        publisher?.setFrameCryptorOptions(options)
    }

    actual inner class WebRTCImpl : WebRtcInterface {
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

    }

    internal actual val keyStore: KeyStore
        get() = TODO("Not yet implemented")
}