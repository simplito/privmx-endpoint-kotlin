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
package com.simplito.java.privmx_endpoint.model

/**
 * Represents a generic event caught by PrivMX Endpoint.
 * @param <T> The type of data associated with the event.
 *
 * @category core
 * @group Events
</T> */
class Event<T> {
    /**
     * Type of the event.
     */
    var type: String? = null

    /**
     * The event channel.
     */
    var channel: String? = null


    /**
     * ID of connection for this event.
     */
    var connectionId: Long? = null

    /**
     * The data payload associated with the event.
     * The type of this data is determined by the generic type parameter `T`.
     */
    var data: T? = null

    /**
     * Creates instance of Event model.
     */
    internal constructor()

    /**
     * Creates instance of Event model.
     *
     * @param type type of event as text
     * @param channel event channel
     * @param connectionId ID of connection for this event
     * @param data event data
     */
    constructor(
        type: String?,
        channel: String?,
        connectionId: Long?,
        data: T?
    ) {
        this.type = type
        this.channel = channel
        this.connectionId = connectionId
        this.data = data
    }
}
