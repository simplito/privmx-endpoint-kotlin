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
 * Contains the user with their status change action.
 *
 * @property user User
 * @property action User status change action
 */
class UserWithAction(
    val user: UserWithPubKey,
    val action: String
)