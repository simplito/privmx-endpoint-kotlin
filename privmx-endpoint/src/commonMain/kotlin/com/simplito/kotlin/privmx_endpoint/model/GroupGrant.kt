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
 * Represents a Group granted access to a container.
 *
 * @property groupId ID of the Group
 * @property role    Role held by the Group in the container ("user" or "manager")
 */
data class GroupGrant(
    val groupId: String,
    val role: String
)
