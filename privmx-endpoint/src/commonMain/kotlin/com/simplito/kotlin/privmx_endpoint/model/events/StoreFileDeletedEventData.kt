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
 * Holds information about a file deleted from Store.
 *
 * @property fileId ID of the deleted file
 * @property contextId ID of the Store's Context
 * @property storeId ID of the deleted file's Store
 */
data class StoreFileDeletedEventData(
    val fileId: String,
    val contextId: String,
    val storeId: String
)
