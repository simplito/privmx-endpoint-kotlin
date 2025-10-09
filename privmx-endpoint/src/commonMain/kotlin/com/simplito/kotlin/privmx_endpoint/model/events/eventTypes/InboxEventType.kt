package com.simplito.kotlin.privmx_endpoint.model.events.eventTypes

/**
 * Defines the types of events that can occur within the inbox for which a client can subscribe.
 * This enum lists the various actions or changes that can happen to
 * inboxes and their entries, allowing observers to be notified of specific occurrences.
 */
enum class InboxEventType : EventType {
    /**
     * Type of event triggered when a new inbox is created.
     */
    INBOX_CREATE,
    /**
     * Type of event triggered when an existing inbox is updated.
     */
    INBOX_UPDATE,
    /**
     * Type of event triggered when an inbox is deleted.
     */
    INBOX_DELETE,
    /**
     * Type of event triggered when a new entry is created within an inbox.
     */
    ENTRY_CREATE,
    /**
     * Type of event triggered when a new entry is created within an inbox.
     */
    ENTRY_DELETE,
    /**
     * Type of event triggered when a collection changes.
     */
    COLLECTION_CHANGE
}