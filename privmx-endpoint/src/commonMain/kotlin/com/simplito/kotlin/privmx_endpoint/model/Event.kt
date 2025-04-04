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
 * Represents a generic event caught by PrivMX Endpoint.
 * @param <T> The type of data associated with the event.
 *
 * @category core
 * @group Events
</T> */
class Event<T: Any>(
    /**
     * Type of the event.
     */
    val type: String,

    /**
     * The event channel.
     */
    val channel: String,


    /**
     * ID of connection for this event.
     */
    val connectionId: Long?,

    /**
     * The data payload associated with the event.
     * The type of this data is determined by the generic type parameter `T`.
     */
    val data: T
)
