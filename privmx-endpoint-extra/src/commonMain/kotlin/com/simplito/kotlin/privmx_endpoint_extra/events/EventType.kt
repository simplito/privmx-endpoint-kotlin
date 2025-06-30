//
// PrivMX Endpoint Kotlin Extra.
// Copyright © 2025 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.simplito.kotlin.privmx_endpoint_extra.events

import com.simplito.java.privmx_endpoint.model.events.KvdbDeletedEntryEventData
import com.simplito.java.privmx_endpoint.model.events.KvdbDeletedEventData
import com.simplito.java.privmx_endpoint.model.events.KvdbStatsEventData
import com.simplito.kotlin.privmx_endpoint.model.File
import com.simplito.kotlin.privmx_endpoint.model.Inbox
import com.simplito.kotlin.privmx_endpoint.model.InboxEntry
import com.simplito.kotlin.privmx_endpoint.model.Kvdb
import com.simplito.kotlin.privmx_endpoint.model.KvdbEntry
import com.simplito.kotlin.privmx_endpoint.model.Message
import com.simplito.kotlin.privmx_endpoint.model.Store
import com.simplito.kotlin.privmx_endpoint.model.Thread
import com.simplito.kotlin.privmx_endpoint.model.events.ContextCustomEventData
import com.simplito.kotlin.privmx_endpoint.model.events.InboxDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.InboxEntryDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreFileDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreStatsChangedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadDeletedMessageEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadStatsEventData
import com.simplito.kotlin.privmx_endpoint_extra.lib.PrivmxEndpoint

/**
 * Defines the structure to register PrivMX Bridge event callbacks using [PrivmxEndpoint.registerCallback].
 *
 * @param T the type of data contained in the Event
 */
sealed class EventType<T: Any>(
    /**
     * Channel of this event type.
     */
    val channel: String,
    /**
     * This event type as a string.
     */
    val eventType: String,
) {

    /**
     * Predefined event type that captures successful platform connection events.
     */
    data object ConnectedEvent : EventType<Unit>("", "libConnected")

    /**
     * Predefined event type to catch special events.
     * This type could be used to emit/handle events with custom implementations (e.g. to break event loops).
     */
    data object LibBreakEvent : EventType<Unit>("", "libBreak")

    /**
     * Predefined event type to catch disconnection events.
     */
    data object DisconnectedEvent : EventType<Unit>("", "libDisconnected")

    /**
     * Predefined event type to catch created Thread events.
     */
    data object ThreadCreatedEvent : EventType<Thread>("thread", "threadCreated")

    /**
     * Predefined event type to catch updated Thread events.
     */
    data object ThreadUpdatedEvent : EventType<Thread>("thread", "threadUpdated")

    /**
     * Predefined event type to catch updated Thread stats events.
     */
    data object ThreadStatsChangedEvent : EventType<ThreadStatsEventData>("thread", "threadStats")

    /**
     * Predefined event type to catch deleted Thread events.
     */
    data object ThreadDeletedEvent : EventType<ThreadDeletedEventData>("thread", "threadDeleted")

    /**
     * Predefined event type to catch created Store events.
     */
    data object StoreCreatedEvent : EventType<Store>("store", "storeCreated")

    /**
     * Predefined event type to catch updated Store events.
     */
    data object StoreUpdatedEvent : EventType<Store>("store", "storeUpdated")

    /**
     * Predefined event type to catch updated Store stats events.
     */
    data object StoreStatsChangedEvent :
        EventType<StoreStatsChangedEventData>("store", "storeStatsChanged")

    /**
     * Predefined event type to catch deleted Store stats events.
     */
    data object StoreDeletedEvent : EventType<StoreDeletedEventData>("store", "storeDeleted")

    /**
     * Type to register on new message Events.
     *
     * @property threadId ID of the Thread to observe
     */
    data class ThreadNewMessageEvent(val threadId: String) :
        EventType<Message>("thread/$threadId/messages", "threadNewMessage")

    /**
     * Type to register on message update Events.
     *
     * @property threadId ID of the Thread to observe
     */
    data class ThreadMessageUpdatedEvent(val threadId: String) :
        EventType<Message>("thread/$threadId/messages", "threadUpdatedMessage")

    /**
     * Type to register on deleted message Events.
     *
     * @property threadId ID of the Thread to observe
     */
    data class ThreadMessageDeletedEvent(val threadId: String) :
        EventType<ThreadDeletedMessageEventData>(
            "thread/$threadId/messages", "threadMessageDeleted"
        )

    /**
     * Type to register on created file Events.
     *
     * @property storeId ID of the store to observe
     */
    data class StoreFileCreatedEvent(val storeId: String) :
        EventType<File>("store/$storeId/files", "storeFileCreated")

    /**
     * Type to register on file update Events.
     *
     * @property storeId ID of the Store to observe
     */
    data class StoreFileUpdatedEvent(val storeId: String) :
        EventType<File>("store/$storeId/files", "storeFileUpdated")

    /**
     * Type to register on deleted file Events.
     *
     * @property storeId ID of the Store to observe
     */
    data class StoreFileDeletedEvent(val storeId: String) :
        EventType<StoreFileDeletedEventData>("store/$storeId/files", "storeFileDeleted")

    /**
     * Predefined event type to catch created Inbox events.
     */
    data object InboxCreatedEvent : EventType<Inbox>("inbox", "inboxCreated")

     /**
     * Predefined event type to catch update Inbox events.
     */
    data object InboxUpdatedEvent : EventType<Inbox>("inbox", "inboxUpdated")

    /**
     * Predefined event type to catch deleted Inbox events.
     */
    data object InboxDeletedEvent : EventType<InboxDeletedEventData>("inbox", "inboxDeleted")

    /**
     * Type to register on created entry Events.
     *
     * @property inboxId ID of the Inbox to observe
     */
    data class InboxEntryCreatedEvent(val inboxId: String) :
        EventType<InboxEntry>("inbox/$inboxId/entries", "inboxEntryCreated")

    /**
     * Type to register on deleting entries Events.
     *
     * @property inboxId ID of the Inbox to observe
     */
    data class InboxEntryDeletedEvent(val inboxId: String) :
        EventType<InboxEntryDeletedEventData>("inbox/$inboxId/entries", "inboxEntryDeleted")

    /**
     * Type to register for custom Context Events.
     *
     * @param contextId   ID of the Context to observe
     * @param channelName name of the Channel
     */
    data class ContextCustomEvent(val contextId: String, val channelName: String) :
        EventType<ContextCustomEventData>("context/$contextId/$channelName", "contextCustom")

    /**
     * Predefined event type to catch deleted Kvdb events.
     */
    data object KvdbDeletedEvent : EventType<KvdbDeletedEventData>("kvdb", "inboxDeleted")

    /**
     * Predefined event type to catch updated Kvdb events.
     */
    data object KvdbUpdatedEvent : EventType<Kvdb>("kvdb", "kvdbUpdated")

    /**
     * Predefined event type to catch updated Kvdb stats events.
     */
    data object KvdbStatsEvent : EventType<KvdbStatsEventData>("kvdb", "kvdbStatsChanged")

    /**
     * Predefined event type to catch created Kvdb events.
     */
    data object KvdbCreatedEvent : EventType<Kvdb>("kvdb", "kvdbCreated")

    /**
     * Predefined event type to catch created KvdbEntry events.
     *
     * @property kvdbId ID of the Kvdb to observe
     */
    data class KvdbNewEntry(val kvdbId: String) :
        EventType<KvdbEntry>("kvdb/$kvdbId/entries", "kvdbNewEntry")

    /**
     * Predefined event type to catch updated KvdbEntry events.
     *
     * @property kvdbId ID of the Kvdb to observe
     */
    data class KvdbEntryUpdatedEvent(val kvdbId: String) :
        EventType<KvdbEntry>("kvdb/$kvdbId/entries", "kvdbEntryUpdated")

    /**
     * Predefined event type to catch deleted KvdbEntry events.
     *
     * @property kvdbId ID of the Kvdb to observe
     */
    data class KvdbEntryDeletedEvent(val kvdbId: String) :
        EventType<KvdbDeletedEntryEventData>("kvdb/$kvdbId/entries", "kvdbEntryDeleted")
}
