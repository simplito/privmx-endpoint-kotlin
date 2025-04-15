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
 * Holds information about the Message.
 *
 * @property info         Message's information created by server.
 * @property publicMeta   Message's public metadata.
 * @property privateMeta  Message's private metadata.
 * @property data         Message's data.
 * @property authorPubKey Public key of the author of the message.
 * @property statusCode   Status code of retrieval and decryption of the `Message`.
 *
 * @category thread
 * @group Thread
 */
class Message(
    var info: ServerMessageInfo?,
    var publicMeta: ByteArray?,
    var privateMeta: ByteArray?,
    var data: ByteArray?,
    var authorPubKey: String?,
    var statusCode: Long?
)
