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
 * @property info         File's information created by server.
 * @property publicMeta   File's public metadata.
 * @property privateMeta  File's private metadata.
 * @property size         File's size.
 * @property authorPubKey Public key of the author of the file.
 * @property statusCode   Status code of retrieval and decryption of the file.
 *
 * @category store
 * @group Store
 */
class File(
    var info: ServerFileInfo?,
    var publicMeta: ByteArray?,
    var privateMeta: ByteArray?,
    var size: Long?,
    var authorPubKey: String?,
    var statusCode: Long?
)