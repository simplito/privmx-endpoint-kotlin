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
 * Contains ID of user and the corresponding public key.
 *
 * @property userId ID of the user.
 * @property pubKey User's public key.
 *
 * @category core
 * @group Core
 */
data class UserWithPubKey(
    val userId: String,
    val pubKey: String
)
