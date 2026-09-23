/*
 *
 * PrivMX Endpoint Kotlin.
 * Copyright © 2025 Simplito sp. z o.o.
 *
 * This file is part of the PrivMX Platform (https://privmx.dev).
 * This software is Licensed under the MIT License.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.simplito.kotlin.privmx_endpoint.model.events.eventTypes

/**
 * Defines the types of events that can occur within a Group for which a client can subscribe.
 */
enum class GroupEventType : EventType {
    /**
     * Type of event triggered when a new Group is created.
     */
    GROUP_CREATE,

    /**
     * Type of event triggered when an existing Group is updated.
     */
    GROUP_UPDATE,

    /**
     * Type of event triggered when a Group is deleted.
     */
    GROUP_DELETE
}
