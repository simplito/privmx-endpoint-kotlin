package com.simplito.kotlin.privmx_endpoint.model.events.eventTypes

enum class InboxEventType : EventType {
    INBOX_CREATE,
    INBOX_UPDATE,
    INBOX_DELETE,
    ENTRY_CREATE,
    ENTRY_DELETE
}