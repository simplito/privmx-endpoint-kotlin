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
 * Holds all available information about an Inbox.
 *
 * @property inboxId              ID of the Inbox
 * @property contextId            ID of the Context
 * @property createDate           Inbox creation timestamp
 * @property creator              ID of the user who created the Inbox
 * @property lastModificationDate Inbox last modification timestamp
 * @property lastModifier         ID of the user who last modified the Inbox
 * @property users                List of users (their IDs) with access to the Inbox
 * @property managers             List of users (their IDs) with management rights
 * @property version              Version number (changes on updates)
 * @property publicMeta           Inbox public metadata
 * @property privateMeta          Inbox private metadata
 * @property filesConfig          Inbox files configuration
 * @property policy               Inbox policies
 * @property statusCode           Status code of retrieval and decryption of the Inbox
 * @property schemaVersion        Version of the Inbox data structure and how it is encoded/encrypted.
 */
data class Inbox(
    val inboxId: String,
    val contextId: String,
    val createDate: Long?,
    val creator: String,
    val lastModificationDate: Long?,
    val lastModifier: String,
    val users: List<String>,
    val managers: List<String>,
    val version: Long?,
    val publicMeta: ByteArray,
    val privateMeta: ByteArray,
    val filesConfig: FilesConfig?,
    val policy: ContainerPolicyWithoutItem,
    val statusCode: Long?,
    val schemaVersion: Long?
) {
    /**
     * Holds all available information about an Inbox.
     *
     * @property inboxId              ID of the Inbox
     * @property contextId            ID of the Context
     * @property createDate           Inbox creation timestamp
     * @property creator              ID of the user who created the Inbox
     * @property lastModificationDate Inbox last modification timestamp
     * @property lastModifier         ID of the user who last modified the Inbox
     * @property users                List of users (their IDs) with access to the Inbox
     * @property managers             List of users (their IDs) with management rights
     * @property version              Version number (changes on updates)
     * @property publicMeta           Inbox public metadata
     * @property privateMeta          Inbox private metadata
     * @property filesConfig          Inbox files configuration
     * @property policy               Inbox policies
     * @property statusCode           Status code of retrieval and decryption of the Inbox
     */
    @Deprecated("Use primary constructor with new parameter.")
    constructor(
        inboxId: String,
        contextId: String,
        createDate: Long?,
        creator: String,
        lastModificationDate: Long?,
        lastModifier: String,
        users: List<String>,
        managers: List<String>,
        version: Long?,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        filesConfig: FilesConfig?,
        policy: ContainerPolicyWithoutItem,
        statusCode: Long?,
    ) : this(
        inboxId,
        contextId,
        createDate,
        creator,
        lastModificationDate,
        lastModifier,
        users,
        managers,
        version,
        publicMeta,
        privateMeta,
        filesConfig,
        policy,
        statusCode,
        null
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Inbox

        if (createDate != other.createDate) return false
        if (lastModificationDate != other.lastModificationDate) return false
        if (version != other.version) return false
        if (statusCode != other.statusCode) return false
        if (schemaVersion != other.schemaVersion) return false
        if (inboxId != other.inboxId) return false
        if (contextId != other.contextId) return false
        if (creator != other.creator) return false
        if (lastModifier != other.lastModifier) return false
        if (users != other.users) return false
        if (managers != other.managers) return false
        if (!publicMeta.contentEquals(other.publicMeta)) return false
        if (!privateMeta.contentEquals(other.privateMeta)) return false
        if (filesConfig != other.filesConfig) return false
        if (policy != other.policy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = createDate?.hashCode() ?: 0
        result = 31 * result + (lastModificationDate?.hashCode() ?: 0)
        result = 31 * result + (version?.hashCode() ?: 0)
        result = 31 * result + (statusCode?.hashCode() ?: 0)
        result = 31 * result + (schemaVersion?.hashCode() ?: 0)
        result = 31 * result + inboxId.hashCode()
        result = 31 * result + contextId.hashCode()
        result = 31 * result + creator.hashCode()
        result = 31 * result + lastModifier.hashCode()
        result = 31 * result + users.hashCode()
        result = 31 * result + managers.hashCode()
        result = 31 * result + publicMeta.contentHashCode()
        result = 31 * result + privateMeta.contentHashCode()
        result = 31 * result + (filesConfig?.hashCode() ?: 0)
        result = 31 * result + policy.hashCode()
        return result
    }

}
