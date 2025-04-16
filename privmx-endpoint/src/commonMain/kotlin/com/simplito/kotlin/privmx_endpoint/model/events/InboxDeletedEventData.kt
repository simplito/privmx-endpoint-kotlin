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
 * Holds information about deleted Inbox.
 *
 * @category core
 * @group Events
 */
data class InboxDeletedEventData
/**
 * Creates instance of `InboxDeletedEventData`.
 * @param inboxId ID of the deleted Inbox.
 */(
    /**
     * ID of the deleted Inbox.
     */
    val inboxId: String
)
