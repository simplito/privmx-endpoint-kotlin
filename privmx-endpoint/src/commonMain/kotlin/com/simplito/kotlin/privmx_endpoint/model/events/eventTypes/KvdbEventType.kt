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
 * Defines the types of events that can occur within the KVDB for which a client can subscribe.
 * This enum lists the various actions or changes that can happen to
 * KVDBs and their entries, allowing observers to be notified of specific occurrences.
 */
enum class KvdbEventType : EventType {
    /**
     * Type of event triggered when a new KVDB is created.
     */
    KVDB_CREATE,
    /**
     * Type of event triggered when an existing KVDB is updated.
     */
    KVDB_UPDATE,
    /**
     * Type of event triggered when a KVDB is deleted.
     */
    KVDB_DELETE,
    /**
     * Type of event triggered when a KVDB statistics are updated.
     */
    KVDB_STATS,
    /**
     * Type of event triggered when a new entry is created within a KVDB.
     */
    ENTRY_CREATE,
    /**
     * Type of event triggered when an existing KVDB entry is updated.
     */
    ENTRY_UPDATE,
    /**
     * Type of event triggered when a KVDB entry is deleted.
     */
    ENTRY_DELETE,
    /**
     * Type of event triggered when a collection changes.
     */
    COLLECTION_CHANGE
}
