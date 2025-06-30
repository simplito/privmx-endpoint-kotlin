package com.simplito.java.privmx_endpoint.model.events

/**
 * Holds information of `KvdbDeletedEvent`.
 *
 * @category core
 * @group Events
 */
data class KvdbDeletedEventData(
    /**
     * Kvdb ID
     */
    val kvdbId: String
) 