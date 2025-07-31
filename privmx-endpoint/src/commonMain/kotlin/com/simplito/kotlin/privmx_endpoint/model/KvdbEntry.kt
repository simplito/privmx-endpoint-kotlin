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
 * Holds all available information about an Entry.
 *
 * @property info Entry information created by server
 * @property publicMeta Entry public metadata
 * @property privateMeta Entry private metadata
 * @property data Entry data
 * @property authorPubKey Public key of an author of the entry
 * @property version Version number (changes on every on existing item)
 * @property statusCode Retrieval and decryption status code
 * @property schemaVersion Version of the Entry data structure and how it is encoded/encrypted
 */
class KvdbEntry(
    val info: ServerKvdbEntryInfo,
    val publicMeta: ByteArray,
    val privateMeta: ByteArray,
    val data: ByteArray,
    val authorPubKey: String,
    val version: Long?,
    val statusCode: Long?,
    val schemaVersion: Long?
)