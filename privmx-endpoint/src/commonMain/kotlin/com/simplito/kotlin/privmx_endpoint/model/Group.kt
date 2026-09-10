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
 * Holds all available information about a Group.
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
 * @property version Metadata version. Changes only on [com.simplito.kotlin.privmx_endpoint.modules.group.GroupApi.updateGroup] — a membership change does not touch it
 * @property rosterVersion Roster version. Changes only on a membership change
 * @property publicMeta Group's public metadata
 * @property privateMeta Group's private metadata
 * @property policy Group's policies
 * @property statusCode Retrieval and decryption status code
 * @property schemaVersion Version of the Group data structure and how it is encoded/encrypted
 * @property type Optional type tag
 * @property keyVersion Epoch counter for the group identity keypair. Increments by 1 on each key rotation triggered by a member removal
 */
data class Group(
    val contextId: String,
    val groupId: String,
    val groupPubKey: String,
    val createDate: Long,
    val creator: String,
    val lastModificationDate: Long,
    val lastModifier: String,
    val users: List<String>,
    val managers: List<String>,
    val version: Long,
    val rosterVersion: Long,
    val publicMeta: ByteArray,
    val privateMeta: ByteArray,
    val policy: ContainerPolicy,
    val statusCode: Long,
    val schemaVersion: Long,
    val keyVersion: Long,
    val type: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Group

        if (createDate != other.createDate) return false
        if (lastModificationDate != other.lastModificationDate) return false
        if (version != other.version) return false
        if (rosterVersion != other.rosterVersion) return false
        if (statusCode != other.statusCode) return false
        if (schemaVersion != other.schemaVersion) return false
        if (keyVersion != other.keyVersion) return false
        if (contextId != other.contextId) return false
        if (groupId != other.groupId) return false
        if (groupPubKey != other.groupPubKey) return false
        if (creator != other.creator) return false
        if (lastModifier != other.lastModifier) return false
        if (users != other.users) return false
        if (managers != other.managers) return false
        if (!publicMeta.contentEquals(other.publicMeta)) return false
        if (!privateMeta.contentEquals(other.privateMeta)) return false
        if (policy != other.policy) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = createDate.hashCode()
        result = 31 * result + lastModificationDate.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + rosterVersion.hashCode()
        result = 31 * result + statusCode.hashCode()
        result = 31 * result + schemaVersion.hashCode()
        result = 31 * result + keyVersion.hashCode()
        result = 31 * result + contextId.hashCode()
        result = 31 * result + groupId.hashCode()
        result = 31 * result + groupPubKey.hashCode()
        result = 31 * result + creator.hashCode()
        result = 31 * result + lastModifier.hashCode()
        result = 31 * result + users.hashCode()
        result = 31 * result + managers.hashCode()
        result = 31 * result + publicMeta.contentHashCode()
        result = 31 * result + privateMeta.contentHashCode()
        result = 31 * result + policy.hashCode()
        result = 31 * result + (type?.hashCode() ?: 0)
        return result
    }

}
