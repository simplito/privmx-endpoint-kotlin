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
 * Holds information about Inbox entry.
 *
 * @property entryId      ID of the entry
 * @property inboxId      ID of the Inbox
 * @property data         Entry data
 * @property files        List of files attached to the entry
 * @property authorPubKey Public key of the author of an entry
 * @property createDate   Inbox entry creation timestamp
 * @property statusCode   Status code of retrieval and decryption of the Inbox entry
 * @property schemaVersion Version of the Entry data structure and how it is encoded/encrypted
 */
data class InboxEntry(
    val entryId: String,
    val inboxId: String,
    val data: ByteArray,
    val files: List<File>,
    val authorPubKey: String,
    val createDate: Long?,
    val statusCode: Long?,
    val schemaVersion: Long?
) {
    /**
     * Holds information about Inbox entry.
     *
     * @property entryId      ID of the entry
     * @property inboxId      ID of the Inbox
     * @property data         Entry data
     * @property files        List of files attached to the entry
     * @property authorPubKey Public key of the author of an entry
     * @property createDate   Inbox entry creation timestamp
     * @property statusCode   Status code of retrieval and decryption of the Inbox entry
     */
    @Deprecated("Use primary constructor with new parameter.")
    constructor(
        entryId: String,
        inboxId: String,
        data: ByteArray,
        files: List<File>,
        authorPubKey: String,
        createDate: Long?,
        statusCode: Long?
    ) : this(
        entryId, inboxId, data, files, authorPubKey, createDate, statusCode, null
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InboxEntry

        if (createDate != other.createDate) return false
        if (statusCode != other.statusCode) return false
        if (schemaVersion != other.schemaVersion) return false
        if (entryId != other.entryId) return false
        if (inboxId != other.inboxId) return false
        if (!data.contentEquals(other.data)) return false
        if (files != other.files) return false
        if (authorPubKey != other.authorPubKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = createDate?.hashCode() ?: 0
        result = 31 * result + (statusCode?.hashCode() ?: 0)
        result = 31 * result + (schemaVersion?.hashCode() ?: 0)
        result = 31 * result + entryId.hashCode()
        result = 31 * result + inboxId.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + files.hashCode()
        result = 31 * result + authorPubKey.hashCode()
        return result
    }
}
