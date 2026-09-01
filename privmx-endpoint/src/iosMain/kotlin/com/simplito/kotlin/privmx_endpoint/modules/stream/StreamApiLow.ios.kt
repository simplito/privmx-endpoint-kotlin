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

import cnames.structs.pson_value
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicyWithoutItem
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.model.stream.*
import com.simplito.kotlin.privmx_endpoint.model.stream.events.eventSelectorTypes.StreamEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.stream.events.eventTypes.StreamEventType
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.utils.PsonValue
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import com.simplito.kotlin.privmx_endpoint.utils.makeArgs
import com.simplito.kotlin.privmx_endpoint.utils.mapOfWithNulls
import com.simplito.kotlin.privmx_endpoint.utils.pson
import com.simplito.kotlin.privmx_endpoint.utils.toDecryptedDataChannelMessage
import com.simplito.kotlin.privmx_endpoint.utils.toPagingList
import com.simplito.kotlin.privmx_endpoint.utils.toStreamHandle
import com.simplito.kotlin.privmx_endpoint.utils.toStreamInfo
import com.simplito.kotlin.privmx_endpoint.utils.toStreamPublishResult
import com.simplito.kotlin.privmx_endpoint.utils.toStreamRoom
import com.simplito.kotlin.privmx_endpoint.utils.toStreamSubscriber
import com.simplito.kotlin.privmx_endpoint.utils.toSubscriberStreamHandle
import com.simplito.kotlin.privmx_endpoint.utils.typedList
import com.simplito.kotlin.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import libprivmxendpoint.*

/**
 * Low-level Stream API for PrivMX Bridge.
 * @param connection active connection to PrivMX Bridge
 * @throws IllegalStateException when one of the passed parameters is closed
 */
@OptIn(ExperimentalForeignApi::class)
actual class StreamApiLow
@Throws(IllegalStateException::class)
actual constructor(
    connection: Connection
) : AutoCloseable {
    private val _nativeStreamApiLow = nativeHeap.allocPointerTo<cnames.structs.StreamApiLow>()
    private val proxyWebrtcList = ProxyWebrtcList()
    private val nativeStreamApiLow: CPointerVar<cnames.structs.StreamApiLow>
        get() = _nativeStreamApiLow.value?.let { _nativeStreamApiLow }
            ?: throw IllegalStateException("StreamApiLow has been closed.")

    init {
        privmx_endpoint_newStreamApiLow(
            connection.getConnectionPtr(),
            _nativeStreamApiLow.ptr
        )

        memScoped {
            val args = makeArgs()
            val pson_result = allocPointerTo<pson_value>()
            try {
                privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 0, args, pson_result.ptr)
                pson_result.value!!.asResponse?.getResultOrThrow()
            } finally {
                pson_free_value(args)
                pson_free_result(pson_result.value)
            }
        }
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
    actual fun getTurnCredentials(): List<TurnCredentials> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = pson_new_array()
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 1, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    actual fun createStreamRoom(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicyWithoutItem?
    ): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId.pson,
            users.map { it.pson }.pson,
            managers.map { it.pson }.pson,
            publicMeta.pson,
            privateMeta.pson,
            policies?.pson
        )
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 2, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    actual fun updateStreamRoom(
        streamRoomId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        version: Long,
        force: Boolean,
        forceGenerateNewKey: Boolean,
        policies: ContainerPolicyWithoutItem?
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            streamRoomId.pson,
            users.map { it.pson }.pson,
            managers.map { it.pson }.pson,
            publicMeta.pson,
            privateMeta.pson,
            version.pson,
            force.pson,
            forceGenerateNewKey.pson,
            policies?.pson
        )
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 3, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    actual fun listStreamRooms(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?,
        sortBy: String?
    ): PagingList<StreamRoom> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId.pson,
            mapOfWithNulls(
                "skip" to skip.pson,
                "limit" to limit.pson,
                "sortOrder" to sortOrder.pson,
                lastId?.let { "lastId" to lastId.pson },
                queryAsJson?.let { "queryAsJson" to queryAsJson.pson },
                sortBy?.let { "sortBy" to sortBy.pson }
            ).pson
        )
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 4, args, pson_result.ptr)
            val psonObject = pson_result.value?.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            psonObject.toPagingList { toStreamRoom() }
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    actual fun getStreamRoom(streamRoomId: String): StreamRoom = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(streamRoomId.pson)
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 5, args, pson_result.ptr)
            val psonObject = pson_result.value?.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            psonObject.toStreamRoom()
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    actual fun deleteStreamRoom(streamRoomId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(streamRoomId.pson)
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 6, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    actual fun listStreams(streamRoomId: String): List<StreamInfo> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(streamRoomId.pson)
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 10, args, pson_result.ptr)
            val psonObject = pson_result.value?.asResponse?.getResultOrThrow() as PsonValue.PsonArray<*>
            psonObject.getValue().map { (it as PsonValue.PsonObject).toStreamInfo() }
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    actual fun listStreamRoomParticipants(streamRoomId: String): List<StreamSubscriber> = memScoped{
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(streamRoomId.pson)
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 26, args, pson_result.ptr)
            val psonObject =
                pson_result.value?.asResponse?.getResultOrThrow() as PsonValue.PsonArray<*>
            psonObject.getValue().map { (it as PsonValue.PsonObject).toStreamSubscriber() }
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

    /**
     * Joins a stream room.
     *
     * @param streamRoomId ID of the room to join
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun joinStreamRoom(
        streamRoomId: String,
        webRtcInterface: WebRTCInterface
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            streamRoomId.pson,
            proxyWebrtcList.new(webRtcInterface).proxy.toLong().pson
        )
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 25, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

    /**
     * Leaves a stream room.
     *
     * @param streamRoomId ID of the room to leave
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun leaveStreamRoom(streamRoomId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(streamRoomId.pson)
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 12, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    actual fun createStream(streamRoomId: String): StreamHandle = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(streamRoomId.pson)
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 13, args, pson_result.ptr)
            (pson_result.value?.asResponse?.getResultOrThrow() as PsonValue.PsonLong).toStreamHandle()
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    actual fun publishStream(streamHandle: StreamHandle): StreamPublishResult = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(streamHandle.value!!.pson)
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 14, args, pson_result.ptr)
            val psonObject = pson_result.value?.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            psonObject.toStreamPublishResult()
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    actual fun updateStream(streamHandle: StreamHandle): StreamPublishResult = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(streamHandle.value!!.pson)
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 22, args, pson_result.ptr)
            val psonObject = pson_result.value?.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            psonObject.toStreamPublishResult()
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

    /**
     * Unpublishes a stream.
     *
     * @param streamHandle handle to the stream to unpublish
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun removeStream(streamHandle: StreamHandle) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(streamHandle.value!!.pson)
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 15, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

    /**
     * Subscribes to remote streams.
     *
     * @param streamRoomId ID of the room where streams are
     * @param subscriptions list of subscriptions
     * @return // todo
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun createSubscriberStream(
        streamRoomId: String,
        subscriptions: List<StreamSubscription>,
    ): SubscriberStreamHandle = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            streamRoomId.pson,
            subscriptions.map { it.pson }.pson
        )
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 16, args, pson_result.ptr)
            val psonObject = pson_result.value?.asResponse?.getResultOrThrow() as PsonValue.PsonLong
            psonObject.toSubscriberStreamHandle()
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    actual fun updateSubscriberStream(
        subscriptionHandle: SubscriberStreamHandle,
        subscriptionsToAdd: List<StreamSubscription>,
        subscriptionsToRemove: List<StreamSubscription>
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            subscriptionHandle.value.pson,
            subscriptionsToAdd.map { it.pson }.pson,
            subscriptionsToRemove.map { it.pson }.pson
        )
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 17, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

    /**
     * Unsubscribes from remote streams.
     *
     * @param subscriptionHandle // todo
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun removeSubscriberStream(
        subscriptionHandle: SubscriberStreamHandle,
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            subscriptionHandle.value.pson,
        )
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 18, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    actual fun trickle(sessionId: Long, candidateAsJson: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(sessionId.pson, candidateAsJson.pson)
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 19, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    actual fun acceptOfferOnReconfigure(sessionId: Long, sdp: SdpWithTypeModel) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(sessionId.pson, sdp.pson)
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 20, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    actual fun subscribeFor(subscriptionQueries: List<String>): List<String> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(subscriptionQueries.map { it.pson }.pson)
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 7, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedList()!!.map { it.typedValue() }
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

    /**
     * Unsubscribes from events.
     *
     * @param subscriptionIds list of subscription IDs
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun unsubscribeFrom(subscriptionIds: List<String>) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(subscriptionIds.map { it.pson }.pson)
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 8, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    ): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(eventType.ordinal.pson, selectorType.ordinal.pson, selectorId.pson)
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 9, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun encryptDataChannelMessage(
        streamRoomId: String,
        plainMessage: DataChannelMessage
    ): ByteArray = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(streamRoomId.pson, plainMessage.pson)
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 28, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun registerRemoteDataChannel(
        streamRoomId: String,
        remoteStreamId: String
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(streamRoomId.pson, remoteStreamId.pson)
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 27, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun decryptDataChannelMessage(
        streamRoomId: String,
        remoteStreamId: String,
        encryptedData: ByteArray
    ): DecryptedDataChannelMessage = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(streamRoomId.pson, remoteStreamId.pson, encryptedData.pson)
        try {
            privmx_endpoint_execStreamApiLow(nativeStreamApiLow.value, 29, args, pson_result.ptr)
            val psonObject = pson_result.value?.asResponse?.getResultOrThrow() as? PsonValue.PsonObject
            psonObject?.toDecryptedDataChannelMessage()!!
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

    /**
     * Frees memory.
     */
    actual override fun close() {
        privmx_endpoint_freeStreamApiLow(nativeStreamApiLow.value)
        _nativeStreamApiLow.value = null
        proxyWebrtcList.close()
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun setNewOfferOnReconfigure(sessionId: Long, sdp: SdpWithTypeModel) {
        TODO("Not yet implemented") // there is no method code/number
    }
}

private class ProxyWebrtcList : AutoCloseable {
    private val webrtcProxies: MutableList<ProxyWebrtc> = mutableListOf()
    private val webrtcProxyMutex = Mutex()

    fun new(webRtcInterface: WebRTCInterface): ProxyWebrtc = runBlocking {
        webrtcProxyMutex.withLock {
            ProxyWebrtc(webRtcInterface).also(webrtcProxies::add)
        }
    }

    override fun close() {
        runBlocking {
            webrtcProxyMutex.withLock {
                webrtcProxies.forEach(ProxyWebrtc::close)
                webrtcProxies.clear()
            }
        }
    }
}