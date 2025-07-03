package com.simplito.kotlin.privmx_endpoint.modules.kvdb

import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.Kvdb
import com.simplito.kotlin.privmx_endpoint.model.KvdbEntry
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection

/**
 * Manages PrivMX Bridge  KVDBs and their messages.
 */
expect class KvdbApi(connection: Connection) : AutoCloseable {

    /**
     * Creates a new KVDB in given Context.
     *
     * @param contextId   ID of the Context to create the KVDB in
     * @param users       list of [UserWithPubKey] which indicates who will have access to the created KVDB
     * @param managers    list of [UserWithPubKey] which indicates who will have access (and management rights) to the created KVDB
     * @param publicMeta  public (unencrypted) metadata
     * @param privateMeta private (encrypted) metadata
     * @param policies    KVDB's policies
     * @return ID of the created KVDB
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun createKvdb(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy? = null
    ): String


    /**
     * Updates an existing KVDB.
     *
     * @param kvdbId              ID of the KVDB to update
     * @param users               list of [UserWithPubKey] which indicates who will have access to the created KVDB
     * @param managers            list of [UserWithPubKey] which indicates who will have access (and management rights) to the created KVDB
     * @param publicMeta          public (unencrypted) metadata
     * @param privateMeta         private (encrypted) metadata
     * @param version             current version of the updated KVDB
     * @param force               force update (without checking version)
     * @param forceGenerateNewKey force to regenerate a key for the KVDB
     * @param policies            KVDB's policies
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun updateKvdb(
        kvdbId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        version: Long,
        force: Boolean,
        forceGenerateNewKey: Boolean = false,
        policies: ContainerPolicy? = null
    )


    /**
     * Deletes a KVDB by given KVDB ID.
     *
     * @param kvdbId ID of the KVDB to delete
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun deleteKvdb(kvdbId: String)

    /**
     * Gets a KVDB by given KVDB ID.
     *
     * @param kvdbId ID of KVDB to get
     * @return object containing info about the KVDB
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun getKvdb(kvdbId: String): Kvdb

    /**
     * Gets a list of KVDBs in given Context.
     *
     * @param contextId   ID of the Context to get the KVDBs from
     * @param skip        skip number of elements to skip from result
     * @param limit       limit of elements to return for query
     * @param sortOrder   order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId      ID of the element from which query results should start
     * @param queryAsJson stringified JSON object with a custom field to filter result
     * @param sortBy      field by elements are sorted in result
     * @return list of KVDBs
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun listKvdbs(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String = "desc",
        lastId: String? = null,
        queryAsJson: String? = null,
        sortBy: String? = null
    ): PagingList<Kvdb>


    /**
     * Gets a KVDB entry by given KVDB entry key and KVDB ID.
     *
     * @param kvdbId KVDB ID of the KVDB entry to get
     * @param key    key of the KVDB entry to get
     * @return object containing the KVDB entry
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun getEntry(
        kvdbId: String,
        key: String
    ): KvdbEntry

    /**
     * Check whether the KVDB entry exists.
     *
     * @param kvdbId KVDB ID of the KVDB entry to check
     * @param key    key of the KVDB entry to check
     * @return 'true' if the KVDB has an entry with given key, 'false' otherwise
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun hasEntry(
        kvdbId: String,
        key: String
    ): Boolean

    /**
     * Gets a list of KVDB entries keys from a KVDB.
     *
     * @param kvdbId      ID of the KVDB to list KVDB entries from
     * @param skip        skip number of elements to skip from result
     * @param limit       limit of elements to return for query
     * @param sortOrder   order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId      ID of the element from which query results should start
     * @param queryAsJson stringified JSON object with a custom field to filter result
     * @param sortBy      field by elements are sorted in result
     * @return list of KVDB entries
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun listEntriesKeys(
        kvdbId: String,
        skip: Long,
        limit: Long,
        sortOrder: String = "desc",
        lastId: String? = null,
        queryAsJson: String? = null,
        sortBy: String? = null
    ): PagingList<String>

    /**
     * Gets a list of KVDB entries from a KVDB.
     *
     * @param kvdbId      ID of the KVDB to list KVDB entries from
     * @param skip        skip number of elements to skip from result
     * @param limit       limit of elements to return for query
     * @param sortOrder   order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId      ID of the element from which query results should start
     * @param queryAsJson stringified JSON object with a custom field to filter result
     * @param sortBy      field by elements are sorted in result
     * @return list of KVDB entries
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun listEntries(
        kvdbId: String,
        skip: Long,
        limit: Long,
        sortOrder: String = "desc",
        lastId: String? = null,
        queryAsJson: String? = null,
        sortBy: String? = null
    ): PagingList<KvdbEntry>


    /**
     * Sets a KVDB entry in the given KVDB.
     *
     * @param kvdbId      ID of the KVDB to set the entry to
     * @param key         KVDB entry key
     * @param publicMeta  public KVDB entry metadata
     * @param privateMeta private KVDB entry metadata
     * @param data        content of the KVDB entry
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun setEntry(
        kvdbId: String,
        key: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        data: ByteArray,
        version: Long = 0
    )

    /**
     * Deletes a KVDB entry by given KVDB entry ID.
     *
     * @param kvdbId KVDB ID of the KVDB entry to delete
     * @param key    key of the KVDB entry to delete
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun deleteEntry(
        kvdbId: String,
        key: String
    )

    /**
     * Deletes KVDB entries by given KVDB IDs and the list of entry keys.
     *
     * @param kvdbId ID of the KVDB database to delete from
     * @param keys   vector of the keys of the KVDB entries to delete
     * @return map with the statuses of deletion for every key
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun deleteEntries(
        kvdbId: String,
        keys: List<String>
    ): Map<String, Boolean>

    /**
     * Subscribes for the KVDB module main events.
     *
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun subscribeForKvdbEvents()

    /**
     * Unsubscribes from the KVDB module main events.
     *
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun unsubscribeFromKvdbEvents()

    /**
     * Subscribes for events in given KVDB.
     *
     * @param kvdbId ID of the KVDB to subscribe
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun subscribeForEntryEvents(kvdbId: String)

    /**
     * Unsubscribes from events in given KVDB.
     *
     * @param kvdbId ID of the KVDB to unsubscribe
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun unsubscribeFromEntryEvents(kvdbId: String)

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed.
     */
    override fun close()
}