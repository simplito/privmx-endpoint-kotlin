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

import kotlin.jvm.JvmOverloads

/**
 * Holds information about emitted custom event.
 *
 * @property contextId Context ID
 * @property userId User ID (event's sender)
 * @property payload Event's actual payload
 * @property statusCode Payload decryption status
 * @property schemaVersion Version of the event data structure and how it is encoded/encrypted
 */
data class ContextCustomEventData
@JvmOverloads
constructor(
    val contextId: String,
    val userId: String,
    val payload: ByteArray,
    val statusCode: Long? = null,
    val schemaVersion: Long? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ContextCustomEventData

        if (statusCode != other.statusCode) return false
        if (schemaVersion != other.schemaVersion) return false
        if (contextId != other.contextId) return false
        if (userId != other.userId) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = statusCode?.hashCode() ?: 0
        result = 31 * result + (schemaVersion?.hashCode() ?: 0)
        result = 31 * result + contextId.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}