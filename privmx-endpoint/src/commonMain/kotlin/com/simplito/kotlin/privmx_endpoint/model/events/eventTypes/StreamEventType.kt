package com.simplito.java.privmx_endpoint.model.events.eventTypes

import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.EventType

enum class StreamEventType : EventType {
    STREAMROOM_CREATE,
    STREAMROOM_UPDATE,
    STREAMROOM_DELETE,
    EMPTY,
    STREAM_JOIN,
    STREAM_LEAVE,
    STREAM_PUBLISH,
    STREAM_UNPUBLISH
}
