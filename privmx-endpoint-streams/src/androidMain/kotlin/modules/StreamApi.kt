package modules

import android.content.Context
import com.simplito.java.privmx_endpoint.model.events.eventSelectorTypes.StreamEventSelectorType
import com.simplito.java.privmx_endpoint.model.events.eventTypes.StreamEventType
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamInfo
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamPublishResult
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamRoom
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamSubscription
import com.simplito.kotlin.privmx_endpoint.modules.stream.StreamApiLow
import org.webrtc.AudioTrack
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.PmxFrameCryptor
import org.webrtc.VideoTrack
import org.webrtc.audio.JavaAudioDeviceModule
import java.lang.AutoCloseable

class StreamApi(
    appContext: Context,
    rootEglBase: EglBase,
    private val api: StreamApiLow
) : AutoCloseable {
    private val pcManager: PeerConnectionManager
    val trackFactory: TrackFactory

    companion object {
        private fun createDefaultPeerConnectionFactory(
            appContext: Context,
            eglBase: EglBase,
            options: PeerConnectionFactory.Options = PeerConnectionFactory.Options()
        ): PeerConnectionFactory {
            val adm = JavaAudioDeviceModule.builder(appContext).createAudioDeviceModule()
            val factory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(
                    DefaultVideoEncoderFactory(
                        eglBase.eglBaseContext,
                        true,
                        false
                    )
                )
                .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
                .setOptions(options)
                .setAudioDeviceModule(adm)
                .createPeerConnectionFactory()
            adm.release()
            return factory
        }
    }

    init {
        val factory = createDefaultPeerConnectionFactory(appContext, rootEglBase)
        pcManager = PeerConnectionManager(
            factory,
            onTrickle = { sessionId, rtcConfiguration ->
                api.trickle(sessionId, rtcConfiguration)
            },
            setNewOfferOnReconfigure = { sessionId, sdp ->
                api.setNewOfferOnReconfigure(sessionId, sdp)
            }
        )
        trackFactory = TrackFactory(pcManager)
    }

    @JvmOverloads
    fun createStreamRoom(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy? = null
    ): String = api.createStreamRoom(
        contextId,
        users,
        managers,
        publicMeta,
        privateMeta,
        policies
    )

    @JvmOverloads
    fun updateStreamRoom(
        streamRoomId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        version: Long,
        force: Boolean,
        forceGenerateNewKey: Boolean,
        policies: ContainerPolicy? = null
    ) = api.updateStreamRoom(
        streamRoomId,
        users,
        managers,
        publicMeta,
        privateMeta,
        version,
        force,
        forceGenerateNewKey,
        policies
    )

    @JvmOverloads
    fun listStreamRooms(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String? = null,
        sortBy: String? = null,
        queryAsJson: String? = null
    ): PagingList<StreamRoom> =
        api.listStreamRooms(
            contextId,
            skip,
            limit,
            sortOrder,
            lastId,
            sortBy,
            queryAsJson
        )

    fun getStreamRoom(streamRoomId: String): StreamRoom =
        api.getStreamRoom(streamRoomId)

    fun deleteStreamRoom(streamRoomId: String) =
        api.deleteStreamRoom(streamRoomId)

    fun listStreams(streamRoomId: String): List<StreamInfo> =
        api.listStreams(streamRoomId)

    fun joinStreamRoom(streamRoomId: String) {
        val session = pcManager.createSession(streamRoomId)
        api.joinStreamRoom(streamRoomId, session.webrtc)
    }

    fun leaveStreamRoom(streamRoomId: String) {
        pcManager.leaveStreamRoom(streamRoomId)
        api.leaveStreamRoom(streamRoomId)
    }

    fun createStream(streamRoomId: String): StreamHandle {
        val session = pcManager.getSession(streamRoomId)
            ?: throw IllegalStateException("Session to this room does not exist. Call joinStreamRoom first.")

        runCatching { session.createPublisher() }
            .onFailure { throw IllegalStateException("Publisher is now active, try use modifyRemoteStreamsSubscriptions") }

        val handle = api.createStream(streamRoomId)
        pcManager.createHandleToRoom(handle, streamRoomId)
        return handle
    }

    fun addTrack(
        streamHandle: StreamHandle,
        track: MediaStreamTrack
    ) {
        val publisher = resolvePublisher(streamHandle)
        when (track.kind()) {
            MediaStreamTrack.VIDEO_TRACK_KIND -> publisher.addVideoTrack(track as VideoTrack)
            MediaStreamTrack.AUDIO_TRACK_KIND -> publisher.addAudioTrack(track as AudioTrack)
        }
    }

    fun removeTrack(
        streamHandle: StreamHandle,
        track: MediaStreamTrack
    ) {
        val publisher = resolvePublisher(streamHandle)
        when (track) {
            is AudioTrack -> publisher.removeAudioTrack(track.id())
            is VideoTrack -> publisher.removeVideoTrack(track.id())
        }
    }

    @JvmOverloads
    fun setTrackObserver(
        roomId: String,
        observer: TrackObserver,
        streamId: String? = null
    ) {
        resolveSession(roomId).setTrackObserver(streamId, observer)
    }

    fun setConnectionStateObserver(
        roomId: String,
        observer: (PeerConnection.IceConnectionState) -> Unit
    ) {
        resolveSession(roomId).setOnConnectionChange(observer)
    }

    fun publishStream(streamHandle: StreamHandle): StreamPublishResult {
        resolvePublisher(streamHandle).setRTCConfiguration(getRTCConfiguration())
        return api.publishStream(streamHandle)
    }

    fun updateStream(streamHandle: StreamHandle): StreamPublishResult {
        resolvePublisher(streamHandle).setRTCConfiguration(getRTCConfiguration())
        return api.updateStream(streamHandle)
    }

    fun unpublishStream(streamHandle: StreamHandle) {
        api.unpublishStream(streamHandle)
    }

    fun subscribeToRemoteStreams(streamRoomId: String, subscriptions: List<StreamSubscription>) {
        val session = resolveSession(streamRoomId)
        runCatching { session.createSubscriber() }
        session.subscriber?.setRTCConfiguration(getRTCConfiguration())
            ?: throw IllegalStateException("This streamRoom has not created companion subscriber.")

        api.subscribeToRemoteStreams(streamRoomId, subscriptions)
    }

    fun modifyRemoteStreamsSubscriptions(
        streamRoomId: String,
        subscriptionsToAdd: List<StreamSubscription>,
        subscriptionsToRemove: List<StreamSubscription>
    ) {
        val session = resolveSession(streamRoomId)
        session.subscriber?.setRTCConfiguration(getRTCConfiguration())
            ?: throw IllegalStateException("This streamRoom has not created companion subscriber.")

        api.modifyRemoteStreamsSubscriptions(
            streamRoomId,
            subscriptionsToAdd,
            subscriptionsToRemove
        )
    }

    fun unsubscribeFromRemoteStreams(
        streamRoomId: String,
        subscriptionsToRemove: List<StreamSubscription>
    ) {
        val session = resolveSession(streamRoomId)
        session.subscriber?.setRTCConfiguration(getRTCConfiguration())
            ?: throw IllegalStateException("This streamRoom has not created companion subscriber.")

        api.unsubscribeFromRemoteStreams(streamRoomId, subscriptionsToRemove)
    }

    fun dropBrokenFrames(
        streamRoomId: String,
        enable: Boolean
    ) {
        pcManager.getSession(streamRoomId)?.setFrameCryptorOptions(
            PmxFrameCryptor.PmxFrameCryptorOptions().apply {
                dropFrameIfCryptionFailed = enable
            }
        )
    }

    fun subscribeFor(subscriptionQueries: List<String>): List<String> =
        api.subscribeFor(subscriptionQueries)


    fun unsubscribeFrom(subscriptionIds: List<String>) =
        api.unsubscribeFrom(subscriptionIds)


    fun buildSubscriptionQuery(
        eventType: StreamEventType,
        selectorType: StreamEventSelectorType,
        selectorId: String
    ): String = api.buildSubscriptionQuery(eventType, selectorType, selectorId)


    override fun close() {
        pcManager.getRoomIds().toList().forEach { leaveStreamRoom(it) }
        pcManager.close()
        api.close()
    }

    private fun resolveSession(roomId: String): RoomJanusSession =
        pcManager.getSession(roomId)
            ?: throw IllegalStateException("Session to this room does not exist. Call joinStreamRoom first.")

    private fun resolvePublisher(streamHandle: StreamHandle): JanusPublisher {
        val session = pcManager.getSession(streamHandle)
            ?: throw IllegalStateException("Stream with this StreamHandle doesn't exist.")
        return session.publisher
            ?: throw IllegalStateException("This StreamHandle has not created companion publisher.")
    }

    private fun getRTCConfiguration(): List<PeerConnection.IceServer> =
        api.getTurnCredentials().map { item ->
            PeerConnection.IceServer.builder(item.url)
                .setUsername(item.username)
                .setPassword(item.password)
                .createIceServer()
        }

}