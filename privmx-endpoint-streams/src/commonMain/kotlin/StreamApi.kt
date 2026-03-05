import com.simplito.java.privmx_endpoint.model.events.eventSelectorTypes.StreamEventSelectorType
import com.simplito.java.privmx_endpoint.model.events.eventTypes.StreamEventType
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.stream.Settings
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamInfo
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamPublishResult
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamRoom
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamSubscription
import com.simplito.kotlin.privmx_endpoint.modules.stream.StreamApiLow

expect class StreamApiInit

expect class StreamApi(
    api: StreamApiLow,
    init: StreamApiInit
) {
//    protected val pcManager: PeerConnectionManager
//    val trackFactory: TrackFactory

//    init {
//        pcManager = PeerConnectionManager(
//            DefaultPeerConnectionFactory(RTCPeerConnectionFactoryOptions())
//        ) { sessionId, rtcConfiguration ->
//            this.api.trickle(sessionId, rtcConfiguration)
//        }
//        trackFactory = TrackFactory(pcManager)
//    }

    fun createStreamRoom(
        contextId: String,
        users: MutableList<UserWithPubKey>,
        managers: MutableList<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy
    ): String

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
    )

    fun listStreamRooms(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        sortBy: String?
    ): PagingList<StreamRoom>

    fun getStreamRoom(streamRoomId: String): StreamRoom

    fun deleteStreamRoom(streamRoomId: String)

    fun listStreams(streamRoomId: String): List<StreamInfo>

    fun joinStreamRoom(
        streamRoomId: String
    )

    fun leaveStreamRoom(streamRoomId: String)

    fun createStream(streamRoomId: String): StreamHandle

    /**
     * @param streamHandle
     * @param track
     * @throws IllegalStateException if call addTrack before call createStream
     */
    @Throws(IllegalStateException::class)
    fun addTrack(
        streamHandle: StreamHandle,
        track: RTCMediaStreamTrack
    )

    fun setTrackObserver(
        roomId: String,
        observer: TrackObserver,
        streamId: String? = null
    )

    /**
     * @param streamHandle
     * @param track
     * @throws IllegalStateException when Stream with this StreamHandle doesn't exist.
     */
    @Throws(IllegalStateException::class)
    fun removeTrack(
        streamHandle: StreamHandle,
        track: RTCMediaStreamTrack
    )

    fun publishStream(streamHandle: StreamHandle): StreamPublishResult

    fun updateStream(streamHandle: StreamHandle): StreamPublishResult

    fun unpublishStream(streamHandle: StreamHandle)

    fun subscribeToRemoteStreams(
        streamRoomId: String,
        subscriptions: MutableList<StreamSubscription>
    )

    @Throws(IllegalStateException::class)
    fun subscribeToRemoteStreams(
        streamRoomId: String,
        subscriptions: MutableList<StreamSubscription>,
        options: Settings
    )

    fun modifyRemoteStreamsSubscriptions(
        streamRoomId: String,
        subscriptionsToAdd: MutableList<StreamSubscription>,
        subscriptionsToRemove: MutableList<StreamSubscription>
    )

    fun modifyRemoteStreamsSubscriptions(
        streamRoomId: String,
        subscriptionsToAdd: List<StreamSubscription>,
        subscriptionsToRemove: List<StreamSubscription>,
        options: Settings
    )

    fun unsubscribeFromRemoteStreams(
        streamRoomId: String,
        subscriptionsToRemove: List<StreamSubscription>
    )

    fun dropBrokenFrames(
        streamRoomId: String?,
        enable: Boolean
    )

    fun subscribeFor(subscriptionQueries: List<String>): List<String>

    fun unsubscribeFrom(subscriptionIds: List<String>)

    fun buildSubscriptionQuery(
        eventType: StreamEventType,
        selectorType: StreamEventSelectorType,
        selectorId: String
    ): String
}