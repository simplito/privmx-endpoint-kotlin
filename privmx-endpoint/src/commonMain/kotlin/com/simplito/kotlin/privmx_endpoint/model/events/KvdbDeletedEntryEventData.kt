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

package com.simplito.java.privmx_endpoint.model.events

/**
 * Holds data of event that arrives when KVDB entry is deleted.
 *
 * @property kvdbId KVDB ID
 * @property kvdbEntryKey Key of deleted Entry
 */
data class KvdbDeletedEntryEventData(
    val kvdbId: String,
    val kvdbEntryKey: String
)