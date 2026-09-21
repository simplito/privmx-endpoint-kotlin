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
 * Carries the Group's verified public key along with grant info,
 * used when granting a Group access to a container.
 *
 * @property groupId     ID of the Group
 * @property role        Role held by the Group in the container
 * @property groupPubKey Verified Group identity public key (base58-DER encoded)
 * @property groupEpoch  Epoch at which [groupPubKey] was verified (equals `Group.keyVersion`).
 * Defaults to `0` for compatibility with Bridges that do not enforce per-epoch coverage.
 */
data class GroupGrantWithKey(
    val groupId: String,
    val role: GroupRole,
    val groupPubKey: String,
    val groupEpoch: Long = 0
)
