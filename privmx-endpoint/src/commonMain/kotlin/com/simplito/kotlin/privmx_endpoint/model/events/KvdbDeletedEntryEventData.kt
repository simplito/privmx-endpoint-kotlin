package com.simplito.java.privmx_endpoint.model.events

/**
 * Holds information of `KvdbDeletedEntryEvent`.
 *
 * @property kvdbId Kvdb ID
 * @property kvdbEntryKey Key of deleted Entry
 */
data class KvdbDeletedEntryEventData(
    val kvdbId: String,
    val kvdbEntryKey: String
) 