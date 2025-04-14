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
 * @category store
 * @group Store
 */
data class Store
(
    /**
     * ID of the Store.
     */
    val storeId: String,
    /**
     * ID of the Context.
     */
    val contextId: String,
    /**
     * Store creation timestamp.
     */
    val createDate: Long?,
    /**
     * ID of the user who created the Store.
     */
    val creator: String,
    /**
     * Store last modification timestamp.
     */
    val lastModificationDate: Long?,
    /**
     * Timestamp of the last created file.
     */
    val lastFileDate: Long?,
    /**
     * ID of the user who last modified the Store.
     */
    val lastModifier: String,
    /**
     * List of users (their IDs) with access to the Store.
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
     * Store's public metadata.
     */
    val publicMeta: ByteArray,
    /**
     * Store's private metadata.
     */
    val privateMeta: ByteArray,
    /**
     * Store's policies
     */
    val policy: ContainerPolicy,
    /**
     * Total number of files in the Store.
     */
    val filesCount: Long?,
    /**
     * Status code of retrieval and decryption of the `Store`.
     */
    val statusCode: Long?
) {
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
