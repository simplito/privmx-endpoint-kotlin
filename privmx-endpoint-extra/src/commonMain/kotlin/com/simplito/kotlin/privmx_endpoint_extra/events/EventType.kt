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

import com.simplito.kotlin.privmx_endpoint.model.File
import com.simplito.kotlin.privmx_endpoint.model.Inbox
import com.simplito.kotlin.privmx_endpoint.model.InboxEntry
import com.simplito.kotlin.privmx_endpoint.model.Message
import com.simplito.kotlin.privmx_endpoint.model.Store
import com.simplito.kotlin.privmx_endpoint.model.Thread
import com.simplito.kotlin.privmx_endpoint.model.events.InboxDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.InboxEntryDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreFileDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreStatsChangedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadDeletedMessageEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadStatsEventData

/**
 * Defines the structure to register PrivMX Bridge event callbacks using [PrivmxEndpoint.registerCallback].
 *
 * @param <T> the type of data contained in the Event.
 * @category core
</T> */
sealed class EventType<out Any>(
    /**
     * Channel of this event type.
     */
    val channel: String,
    /**
     * This event type as a string.
     */
    val eventType: String,
) {

    data object ConnectedEvent : EventType<Unit>("", "libConnected")

    data object LibBreakEvent : EventType<Unit>("", "libBreak")

    data object DisconnectedEvent : EventType<Unit>("", "libDisconnected")

    data object ThreadCreatedEvent : EventType<Unit>("thread", "threadCreated")
    data object ThreadUpdatedEvent : EventType<Thread>("thread", "threadUpdated")
    data object ThreadStatsChangedEvent : EventType<ThreadStatsEventData>("thread", "threadStats")
    data object ThreadDeletedEvent : EventType<ThreadDeletedEventData>("thread", "threadDeleted")
    data object StoreCreatedEvent : EventType<Store>("store", "storeCreated")
    data object StoreUpdatedEvent : EventType<Store>("store", "storeUpdated")
    data object StoreStatsChangedEvent : EventType<StoreStatsChangedEventData>("store", "storeStatsChanged")
    data object StoreDeletedEvent : EventType<StoreDeletedEventData>("store", "storeDeleted")
    data class ThreadNewMessageEvent(val threadId: String): EventType<Message>("thread/$threadId/messages","threadNewMessage")

    data class ThreadMessageUpdatedEvent(val threadId: String): EventType<Message>("thread/$threadId/messages","threadUpdatedMessage")

    data class ThreadMessageDeletedEvent(val threadId: String): EventType<ThreadDeletedMessageEventData>(
        "thread/$threadId/messages","threadMessageDeleted")

    data class StoreFileCreatedEvent(val storeId: String): EventType<File>("store/$storeId/files","storeFileCreated")

    data class StoreFileUpdatedEvent(val storeId: String): EventType<File>("store/$storeId/files","storeFileUpdated")

    data class StoreFileDeletedEvent(val storeId: String): EventType<StoreFileDeletedEventData>("store/$storeId/files","storeFileDeleted")

    data object InboxCreatedEvent: EventType<Inbox>("inbox","inboxCreated")
    data object InboxUpdatedEvent: EventType<Inbox>("inbox","inboxUpdated")
    data object InboxDeletedEvent: EventType<InboxDeletedEventData>("inbox","inboxDeleted")

    data class InboxEntryCreatedEvent(val inboxId: String): EventType<InboxEntry>("inbox/$inboxId/entries", "inboxEntryCreated")

    data class InboxEntryDeletedEvent(val inboxId: String): EventType<InboxEntryDeletedEventData>("inbox/$inboxId/entries", "inboxEntryDeleted")
}
