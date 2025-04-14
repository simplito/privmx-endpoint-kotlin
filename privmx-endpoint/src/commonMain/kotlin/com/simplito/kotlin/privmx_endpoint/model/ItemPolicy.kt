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
package com.simplito.kotlin.privmx_endpoint.model

/**
 * Contains container items policies.
 *
 * @category core
 * @group Core
 */
class ItemPolicy
/**
 * Creates instance of [ItemPolicy].
 *
 * @param get     determines who can get an item
 * @param listMy  determines who can list items created by themselves
 * @param listAll determines who can list all items
 * @param create  determines who can create an item
 * @param update  determines who can update an item
 * @param delete  determines who can delete an item
 */(
    /**
     * Determines who can get an item.
     */
    val get: String?,
    /**
     * Determines who can list items created by themselves.
     */
    val listMy: String?,
    /**
     * Determines who can list all items.
     */
    val listAll: String?,
    /**
     * Determines who can create an item.
     */
    val create: String?,
    /**
     * Determines who can update an item.
     */
    val update: String?,
    /**
     * Determines who can delete an item.
     */
    val delete: String?
)