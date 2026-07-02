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
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.event.EventApi
import kotlin.jvm.JvmOverloads

/**
 * Low-level Stream API for PrivMX Bridge.
 * @param connection active connection to PrivMX Bridge
 * @param eventApi   instance of [EventApi] created on passed Connection
 * @param streamEncryptionMode encryption mode for streams
 * @throws IllegalStateException when one of the passed parameters is closed
 */
expect class StreamApiLow
@Throws(IllegalStateException::class)
constructor(
    connection: Connection
) : AutoCloseable {

    /**
     * Gets TURN credentials.
     *
     * @return list of TURN credentials
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun getTurnCredentials(): List<TurnCredentials>

    /**
     * Creates a stream room.
     *
     * @param contextId ID of the context where the room will be created
     * @param users IDs of the users who will be members of the room
     * @param managers IDs of the users who will be managers of the room
     * @param publicMeta public metadata
     * @param privateMeta private metadata
     * @param policies additional container access policies
     * @return ID of the created room
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    fun createStreamRoom(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicyWithoutItem? = null
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
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
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
        policies: ContainerPolicyWithoutItem? = null
    )

    /**
     * Lists stream rooms.
     *
     * @param contextId ID of the context to list rooms from
     * @param skip number of elements to skip
     * @param limit limit of elements to return
     * @param sortOrder sort order
     * @param lastId ID of the element from which query results should start
     * @param queryAsJson custom query
     * @param sortBy field name to sort by
     * @return paging list of stream rooms
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    fun listStreamRooms(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String = "desc",
        lastId: String? = null,
        queryAsJson: String? = null,
        sortBy: String? = null
    ): PagingList<StreamRoom>

    /**
     * Gets a stream room.
     *
     * @param streamRoomId ID of the room to get
     * @return stream room information
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun getStreamRoom(streamRoomId: String): StreamRoom

    /**
     * Deletes a stream room.
     *
     * @param streamRoomId ID of the room to delete
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun deleteStreamRoom(streamRoomId: String)

    /**
     * Lists streams in a room.
     *
     * @param streamRoomId ID of the room to list streams from
     * @return list of streams
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun listStreams(streamRoomId: String): List<StreamInfo>

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun listStreamRoomParticipants(streamRoomId: String): List<StreamSubscriber>

    /**
     * Joins a stream room.
     *
     * @param streamRoomId ID of the room to join
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun joinStreamRoom(
        streamRoomId: String,
        webRtcInterface: WebRTCInterface
    )

    /**
     * Leaves a stream room.
     *
     * @param streamRoomId ID of the room to leave
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun leaveStreamRoom(streamRoomId: String)

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
    fun createStream(streamRoomId: String): StreamHandle

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
    fun publishStream(streamHandle: StreamHandle): StreamPublishResult

    /**
     * Updates a published stream.
     *
     * @param streamHandle handle to the stream to update
     * @return publish result
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun updateStream(streamHandle: StreamHandle): StreamPublishResult

    /**
     * Unpublishes a stream.
     *
     * @param streamHandle handle to the stream to unpublish
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun removeStream(streamHandle: StreamHandle)

    /**
     * Subscribes to remote streams.
     *
     * @param streamRoomId ID of the room where streams are
     * @param subscriptions list of subscriptions
     * @throws PrivmxException thrown when method encounters an exception
     * @return // todo
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun createSubscriberStream(
        streamRoomId: String,
        subscriptions: List<StreamSubscription>
    ): SubscriberStreamHandle

    /**
     * Modifies remote streams subscriptions.
     *
     * @param subscriptionHandle // todo
     * @param subscriptionsToAdd list of subscriptions to add
     * @param subscriptionsToRemove list of subscriptions to remove
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun updateSubscriberStream(
        subscriptionHandle: SubscriberStreamHandle,
        subscriptionsToAdd: List<StreamSubscription>,
        subscriptionsToRemove: List<StreamSubscription>
    )

    /**
     * Unsubscribes from remote streams.
     *
     * @param subscriptionHandle // todo
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun removeSubscriberStream(
        subscriptionHandle: SubscriberStreamHandle,
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
    fun trickle(sessionId: Long, candidateAsJson: String)

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
    fun acceptOfferOnReconfigure(sessionId: Long, sdp: SdpWithTypeModel)

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun setNewOfferOnReconfigure(sessionId: Long, sdp: SdpWithTypeModel)

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
    fun subscribeFor(subscriptionQueries: List<String>): List<String>

    /**
     * Unsubscribes from events.
     *
     * @param subscriptionIds list of subscription IDs
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun unsubscribeFrom(subscriptionIds: List<String>)

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
    fun buildSubscriptionQuery(
        eventType: StreamEventType,
        selectorType: StreamEventSelectorType,
        selectorId: String
    ): String

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun encryptDataChannelMessage(
        streamRoomId: String,
        plainMessage: DataChannelMessage
    ): ByteArray

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun registerRemoteDataChannel(
        streamRoomId: String,
        remoteStreamId: String,
    )

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun decryptDataChannelMessage(
        streamRoomId: String,
        remoteStreamId: String,
        encryptedData: ByteArray
    ): DecryptedDataChannelMessage

    /**
     * Frees memory.
     */
    override fun close()
}
