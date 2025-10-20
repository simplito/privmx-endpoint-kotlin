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

package com.simplito.kotlin.privmx_endpoint.model

/**
 * Contains information about the changed item in the collection.
 *
 * @property itemId ID of the item
 * @property action Item change action, which can be "create", "update" or "delete"
 */
class CollectionItemChange(
    val itemId: String,
    val action: String
)