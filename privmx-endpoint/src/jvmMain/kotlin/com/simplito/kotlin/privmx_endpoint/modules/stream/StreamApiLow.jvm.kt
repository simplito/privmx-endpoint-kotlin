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

import com.simplito.java.privmx_endpoint.model.events.eventSelectorTypes.StreamEventSelectorType
import com.simplito.java.privmx_endpoint.model.events.eventTypes.StreamEventType
import com.simplito.kotlin.privmx_endpoint.LibLoader
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamInfo
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamPublishResult
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamRoom
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamSubscription
import com.simplito.kotlin.privmx_endpoint.model.stream.TurnCredentials
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

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun getTurnCredentials(): List<TurnCredentials>

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual external fun createStreamRoom(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy?
    ): String

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
        policies: ContainerPolicy?
    )

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

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun getStreamRoom(streamRoomId: String): StreamRoom

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun deleteStreamRoom(streamRoomId: String)

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun listStreams(streamRoomId: String): List<StreamInfo>

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun joinStreamRoom(streamRoomId: String, webRtcInterface: WebRtcInterface)

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun leaveStreamRoom(streamRoomId: String)

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun createStream(streamRoomId: String): StreamHandle

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun publishStream(streamHandle: StreamHandle): StreamPublishResult

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun updateStream(streamHandle: StreamHandle): StreamPublishResult

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun unpublishStream(streamHandle: StreamHandle)

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun subscribeToRemoteStreams(
        streamRoomId: String,
        subscriptions: List<StreamSubscription>
    )

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun modifyRemoteStreamsSubscriptions(
        streamRoomId: String,
        subscriptionsToAdd: List<StreamSubscription>,
        subscriptionsToRemove: List<StreamSubscription>
    )

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun unsubscribeFromRemoteStreams(
        streamRoomId: String,
        subscriptionsToRemove: List<StreamSubscription>
    )

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun trickle(sessionId: Long, candidateAsJson: String)

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun acceptOfferOnReconfigure(sessionId: Long, sdp: SdpWithTypeModel)

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun subscribeFor(subscriptionQueries: List<String>): List<String>

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun unsubscribeFrom(subscriptionIds: List<String>)

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun buildSubscriptionQuery(
        eventType: StreamEventType,
        selectorType: StreamEventSelectorType,
        selectorId: String
    ): String

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
