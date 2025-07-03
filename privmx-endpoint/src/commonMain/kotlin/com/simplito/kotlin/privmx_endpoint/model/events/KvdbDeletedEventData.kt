package com.simplito.java.privmx_endpoint.model.events

/**
 * Holds data of event that arrives when KVDB is deleted.
 *
 * @property kvdbId KVDB ID
 */
data class KvdbDeletedEventData(
    val kvdbId: String
) 