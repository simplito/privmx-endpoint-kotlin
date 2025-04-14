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
 * @category inbox
 * @group Inbox
 */
data class Inbox
/**
 * Creates instance of `Inbox`.
 *
 * @param inboxId              ID of the Inbox.
 * @param contextId            ID of the Context.
 * @param createDate           Inbox creation timestamp.
 * @param creator              ID of the user who created the Inbox.
 * @param lastModificationDate Inbox last modification timestamp.
 * @param lastModifier         ID of the user who last modified the Inbox.
 * @param users                List of users (their IDs) with access to the Inbox.
 * @param managers             List of users (their IDs) with management rights.
 * @param version              Version number (changes on updates).
 * @param publicMeta           Inbox public metadata.
 * @param privateMeta          Inbox private metadata.
 * @param filesConfig          Inbox files configuration.
 * @param policy               Inbox policies.
 * @param statusCode           Status code of retrieval and decryption of the `Inbox`.
 */(
    /**
     * ID of the Inbox.
     */
    val inboxId: String,
    /**
     * ID of the Context.
     */
    val contextId: String,
    /**
     * Inbox creation timestamp.
     */
    val createDate: Long?,
    /**
     * ID of the user who created the Inbox.
     */
    val creator: String,
    /**
     * Inbox last modification timestamp.
     */
    val lastModificationDate: Long?,
    /**
     * ID of the user who last modified the Inbox.
     */
    val lastModifier: String,
    /**
     * List of users (their IDs) with access to the Inbox.
     */
    val users: List<String>,
    /**
     * List of users (their IDs) with management rights.
     */
    val managers: List<String>,
    /**
     * Version number (changes on updates).
     */
    val version: Long?,
    /**
     * Inbox public metadata.
     */
    val publicMeta: ByteArray,
    /**
     * Inbox private metadata.
     */
    val privateMeta: ByteArray,
    /**
     * Inbox files configuration.
     */
    val filesConfig: FilesConfig?,
    /**
     * Inbox policies.
     */
    val policy: ContainerPolicyWithoutItem,
    /**
     * Status code of retrieval and decryption of the `Inbox`.
     */
    val statusCode: Long?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Inbox

        if (createDate != other.createDate) return false
        if (lastModificationDate != other.lastModificationDate) return false
        if (version != other.version) return false
        if (statusCode != other.statusCode) return false
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
