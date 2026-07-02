package com.simplito.kotlin.privmx_endpoint.model.stream.events.eventTypes

import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.EventType

/**
 * Event types that clients can subscribe to within a stream room.
 * Covers state changes across stream rooms and their individual streams.
 */
enum class StreamEventType : EventType {

    /**
     * Type of event triggered when a new StreamRoom is created.
     */
    STREAMROOM_CREATE,

    /**
     * Type of event triggered when an existing StreamRoom is updated.
     */
    STREAMROOM_UPDATE,

    /**
     * Type of event triggered when a StreamRoom is deleted.
     */
    STREAMROOM_DELETE,

    /**
     * Do not use.
     */
    EMPTY,
    /**
     * Type of event triggered when a user joins a StreamRoom.
     */
    STREAMROOM_JOIN,
    /**
     * Type of event triggered when a user leaves a StreamRoom.
     */
    STREAMROOM_LEAVE,
    /**
     * Type of event triggered when a user starts publishing a stream.
     */
    STREAM_PUBLISH,
    /**
     * Type of event triggered when a user stops publishing a stream.
     */
    STREAM_UNPUBLISH,
    STREAM_SUBSCRIBE,
    STREAM_UNSUBSCRIBE,
    STREAM_UPDATE,
}