package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicyWithoutItem
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamInfo
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamPublishResult
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamRoom
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamSubscriber
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamSubscription
import com.simplito.kotlin.privmx_endpoint.model.stream.SubscriberStreamHandle
import com.simplito.kotlin.privmx_endpoint.model.stream.events.eventSelectorTypes.StreamEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.stream.events.eventTypes.StreamEventType
import com.simplito.kotlin.privmx_endpoint.modules.stream.StreamApiLow
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.AudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.IceConnectionState
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.IceServer
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.MediaStreamTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnectionFactory
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PmxFrameCryptorOptions
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.VideoTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.kind
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.trackId
import kotlin.jvm.JvmOverloads

/**
 * Platform-specific data required to initialize a [StreamApi] instance.
 */
expect class StreamApiInit

/**
 * Manages PrivMX StreamRooms and WebRTC media sessions.
 * High-level wrapper over [StreamApiLow] and WebRTC, providing a simplified interface for audio and video communication
 *
 * @param api     Active [StreamApiLow] instance
 * @param apiInit Platform-specific initialization data
 */
class StreamApi(
    val api: StreamApiLow,
    val apiInit: StreamApiInit
) : AutoCloseable {
    internal var pcManager: PeerConnectionManager

    /**
     * Factory providing helpers for creating WebRTC media sources and tracks.
     */
    var trackFactory: TrackFactory
        private set

    init {
        val factory = createDefaultPeerConnectionFactory(apiInit)
        pcManager = PeerConnectionManager(
            factory,
            onTrickle = { sessionId, rtcConfiguration ->
                this.api.trickle(sessionId, rtcConfiguration)
            },
            acceptOfferOnReconfigure = { sessionId, sdp ->
                this.api.acceptOfferOnReconfigure(sessionId, sdp)
            }
        )
        trackFactory = TrackFactory(pcManager)
    }

    /**
     * Creates a new StreamRoom in given Context.
     *
     * @param contextId   ID of the Context to create the StreamRoom in
     * @param users       list of [UserWithPubKey] which indicates who will have access to the created StreamRoom
     * @param managers    list of [UserWithPubKey] which indicates who will have access (and management rights) to the
     * created StreamRoom
     * @param publicMeta  public (unencrypted) metadata
     * @param privateMeta private (encrypted) metadata
     * @param policies    additional container access policies, or `null` to use default settings
     *
     * @return Created StreamRoom ID
     * @throws IllegalStateException thrown when instance is closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun createStreamRoom(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicyWithoutItem?
    ): String {
        return api.createStreamRoom(
            contextId,
            users,
            managers,
            publicMeta,
            privateMeta,
            policies
        )
    }

    /**
     * Updates an existing StreamRoom.
     *
     * @param streamRoomId        ID of the StreamRoom to update
     * @param users               list of [UserWithPubKey] which indicates who will have access to the updated StreamRoom
     * @param managers            list of [UserWithPubKey] which indicates who will have access (and management rights) to the
     * updated StreamRoom
     * @param publicMeta          public (unencrypted) metadata
     * @param privateMeta         private (encrypted) metadata
     * @param version             current version of the updated StreamRoom
     * @param force               force update (without checking version)
     * @param forceGenerateNewKey force to regenerate the encryption key for the StreamRoom
     * @param policies            additional container access policies, or `null` to restore defaults
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun updateStreamRoom(
        streamRoomId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        version: Long,
        force: Boolean,
        forceGenerateNewKey: Boolean,
        policies: ContainerPolicyWithoutItem?
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


    /**
     * Gets a list of StreamRooms in given Context.
     *
     * @param contextId ID of the Context to get the StreamRooms from
     * @param skip      number of elements to skip from result
     * @param limit     limit of elements to return for query
     * @param sortOrder order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId    ID of the element from which query results should start
     * @param sortBy    field name to sort elements by
     * @return list of StreamRooms
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
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


    /**
     * Gets a single StreamRoom by given StreamRoom ID.
     *
     * @param streamRoomId ID of the StreamRoom to get
     * @return Information about the StreamRoom
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun getStreamRoom(streamRoomId: String): StreamRoom {
        return api.getStreamRoom(streamRoomId)
    }

    /**
     * Deletes a StreamRoom by given StreamRoom ID.
     *
     * @param streamRoomId ID of the StreamRoom to delete
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun deleteStreamRoom(streamRoomId: String) {
        api.deleteStreamRoom(streamRoomId)
    }

    /**
     * Gets a list of currently published streams in given StreamRoom.
     *
     * @param streamRoomId ID of the StreamRoom to list streams from
     * @return list of [StreamInfo] describing currently published streams
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun listStreams(streamRoomId: String): List<StreamInfo> {
        return api.listStreams(streamRoomId)
    }

    fun listStreamRoomParticipants(streamRoomId: String): List<StreamSubscriber> {
        return api.listStreamRoomParticipants(streamRoomId);
    }

    /**
     * Subscribes for events for StreamRooms and their individual streams on the given subscription queries.
     *
     * @param subscriptionQueries list of queries built with [buildSubscriptionQuery]
     * @return list of subscriptionIds in matching order to subscriptionQueries
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun subscribeFor(subscriptionQueries: List<String>): List<String> {
        return api.subscribeFor(subscriptionQueries)
    }

    /**
     * Unsubscribes from events with the given subscriptionIds.
     *
     * @param subscriptionIds list of subscriptionId
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun unsubscribeFrom(subscriptionIds: List<String>) {
        api.unsubscribeFrom(subscriptionIds)
    }

    /**
     * Generates a subscription query for events for a StreamRoom and its individual streams.
     *
     * @param eventType    type of event you listen for
     * @param selectorType scope on which you listen for events
     * @param selectorId   ID of the selector
     * @return Query for subscribing event
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun buildSubscriptionQuery(
        eventType: StreamEventType,
        selectorType: StreamEventSelectorType,
        selectorId: String
    ): String {
        return api.buildSubscriptionQuery(
            eventType,
            selectorType,
            selectorId
        )
    }

    /**
     * Leaves a StreamRoom and releases the associated WebRTC session.
     *
     * @param streamRoomId ID of the StreamRoom to leave
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    fun leaveStreamRoom(streamRoomId: String) {
        pcManager.leaveStreamRoom(streamRoomId)
        api.leaveStreamRoom(streamRoomId)
    }

    /**
     * Removes a media track from a stream.
     *
     * After removing tracks, call [updateStream] to propagate the change to other participants.
     *
     * @param streamHandle handle returned by [createStream]
     * @param track        [VideoTrack] or [AudioTrack] to remove
     * @throws IllegalStateException thrown when there is no stream for the given handle
     */
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

    /**
     * Creates a local stream handle for publishing media in given StreamRoom.
     *
     * [joinStreamRoom] must be called for the room before this method.
     *
     * @param streamRoomId ID of the StreamRoom to create the stream in
     * @return Handle to the local stream instance
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when the room has not been joined
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun createStream(streamRoomId: String): StreamHandle {
        val session = pcManager.getSession(streamRoomId)
            ?: throw IllegalStateException("Session to this room does not exist. Call joinStreamRoom first.")

        runCatching { session.createPublisher() }
            .onFailure { throw IllegalStateException("Publisher is now active, try use modifyRemoteStreamsSubscriptions") }  // Stream has already been created for this StreamRoom, try use modifyRemoteStreamsSubscriptions

        val handle = api.createStream(streamRoomId)
        pcManager.createHandleToRoom(handle, streamRoomId)
        return handle
    }

    /**
     * Adds a local media track to a stream handle.
     *
     * The track is staged locally and becomes visible to others after [publishStream] or [updateStream].
     *
     * @param streamHandle handle returned by [createStream]
     * @param track        [VideoTrack] or [AudioTrack] to add
     * @throws IllegalStateException thrown when there is no stream for the given handle
     */
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

    /**
     * Registers a [TrackObserver] to receive callbacks when a remote media track becomes available.
     *
     * @param roomId   ID of the StreamRoom
     * @param observer observer implementation receiving track callbacks
     * @param streamId ID of a specific remote stream to observe, or `null` for all streams in the given StreamRoom
     * @throws IllegalStateException thrown when there is no active session for the given room
     */
    @JvmOverloads
    fun setTrackObserver(
        roomId: String,
        observer: TrackObserver,
        streamId: String? = null
    ) {
        resolveSession(roomId).setTrackObserver(streamId, observer)
    }

    /**
     * Unsubscribes from selected remote streams in a StreamRoom.
     *
     * @param streamRoomId          ID of the StreamRoom
     * @param subscriptionsToRemove list of [StreamSubscription] to remove
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when there is no active subscription to unsubscribe from
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun removeSubscriberStream(
        subscriptionHandle: SubscriberStreamHandle
    ) {
        val session = this.resolveSession(subscriptionHandle)
        session.subscriber?.setRTCConfiguration(getRTCConfiguration())
            ?: throw IllegalStateException("No active subscription to unsubscribe from. Call subscribeToRemoteStreams first.")

        api.removeSubscriberStream(subscriptionHandle)
    }

    /**
     * Publishes the stream (with currently added tracks) to the server, making it visible to other participants.
     *
     * @param streamHandle handle returned by [createStream]
     * @return Result of the publish operation containing stream information
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when there is no stream for the given handle
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun publishStream(streamHandle: StreamHandle): StreamPublishResult {
        resolvePublisher(streamHandle).setRTCConfiguration(getRTCConfiguration())
        return api.publishStream(streamHandle)
    }

    /**
     * Stops publishing the stream
     *
     * @param streamHandle handle returned by [createStream]
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when there is no stream to unpublish
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun removeStream(streamHandle: StreamHandle) {
        val session = resolveSession(streamHandle)
        if (session.publisher == null)
            throw IllegalStateException("No stream to unpublish. Call createStream and publishStream first.")

        api.removeStream(streamHandle)
        session.unpublish()
        pcManager.closeHandleToRoom(streamHandle)
    }

    /**
     * Updates a published stream after track changes.
     *
     * Call this after [addTrack] or [removeTrack] on an already published stream to propagate the changes to other participants.
     *
     * @param streamHandle handle returned by [createStream]
     * @return Result of the update operation containing updated stream information
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when there is no stream for the given handle
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun updateStream(streamHandle: StreamHandle): StreamPublishResult {
        resolvePublisher(streamHandle).setRTCConfiguration(getRTCConfiguration())
        return api.updateStream(streamHandle)
    }

    /**
     * Subscribes to selected remote streams or tracks in a StreamRoom.
     *
     * [joinStreamRoom] must be called for the room before this method.
     *
     * @param streamRoomId  ID of the StreamRoom
     * @param subscriptions list of [StreamSubscription] describing the remote streams to subscribe to
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when the room has not been joined
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun createSubscriberStream(streamRoomId: String, subscriptions: List<StreamSubscription>):SubscriberStreamHandle {
        val session = resolveSession(streamRoomId)
        runCatching { session.createSubscriber() }
        session.subscriber?.setRTCConfiguration(getRTCConfiguration())
            ?: throw IllegalStateException("No active subscription to modify. Call subscribeToRemoteStreams first.")

       return api.createSubscriberStream(streamRoomId, subscriptions)
    }


    /**
     * Modifies the current list of remote stream subscriptions in a StreamRoom.
     *
     * @param streamRoomId          ID of the StreamRoom
     * @param subscriptionsToAdd    list of [StreamSubscription] to add
     * @param subscriptionsToRemove list of [StreamSubscription] to remove
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when there's no active subscription
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun updateSubscriberStream(
        subscriberStreamHandle: SubscriberStreamHandle,
        subscriptionsToAdd: List<StreamSubscription>,
        subscriptionsToRemove: List<StreamSubscription>
    ) {
        val session = resolveSession(subscriberStreamHandle)
        session.subscriber?.setRTCConfiguration(getRTCConfiguration())
            ?: throw IllegalStateException("No active subscription to modify. Call subscribeToRemoteStreams first.")

        api.updateSubscriberStream(
            subscriberStreamHandle,
            subscriptionsToAdd,
            subscriptionsToRemove
        )
    }

    /**
     * Registers an observer to receive ICE connection state changes for the given StreamRoom.
     *
     * @param roomId   ID of the StreamRoom
     * @param observer callback receiving [IceConnectionState] values
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when there is no active subscription for the given room
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun setConnectionStateObserver(
        roomId: String,
        observer: (IceConnectionState) -> Unit
    ) {
        resolveSession(roomId).setOnConnectionChange(observer)
    }

    /**
     * Controls whether encrypted media frames that cannot be decrypted should be dropped.
     *
     * Has no effect if there is no active session for the given room.
     *
     * @param streamRoomId ID of the StreamRoom
     * @param enable       if `true`, frames that fail decryption are dropped
     *
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun dropBrokenFrames(streamRoomId: String, enable: Boolean) {
        pcManager.getSession(streamRoomId)?.setFrameCryptorOptions(
            PmxFrameCryptorOptions(enable)
        )
    }

    /**
     * Frees memory and releases all resources.
     *
     * Leaves all active StreamRooms, releases the WebRTC resources, and closes the underlying [api].
     */
    override fun close() {
        pcManager.getRoomIds().toList().forEach { leaveStreamRoom(it) }
        pcManager.close()
        api.close()
    }

    private fun resolveSession(roomId: String): RoomJanusSession =
        pcManager.getSession(roomId)
            ?: throw IllegalStateException("Session to this room does not exist. Call joinStreamRoom first.")

    private fun resolveSession(handle: StreamHandle): RoomJanusSession =
        pcManager.getSession(handle)
            ?: throw IllegalStateException("Session to this room does not exist. Call joinStreamRoom first.")

    private fun resolvePublisher(streamHandle: StreamHandle): JanusPublisher {
        val session = pcManager.getSession(streamHandle)
            ?: throw IllegalStateException("Stream with this StreamHandle doesn't exist.")
        return session.publisher
            ?: throw IllegalStateException("No active stream for this streamHandle. Call createStream first.")
    }
}


/**
 * Joins a StreamRoom and prepares the session for WebRTC communication.
 *
 * @param streamRoomId ID of the StreamRoom to join
 * @throws PrivmxException       thrown when method encounters an exception
 * @throws NativeException       thrown when method encounters an unknown exception
 * @throws IllegalStateException thrown when instance is closed
 */
@Throws(
    PrivmxException::class,
    NativeException::class,
    IllegalStateException::class
)
expect fun StreamApi.joinStreamRoom(
    streamRoomId: String
)

internal expect fun StreamApi.createDefaultPeerConnectionFactory(init: StreamApiInit): PeerConnectionFactory
internal expect fun StreamApi.getRTCConfiguration(): List<IceServer>