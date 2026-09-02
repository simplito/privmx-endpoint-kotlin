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
 * Holds all available information about a Search Index.
 *
 * @property contextId ID of the Context
 * @property indexId ID of the Search Index
 * @property createDate Search Index creation timestamp
 * @property creator ID of user who created the Search Index
 * @property lastModificationDate Search Index last modification timestamp
 * @property lastModifier ID of the user who last modified the Search Index
 * @property users List of users (their IDs) with access to the Search Index
 * @property managers List of users (their IDs) with management rights
 * @property version Version number (changes on updates)
 * @property publicMeta Search Index public metadata
 * @property privateMeta Search Index private metadata
 * @property policy Search Index policies
 * @property mode The operating mode of the Search Index, defining how document content is handled
 * @property statusCode Status code of retrieval and decryption of the Search Index
 * @property schemaVersion Version of the Search Index data structure and how it is encoded/encrypted
 */
data class SearchIndex(
    val contextId: String,
    val indexId: String,
    val createDate: Long?,
    val creator: String,
    val lastModificationDate: Long?,
    val lastModifier: String,
    val users: List<String>,
    val managers: List<String>,
    val version: Long?,
    val publicMeta: ByteArray,
    val privateMeta: ByteArray,
    val policy: ContainerPolicy?,
    val mode: IndexMode?,
    val statusCode: Long?,
    val schemaVersion: Long?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SearchIndex

        if (createDate != other.createDate) return false
        if (lastModificationDate != other.lastModificationDate) return false
        if (version != other.version) return false
        if (statusCode != other.statusCode) return false
        if (schemaVersion != other.schemaVersion) return false
        if (contextId != other.contextId) return false
        if (indexId != other.indexId) return false
        if (creator != other.creator) return false
        if (lastModifier != other.lastModifier) return false
        if (users != other.users) return false
        if (managers != other.managers) return false
        if (!publicMeta.contentEquals(other.publicMeta)) return false
        if (!privateMeta.contentEquals(other.privateMeta)) return false
        if (policy != other.policy) return false
        if (mode != other.mode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = createDate?.hashCode() ?: 0
        result = 31 * result + (lastModificationDate?.hashCode() ?: 0)
        result = 31 * result + (version?.hashCode() ?: 0)
        result = 31 * result + (statusCode?.hashCode() ?: 0)
        result = 31 * result + (schemaVersion?.hashCode() ?: 0)
        result = 31 * result + contextId.hashCode()
        result = 31 * result + indexId.hashCode()
        result = 31 * result + creator.hashCode()
        result = 31 * result + lastModifier.hashCode()
        result = 31 * result + users.hashCode()
        result = 31 * result + managers.hashCode()
        result = 31 * result + publicMeta.contentHashCode()
        result = 31 * result + privateMeta.contentHashCode()
        result = 31 * result + (policy?.hashCode() ?: 0)
        result = 31 * result + (mode?.hashCode() ?: 0)
        return result
    }
}
