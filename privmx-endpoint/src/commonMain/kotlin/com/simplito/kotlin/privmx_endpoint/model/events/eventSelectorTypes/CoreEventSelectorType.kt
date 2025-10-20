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

package com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes

/**
 * Specifies the type of identifier used to select a core event.
 * This enum defines the possible types of selectors for the core events.
 */
enum class CoreEventSelectorType : EventSelectorType {
    /**
     * Selects events based on the ID of the context.
     */
    CONTEXT_ID
}