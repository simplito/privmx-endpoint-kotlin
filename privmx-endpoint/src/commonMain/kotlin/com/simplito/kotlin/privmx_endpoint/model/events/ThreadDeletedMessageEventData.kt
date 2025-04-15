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
package com.simplito.kotlin.privmx_endpoint.model.events

/**
 * Holds information about a message deleted from a Thread.
 *
 * @property threadId ID of the deleted message's Thread.
 * @property messageId ID of the deleted Message.
 *
 * @category core
 * @group Events
 */
data class ThreadDeletedMessageEventData internal constructor(
    val threadId: String?,
    val messageId: String?
)
