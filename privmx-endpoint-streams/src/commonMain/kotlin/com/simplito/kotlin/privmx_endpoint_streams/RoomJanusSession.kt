@file:OptIn(ExperimentalCoroutinesApi::class)

package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint.model.stream.Key
import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import com.simplito.kotlin.privmx_endpoint.modules.stream.WebRtcInterface
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
import webrtc.IceConnectionState
import webrtc.KeyStore
import webrtc.MediaStreamTrack
import webrtc.PeerConnectionFactory
import webrtc.PmxFrameCryptorOptions
import webrtc.applyKeys
import webrtc.createKeyStore
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

    fun createSubscriber(observer: TrackObserver = trackObserver) = runBlocking(context) {
        check(subscriber?.isEnded ?: true) { "Subscriber is currently active." }
        subscriber?.close()
        subscriber = JanusSubscriber(pcFactory, keyStore, observer, onTrickle)
    }

    fun createPublisher(observer: TrackObserver? = null) = runBlocking(context) {
        check(publisher?.isEnded ?: true) { "Publisher is currently active." }
        publisher?.close()
        publisher = JanusPublisher(
            pcFactory, keyStore, observer, onTrickle,
            acceptOfferOnReconfigure, ::onConnectionChange
        )
    }

    fun setTrackObserver(observer: TrackObserver) = setTrackObserver(null, observer)

    fun setTrackObserver(streamId: String?, observer: TrackObserver) = runBlocking(context) {
        trackObserversByStreamId[streamId] = observer
    }

    fun setOnConnectionChange(onConnectionChange: (IceConnectionState) -> Unit) = runBlocking(context) {
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

    private fun onConnectionChange(state: IceConnectionState) = onConnectionChangeCallback(state)

    private inner class WebRTCImpl : WebRtcInterface {
        override fun createOfferAndSetLocalDescription(streamRoomId: String): String =
            runBlocking(context) {
                (publisher ?: throw RuntimeException("Create publisher first")).createOffer()
            }

        override fun createAnswerAndSetDescriptions(
            streamRoomId: String, sdp: String, type: String
        ): String = runBlocking(context) {
            (subscriber ?: throw RuntimeException("Create subscriber first")).createAnswer(sdp, type)
        }

        override fun setAnswerAndSetRemoteDescription(
            streamRoomId: String, sdp: String, type: String
        ) {
            runBlocking(context) {
                (publisher ?: throw RuntimeException("Create publisher first")).setAnswer(sdp, type)
            }
        }

        override fun updateSessionId(streamRoomId: String, sessionId: Long, connectionType: String) {
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

        override fun updateKeys(streamRoomId: String, keys: List<Key>) = runBlocking(context) {
            keyStore.applyKeys(keys)
        }
    }

    private inner class TrackObserverImpl : TrackObserver {
        override fun onRemoteTrack(streamId: String?, track: MediaStreamTrack) {
            runBlocking(context) {
                trackObserversByStreamId[streamId]?.onRemoteTrack(streamId, track)
                trackObserversByStreamId[null]?.onRemoteTrack(streamId, track)
            }
        }
    }
}