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
 * @category core
 * @group Events
 */
data class ThreadDeletedMessageEventData
/**
 * Creates instance of `ThreadDeletedMessageEventData`.
 * @param threadId ID of the deleted message's Thread.
 * @param messageId ID of the deleted Message.
 */ internal constructor(
    /**
     * ID of the deleted message's Thread.
     */
    val threadId: String?,
    /**
     * ID of the deleted Message.
     */
    val messageId: String?
)
