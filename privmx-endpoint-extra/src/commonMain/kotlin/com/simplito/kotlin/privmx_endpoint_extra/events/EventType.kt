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

import com.simplito.kotlin.privmx_endpoint.model.Event
import com.simplito.kotlin.privmx_endpoint.model.File
import com.simplito.kotlin.privmx_endpoint.model.Inbox
import com.simplito.kotlin.privmx_endpoint.model.InboxEntry
import com.simplito.kotlin.privmx_endpoint.model.Kvdb
import com.simplito.kotlin.privmx_endpoint.model.KvdbEntry
import com.simplito.kotlin.privmx_endpoint.model.Message
import com.simplito.kotlin.privmx_endpoint.model.Store
import com.simplito.kotlin.privmx_endpoint.model.Thread
import com.simplito.kotlin.privmx_endpoint.model.events.CollectionChangedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ContextCustomEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ContextUserEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ContextUsersStatusChangedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.InboxDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.InboxEntryDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.KvdbDeletedEntryEventData
import com.simplito.kotlin.privmx_endpoint.model.events.KvdbDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.KvdbStatsEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreFileDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreStatsChangedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadDeletedMessageEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadStatsEventData
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.CoreEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.CustomEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.EventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.InboxEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.KvdbEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.StoreEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.ThreadEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.CoreEventType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.InboxEventType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.KvdbEventType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.StoreEventType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.ThreadEventType
import com.simplito.kotlin.privmx_endpoint_extra.events.EventType.DisconnectedEvent
import com.simplito.kotlin.privmx_endpoint_extra.events.EventType.LibBreakEvent
import com.simplito.kotlin.privmx_endpoint_extra.lib.PrivmxEndpoint
import kotlin.reflect.KClass

/**
 * Check if event was produced by PrivMX library.
 *
 * @param eventTypeName name of event instance to check
 * @return true if event is one of the PrivMX library Events.
 */
internal fun isLibEvent(eventTypeName: String): Boolean =
    EventType.ConnectedEvent.eventName == eventTypeName
            || LibBreakEvent.eventName == eventTypeName
            || DisconnectedEvent.eventName == eventTypeName

/**
 * Defines the structure to register PrivMX Bridge event callbacks using [PrivmxEndpoint.registerCallback].
 *
 * @param T the type of data contained in the Event
 */
sealed class EventType<T : Any>(
    val eventName: String,
    open var channelName: String?,
    val libEventType: com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.EventType?,
    val eventSelectorType: EventSelectorType?,
    val eventSelectorId: String?,
    val eventResultClass: KClass<T>
) {
    constructor (
        eventName: String,
        libEventType: com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.EventType,
        eventSelectorType: EventSelectorType,
        eventSelectorId: String,
        eventResultClass: KClass<T>
    ) : this(eventName, null, libEventType, eventSelectorType, eventSelectorId, eventResultClass)

    constructor (
        eventName: String,
        eventResultClass: KClass<T>
    ) : this(eventName, null, null, null, null, eventResultClass)


    /**
     * Predefined event type that captures successful platform connection events.
     */
    data object ConnectedEvent : EventType<Unit>(
        "libConnected",
        Unit::class
    )

    /**
     * Predefined event type to catch special events.
     * This type could be used to emit/handle events with custom implementations (e.g. to break event loops).
     */
    data object LibBreakEvent : EventType<Unit>(
        "libBreak",
        Unit::class
    )

    /**
     * Predefined event type to catch disconnection events.
     */
    data object DisconnectedEvent : EventType<Unit>(
        "libDisconnected",
        Unit::class
    )

    /**
     * Predefined event type to catch created Thread events.
     *
     * @property contextId Context Id on which you listen for events
     * @return Predefined event type to catch created Thread events.
     */
    data class ThreadCreatedEvent(
        val contextId: String
    ) : EventType<Thread>(
        "threadCreated",
        ThreadEventType.THREAD_CREATE,
        ThreadEventSelectorType.CONTEXT_ID,
        contextId,
        Thread::class
    )

    /**
     * Predefined event type to catch updated Thread events.
     *
     * @property selectorType scope on which you listen for events
     * @property selectorId   ID of the selector
     * @return Predefined event type to catch updated Thread events.
     */
    data class ThreadUpdatedEvent(
        val selectorType: ThreadEventSelectorType,
        val selectorId: String
    ) : EventType<Thread>(
        "threadUpdated",
        ThreadEventType.THREAD_UPDATE,
        selectorType,
        selectorId,
        Thread::class
    )

    /**
     * Predefined event type to catch updated Thread stats events.
     *
     * @property selectorType scope on which you listen for events
     * @property selectorId   ID of the selector
     * @return Predefined event type to catch updated Thread stats events.
     */
    data class ThreadStatsChangedEvent(
        val selectorType: ThreadEventSelectorType,
        val selectorId: String
    ) : EventType<ThreadStatsEventData>(
        "threadStats",
        ThreadEventType.THREAD_STATS,
        selectorType,
        selectorId, ThreadStatsEventData::class
    )

    /**
     * Predefined event type to catch deleted Thread events.
     *
     * @property selectorType scope on which you listen for events
     * @property selectorId   ID of the selector
     * @return Predefined event type to catch deleted Thread events.
     */
    data class ThreadDeletedEvent(
        val selectorType: ThreadEventSelectorType,
        val selectorId: String
    ) : EventType<ThreadDeletedEventData>(
        "threadDeleted",
        ThreadEventType.THREAD_DELETE,
        selectorType,
        selectorId, ThreadDeletedEventData::class
    )

    /**
     * Predefined event type to catch created Store events.
     *
     * @property contextId Context Id on which you listen for events
     * @return Predefined event type to catch created Store events.
     */
    data class StoreCreatedEvent(
        val contextId: String
    ) : EventType<Store>(
        "storeCreated",
        StoreEventType.STORE_CREATE,
        StoreEventSelectorType.CONTEXT_ID,
        contextId, Store::class
    )

    /**
     * Predefined event type to catch updated Store events.
     *
     * @property selectorType scope on which you listen for events
     * @property selectorId   ID of the selector
     * @return Predefined event type to catch updated Store events.
     */
    data class StoreUpdatedEvent(
        val selectorType: StoreEventSelectorType,
        val selectorId: String
    ) : EventType<Store>(
        "storeUpdated",
        StoreEventType.STORE_UPDATE,
        selectorType,
        selectorId, Store::class
    )

    /**
     * Predefined event type to catch updated Store stats events.
     *
     * @property selectorType scope on which you listen for events
     * @property selectorId   ID of the selector
     * @return Predefined event type to catch updated Store stats events.
     */
    data class StoreStatsChangedEvent(
        val selectorType: StoreEventSelectorType,
        val selectorId: String
    ) : EventType<StoreStatsChangedEventData>(
        "storeStatsChanged",
        StoreEventType.STORE_UPDATE,
        selectorType,
        selectorId,
        StoreStatsChangedEventData::class
    )

    /**
     * Predefined event type to catch deleted Store events.
     *
     * @property selectorType scope on which you listen for events
     * @property selectorId   ID of the selector
     * @return Predefined event type to catch deleted Store events.
     */
    data class StoreDeletedEvent(
        val selectorType: StoreEventSelectorType,
        val selectorId: String
    ) : EventType<StoreDeletedEventData>(
        "storeDeleted",
        StoreEventType.STORE_DELETE,
        selectorType,
        selectorId, StoreDeletedEventData::class
    )

    /**
     * Type to register on new message Events.
     *
     * @property threadId ID of the Thread to observe
     */
    data class ThreadNewMessageEvent(
        val selectorType: ThreadEventSelectorType,
        val selectorId: String
    ) : EventType<Message>(
        "threadNewMessage",
        ThreadEventType.MESSAGE_CREATE,
        selectorType,
        selectorId,
        Message::class
    )

    /**
     * Type to register on message update Events.
     *
     * @property threadId ID of the Thread to observe
     */
    data class ThreadMessageUpdatedEvent(
        val selectorType: ThreadEventSelectorType,
        val selectorId: String
    ) : EventType<Message>(
        "threadUpdatedMessage",
        ThreadEventType.MESSAGE_UPDATE,
        selectorType,
        selectorId,
        Message::class
    )

    /**
     * Type to register on deleted message Events.
     *
     * @property threadId ID of the Thread to observe
     */
    data class ThreadMessageDeletedEvent(
        val selectorType: ThreadEventSelectorType,
        val selectorId: String
    ) : EventType<ThreadDeletedMessageEventData>(
        "threadMessageDeleted",
        ThreadEventType.MESSAGE_DELETE,
        selectorType,
        selectorId,
        ThreadDeletedMessageEventData::class
    )

    /**
     * Type to register on created file Events.
     *
     * @property storeId ID of the store to observe
     */
    data class StoreFileCreatedEvent(
        val selectorType: StoreEventSelectorType,
        val selectorId: String
    ) : EventType<File>(
        "storeFileCreated",
        StoreEventType.FILE_CREATE,
        selectorType,
        selectorId,
        File::class
    )

    /**
     * Type to register on file update Events.
     *
     * @property storeId ID of the Store to observe
     */
    data class StoreFileUpdatedEvent(
        val selectorType: StoreEventSelectorType,
        val selectorId: String
    ) : EventType<File>(
        "storeFileUpdated",
        StoreEventType.FILE_UPDATE,
        selectorType,
        selectorId,
        File::class
    )

    /**
     * Type to register on deleted file Events.
     *
     * @property storeId ID of the Store to observe
     */
    data class StoreFileDeletedEvent(
        val selectorType: StoreEventSelectorType,
        val selectorId: String
    ) : EventType<StoreFileDeletedEventData>(
        "storeFileDeleted",
        StoreEventType.FILE_DELETE,
        selectorType,
        selectorId,
        StoreFileDeletedEventData::class
    )

    /**
     * Predefined event type to catch created Inbox events.
     */
    data class InboxCreatedEvent(
        val selectorType: InboxEventSelectorType,
        val selectorId: String
    ) : EventType<Inbox>(
        "inboxCreated",
        InboxEventType.INBOX_CREATE,
        selectorType,
        selectorId,
        Inbox::class
    )

    /**
     * Predefined event type to catch update Inbox events.
     */
    data class InboxUpdatedEvent(
        val selectorType: InboxEventSelectorType,
        val selectorId: String
    ) : EventType<Inbox>(
        "inboxUpdated",
        InboxEventType.INBOX_UPDATE,
        selectorType,
        selectorId,
        Inbox::class
    )

    /**
     * Predefined event type to catch deleted Inbox events.
     */
    data class InboxDeletedEvent(
        val selectorType: InboxEventSelectorType,
        val selectorId: String
    ) : EventType<InboxDeletedEventData>(
        "inboxDeleted",
        InboxEventType.INBOX_DELETE,
        selectorType,
        selectorId,
        InboxDeletedEventData::class
    )

    /**
     * Type to register on created entry Events.
     *
     * @property inboxId ID of the Inbox to observe
     */
    data class InboxEntryCreatedEvent(
        val selectorType: InboxEventSelectorType,
        val selectorId: String
    ) : EventType<InboxEntry>(
        "inboxEntryCreated",
        InboxEventType.ENTRY_CREATE,
        selectorType,
        selectorId,
        InboxEntry::class
    )

    /**
     * Type to register on deleting entries Events.
     *
     * @property inboxId ID of the Inbox to observe
     */
    data class InboxEntryDeletedEvent(
        val selectorType: InboxEventSelectorType,
        val selectorId: String
    ) : EventType<InboxEntryDeletedEventData>(
        "inboxEntryDeleted",
        InboxEventType.ENTRY_DELETE,
        selectorType,
        selectorId,
        InboxEntryDeletedEventData::class
    )

    /**
     * Type to register for custom Context Events.
     *
     * @property contextId   ID of the Context to observe
     * @property channelName name of the Channel
     */
    data class ContextCustomEvent(
        val contextId: String,
        val channel: String
    ) : EventType<ContextCustomEventData>(
        "contextCustom",
        channel,
        null,
        CustomEventSelectorType.CONTEXT_ID,
        contextId,
        ContextCustomEventData::class
    )

    /**
     * Predefined event type to catch created KVDB events.
     *
     * @property contextId Context Id on which you listen for events
     * @return Predefined event type to catch created KVDB events.
     */
    data class KvdbCreatedEvent(
        val contextId: String
    ) : EventType<Kvdb>(
        "kvdbCreated",
        KvdbEventType.KVDB_CREATE,
        KvdbEventSelectorType.CONTEXT_ID,
        contextId,
        Kvdb::class
    )

    /**
     * Predefined event type to catch updated KVDB events.
     *
     * @property selectorType scope on which you listen for events
     * @property selectorId   ID of the selector
     * @return Predefined event type to catch updated KVDB events.
     */
    data class KvdbUpdatedEvent(
        val selectorType: KvdbEventSelectorType,
        val selectorId: String
    ) : EventType<Kvdb>(
        "kvdbUpdated",
        KvdbEventType.KVDB_UPDATE,
        selectorType,
        selectorId,
        Kvdb::class
    )

    /**
     * Predefined event type to catch updated KVDB stats events.
     *
     * @property selectorType scope on which you listen for events
     * @property selectorId   ID of the selector
     * @return Predefined event type to catch updated KVDB stats events.
     */
    data class KvdbStatsEvent(
        val selectorType: KvdbEventSelectorType,
        val selectorId: String
    ) : EventType<KvdbStatsEventData>(
        "kvdbStatsChanged",
        KvdbEventType.KVDB_STATS,
        selectorType,
        selectorId,
        KvdbStatsEventData::class
    )

    /**
     * Predefined event type to catch deleted KVDB events.
     *
     * @property selectorType scope on which you listen for events
     * @property selectorId   ID of the selector
     * @return Predefined event type to catch deleted KVDB events.
     */
    data class KvdbDeletedEvent(
        val selectorType: KvdbEventSelectorType,
        val selectorId: String
    ) : EventType<KvdbDeletedEventData>(
        "kvdbDeleted",
        KvdbEventType.KVDB_DELETE,
        selectorType,
        selectorId,
        KvdbDeletedEventData::class
    )

    /**
     * Predefined event type to catch created KVDB entries events.
     *
     * @property selectorType scope on which you listen for events
     * @property selectorId   ID of the selector
     * @return Predefined event type to catch created KVDB entries events.
     */
    data class KvdbNewEntryEvent(
        val selectorType: KvdbEventSelectorType,
        val selectorId: String
    ) : EventType<KvdbEntry>(
        "kvdbNewEntry",
        KvdbEventType.ENTRY_CREATE,
        selectorType,
        selectorId,
        KvdbEntry::class
    )

    /**
     * Predefined event type to catch updated KVDB entries events.
     *
     * @property selectorType scope on which you listen for events
     * @property selectorId   ID of the selector
     * @return Predefined event type to catch updated KVDB entries events.
     */
    data class KvdbEntryUpdatedEvent(
        val selectorType: KvdbEventSelectorType,
        val selectorId: String
    ) : EventType<KvdbEntry>(
        "kvdbEntryUpdated",
        KvdbEventType.ENTRY_UPDATE,
        selectorType,
        selectorId,
        KvdbEntry::class
    )

    /**
     * Predefined event type to catch deleted KVDB entries events.
     *
     * @property selectorType scope on which you listen for events
     * @property selectorId   ID of the selector
     * @return Predefined event type to catch deleted KVDB entries events.
     */
    data class KvdbEntryDeletedEvent(
        val selectorType: KvdbEventSelectorType,
        val selectorId: String
    ) : EventType<KvdbDeletedEntryEventData>(
        "kvdbEntryDeleted",
        KvdbEventType.ENTRY_DELETE,
        selectorType,
        selectorId,
        KvdbDeletedEntryEventData::class
    )


    class CollectionChangedEvent private constructor(
        val eventType: com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.EventType,
        val selectorType: EventSelectorType,
        val selectorId: String
    ) : EventType<CollectionChangedEventData>(
        "collectionChanged",
        eventType,
        selectorType,
        selectorId,
        CollectionChangedEventData::class
    ) {
        /**
         * Returns instance to register for collection change events.
         *
         * @property selectorType scope on which you listen for events
         * @property selectorId   ID of the selector
         * @return Predefined event type to catch collection change events
         */
        constructor(
            selectorType: ThreadEventSelectorType,
            selectorId: String
        ) : this(
            ThreadEventType.COLLECTION_CHANGE,
            selectorType,
            selectorId,
        )

        /**
         * Returns instance to register for collection change events.
         *
         * @property selectorType scope on which you listen for events
         * @property selectorId   ID of the selector
         * @return Predefined event type to catch collection change events
         */
        constructor(
            selectorType: StoreEventSelectorType,
            selectorId: String
        ) : this(
            StoreEventType.COLLECTION_CHANGE,
            selectorType,
            selectorId,
        )

        /**
         * Returns instance to register for collection change events.
         *
         * @property selectorType scope on which you listen for events
         * @property selectorId   ID of the selector
         * @return Predefined event type to catch collection change events
         */
        constructor(
            selectorType: InboxEventSelectorType,
            selectorId: String
        ) : this(
            InboxEventType.COLLECTION_CHANGE,
            selectorType,
            selectorId,
        )

        /**
         * Returns instance to register for collection change events.
         *
         * @property selectorType scope on which you listen for events
         * @property selectorId   ID of the selector
         * @return Predefined event type to catch collection change events
         */
        constructor(
            selectorType: KvdbEventSelectorType,
            selectorId: String
        ) : this(
            KvdbEventType.COLLECTION_CHANGE,
            selectorType,
            selectorId,
        )
    }

    /**
     * Returns instance to register for user added to the Context events.
     *
     * @property contextId Context Id on which you listen for events
     * @return Predefined event type to catch user added to the Context events
     */
    data class ContextUserAddedEvent(
        val contextId: String
    ) : EventType<ContextUserEventData>(
        "contextUserAdded",
        CoreEventType.USER_ADD,
        CoreEventSelectorType.CONTEXT_ID,
        contextId,
        ContextUserEventData::class
    )

    /**
     * Returns instance to register for user removed from the Context events.
     *
     * @property contextId Context Id on which you listen for events
     * @return Predefined event type to catch user removed from the Context events
     */
    data class ContextUserRemovedEvent(
        val contextId: String
    ) : EventType<ContextUserEventData>(
        "contextUserRemoved",
        CoreEventType.USER_REMOVE,
        CoreEventSelectorType.CONTEXT_ID,
        contextId,
        ContextUserEventData::class
    )

    /**
     * Returns instance to register for user status change events.
     *
     * @property contextId Context Id on which you listen for events
     * @return Predefined event type to catch user status change events
     */
    data class ContextUsersStatusChangeEvent(
        val contextId: String
    ) : EventType<ContextUsersStatusChangedEventData>(
        "contextUserStatusChanged",
        CoreEventType.USER_STATUS,
        CoreEventSelectorType.CONTEXT_ID,
        contextId,
        ContextUsersStatusChangedEventData::class
    )
}
