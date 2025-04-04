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
 * @param info         Message's information created by server.
 * @param publicMeta   Message's public metadata.
 * @param privateMeta  Message's private metadata.
 * @param data         Message's data.
 * @param authorPubKey Public key of the author of the message.
 * @param statusCode   Status code of retrieval and decryption of the `Message`.
 *
 * @category thread
 * @group Thread
 */
class Message(
    /**
     * Message's information created by server.
     */
    var info: ServerMessageInfo?,
    /**
     * Message's public metadata.
     */
    var publicMeta: ByteArray?,
    /**
     * Message's private metadata.
     */
    var privateMeta: ByteArray?,
    /**
     * Message's data.
     */
    var data: ByteArray?,
    /**
     * Public key of the author of the message.
     */
    var authorPubKey: String?,
    /**
     * Status code of retrieval and decryption of the `Message`.
     */
    var statusCode: Long?
)