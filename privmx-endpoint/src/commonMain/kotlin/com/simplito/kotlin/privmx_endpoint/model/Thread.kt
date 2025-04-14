//
// PrivMX Endpoint Java.
// Copyright © 2024 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.simplito.kotlin.privmx_endpoint.model

/**
 * Holds all available information about a Thread.
 *
 * @category thread
 * @group Thread
 */
data class Thread(
    /**
     * ID of the Thread's Context.
     */
    val contextId: String,
    /**
     * ID of the Thread.
     */
    val threadId: String,
    /**
     * Thread creation timestamp.
     */
    val createDate: Long?,
    /**
     * ID of the user who created the Thread.
     */
    val creator: String,
    /**
     * Thread last modification timestamp.
     */
    val lastModificationDate: Long?,
    /**
     * ID of the user who last modified the Thread.
     */
    val lastModifier: String,
    /**
     * List of users (their IDs) with access to the Thread.
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
     * Timestamp of the last posted message.
     */
    val lastMsgDate: Long?,
    /**
     * Thread's public metadata.
     */
    val publicMeta: ByteArray,
    /**
     * Thread's private metadata.
     */
    val privateMeta: ByteArray,

    /**
     * Total number of messages in the Thread.
     */
    val messagesCount: Long?,

    /**
     * Thread's policies
     */
    val policy: ContainerPolicy,

    /**
     * Status code of retrieval and decryption of the `Thread`.
     */
    val statusCode: Long?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Thread

        if (createDate != other.createDate) return false
        if (lastModificationDate != other.lastModificationDate) return false
        if (version != other.version) return false
        if (lastMsgDate != other.lastMsgDate) return false
        if (messagesCount != other.messagesCount) return false
        if (statusCode != other.statusCode) return false
        if (contextId != other.contextId) return false
        if (threadId != other.threadId) return false
        if (creator != other.creator) return false
        if (lastModifier != other.lastModifier) return false
        if (users != other.users) return false
        if (managers != other.managers) return false
        if (!publicMeta.contentEquals(other.publicMeta)) return false
        if (!privateMeta.contentEquals(other.privateMeta)) return false
        if (policy != other.policy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = createDate?.hashCode() ?: 0
        result = 31 * result + (lastModificationDate?.hashCode() ?: 0)
        result = 31 * result + (version?.hashCode() ?: 0)
        result = 31 * result + (lastMsgDate?.hashCode() ?: 0)
        result = 31 * result + (messagesCount?.hashCode() ?: 0)
        result = 31 * result + (statusCode?.hashCode() ?: 0)
        result = 31 * result + contextId.hashCode()
        result = 31 * result + threadId.hashCode()
        result = 31 * result + creator.hashCode()
        result = 31 * result + lastModifier.hashCode()
        result = 31 * result + users.hashCode()
        result = 31 * result + managers.hashCode()
        result = 31 * result + publicMeta.contentHashCode()
        result = 31 * result + privateMeta.contentHashCode()
        result = 31 * result + policy.hashCode()
        return result
    }
}
