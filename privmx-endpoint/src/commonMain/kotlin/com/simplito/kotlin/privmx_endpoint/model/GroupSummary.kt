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
 * Holds the information about a Group that a listing serves: identity, roster, epoch and policies.
 *
 * A listing deliberately carries no per-Group state, so there is no [Group.publicMeta]/[Group.privateMeta] here,
 * no [Group.schemaVersion] and no [Group.statusCode]. Call
 * [com.simplito.kotlin.privmx_endpoint.modules.group.GroupApi.getGroup] for any of those.
 *
 * @property contextId ID of the Context
 * @property groupId ID of the Group
 * @property groupPubKey Group identity public key (base58-DER encoded)
 * @property createDate Group creation timestamp
 * @property creator ID of user who created the Group
 * @property lastModificationDate Group last modification timestamp
 * @property lastModifier ID of the user who last modified the Group
 * @property users List of users (their IDs) with access to the Group
 * @property managers List of users (their IDs) with management rights
 * @property publicMetaVersion Public-metadata version
 * @property privateMetaVersion Private-metadata version
 * @property rosterVersion Roster version. Changes only on a membership change
 * @property policy Group's policies
 * @property type Optional type tag
 * @property keyVersion Epoch counter for the group identity keypair
 */
data class GroupSummary(
    val contextId: String,
    val groupId: String,
    val groupPubKey: String,
    val createDate: Long,
    val creator: String,
    val lastModificationDate: Long,
    val lastModifier: String,
    val users: List<String>,
    val managers: List<String>,
    val publicMetaVersion: Long,
    val privateMetaVersion: Long,
    val rosterVersion: Long,
    val policy: ContainerPolicy,
    val keyVersion: Long,
    val type: String?
)
