package com.simplito.kotlin.privmx_endpoint.model

/**
 * Holds Kvdb entry's information created by the server.
 *
 * @param kvdbId ID of the Kvdb
 * @param key Kvdb entry's key
 * @param createDate Entry's creation timestamp
 * @param author ID of the user who created the entry
 *
 * @category kvdb
 * @group Kvdb
 */
class ServerKvdbEntryInfo(
    val kvdbId: String,
    val key: String,
    val createDate: Long?,
    val author: String
) 