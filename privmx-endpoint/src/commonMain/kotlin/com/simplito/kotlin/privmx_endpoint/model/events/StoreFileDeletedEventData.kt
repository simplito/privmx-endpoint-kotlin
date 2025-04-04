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
 * Holds information about a file deleted from Store.
 *
 * @param fileId ID of the deleted file.
 * @param contextId ID of the Store's Context.
 * @param storeId ID of the Store of the deleted file.
 *
 * @category core
 * @group Events
 */
data class StoreFileDeletedEventData(
    /**
     * ID of the deleted file.
     */
    val fileId: String?,
    /**
     * ID of the Store's Context.
     */
    val contextId: String?,
    /**
     * ID of the deleted file's Store.
     */
    val storeId: String?
)
