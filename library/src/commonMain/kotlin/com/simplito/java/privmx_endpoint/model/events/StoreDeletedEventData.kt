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
package com.simplito.java.privmx_endpoint.model.events

/**
 * Holds information about deleted Store.
 * @category core
 * @group Events
 */
data class StoreDeletedEventData
/**
 * Creates instance of `StoreDeletedEventData`.
 * @param storeId ID of the deleted Store.
 */(
    /**
     * ID of the deleted Store.
     */
    val storeId: String?
)
