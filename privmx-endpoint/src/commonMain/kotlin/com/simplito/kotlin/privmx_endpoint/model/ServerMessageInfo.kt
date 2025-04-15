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
 * @property threadId   ID of the message's Thread.
 * @property messageId  ID of the message.
 * @property createDate Message's creation timestamp.
 * @property author     ID of the user who created the message.
 *
 * @category thread
 * @group Thread
 */
class ServerMessageInfo(
    var threadId: String?,
    var messageId: String?,
    var createDate: Long?,
    var author: String?
)
