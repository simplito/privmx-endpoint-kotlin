/*
 *
 * PrivMX Endpoint Kotlin Extra.
 * Copyright © 2025 Simplito sp. z o.o.
 *
 * This file is part of the PrivMX Platform (https://privmx.dev).
 * This software is Licensed under the MIT License.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.simplito.kotlin.privmx_endpoint_extra.events

/**
 * Represents a registration of a callback for a specific event type.
 * This class encapsulates the information needed to register and subscribe a callback.
 * Callbacks are grouped for easier management, such as unregistering multiple related callbacks at once.
 * @param T The type of event data.
 */
class CallbackRegistration<T : Any>(
    /**
     * An identifier used to group related callbacks.
     *
     * This allows for collective operations, like unregistering
     * all callbacks belonging to the same group. Can be any object,
     * its identity (reference equality) is used for grouping.
     */
    var callbackGroup: Any,

    /**
     * The specific type of event to subscribe to.
     *
     * The provided `callback` will only be invoked for events matching this type.
     */
    var eventType: EventType<T>,

    /**
     * The block of code that will be executed
     * when an event of the specified [eventType] is handled.
     *
     * This callback will receive the event data as a parameter.
     */
    var callback: EventCallback<T>,
)