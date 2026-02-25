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
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.EventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.EventType
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.model.stream.RecordingEncKey
import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import com.simplito.kotlin.privmx_endpoint.model.stream.Settings
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamEncryptionMode
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamInfo
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamPublishResult
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamRoom
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamSubscription
import com.simplito.kotlin.privmx_endpoint.model.stream.TurnCredentials
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.event.EventApi

/**
 * Low-level Stream API for PrivMX Bridge.
 * @param connection active connection to PrivMX Bridge
 * @param eventApi   instance of [EventApi] created on passed Connection
 * @param streamEncryptionMode encryption mode for streams
 * @throws IllegalStateException when one of the passed parameters is closed
 */
actual class StreamApiLow
@Throws(IllegalStateException::class)
actual constructor(
    connection: Connection,
    eventApi: EventApi?,
    streamEncryptionMode: StreamEncryptionMode
) : AutoCloseable {

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getTurnCredentials(): List<TurnCredentials> {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun createStreamRoom(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy?
    ): String {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

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
        policies: ContainerPolicy?
    ) {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun listStreamRooms(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?,
        sortBy: String?
    ): PagingList<StreamRoom> {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getStreamRoom(streamRoomId: String): StreamRoom {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun deleteStreamRoom(streamRoomId: String) {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun listStreams(streamRoomId: String): List<StreamInfo> {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun joinStreamRoom(streamRoomId: String, webRtcInterface: WebRtcInterface) {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun leaveStreamRoom(streamRoomId: String) {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun createStream(streamRoomId: String): StreamHandle {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun publishStream(streamHandle: StreamHandle): StreamPublishResult {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun updateStream(streamHandle: StreamHandle): StreamPublishResult {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun unpublishStream(streamHandle: StreamHandle) {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun subscribeToRemoteStreams(
        streamRoomId: String,
        subscriptions: List<StreamSubscription>,
        options: Settings
    ) {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun modifyRemoteStreamsSubscriptions(
        streamRoomId: String,
        subscriptionsToAdd: List<StreamSubscription>,
        subscriptionsToRemove: List<StreamSubscription>,
        options: Settings
    ) {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun unsubscribeFromRemoteStreams(
        streamRoomId: String,
        subscriptionsToRemove: List<StreamSubscription>
    ) {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun trickle(sessionId: Long, candidateAsJson: String) {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun acceptOfferOnReconfigure(sessionId: Long, sdp: SdpWithTypeModel) {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun subscribeFor(subscriptionQueries: List<String>): List<String> {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun unsubscribeFrom(subscriptionIds: List<String>) {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun buildSubscriptionQuery(
        eventType: StreamEventType,
        selectorType: StreamEventSelectorType,
        selectorId: String
    ): String {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun keyManagement(streamRoomId: String, disable: Boolean) {
        throw NotImplementedError("StreamApiLow is not implemented for JVM.")
    }

    /**
     * Frees memory.
     */
    actual override fun close() {
    }
}
