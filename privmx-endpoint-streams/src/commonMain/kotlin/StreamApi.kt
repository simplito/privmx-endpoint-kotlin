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
import webrtc.AudioTrack
import webrtc.IceServer
import webrtc.JanusPublisher
import webrtc.MediaStreamTrack
import webrtc.PeerConnectionManager
import webrtc.RoomJanusSession
import webrtc.TrackFactory
import webrtc.VideoTrack
import kotlin.jvm.JvmOverloads

expect class StreamApiInit

class StreamApi(
     val api: StreamApiLow,
     val init: StreamApiInit
) {
    internal lateinit var pcManager: PeerConnectionManager
    var trackFactory: TrackFactory
        private set


    init {
        initialFun()

        trackFactory = TrackFactory(pcManager)
    }

    fun createStreamRoom(
        contextId: String,
        users: MutableList<UserWithPubKey>,
        managers: MutableList<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy
    ): String {
        return api.createStreamRoom(contextId, users, managers, publicMeta, privateMeta, policies)
    }

    fun updateStreamRoom(
        streamRoomId: String,
        users: MutableList<UserWithPubKey>,
        managers: MutableList<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        version: Long,
        force: Boolean,
        forceGenerateNewKey: Boolean,
        policies: ContainerPolicy?
    ) {
        api.updateStreamRoom(
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
    }

    @JvmOverloads
    fun listStreamRooms(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String = "desc",
        lastId: String? = null,
        sortBy: String? = null
    ): PagingList<StreamRoom> {
        return api.listStreamRooms(contextId, skip, limit, sortOrder, lastId, sortBy)
    }

    fun getStreamRoom(streamRoomId: String): StreamRoom {
        return api.getStreamRoom(streamRoomId);
    }

    fun deleteStreamRoom(streamRoomId: String) {
        api.deleteStreamRoom(streamRoomId);
    }


    fun listStreams(streamRoomId: String): List<StreamInfo> {
        return api.listStreams(streamRoomId);
    }

    fun subscribeFor(subscriptionQueries: List<String>): List<String> {
        return api.subscribeFor(subscriptionQueries);
    }

    fun unsubscribeFrom(subscriptionIds: List<String>) {
        api.unsubscribeFrom(subscriptionIds);
    }

    fun buildSubscriptionQuery(
        eventType: StreamEventType,
        selectorType: StreamEventSelectorType,
        selectorId: String
    ): String {
        return api.buildSubscriptionQuery(
            eventType,
            selectorType,
            selectorId
        );
    }


    internal fun resolveSession(roomId: String): RoomJanusSession =
        pcManager.getSession(roomId)
            ?: throw IllegalStateException("Session to this room does not exist. Call joinStreamRoom first.")

    private fun resolveSession(handle: StreamHandle): RoomJanusSession =
        pcManager.getSession(handle)
            ?: throw IllegalStateException("Session to this room does not exist. Call joinStreamRoom first.")

    private fun resolvePublisher(streamHandle: StreamHandle): JanusPublisher {
        val session = pcManager.getSession(streamHandle)
            ?: throw IllegalStateException("Stream with this StreamHandle doesn't exist.")
        return session.publisher
            ?: throw IllegalStateException("This StreamHandle has not created companion publisher.")
    }

    fun leaveStreamRoom(streamRoomId: String) {
        pcManager.leaveStreamRoom(streamRoomId)
        api.leaveStreamRoom(streamRoomId)
    }

    fun removeTrack(
        streamHandle: StreamHandle,
        track: MediaStreamTrack
    ) {
        val publisher = resolvePublisher(streamHandle)
        when (track) {
            is AudioTrack -> publisher.removeAudioTrack(track.trackId)
            is VideoTrack -> publisher.removeVideoTrack(track.trackId)
        }
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
        when (track.kind) {
            "video" -> publisher.addVideoTrack(track as VideoTrack)
            "audio" -> publisher.addAudioTrack(track as AudioTrack)
        }
    }

    @JvmOverloads
     fun setTrackObserver(
        roomId: String,
        observer: TrackObserver,
        streamId: String? = null
    ){
         resolveSession(roomId).setTrackObserver(streamId, observer)
     }

     fun unsubscribeFromRemoteStreams(
        streamRoomId: String,
        subscriptionsToRemove: List<StreamSubscription>
    ) {
        val session = this.resolveSession(streamRoomId)
        session.subscriber?.setRTCConfiguration(getRTCConfiguration())
            ?: throw IllegalStateException("This streamRoom has not created companion subscriber.")

        api.unsubscribeFromRemoteStreams(streamRoomId, subscriptionsToRemove)
    }

    fun publishStream(streamHandle: StreamHandle): StreamPublishResult {
        resolvePublisher(streamHandle).setRTCConfiguration(getRTCConfiguration())
        return api.publishStream(streamHandle)
    }

    fun unpublishStream(streamHandle: StreamHandle) {
        val session = resolveSession(streamHandle)
        if (session.publisher == null)
            throw IllegalStateException("No stream to unpublish. Call createStream and publishStream first.")

        api.unpublishStream(streamHandle)
        session.unpublish()
        pcManager.closeHandleToRoom(streamHandle)
    }

    fun updateStream(streamHandle: StreamHandle): StreamPublishResult {
        resolvePublisher(streamHandle).setRTCConfiguration(getRTCConfiguration())
        return api.updateStream(streamHandle)
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
}

expect fun StreamApi.initialFun()
internal expect fun StreamApi.getRTCConfiguration(): List<IceServer>

expect fun StreamApi. joinStreamRoom(
    streamRoomId: String
)



// todo - only for andoroid
expect fun StreamApi. dropBrokenFrames(
    streamRoomId: String,
    enable: Boolean
)
