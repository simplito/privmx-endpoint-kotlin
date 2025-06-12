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
 * Holds all available information about a Store.
 *
 * @property storeId               ID of the Store
 * @property contextId             ID of the Context
 * @property createDate            Store creation timestamp
 * @property creator               ID of the user who created the Store
 * @property lastModificationDate  Store last modification timestamp
 * @property lastFileDate          Timestamp of the last created file
 * @property lastModifier          ID of the user who last modified the Store
 * @property users                 List of users (their IDs) with access to the Store
 * @property managers              List of users (their IDs) with management rights
 * @property version               Version number (changes on updates)
 * @property publicMeta            Store's public metadata
 * @property privateMeta           Store's private metadata
 * @property policy                Store's policies
 * @property filesCount            Total number of files in the Store
 * @property statusCode            Status code of retrieval and decryption of the `Store`
 * @property schemaVersion            Version of the Store data structure and how it is encoded/encrypted.
 */
data class Store(
    val storeId: String,
    val contextId: String,
    val createDate: Long?,
    val creator: String,
    val lastModificationDate: Long?,
    val lastFileDate: Long?,
    val lastModifier: String,
    val users: List<String>,
    val managers: List<String>,
    val version: Long?,
    val publicMeta: ByteArray,
    val privateMeta: ByteArray,
    val policy: ContainerPolicy,
    val filesCount: Long?,
    val statusCode: Long?,
    val schemaVersion: Long?
) {
    /**
     * Holds all available information about a Store.
     *
     * @property storeId               ID of the Store
     * @property contextId             ID of the Context
     * @property createDate            Store creation timestamp
     * @property creator               ID of the user who created the Store
     * @property lastModificationDate  Store last modification timestamp
     * @property lastFileDate          Timestamp of the last created file
     * @property lastModifier          ID of the user who last modified the Store
     * @property users                 List of users (their IDs) with access to the Store
     * @property managers              List of users (their IDs) with management rights
     * @property version               Version number (changes on updates)
     * @property publicMeta            Store's public metadata
     * @property privateMeta           Store's private metadata
     * @property policy                Store's policies
     * @property filesCount            Total number of files in the Store
     * @property statusCode            Status code of retrieval and decryption of the `Store`
     */
    @Deprecated("Use primary constructor with new parameter.")
    constructor(
        storeId: String,
        contextId: String,
        createDate: Long?,
        creator: String,
        lastModificationDate: Long?,
        lastFileDate: Long?,
        lastModifier: String,
        users: List<String>,
        managers: List<String>,
        version: Long?,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policy: ContainerPolicy,
        filesCount: Long?,
        statusCode: Long?
    ) : this(
        storeId,
        contextId,
        createDate,
        creator,
        lastModificationDate,
        lastFileDate,
        lastModifier,
        users,
        managers,
        version,
        publicMeta,
        privateMeta,
        policy,
        filesCount,
        statusCode,
        null
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Store

        if (createDate != other.createDate) return false
        if (lastModificationDate != other.lastModificationDate) return false
        if (lastFileDate != other.lastFileDate) return false
        if (version != other.version) return false
        if (filesCount != other.filesCount) return false
        if (statusCode != other.statusCode) return false
        if (schemaVersion != other.schemaVersion) return false
        if (storeId != other.storeId) return false
        if (contextId != other.contextId) return false
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
        result = 31 * result + (lastFileDate?.hashCode() ?: 0)
        result = 31 * result + (version?.hashCode() ?: 0)
        result = 31 * result + (filesCount?.hashCode() ?: 0)
        result = 31 * result + (statusCode?.hashCode() ?: 0)
        result = 31 * result + (schemaVersion?.hashCode() ?: 0)
        result = 31 * result + storeId.hashCode()
        result = 31 * result + contextId.hashCode()
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
