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
 * Holds information about a entry deleted from Inbox.
 *
 * @param inboxId ID of the deleted entry's Inbox.
 * @param entryId ID of the deleted entry.
 *
 * @category core
 * @group Events
 */
data class InboxEntryDeletedEventData
(
    /**
     * ID of the deleted entry's Inbox.
     */
    val inboxId: String?,
    /**
     * ID of the deleted entry.
     */
    val entryId: String?
)
