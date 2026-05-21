package webrtc

import TrackObserver
import WebRTCFramework.PMXKSKey
import WebRTCFramework.PMXKSKeyTypeLOCAL
import WebRTCFramework.PMXKSKeyTypeREMOTE
import WebRTCFramework.PMXKeyStore
import com.simplito.kotlin.privmx_endpoint.model.stream.Key
import com.simplito.kotlin.privmx_endpoint.model.stream.KeyType
import com.simplito.kotlin.privmx_endpoint.modules.stream.WebRtcInterface
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes

actual class RoomJanusSession @OptIn(ExperimentalForeignApi::class)
actual constructor(
    streamRoomId: String,
    pcFactory: PeerConnectionFactory,
    onTrickle: (Long, String) -> Unit,
) {
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

    @OptIn(ExperimentalForeignApi::class)
    internal actual val keyStore: KeyStore = PMXKeyStore()

    actual val webrtc: WebRtcInterface
        get() = TODO("Not yet implemented")


    actual fun unpublish() {
    }

    actual fun createSubscriber() {
    }

    actual inner class WebRTCImpl : WebRtcInterface {
        private val keysDispatcher = Dispatchers.IO.limitedParallelism(1)
        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        override fun createOfferAndSetLocalDescription(streamRoomId: String): String {
            try {
                if (publisher == null) throw RuntimeException("Create publisher first")
                return runBlocking(Dispatchers.IO) {
                    publisher!!.createOffer()
                }
            } catch (ignored: Exception) {
                return ""
            }
        }

        override fun createAnswerAndSetDescriptions(
            streamRoomId: String,
            sdp: String,
            type: String
        ): String {
            try {
                if (subscriber == null) throw RuntimeException("Create subscriber first")
                return runBlocking(Dispatchers.IO) {
                    subscriber!!.createAnswer(sdp)
                }
            } catch (ignored: Exception) {
                return ""
            }
        }

        override fun setAnswerAndSetRemoteDescription(
            streamRoomId: String,
            sdp: String,
            type: String
        ) {
            try {
                if (publisher == null) throw RuntimeException("Create publisher first")
                return runBlocking(Dispatchers.IO) {
                    publisher!!.setAnswer(sdp)
                }
            } catch (ignored: Exception) {
            }
        }

        override fun updateSessionId(
            streamRoomId: String,
            sessionId: Long,
            connectionType: String
        ) {
            when (connectionType) {
                "subscriber" -> subscriber?.updateSessionId(sessionId)
                "publisher" -> publisher?.updateSessionId(sessionId)
            }
        }

        override fun close(streamRoomId: String) {
            //TODO: Clean all objects correctly
            publisher?.close()
            subscriber?.close()
        }

        @OptIn(ExperimentalForeignApi::class)
        override fun updateKeys(
            streamRoomId: String,
            keys: List<Key>
        ) {
            val list: List<PMXKSKey> = keys.map { key ->
                PMXKSKey(
                    key.keyId,
                    key.key.usePinned {
                        NSData.dataWithBytes(it.addressOf(0), it.get().size.toULong())
                    },
                    if (key.type === KeyType.LOCAL) PMXKSKeyTypeLOCAL else PMXKSKeyTypeREMOTE
                )
            }
            coroutineScope.launch(keysDispatcher) {
                keyStore.setKeys(list)
            }
        }


    }


}