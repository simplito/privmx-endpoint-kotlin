//
// PrivMX Endpoint Kotlin.
// Copyright © 2025 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.simplito.kotlin.privmx_endpoint.modules.kvdb

import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.GroupGrantWithKey
import com.simplito.kotlin.privmx_endpoint.model.Kvdb
import com.simplito.kotlin.privmx_endpoint.model.KvdbEntry
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.KvdbEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.KvdbEventType
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.group.GroupApi

/**
 * Manages PrivMX Bridge KVDBs and their entries.
 * @param connection active connection to PrivMX Bridge
 * @param groupApi instance of [GroupApi], required to read and write KVDBs granted to Groups. Passing `null`
 * creates a Group-unaware `KvdbApi`.
 * @throws IllegalStateException when given [Connection] is not connected
 */
expect class KvdbApi(connection: Connection, groupApi: GroupApi? = null) : AutoCloseable {

    /**
     * Creates a new KVDB in given Context.
     *
     * @param contextId   ID of the Context to create the KVDB in
     * @param users       list of [UserWithPubKey] which indicates who will have access to the created KVDB
     * @param managers    list of [UserWithPubKey] which indicates who will have access (and management rights) to the created KVDB
     * @param publicMeta  public (unencrypted) metadata
     * @param privateMeta private (encrypted) metadata
     * @param policies    KVDB's policies
     * @param groups      Groups granted access to the created KVDB, with their verified epoch public keys
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
        policies: ContainerPolicy? = null,
        groups: List<GroupGrantWithKey> = emptyList()
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
     * @param groups              Groups granted access to the KVDB, with their verified epoch public keys.
     * The list is authoritative — an empty list revokes every Group grant the KVDB had.
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
        policies: ContainerPolicy? = null,
        groups: List<GroupGrantWithKey> = emptyList()
    )

    /**
     * Re-encrypts the KVDB key for all current members without changing data, membership, or policy.
     *
     * Unlike [updateKvdb] this can be called by any KVDB member (not just managers) when the default
     * `rotateKeys` policy of `"user"` is in effect.
     *
     * The KVDB's key is re-wrapped to every one of its grantee Groups at that Group's current epoch, whether or
     * not it is named in [groups]: the grantee list comes from the KVDB itself, and any epoch public key missing
     * from [groups] is read from the Bridge. A caller who belongs to none of the KVDB's grantee Groups, and
     * cannot supply their epoch keys in [groups] either, gets `UnresolvedGroupGranteeException`.
     *
     * @param kvdbId   ID of the KVDB to re-key
     * @param users    current KVDB users with their public keys
     * @param managers current KVDB managers with their public keys
     * @param version  current KVDB version (optimistic lock guard)
     * @param force    skip the version check when `true`
     * @param groups   epoch public keys of grantee Groups the caller has verified itself; optional, and Groups the
     * KVDB does not grant are ignored — a re-key changes no grants
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun rotateKvdbKeys(
        kvdbId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        version: Long,
        force: Boolean = false,
        groups: List<GroupGrantWithKey> = emptyList()
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
     * Deletes KVDB entries by given KVDB IDs and the set of entry keys.
     *
     * @param kvdbId ID of the KVDB database to delete from
     * @param keys   set of the keys of the KVDB entries to delete
     * @return map with the statuses of deletion for every key
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun deleteEntries(
        kvdbId: String,
        keys: Set<String>
    ): Map<String, Boolean>

    /**
     * Subscribe for the KVDB events on the given subscription query.
     *
     * @param subscriptionQueries list of queries
     * @return list of subscriptionIds in matching order to subscriptionQueries
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun subscribeFor(subscriptionQueries: List<String>): List<String>

    /**
     * Unsubscribe from events with the given subscriptionId.
     *
     * @param subscriptionIds list of subscriptionId
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun unsubscribeFrom(subscriptionIds: List<String>)

    /**
     * Generate subscription Query for the KVDB events.
     *
     * @param eventType    type of event you listen for
     * @param selectorType scope on which you listen for events
     * @param selectorId   ID of the selector
     * @return Query for subscribing event
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun buildSubscriptionQuery(
        eventType: KvdbEventType,
        selectorType: KvdbEventSelectorType,
        selectorId: String
    ): String

    /**
     * Generate subscription Query for the KVDB events for single KvdbEntry.
     *
     * @param eventType    type of event you listen for
     * @param kvdbId       Id of Kvdb
     * @param kvdbEntryKey Key of Kvdb Entry
     * @return Query to subscribe to an event.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun buildSubscriptionQueryForSelectedEntry(
        eventType: KvdbEventType,
        kvdbId: String,
        kvdbEntryKey: String
    ): String

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed.
     */
    override fun close()
}