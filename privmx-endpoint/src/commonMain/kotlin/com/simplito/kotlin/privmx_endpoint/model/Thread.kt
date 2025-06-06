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
 * Holds all available information about a Thread.
 *
 * @property contextId                 ID of the Thread's Context
 * @property threadId                  ID of the Thread
 * @property createDate                Thread creation timestamp
 * @property creator                   ID of the user who created the Thread
 * @property lastModificationDate      Thread last modification timestamp
 * @property lastModifier              ID of the user who last modified the Thread
 * @property users                     List of users (their IDs) with access to the Thread
 * @property managers                  List of users (their IDs) with management rights
 * @property version                   Version number (changes on updates)
 * @property lastMsgDate               Timestamp of the last posted message
 * @property publicMeta                Thread's public metadata
 * @property privateMeta               Thread's private metadata
 * @property policy                    Thread's policies
 * @property messagesCount             Total number of messages in the Thread
 * @property statusCode                Status code of retrieval and decryption of the `Thread`
 * @property schemaVersion                Version of the Thread data structure and how it is encoded/encrypted.
 */
data class Thread(
    val contextId: String,
    val threadId: String,
    val createDate: Long?,
    val creator: String,
    val lastModificationDate: Long?,
    val lastModifier: String,
    val users: List<String>,
    val managers: List<String>,
    val version: Long?,
    val lastMsgDate: Long?,
    val publicMeta: ByteArray,
    val privateMeta: ByteArray,
    val policy: ContainerPolicy,
    val messagesCount: Long?,
    val statusCode: Long?,
    val schemaVersion: Long?
) {
    /**
     * Holds all available information about a Thread.
     *
     * @property contextId                 ID of the Thread's Context
     * @property threadId                  ID of the Thread
     * @property createDate                Thread creation timestamp
     * @property creator                   ID of the user who created the Thread
     * @property lastModificationDate      Thread last modification timestamp
     * @property lastModifier              ID of the user who last modified the Thread
     * @property users                     List of users (their IDs) with access to the Thread
     * @property managers                  List of users (their IDs) with management rights
     * @property version                   Version number (changes on updates)
     * @property lastMsgDate               Timestamp of the last posted message
     * @property publicMeta                Thread's public metadata
     * @property privateMeta               Thread's private metadata
     * @property policy                    Thread's policies
     * @property messagesCount             Total number of messages in the Thread
     * @property statusCode                Status code of retrieval and decryption of the `Thread`
     */
    @Deprecated("Use primary constructor with new parameter.")
    constructor(
        contextId: String,
        threadId: String,
        createDate: Long?,
        creator: String,
        lastModificationDate: Long?,
        lastModifier: String,
        users: List<String>,
        managers: List<String>,
        version: Long?,
        lastMsgDate: Long?,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policy: ContainerPolicy,
        messagesCount: Long?,
        statusCode: Long?
    ) : this(
        contextId,
        threadId,
        createDate,
        creator,
        lastModificationDate,
        lastModifier,
        users,
        managers,
        version,
        lastMsgDate,
        publicMeta,
        privateMeta,
        policy,
        messagesCount,
        statusCode,
        null
    )

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
        if (schemaVersion != other.schemaVersion) return false

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
        result = 31 * result + (schemaVersion.hashCode() ?: 0)
        return result
    }
}