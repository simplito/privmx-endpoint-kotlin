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
 * Holds information about Inbox entry.
 *
 * @property entryId      ID of the entry.
 * @property inboxId      ID of the Inbox.
 * @property data         Entry data.
 * @property files        List of files attached to the entry.
 * @property authorPubKey Public key of the author of an entry.
 * @property createDate   Inbox entry creation timestamp.
 * @property statusCode   Status code of retrieval and decryption of the Inbox entry.
 *
 * @category inbox
 * @group Inbox
 */
class InboxEntry
    (
    var entryId: String?,
    var inboxId: String?,
    var data: ByteArray?,
    var files: List<File?>?,
    var authorPubKey: String?,
    var createDate: Long?,
    var statusCode: Long?
)
