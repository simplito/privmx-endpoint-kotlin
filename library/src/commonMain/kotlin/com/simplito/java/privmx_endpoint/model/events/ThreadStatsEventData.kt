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
package com.simplito.java.privmx_endpoint.model.events

/**
 * Holds information about changes in a Thread's statistics.
 * @category core
 * @group Events
 */
data class ThreadStatsEventData
/**
 * Creates instance of `ThreadStatsEventData`.
 * @param threadId ID of the changed Thread.
 * @param lastMsgDate Timestamp of the most recent Thread message.
 * @param messagesCount Updated number of messages in the Thread.
 */(
    /**
     * ID of the changed Thread.
     */
    val threadId: String?,
    /**
     * Timestamp of the most recent Thread message.
     */
    val lastMsgDate: Long?,
    /**
     * Updated number of messages in the Thread.
     */
    val messagesCount: Long?
)
