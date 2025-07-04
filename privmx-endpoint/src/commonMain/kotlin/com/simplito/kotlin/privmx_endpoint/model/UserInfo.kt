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
 * Contains Information about user.
 *
 * @property user       User publicKey and userId.
 * @property isActive    Is user connected to bridge.
 */
data class UserInfo(
    val user: UserWithPubKey,
    val isActive: Boolean
)