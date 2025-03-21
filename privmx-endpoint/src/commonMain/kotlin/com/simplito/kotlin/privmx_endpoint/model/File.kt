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
 * Holds information about the file.
 *
 * @category store
 * @group Store
 */
class File(
    info: ServerFileInfo?,
    publicMeta: ByteArray?,
    privateMeta: ByteArray?,
    size: Long?,
    authorPubKey: String?,
    statusCode: Long?) {

    /**
     * File's information created by server.
     */
    var info: ServerFileInfo?

    /**
     * File's public metadata.
     */
    var publicMeta: ByteArray?

    /**
     * File's private metadata.
     */
    var privateMeta: ByteArray?

    /**
     * File's size.
     */
    var size: Long?

    /**
     * Public key of the author of the file.
     */
    var authorPubKey: String?

    /**
     * Status code of retrieval and decryption of the file.
     */
    var statusCode: Long?

    /**
     * Creates instance of `File`.
     *
     * @param info         File's information created by server.
     * @param publicMeta   File's public metadata.
     * @param privateMeta  File's private metadata.
     * @param size         File's size.
     * @param authorPubKey Public key of the author of the file.
     * @param statusCode   Status code of retrieval and decryption of the `File`.
     */
    init {
        this.info = info
        this.publicMeta = publicMeta
        this.privateMeta = privateMeta
        this.authorPubKey = authorPubKey
        this.size = size
        this.statusCode = statusCode
    }
}
