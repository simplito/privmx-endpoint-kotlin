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
 * Holds information about changes in a Store's statistics.
 *
 * @property storeId ID of the changed Store
 * @property contextId ID of the changed Store's Context
 * @property lastFileDate Updated date of the last file in the Store
 * @property filesCount Updated number of files in the Store
 */
data class StoreStatsChangedEventData(
    val storeId: String,
    val contextId: String,
    val lastFileDate: Long?,
    val filesCount: Long?
)
