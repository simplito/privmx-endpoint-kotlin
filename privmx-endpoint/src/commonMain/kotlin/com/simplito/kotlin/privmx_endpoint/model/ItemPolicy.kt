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
 * @property get     Determines who can get an item.
 * @property listMy  Determines who can list items created by themselves.
 * @property listAll Determines who can list all items.
 * @property create  Determines who can create an item.
 * @property update  Determines who can update an item.
 * @property delete  Determines who can delete an item.
 *
 * @category core
 * @group Core
 */
class ItemPolicy(
    val get: String?,
    val listMy: String?,
    val listAll: String?,
    val create: String?,
    val update: String?,
    val delete: String?
)