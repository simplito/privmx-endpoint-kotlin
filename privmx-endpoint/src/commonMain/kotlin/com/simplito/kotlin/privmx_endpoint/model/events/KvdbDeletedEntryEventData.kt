package com.simplito.java.privmx_endpoint.model.events

/**
 * Holds data of event that arrives when KVDB entry is deleted.
 *
 * @property kvdbId KVDB ID
 * @property kvdbEntryKey Key of deleted Entry
 */
data class KvdbDeletedEntryEventData(
    val kvdbId: String,
    val kvdbEntryKey: String
)