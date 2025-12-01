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

import com.simplito.kotlin.privmx_endpoint.model.UserWithAction

/**
 * Holds data of event that arrives when the statuses of users in the Context change.
 *
 * @property contextId ID of the Context
 * @property users List of users with their changed statuses
 */
data class ContextUsersStatusChangedEventData(
    val contextId: String,
    val users: List<UserWithAction>,
)