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
 * Holds information about changes in a Thread's statistics.
 *
 * @property threadId ID of the changed Thread.
 * @property lastMsgDate Timestamp of the most recent Thread message.
 * @property messagesCount Updated number of messages in the Thread.
 *
 * @category core
 * @group Events
 */
data class ThreadStatsEventData(
    val threadId: String?,
    val lastMsgDate: Long?,
    val messagesCount: Long?
)
