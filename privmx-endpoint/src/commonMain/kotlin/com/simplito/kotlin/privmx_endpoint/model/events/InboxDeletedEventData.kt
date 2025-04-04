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
 * Holds information about deleted Inbox.
 *
 * @param inboxId ID of the deleted Inbox.
 *
 * @category core
 * @group Events
 */
data class InboxDeletedEventData
(
    /**
     * ID of the deleted Inbox.
     */
    val inboxId: String?
)
