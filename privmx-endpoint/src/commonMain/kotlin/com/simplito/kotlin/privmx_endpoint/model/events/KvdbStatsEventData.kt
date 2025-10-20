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
package com.simplito.kotlin.privmx_endpoint.model.events

/**
 * Holds data of event that arrives when KVDB stats change.
 *
 * @property kvdbId KVDB ID
 * @property lastEntryDate Timestamp of the most recent KVDB item
 * @property entries  Updated number of entries in the KVDB
 */
data class KvdbStatsEventData(
    val kvdbId: String,
    val lastEntryDate: Long?,
    val entries: Long?
) 