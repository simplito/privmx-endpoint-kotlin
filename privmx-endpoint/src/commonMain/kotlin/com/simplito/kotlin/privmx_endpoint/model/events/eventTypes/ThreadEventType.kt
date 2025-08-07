package com.simplito.kotlin.privmx_endpoint.model.events.eventTypes

enum class ThreadEventType : EventType {
    THREAD_CREATE,
    THREAD_UPDATE,
    THREAD_DELETE,
    THREAD_STATS,
    MESSAGE_CREATE,
    MESSAGE_UPDATE,
    MESSAGE_DELETE
}
