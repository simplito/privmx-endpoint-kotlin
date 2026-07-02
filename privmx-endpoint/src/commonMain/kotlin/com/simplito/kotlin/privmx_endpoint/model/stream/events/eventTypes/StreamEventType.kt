package com.simplito.kotlin.privmx_endpoint.model.stream.events.eventTypes

import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.EventType

enum class StreamEventType : EventType {
    STREAMROOM_CREATE,
    STREAMROOM_UPDATE,
    STREAMROOM_DELETE,
    EMPTY,
    STREAMROOM_JOIN,
    STREAMROOM_LEAVE,
    STREAM_PUBLISH,
    STREAM_UNPUBLISH,
    STREAM_SUBSCRIBE,
    STREAM_UNSUBSCRIBE,
    STREAM_UPDATE,
}