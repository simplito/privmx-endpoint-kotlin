package com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes

/**
 * Specifies the type of identifier used to select a store event.
 * Store events can be targeted based on different levels of granularity within the store structure.
 * This enum defines the possible types of selectors for these events.
 */
enum class StoreEventSelectorType : EventSelectorType {
    /**
     * Selects events based on the ID of the context.
     */
    CONTEXT_ID,
    /**
     * Selects events based on the ID of the store.
     */
    STORE_ID,
    /**
     * Selects events based on the ID of a specific file.
     */
    FILE_ID
}
