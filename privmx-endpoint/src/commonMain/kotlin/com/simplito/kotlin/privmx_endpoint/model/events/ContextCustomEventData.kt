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
 * Holds information about emitted custom event.
 *
 * @property contextId id of inbox from which it was sent
 * @property userId id of user which sent it
 * @property data event data
 * @property statusCode Payload decryption status
 */
class ContextCustomEventData(
    val contextId: String,
    val userId: String,
    val data: ByteArray,
    val statusCode: Long?
) {
    @Deprecated("Use primary constructor with new parameter.")
    constructor(
        contextId: String,
        userId: String,
        data: ByteArray
    ) : this(
        contextId, userId, data, null
    )
}
