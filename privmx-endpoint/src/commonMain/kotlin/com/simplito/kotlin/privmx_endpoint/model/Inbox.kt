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
 * Holds all available information about an Inbox.
 *
 * @property inboxId              ID of the Inbox.
 * @property contextId            ID of the Context.
 * @property createDate           Inbox creation timestamp.
 * @property creator              ID of the user who created the Inbox.
 * @property lastModificationDate Inbox last modification timestamp.
 * @property lastModifier         ID of the user who last modified the Inbox.
 * @property users                List of users (their IDs) with access to the Inbox.
 * @property managers             List of users (their IDs) with management rights.
 * @property version              Version number (changes on updates).
 * @property publicMeta           Inbox public metadata.
 * @property privateMeta          Inbox private metadata.
 * @property filesConfig          Inbox files configuration.
 * @property policy               Inbox policies.
 * @property statusCode           Status code of retrieval and decryption of the Inbox.
 *
 * @category inbox
 * @group Inbox
 */
class Inbox(
    var inboxId: String?,
    var contextId: String?,
    var createDate: Long?,
    var creator: String?,
    var lastModificationDate: Long?,
    var lastModifier: String?,
    var users: List<String?>?,
    var managers: List<String?>?,
    var version: Long?,
    var publicMeta: ByteArray?,
    var privateMeta: ByteArray?,
    var filesConfig: FilesConfig?,
    var policy: ContainerPolicyWithoutItem?,
    var statusCode: Long?
)
