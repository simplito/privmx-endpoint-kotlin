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
 * Holds all available information about a Store.
 *
 * @property storeId               ID of the Store.
 * @property contextId             ID of the Context.
 * @property createDate            Store creation timestamp.
 * @property creator               ID of the user who created the Store.
 * @property lastModificationDate  Store last modification timestamp.
 * @property lastFileDate          Timestamp of the last created file.
 * @property lastModifier          ID of the user who last modified the Store.
 * @property users                 List of users (their IDs) with access to the Store.
 * @property managers              List of users (their IDs) with management rights.
 * @property version               Version number (changes on updates).
 * @property publicMeta            Store's public metadata.
 * @property privateMeta           Store's private metadata.
 * @property policy                Store's policies.
 * @property filesCount            Total number of files in the Store.
 * @property statusCode            Status code of retrieval and decryption of the `Store`.
 *
 * @category store
 * @group Store
 */
class Store
(
    var storeId: String?,
    var contextId: String?,
    var createDate: Long?,
    var creator: String?,
    var lastModificationDate: Long?,
    var lastFileDate: Long?,
    var lastModifier: String?,
    var users: List<String?>?,
    var managers: List<String?>?,
    var version: Long?,
    var publicMeta: ByteArray?,
    var privateMeta: ByteArray?,
    var policy: ContainerPolicy?,
    var filesCount: Long?,
    var statusCode: Long?
)