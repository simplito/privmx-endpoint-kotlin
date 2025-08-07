package com.simplito.kotlin.privmx_endpoint.model.events.eventTypes

enum class KvdbEventType : EventType {
    KVDB_CREATE,
    KVDB_UPDATE,
    KVDB_DELETE,
    KVDB_STATS,
    ENTRY_CREATE,
    ENTRY_UPDATE,
    ENTRY_DELETE
}
