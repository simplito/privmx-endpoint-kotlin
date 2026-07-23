@file:OptIn(ExperimentalCoroutinesApi::class)

package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint.model.stream.Key
import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import com.simplito.kotlin.privmx_endpoint.modules.stream.WebRTCInterface
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.IceConnectionState
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.KeyStore
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.MediaStreamTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnectionFactory
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PmxFrameCryptorOptions
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.applyKeys
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.createKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlin.ByteArray
import kotlin.Long
import kotlin.OptIn
import kotlin.RuntimeException
import kotlin.String
import kotlin.Unit
import kotlin.check
import kotlin.collections.List
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext
import kotlin.let

internal class RoomJanusSession(
    val roomId: String,
    private val pcFactory: PeerConnectionFactory,
    private val onTrickle: (Long, String) -> Unit,
    private val acceptOfferOnReconfigure: (Long, SdpWithTypeModel) -> Unit = { _, _ -> }
) {
    internal val keyStore: KeyStore = createKeyStore()

    private val remoteStreamObserversByStreamId = mutableMapOf<String?, RemoteStreamObserver>()
    private val remoteStreamObserver: RemoteStreamObserver = RemoteStreamObserverImpl()
    private var onConnectionChangeCallback: (IceConnectionState) -> Unit = {}

    private val context: CoroutineContext = Dispatchers.Default.limitedParallelism(1)

    var subscriber: JanusSubscriber? = null
        private set
    var publisher: JanusPublisher? = null
        private set

    val webrtc: WebRTCInterface = WebRTCImpl()

    fun createSubscriber(dataChannelCryptoProvider: InternalDataChannelMessageCryptoProvider, observer: RemoteStreamObserver = remoteStreamObserver) = runBlocking(context) {
        check(subscriber?.isEnded ?: true) { "Subscriber is currently active." }
        subscriber?.close()
        subscriber = JanusSubscriber(pcFactory, keyStore, roomId, dataChannelCryptoProvider, observer, onTrickle)
    }

    fun createPublisher(dataChannelCryptoProvider: InternalDataChannelMessageCryptoProvider, observer: RemoteStreamObserver? = null) = runBlocking(context) {
        check(publisher?.isEnded ?: true) { "Publisher is currently active." }
        publisher?.close()
        publisher = JanusPublisher(
            pcFactory, keyStore, roomId, dataChannelCryptoProvider, observer, onTrickle,
            acceptOfferOnReconfigure, ::onConnectionChange
        )
    }

    fun setRemoteStreamObserver(observer: RemoteStreamObserver) = setRemoteStreamObserver(null, observer)

    fun setRemoteStreamObserver(streamId: String?, observer: RemoteStreamObserver) = runBlocking(context) {
        remoteStreamObserversByStreamId[streamId] = observer
    }

    fun setOnConnectionChange(onConnectionChange: (IceConnectionState) -> Unit) =
        runBlocking(context) {
            onConnectionChangeCallback = onConnectionChange
        }

    fun setFrameCryptorOptions(options: PmxFrameCryptorOptions) {
        subscriber?.setFrameCryptorOptions(options)
        publisher?.setFrameCryptorOptions(options)
    }

    fun unpublish() = runBlocking(context) {
        publisher?.let {
            if (!it.isEnded) {
                it.close()
                publisher = null
            }
        }
    }

    fun unsubscribe() = runBlocking(context) {
        subscriber?.let {
            if (!it.isEnded) {
                it.close()
                subscriber = null
            }
        }
    }

    private fun onConnectionChange(state: IceConnectionState) = onConnectionChangeCallback(state)

    private inner class WebRTCImpl : WebRTCInterface {
        override fun createOfferAndSetLocalDescription(
            streamRoomId: String,
            connectionType: String
        ): String =
            runBlocking {
                (publisher ?: throw RuntimeException("Create publisher first")).createOffer()
            }

        override fun createAnswerAndSetDescriptions(
            streamRoomId: String,
            sdp: String,
            type: String,
            connectionType: String
        ): String = runBlocking {
            (subscriber ?: throw RuntimeException("Create subscriber first")).createAnswer(
                sdp,
                type
            )
        }

        override fun setAnswerAndSetRemoteDescription(
            streamRoomId: String,
            sdp: String,
            type: String,
            connectionType: String
        ) {
            runBlocking {
                (publisher ?: throw RuntimeException("Create publisher first")).setAnswer(sdp, type)
            }
        }

        override fun updateSessionId(
            streamRoomId: String,
            sessionId: Long?,
            connectionType: String
        ) {
            if (sessionId == null) return
            runBlocking {
                when (connectionType) {
                    "subscriber" -> subscriber?.sessionId = sessionId
                    "publisher" -> publisher?.sessionId = sessionId
                }
            }
        }

        override fun closeAll(streamRoomId: String) {
            runBlocking(context) {
                publisher?.close()
                subscriber?.close()
            }
        }

        override fun close(streamRoomId: String, connectionType: String) {
            runBlocking(context) {
                when(connectionType){
                    "publisher" -> publisher?.close()
                    "subscriber" -> subscriber?.close()
                }
            }
        }

        override fun updateKeys(streamRoomId: String, keys: List<Key>) = runBlocking(context) {
            keyStore.applyKeys(keys)
        }
    }

    private inner class RemoteStreamObserverImpl : RemoteStreamObserver {
        override fun onTrack(streamId: String?, track: MediaStreamTrack) {
            remoteStreamObserversByStreamId[streamId]?.onTrack(streamId, track)
            remoteStreamObserversByStreamId[null]?.onTrack(streamId, track)
        }

        override fun onMessage(streamId: String, message: ByteArray) {
            remoteStreamObserversByStreamId[streamId]?.onMessage(streamId, message)
            remoteStreamObserversByStreamId[null]?.onMessage(streamId, message)
        }
    }
}