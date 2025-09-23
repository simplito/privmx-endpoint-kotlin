package com.simplito.kotlin.privmx_endpoint.model.events.eventTypes

/**
 * Defines the types of core events for which a client can subscribe.
 * This enum lists the various actions or changes that can happen on core functionalities,
 * allowing observers to be notified of specific occurrences.
 */
enum class CoreEventType : EventType {
    /**
     * Type of event triggered when a new user is added within a context.
     */
    USER_ADD,

    /**
     * Type of event triggered when a user is deleted from context.
     */
    USER_REMOVE,

    /**
     * Type of event triggered when an user status is changed.
     */
    USER_STATUS
}