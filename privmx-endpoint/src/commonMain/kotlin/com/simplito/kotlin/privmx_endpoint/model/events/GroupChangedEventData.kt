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

package com.simplito.kotlin.privmx_endpoint.model.events

/**
 * Holds what changed about a Group, and nothing that grows with it.
 *
 * A Group event no longer carries the Group itself: the four counters are enough to decide whether the change
 * matters — and which plane moved. Call
 * [com.simplito.kotlin.privmx_endpoint.modules.group.GroupApi.getGroup] when it does.
 *
 * @property groupId Group ID
 * @property contextId Context ID
 * @property publicMetaVersion Public-metadata version after the change
 * @property privateMetaVersion Private-metadata version after the change
 * @property rosterVersion Roster version after the change. Moves only on a membership change
 * @property keyVersion Group key epoch after the change
 * @property changeKind Which operation changed the Group: "created", "publicMetaUpdated", "privateMetaUpdated",
 * "policyUpdated", "keyRotated", "memberAdded", "memberRemoved", "eraCut" or "archivePruned"
 */
data class GroupChangedEventData(
    val groupId: String,
    val contextId: String,
    val publicMetaVersion: Long,
    val privateMetaVersion: Long,
    val rosterVersion: Long,
    val keyVersion: Long,
    val changeKind: String
)
