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
 * Defines the types of events that can occur within the store for which a client can subscribe.
 * This enum lists the various actions or changes that can happen to
 * stores and their files, allowing observers to be notified of specific occurrences.
 */
enum class StoreEventType : EventType {
    /**
     * Type of event triggered when a new store is created.
     */
    STORE_CREATE,
    /**
     * Type of event triggered when an existing store is updated.
     */
    STORE_UPDATE,
    /**
     * Type of event triggered when a store is deleted.
     */
    STORE_DELETE,
    /**
     * Type of event triggered when a store statistics are updated.
     */
    STORE_STATS,
    /**
     * Type of event triggered when a new file is created within a store.
     */
    FILE_CREATE,
    /**
     * Type of event triggered when an existing file is updated.
     */
    FILE_UPDATE,
    /**
     * Type of event triggered when a file is deleted.
     */
    FILE_DELETE,
    /**
     * Type of event triggered when a collection changes.
     */
    COLLECTION_CHANGE
}
