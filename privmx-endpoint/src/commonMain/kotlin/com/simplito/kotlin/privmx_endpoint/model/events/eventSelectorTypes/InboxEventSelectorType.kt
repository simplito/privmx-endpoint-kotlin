package com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes

/**
 * Specifies the type of identifier used to select an inbox event.
 * Inbox events can be targeted based on different levels of granularity within the inbox structure.
 * This enum defines the possible types of selectors for these events.
 */
enum class InboxEventSelectorType : EventSelectorType {
    /**
     * Selects events based on the ID of the context.
     */
    CONTEXT_ID,
    /**
     * Selects events based on the ID of the inbox.
     */
    INBOX_ID,
    /**
     * Selects events based on the ID of a specific entry.
     */
    ENTRY_ID
}