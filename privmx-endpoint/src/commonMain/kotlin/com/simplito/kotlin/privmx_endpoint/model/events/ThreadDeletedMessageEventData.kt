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
package com.simplito.kotlin.privmx_endpoint.model.events

/**
 * Holds information about a message deleted from a Thread.
 *
 * @property threadId ID of the deleted message's Thread
 * @property messageId ID of the deleted Message
 */
data class ThreadDeletedMessageEventData(
    val threadId: String,
    val messageId: String
)
