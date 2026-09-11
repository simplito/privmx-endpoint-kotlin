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
 * One member to seat in a Group, and the role they take.
 *
 * The role travels per member rather than per call: seating a manager and a user together is one delta over the
 * union of their paths.
 *
 * @property user ID of the user and their public key
 * @property role "user" or "manager"
 */
data class GroupMemberToAdd(
    val user: UserWithPubKey,
    val role: String
)
