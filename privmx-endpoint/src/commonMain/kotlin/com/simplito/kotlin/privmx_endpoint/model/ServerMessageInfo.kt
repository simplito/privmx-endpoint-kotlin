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
 * Holds message's information created by server.
 *
 * @property threadId   ID of the message's Thread
 * @property messageId  ID of the message
 * @property createDate Message's creation timestamp
 * @property author     ID of the user who created the message
 */
data class ServerMessageInfo(
    val threadId: String,
    val messageId: String,
    val createDate: Long?,
    val author: String
)
