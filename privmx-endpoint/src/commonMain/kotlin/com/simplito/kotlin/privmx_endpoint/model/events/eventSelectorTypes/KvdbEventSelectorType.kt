package com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes

/**
 * Specifies the type of identifier used to select a KVDB event.
 * KVDB events can be targeted based on different levels of granularity within the KVDB structure.
 * This enum defines the possible types of selectors for these events.
 */
enum class KvdbEventSelectorType : EventSelectorType {
    /**
     * Selects events based on the ID of the context.
     */
    CONTEXT_ID,
    /**
     * Selects events based on the ID of the KVDB.
     */
    KVDB_ID,
}
