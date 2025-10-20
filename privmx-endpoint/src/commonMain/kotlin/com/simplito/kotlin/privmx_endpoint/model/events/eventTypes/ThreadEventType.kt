/*
 *
 * PrivMX Endpoint Kotlin.
 * Copyright © 2025 Simplito sp. z o.o.
 *
 * This file is part of the PrivMX Platform (https://privmx.dev).
 * This software is Licensed under the MIT License.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.simplito.kotlin.privmx_endpoint.model.events.eventTypes

/**
 * Defines the types of events that can occur within the thread for which a client can subscribe.
 * This enum lists the various actions or changes that can happen to
 * threads and their messages, allowing observers to be notified of specific occurrences.
 */
enum class ThreadEventType : EventType {
    /**
     * Type of event triggered when a new thread is created.
     */
    THREAD_CREATE,
    /**
     * Type of event triggered when an existing thread is updated.
     */
    THREAD_UPDATE,
    /**
     * Type of event triggered when a thread is deleted.
     */
    THREAD_DELETE,
    /**
     * Type of event triggered when a thread statistics are updated.
     */
    THREAD_STATS,
    /**
     * Type of event triggered when a new message is created within a thread.
     */
    MESSAGE_CREATE,
    /**
     * Type of event triggered when an existing message is updated.
     */
    MESSAGE_UPDATE,
    /**
     * Type of event triggered when a message is deleted.
     */
    MESSAGE_DELETE,
    /**
     * Type of event triggered when a collection changes.
     */
    COLLECTION_CHANGE
}
