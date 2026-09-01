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
     * Leaves a Stream Room and closes all opened Publisher/Subscriber Streams.
     *
     * The handles of the Stream Room's publisher and subscriber Streams are invalidated by this call and the
     * Stream Room has to be joined again to publish or receive anything in it.
     * It also closes all the connections, so the user disappears from the list of participants.
     *
     * @param streamRoomId ID of the room to leave
     *
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun leaveStreamRoom(streamRoomId: String)

    /**
     * Creates a Publisher Stream in given Stream Room.
     *
     * The Stream is only created locally - nothing is sent to the server and the Stream becomes visible to other
     * participants after calling [publishStream].
     * A Stream Room can hold one Publisher Stream at a time - creating a second one throws
     * 'StreamAlreadyPublishedException' until the current one is removed by removeStream.
     *
     * @param streamRoomId ID of the room to create stream in
     *
     * @return handle to the created stream
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmName("createStream")
    actual external fun createStream(streamRoomId: String): StreamHandle

    /**
     * Publishes the Stream with the feeds currently added to it by the WebRTC layer.
     * A Publisher Stream has to have at least one feed added to be published successfully.
     *
     * @param streamHandle handle to the stream to publish
     *
     * @return publish result
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmName("publishStream")
    actual external fun publishStream(streamHandle: StreamHandle): StreamPublishResult

    /**
     * Updates an already published Stream after its feeds have changed (added or removed).
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
     * Stops publishing and closes the Publisher Stream.
     *
     * The handle is closed after this call and cannot be used anymore, but a new Publisher Stream can be created
     * in the same Stream Room with [createStream].
     *
     * @param streamHandle handle to the stream to unpublish
     *
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @JvmName("removeStream")
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun removeStream(streamHandle: StreamHandle)

    /**
     * Creates a Subscriber Stream receiving the selected streams or tracks published in given Stream Room.
     *
     * A Stream Room can hold one subscriber Stream at a time.
     * The 'subscriptions' list has to contain at least one feed to create a subscriber Stream successfully.
     * A StreamSubscription without `streamTrackId` subscribes to all the tracks available in that Stream.
     *
     * @param streamRoomId ID of the Stream Room to create the Stream in
     * @param subscriptions list of Streams and tracks to subscribe to
     *
     * @return handle to the created Stream
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
     * Modifies the subscriptions of an existing Subscriber Stream.
     *
     * The resulting set of subscriptions is the current one without 'subscriptionsToRemove' plus
     * 'subscriptionsToAdd'. As in createSubscriberStream, the negotiation which may follow is completed internally.
     *
     * @param subscriptionHandle  handle to the stream to update
     * @param subscriptionsToAdd list of subscriptions to add
     * @param subscriptionsToRemove list of subscriptions to remove
     *
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
     * Unsubscribes from all the Streams received by the given Subscriber Stream and closes it.
     *
     * The handle is closed after this call and cannot be used anymore, but a new Subscriber Stream can be created
     * in the same Stream Room with [createSubscriberStream].
     *
     * @param subscriptionHandle  handle to the stream to remove
     *
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
     * Sends a locally gathered ICE candidate to the media server.
     *
     * This is meant to be called by the WebRTC layer for every candidate it gathers, with the session ID which
     * this API has assigned to that Stream by calling [WebRTCInterface.updateSessionId].
     *
     * @param sessionId ID of the media server session the candidate belongs to
     * @param candidateAsJson candidate as JSON string
     *
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun trickle(sessionId: Long, candidateAsJson: String)

    /**
     * Sends a new offer to the media server to reconfigure an existing Stream.
     *
     * This method can be used to start the renegotiation process when the WebRTC layer signals that renegotiation
     * is needed on the PeerConnection observer.
     *
     * @param sessionId ID of the media server session to reconfigure
     * @param sdp offer created by the WebRTC layer
     *
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun setNewOfferOnReconfigure(sessionId: Long, sdp: SdpWithTypeModel)

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

    /**
     * Encrypts a message to be sent over the Stream Room's data channel.
     *
     * The Stream Room has to be joined, as the message is encrypted with its current key.
     * The message's 'seq' is assigned by the caller and has to grow strictly with every message sent over the same
     * Stream - the receiving side rejects a message whose 'seq' is not greater than the last accepted one.
     *
     * @param streamRoomId ID of the Stream Room to send the message in
     * @param plainMessage message to encrypt
     *
     * @return encrypted message
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
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
