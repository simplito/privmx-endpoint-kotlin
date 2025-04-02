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
 * @category inbox
 * @group Inbox
 */
class InboxPublicView
/**
 * Creates instance of `InboxPublicView`.
 *
 * @param inboxId    ID of the Inbox.
 * @param version    Version of the Inbox.
 * @param publicMeta Inbox public metadata.
 */(
    /**
     * ID of the Inbox.
     */
    var inboxId: String?,
    /**
     * Version of the Inbox.
     */
    var version: Long?,
    /**
     * Inbox public metadata.
     */
    var publicMeta: ByteArray?
)
