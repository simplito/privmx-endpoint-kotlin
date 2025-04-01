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
 * Holds all available information about a Store.
 *
 * @category store
 * @group Store
 */
class Store
(
    /**
     * ID of the Store.
     */
    var storeId: String?,
    /**
     * ID of the Context.
     */
    var contextId: String?,
    /**
     * Store creation timestamp.
     */
    var createDate: Long?,
    /**
     * ID of the user who created the Store.
     */
    var creator: String?,
    /**
     * Store last modification timestamp.
     */
    var lastModificationDate: Long?,
    /**
     * Timestamp of the last created file.
     */
    var lastFileDate: Long?,
    /**
     * ID of the user who last modified the Store.
     */
    var lastModifier: String?,
    /**
     * List of users (their IDs) with access to the Store.
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
     * Store's public metadata.
     */
    var publicMeta: ByteArray?,
    /**
     * Store's private metadata.
     */
    var privateMeta: ByteArray?,
    /**
     * Store's policies
     */
    var policy: ContainerPolicy?,
    /**
     * Total number of files in the Store.
     */
    var filesCount: Long?,
    /**
     * Status code of retrieval and decryption of the `Store`.
     */
    var statusCode: Long?
) {

    /**
     * Creates instance of `Store` with null policy value.
     *
     * @param storeId              ID of the Store.
     * @param contextId            ID of the Context.
     * @param createDate           Store creation timestamp.
     * @param creator              ID of the user who created the Store.
     * @param lastModificationDate Store last modification timestamp.
     * @param lastFileDate         Timestamp of the last created file.
     * @param lastModifier         ID of the user who last modified the Store.
     * @param users                List of users (their IDs) with access to the Store.
     * @param managers             List of users (their IDs) with management rights.
     * @param version              Version number (changes on updates).
     * @param publicMeta           Store's public metadata.
     * @param privateMeta          Store's private metadata.
     * @param filesCount           Total number of files in the Store.
     * @param statusCode           Status code of retrieval and decryption of the `Store`.
     */
    @Deprecated("")
    constructor(
        storeId: String,
        contextId: String,
        createDate: Long,
        creator: String,
        lastModificationDate: Long,
        lastFileDate: Long,
        lastModifier: String,
        users: List<String>,
        managers: List<String>,
        version: Long,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        filesCount: Long,
        statusCode: Long
    ) : this(
        storeId,
        contextId,
        createDate,
        creator,
        lastModificationDate,
        lastFileDate,
        lastModifier,
        users,
        managers,
        version,
        publicMeta,
        privateMeta,
        null,
        filesCount,
        statusCode
    )
}
