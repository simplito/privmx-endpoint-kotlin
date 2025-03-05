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
package com.simplito.java.privmx_endpoint.model

/**
 * Holds all available information about an Inbox.
 *
 * @category inbox
 * @group Inbox
 */
class Inbox
/**
 * Creates instance of `Inbox`.
 *
 * @param inboxId              ID of the Inbox.
 * @param contextId            ID of the Context.
 * @param createDate           Inbox creation timestamp.
 * @param creator              ID of the user who created the Inbox.
 * @param lastModificationDate Inbox last modification timestamp.
 * @param lastModifier         ID of the user who last modified the Inbox.
 * @param users                List of users (their IDs) with access to the Inbox.
 * @param managers             List of users (their IDs) with management rights.
 * @param version              Version number (changes on updates).
 * @param publicMeta           Inbox public metadata.
 * @param privateMeta          Inbox private metadata.
 * @param filesConfig          Inbox files configuration.
 * @param policy               Inbox policies.
 * @param statusCode           Status code of retrieval and decryption of the `Inbox`.
 */(
    /**
     * ID of the Inbox.
     */
    var inboxId: String?,
    /**
     * ID of the Context.
     */
    var contextId: String?,
    /**
     * Inbox creation timestamp.
     */
    var createDate: Long?,
    /**
     * ID of the user who created the Inbox.
     */
    var creator: String?,
    /**
     * Inbox last modification timestamp.
     */
    var lastModificationDate: Long?,
    /**
     * ID of the user who last modified the Inbox.
     */
    var lastModifier: String?,
    /**
     * List of users (their IDs) with access to the Inbox.
     */
    var users: List<String?>?,
    /**
     * List of users (their IDs) with management rights.
     */
    var managers: List<String?>?,
    /**
     * Version number (changes on updates).
     */
    var version: Long?,
    /**
     * Inbox public metadata.
     */
    var publicMeta: ByteArray?,
    /**
     * Inbox private metadata.
     */
    var privateMeta: ByteArray?,
    /**
     * Inbox files configuration.
     */
    var filesConfig: FilesConfig?,
    /**
     * Inbox policies.
     */
    var policy: ContainerPolicyWithoutItem?,
    /**
     * Status code of retrieval and decryption of the `Inbox`.
     */
    var statusCode: Long?
) {
    /**
     * Creates instance of `Inbox` with null policy value.
     *
     * @param inboxId              ID of the Inbox.
     * @param contextId            ID of the Context.
     * @param createDate           Inbox creation timestamp.
     * @param creator              ID of the user who created the Inbox.
     * @param lastModificationDate Inbox last modification timestamp.
     * @param lastModifier         ID of the user who last modified the Inbox.
     * @param users                List of users (their IDs) with access to the Inbox.
     * @param managers             List of users (their IDs) with management rights.
     * @param version              Version number (changes on updates).
     * @param publicMeta           Inbox public metadata.
     * @param privateMeta          Inbox private metadata.
     * @param filesConfig          Inbox files configuration.
     * @param statusCode           Status code of retrieval and decryption of the `Inbox`.
     */
    @Deprecated("")
    constructor(
        inboxId: String,
        contextId: String,
        createDate: Long,
        creator: String,
        lastModificationDate: Long,
        lastModifier: String,
        users: List<String>,
        managers: List<String>,
        version: Long,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        filesConfig: FilesConfig,
        statusCode: Long
    ) : this(
        inboxId,
        contextId,
        createDate,
        creator,
        lastModificationDate,
        lastModifier,
        users,
        managers,
        version,
        publicMeta,
        privateMeta,
        filesConfig,
        null,
        statusCode
    )
}
