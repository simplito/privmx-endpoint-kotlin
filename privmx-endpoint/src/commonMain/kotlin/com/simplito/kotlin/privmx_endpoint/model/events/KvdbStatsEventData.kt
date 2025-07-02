package com.simplito.java.privmx_endpoint.model.events

/**
 * Holds Kvdb statistical data.
 */
data class KvdbStatsEventData(
    /**
     * Kvdb ID
     */
    val kvdbId: String,
    /**
     * Timestamp of the most recent Kvdb item
     */
    val lastEntryDate: Long,
    /**
     * Updated number of entries in the Kvdb
     */
    val entries: Long
) 