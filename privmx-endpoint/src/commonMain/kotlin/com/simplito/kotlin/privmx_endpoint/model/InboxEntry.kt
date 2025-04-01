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
class InboxEntry
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
    var entryId: String?,
    /**
     * ID of the Inbox.
     */
    var inboxId: String?,
    /**
     * Entry data.
     */
    var data: ByteArray?,
    /**
     * List of files attached to the entry.
     */
    var files: List<File?>?,
    /**
     * Public key of the author of an entry.
     */
    var authorPubKey: String?,
    /**
     * Inbox entry creation timestamp.
     */
    var createDate: Long?,
    /**
     * Status code of retrieval and decryption of the `Inbox` entry.
     */
    var statusCode: Long?
)
