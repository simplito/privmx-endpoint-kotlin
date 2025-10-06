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