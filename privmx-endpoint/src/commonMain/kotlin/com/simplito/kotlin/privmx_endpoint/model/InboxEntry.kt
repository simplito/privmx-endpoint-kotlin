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
 * @category inbox
 * @group Inbox
 */
data class InboxEntry
/**
 * Creates instance of `InboxEntry`.
 *
 * @param entryId      ID of the entry.
 * @param inboxId      ID of the Inbox.
 * @param data         Entry data.
 * @param files        List of files attached to the entry.
 * @param authorPubKey Public key of the author of an entry.
 * @param createDate   Inbox entry creation timestamp.
 * @param statusCode   Status code of retrieval and decryption of the `Inbox` entry.
 */(
    /**
     * ID of the entry.
     */
    val entryId: String,
    /**
     * ID of the Inbox.
     */
    val inboxId: String,
    /**
     * Entry data.
     */
    val data: ByteArray,
    /**
     * List of files attached to the entry.
     */
    val files: List<File>,
    /**
     * Public key of the author of an entry.
     */
    val authorPubKey: String,
    /**
     * Inbox entry creation timestamp.
     */
    val createDate: Long?,
    /**
     * Status code of retrieval and decryption of the `Inbox` entry.
     */
    val statusCode: Long?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InboxEntry

        if (createDate != other.createDate) return false
        if (statusCode != other.statusCode) return false
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
        result = 31 * result + entryId.hashCode()
        result = 31 * result + inboxId.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + files.hashCode()
        result = 31 * result + authorPubKey.hashCode()
        return result
    }
}
