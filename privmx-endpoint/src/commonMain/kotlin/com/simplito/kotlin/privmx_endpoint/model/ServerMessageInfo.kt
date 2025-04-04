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
 * Holds message's information created by server.
 *
 * @category thread
 * @group Thread
 */
data class ServerMessageInfo
/**
 * Creates instance of `ServerMessageInfo`.
 *
 * @param threadId   ID of the message's Thread.
 * @param messageId  ID of the message.
 * @param createDate Message's creation timestamp.
 * @param author     ID of the user who created the message.
 */(
    /**
     * ID of the message's Thread.
     */
    val threadId: String,
    /**
     * ID of the message.
     */
    val messageId: String,
    /**
     * Message's creation timestamp.
     */
    val createDate: Long?,
    /**
     * ID of the user who created the message.
     */
    val author: String
)
