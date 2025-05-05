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
 * Holds information about a entry deleted from Inbox.
 *
 * @property inboxId ID of the deleted entry's Inbox
 * @property entryId ID of the deleted entry
 */
data class InboxEntryDeletedEventData(val inboxId: String, val entryId: String)
