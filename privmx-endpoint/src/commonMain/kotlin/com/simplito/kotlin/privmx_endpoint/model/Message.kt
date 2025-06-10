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
 * Holds information about the Message.
 *
 * @property info         Message's information created by server
 * @property publicMeta   Message's public metadata
 * @property privateMeta  Message's private metadata
 * @property data         Message's data
 * @property authorPubKey Public key of the author of the message
 * @property statusCode   Status code of retrieval and decryption of the `Message`
 * @property schemaVersion Version of the Message data structure and how it is encoded/encrypted.
 */
data class Message(
    val info: ServerMessageInfo,
    val publicMeta: ByteArray,
    val privateMeta: ByteArray,
    val data: ByteArray,
    val authorPubKey: String,
    val statusCode: Long?,
    val schemaVersion: Long?
) {
    /**
     * Holds information about the Message.
     *
     * @property info         Message's information created by server
     * @property publicMeta   Message's public metadata
     * @property privateMeta  Message's private metadata
     * @property data         Message's data
     * @property authorPubKey Public key of the author of the message
     * @property statusCode   Status code of retrieval and decryption of the `Message`
     */
    @Deprecated("Use primary constructor with new parameter.")
    constructor(
        info: ServerMessageInfo,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        data: ByteArray,
        authorPubKey: String,
        statusCode: Long?
    ) : this(
        info, publicMeta, privateMeta, data, authorPubKey, statusCode, null
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Message

        if (statusCode != other.statusCode) return false
        if (schemaVersion != other.schemaVersion) return false
        if (info != other.info) return false
        if (!publicMeta.contentEquals(other.publicMeta)) return false
        if (!privateMeta.contentEquals(other.privateMeta)) return false
        if (!data.contentEquals(other.data)) return false
        if (authorPubKey != other.authorPubKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = statusCode?.hashCode() ?: 0
        result = 31 * result + (schemaVersion?.hashCode() ?: 0)
        result = 31 * result + info.hashCode()
        result = 31 * result + publicMeta.contentHashCode()
        result = 31 * result + privateMeta.contentHashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + authorPubKey.hashCode()
        return result
    }
}
