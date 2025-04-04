//
// PrivMX Endpoint Java.
// Copyright © 2024 Simplito sp. z o.o.
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
 * @param storeId ID of the changed Store's Context.
 * @param contextId ID of the changed Store.
 * @param lastFileDate Updated date of the last file in the Store.
 * @param filesCount Updated number of files in the Store.
 *
 * @category core
 * @group Events
 */
data class StoreStatsChangedEventData
(
    /**
     * ID of the changed Store.
     */
    val storeId: String?,
    /**
     * ID of the changed Store's Context.
     */
    val contextId: String?,
    /**
     * Updated date of the last file in the Store.
     */
    val lastFileDate: Long?,
    /**
     * Updated number of files in the Store.
     */
    val filesCount: Long?
)
