package com.simplito.java.privmx_endpoint.model.events

/**
 * Holds Kvdb statistical data.
 *
 * @property kvdbId Kvdb ID
 * @property lastEntryDate Timestamp of the most recent Kvdb item
 * @property entries  Updated number of entries in the Kvdb
 */
data class KvdbStatsEventData(
    val kvdbId: String,
    val lastEntryDate: Long,
    val entries: Long
) 