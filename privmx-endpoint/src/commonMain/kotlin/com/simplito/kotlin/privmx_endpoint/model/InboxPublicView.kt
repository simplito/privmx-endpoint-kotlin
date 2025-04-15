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
package com.simplito.kotlin.privmx_endpoint.model

/**
 * Holds Inbox public information.
 *
 * @property inboxId    ID of the Inbox.
 * @property version    Version of the Inbox.
 * @property publicMeta Inbox public metadata.
 *
 * @category inbox
 * @group Inbox
 */
class InboxPublicView(
    var inboxId: String?,
    var version: Long?,
    var publicMeta: ByteArray?
)
