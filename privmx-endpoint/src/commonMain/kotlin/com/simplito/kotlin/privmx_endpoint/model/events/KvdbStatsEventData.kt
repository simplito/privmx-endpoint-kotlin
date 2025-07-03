package com.simplito.java.privmx_endpoint.model.events

/**
 * Holds data of event that arrives when KVDB stats change.
 *
 * @property kvdbId KVDB ID
 * @property lastEntryDate Timestamp of the most recent KVDB item
 * @property entries  Updated number of entries in the KVDB
 */
data class KvdbStatsEventData(
    val kvdbId: String,
    val lastEntryDate: Long?,
    val entries: Long?
) 