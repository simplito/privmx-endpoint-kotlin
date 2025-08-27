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
 * @property data Event's actual payload
 * @property statusCode Payload decryption status
 * @property schemaVersion Version of the event data structure and how it is encoded/encrypted
 */
class ContextCustomEventData
@JvmOverloads
constructor(
    val contextId: String,
    val userId: String,
    val data: ByteArray,
    val statusCode: Long? = null,
    val schemaVersion: Long? = null
)