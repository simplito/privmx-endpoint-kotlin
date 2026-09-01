//
// PrivMX Endpoint Kotlin.
// Copyright © 2025 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.simplito.kotlin.privmx_endpoint.modules.stream

import com.simplito.kotlin.privmx_endpoint.LibLoader
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicyWithoutItem
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.model.stream.DataChannelMessage
import com.simplito.kotlin.privmx_endpoint.model.stream.DecryptedDataChannelMessage
import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamInfo
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamPublishResult
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamRoom
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamSubscriber
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamSubscription
import com.simplito.kotlin.privmx_endpoint.model.stream.SubscriberStreamHandle
import com.simplito.kotlin.privmx_endpoint.model.stream.TurnCredentials
import com.simplito.kotlin.privmx_endpoint.model.stream.events.eventSelectorTypes.StreamEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.stream.events.eventTypes.StreamEventType
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import kotlin.jvm.JvmOverloads

/**
 * Low-level Stream API for PrivMX Bridge.
 * @param connection active connection to PrivMX Bridge
 * @throws IllegalStateException when one of the passed parameters is closed
 */
actual class StreamApiLow
@Throws(IllegalStateException::class)
actual constructor(
    connection: Connection
) : AutoCloseable {

    companion object {
        init {
            LibLoader.loadPrivmxLibraries()
        }
    }

    private var api: Long? = null

    init {
        api = init(connection)
    }

    /**
     * Gets credentials of the TURN servers.
     *
     * A TURN server relays the Streams when the network configuration blocks direct traffic, e.g. because of
     * a firewall or a double NAT.
     * The credentials expire, so they should be fetched again when a new connection is being configured rather
     * than stored for the lifetime of the application.
     *
     * @return list of TURN servers credentials
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun getTurnCredentials(): List<TurnCredentials>

    /**
     * Creates a stream room.
     *
     * @param contextId ID of the context where the room will be created
     * @param users IDs of the users who will be members of the room
     * @param managers IDs of the users who will be managers of the room
     * @param publicMeta public metadata
     * @param privateMeta private metadata
     * @param policies additional container access policies
     * @param emptyRoomTtl grace period (ms) the Stream Room stays open after the last participant leaves;
     * 0 closes it immediately; null use the server default (closes it immediately)
     *
     * @return ID of the created room
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual external fun createStreamRoom(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicyWithoutItem?
    ): String

    /**
     * Updates an existing stream room.
     *
     * @param streamRoomId ID of the room to update
     * @param users IDs of the users who will be members of the room
     * @param managers IDs of the users who will be managers of the room
     * @param publicMeta public metadata
     * @param privateMeta private metadata
     * @param version current version of the room
     * @param force force update
     * @param forceGenerateNewKey force to regenerate a key for the room
     * @param policies additional container access policies
     *
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual external fun updateStreamRoom(
        streamRoomId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        version: Long,
        force: Boolean,
        forceGenerateNewKey: Boolean,
        policies: ContainerPolicyWithoutItem?
    )

    /**
     * Gets a list of Stream Rooms in given Context.
     *
     * @param contextId ID of the context to list rooms from
     * @param skip number of elements to skip
     * @param limit limit of elements to return
     * @param sortOrder sort order
     * @param lastId ID of the element from which query results should start
     * @param queryAsJson custom query
     * @param sortBy field name to sort by
     *
     * @return paging list of stream rooms
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual external fun listStreamRooms(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?,
        sortBy: String?
    ): PagingList<StreamRoom>

    /**
     * Gets a single Stream Room by given Stream Room ID.
     *
     * @param streamRoomId ID of the room to get
     *
     * @return stream room information
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun getStreamRoom(streamRoomId: String): StreamRoom

    /**
     * Deletes a stream room.
     *
     * @param streamRoomId ID of the room to delete
     *
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun deleteStreamRoom(streamRoomId: String)

    /**
     * Gets a list of currently published Streams in given Stream Room.
     * The returned Streams and their feeds are what can be passed to createSubscriberStream.
     *
     * @param streamRoomId ID of the room to list streams from
     *
     * @return list of currently published streams
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun listStreams(streamRoomId: String): List<StreamInfo>

    /**
     * Gets a list of participants of given StreamRoom.
     *
     * Each participant is described by their current subscriptions and by the stream they publish, if any.
     * A user is a participant from the moment they call [joinStreamRoom] until they call [leaveStreamRoom].
     *
     * @param streamRoomId ID of the StreamRoom
     *
     * @return list of [StreamSubscriber] describing current participants
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun listStreamRoomParticipants(streamRoomId: String): List<StreamSubscriber>

    /**
     * Joins a Stream Room using the given WebRTC layer implementation.
     *
     * This is required to work with the Streams, the Stream events and the data channels inside a Stream Room.
     * Joining passes the Stream Room's current encryption keys to the given WebRTC layer and keeps them up to date
     * for as long as the Stream Room is joined, so the same instance has to stay alive until leaveStreamRoom.
     * A Stream Room can be joined only once at a time.
     *
     * @param streamRoomId ID of the room to join
     *
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun joinStreamRoom(streamRoomId: String, webRtcInterface: WebRTCInterface)

    /**
     * Leaves a stream room.
     *
     * @param streamRoomId ID of the room to leave
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun leaveStreamRoom(streamRoomId: String)

    /**
     * Creates a stream in a room.
     *
     * @param streamRoomId ID of the room to create stream in
     * @return handle to the created stream
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmName("createStream")
    actual external fun createStream(streamRoomId: String): StreamHandle

    /**
     * Publishes a stream.
     *
     * @param streamHandle handle to the stream to publish
     * @return publish result
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmName("publishStream")
    actual external fun publishStream(streamHandle: StreamHandle): StreamPublishResult

    /**
     * Updates a published stream.
     *
     * @param streamHandle handle to the stream to update
     * @return publish result
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @JvmName("updateStream")
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun updateStream(streamHandle: StreamHandle): StreamPublishResult

    /**
     * Unpublishes a stream.
     *
     * @param streamHandle handle to the stream to unpublish
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @JvmName("removeStream")
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun removeStream(streamHandle: StreamHandle)

    /**
     * Subscribes to remote streams.
     *
     * @param streamRoomId ID of the room where streams are
     * @param subscriptions list of subscriptions
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @JvmName("createSubscriberStream")
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun createSubscriberStream(
        streamRoomId: String,
        subscriptions: List<StreamSubscription>
    ): SubscriberStreamHandle

    /**
     * Modifies remote streams subscriptions.
     *
     * @param streamRoomId ID of the room where streams are
     * @param subscriptionsToAdd list of subscriptions to add
     * @param subscriptionsToRemove list of subscriptions to remove
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @JvmName("updateSubscriberStream")
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun updateSubscriberStream(
        subscriptionHandle: SubscriberStreamHandle,
        subscriptionsToAdd: List<StreamSubscription>,
        subscriptionsToRemove: List<StreamSubscription>
    )

    /**
     * Unsubscribes from remote streams.
     *
     * @param streamRoomId ID of the room where streams are
     * @param subscriptionsToRemove list of subscriptions to remove
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @JvmName("removeSubscriberStream")
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun removeSubscriberStream(
        subscriptionHandle: SubscriberStreamHandle
    )

    /**
     * Trickles a candidate.
     *
     * @param sessionId session ID
     * @param candidateAsJson candidate as JSON string
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun trickle(sessionId: Long, candidateAsJson: String)

    /**
     * Accepts offer on reconfigure.
     *
     * @param sessionId session ID
     * @param sdp SDP with type
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun acceptOfferOnReconfigure(sessionId: Long, sdp: SdpWithTypeModel)

    /**
     * Subscribes for stream events.
     *
     * @param subscriptionQueries list of subscription queries
     * @return list of subscription IDs
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun subscribeFor(subscriptionQueries: List<String>): List<String>

    /**
     * Unsubscribes from events.
     *
     * @param subscriptionIds list of subscription IDs
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun unsubscribeFrom(subscriptionIds: List<String>)

    /**
     * Builds a subscription query.
     *
     * @param eventType event type
     * @param selectorType selector type
     * @param selectorId selector ID
     * @return subscription query
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun buildSubscriptionQuery(
        eventType: StreamEventType,
        selectorType: StreamEventSelectorType,
        selectorId: String
    ): String{
        return buildSubscriptionQuery (
            eventType.ordinal.toLong(),
            selectorType.ordinal.toLong(),
            selectorId
        )
    }

    private external fun buildSubscriptionQuery(
        eventType: Long,
        selectorType: Long,
        selectorId: String
    ):String

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun encryptDataChannelMessage(
        streamRoomId: String,
        plainMessage: DataChannelMessage
    ): ByteArray

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun registerRemoteDataChannel(
        streamRoomId: String,
        remoteStreamId: String
    )

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun decryptDataChannelMessage(
        streamRoomId: String,
        remoteStreamId: String,
        encryptedData: ByteArray
    ): DecryptedDataChannelMessage

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun setNewOfferOnReconfigure(sessionId: Long, sdp: SdpWithTypeModel)

    @Throws(IllegalStateException::class)
    private external fun init(
        connection: Connection
    ): Long?

    @Throws(IllegalStateException::class)
    private external fun deinit()

    /**
     * Frees memory.
     */
    actual override fun close() {
        deinit()
    }
}
