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
 * Represents a generic event caught by PrivMX Endpoint.
 * @param T             The type of data associated with the event
 * @property type          Type of the event
 * @property channel       The event channel
 * @property connectionId  ID of connection for this event
 * @property data          The data payload associated with the event
 */
class Event<T: Any>(
    val type: String,
    val channel: String,
    val connectionId: Long?,
    val data: T
)
