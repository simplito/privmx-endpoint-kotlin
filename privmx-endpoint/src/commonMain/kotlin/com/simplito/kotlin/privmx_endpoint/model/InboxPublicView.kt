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
data class InboxPublicView(
    val inboxId: String,
    val version: Long?,
    val publicMeta: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InboxPublicView

        if (version != other.version) return false
        if (inboxId != other.inboxId) return false
        if (!publicMeta.contentEquals(other.publicMeta)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version?.hashCode() ?: 0
        result = 31 * result + inboxId.hashCode()
        result = 31 * result + publicMeta.contentHashCode()
        return result
    }
}
