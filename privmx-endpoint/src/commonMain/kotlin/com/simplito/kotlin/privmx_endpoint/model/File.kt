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
 * Holds information about the file.
 *
 * @category store
 * @group Store
 */
data class File(

    /**
     * File's information created by server.
     */
    val info: ServerFileInfo,

    /**
     * File's public metadata.
     */
    val publicMeta: ByteArray,

    /**
     * File's private metadata.
     */
    val privateMeta: ByteArray,

    /**
     * File's size.
     */
    val size: Long?,

    /**
     * Public key of the author of the file.
     */
    val authorPubKey: String,

    /**
     * Status code of retrieval and decryption of the file.
     */
    val statusCode: Long?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as File

        if (size != other.size) return false
        if (statusCode != other.statusCode) return false
        if (info != other.info) return false
        if (!publicMeta.contentEquals(other.publicMeta)) return false
        if (!privateMeta.contentEquals(other.privateMeta)) return false
        if (authorPubKey != other.authorPubKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size?.hashCode() ?: 0
        result = 31 * result + (statusCode?.hashCode() ?: 0)
        result = 31 * result + info.hashCode()
        result = 31 * result + publicMeta.contentHashCode()
        result = 31 * result + privateMeta.contentHashCode()
        result = 31 * result + authorPubKey.hashCode()
        return result
    }
}
