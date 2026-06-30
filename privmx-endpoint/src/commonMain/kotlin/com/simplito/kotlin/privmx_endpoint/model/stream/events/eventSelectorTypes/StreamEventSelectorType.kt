package com.simplito.kotlin.privmx_endpoint.model.stream.events.eventSelectorTypes

import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.EventSelectorType

/**
 * Specifies the type of identifier used to select a stream event.
 * Stream events can be targeted based on different levels of granularity within the stream structure.
 * This enum defines the possible types of selectors for these events.
 *
 */
enum class StreamEventSelectorType : EventSelectorType {
    /**
     * Selects events based on the ID of the context.
     */
    CONTEXT_ID,

    /**
     * Selects events based on the ID of the stream room.
     */
    STREAMROOM_ID,

    /**
     * Selects events based on the ID of a specific stream.
     */
    STREAM_ID
}
