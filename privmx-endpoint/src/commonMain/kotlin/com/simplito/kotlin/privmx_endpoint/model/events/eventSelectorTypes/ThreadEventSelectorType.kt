package com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes

/**
 * Specifies the type of identifier used to select a thread event.
 * Thread events can be targeted based on different levels of granularity within the thread structure.
 * This enum defines the possible types of selectors for these events.
 */
enum class ThreadEventSelectorType : EventSelectorType {
    /**
     * Selects events based on the ID of the context.
     */
    CONTEXT_ID,
    /**
     * Selects events based on the ID of the thread.
     */
    THREAD_ID,
    /**
     * Selects events based on the ID of a specific message.
     */
    MESSAGE_ID
}
