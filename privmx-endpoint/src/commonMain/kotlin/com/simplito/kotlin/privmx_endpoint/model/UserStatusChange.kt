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
 * Contains information about the change of user status.
 *
 * @property action Timestamp of the change
 * @property timestamp User status change action, which can be "login" or "logout".
 */
class UserStatusChange(
    val action: String,
    val timestamp: Long?
)