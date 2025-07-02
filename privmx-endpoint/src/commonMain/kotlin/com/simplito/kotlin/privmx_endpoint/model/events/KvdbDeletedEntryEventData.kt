package com.simplito.java.privmx_endpoint.model.events

/**
 * Holds information of `KvdbDeletedEntryEvent`.
 */
data class KvdbDeletedEntryEventData(
    /**
     * Kvdb ID
     */
    val kvdbId: String,
    /**
     * Key of deleted Entry
     */
    val kvdbEntryKey: String
) 