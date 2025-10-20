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

package com.simplito.kotlin.privmx_endpoint.model.events

import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey

/**
 * Contains information about a user who was added to or removed from the Context.
 *
 * @property contextId ID of the Context
 * @property user User
 */
data class ContextUserEventData(
    val contextId: String,
    val user: UserWithPubKey,
)