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
package com.simplito.kotlin.privmx_endpoint.modules.thread

import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.GroupGrantWithKey
import com.simplito.kotlin.privmx_endpoint.model.Message
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.Thread
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.ThreadEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.ThreadEventType
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.group.GroupApi

/**
 * Manages Threads and messages.
 * @param connection active connection to PrivMX Bridge
 * @param groupApi instance of [GroupApi], required to read and write Threads granted to Groups. Passing `null`
 * creates a Group-unaware `ThreadApi`.
 * @throws IllegalStateException when given [Connection] is not connected
 */
expect class ThreadApi
@Throws(IllegalStateException::class)
constructor(connection: Connection, groupApi: GroupApi? = null) : AutoCloseable {
    /**
     * Creates a new Thread in given Context.
     *
     * @param contextId   ID of the Context to create the Thread in
     * @param users       list of [UserWithPubKey] which indicates who will have access to the created Thread
     * @param managers    list of [UserWithPubKey] which indicates who will have access (and management rights) to
     * the created Thread
     * @param publicMeta  public (unencrypted) metadata
     * @param privateMeta private (encrypted) metadata
     * @param policies    additional container access policies
     * @param groups      Groups granted access to the created Thread, with their verified epoch public keys
     * @return ID of the created Thread
     * @throws IllegalStateException thrown when instance is closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun createThread(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy? = null,
        groups: List<GroupGrantWithKey> = emptyList()
    ): String

    /**
     * Updates an existing Thread.
     *
     * @param threadId            ID of the Thread to update
     * @param users               list of [UserWithPubKey] which indicates who will have access to the updated Thread
     * @param managers            list of [UserWithPubKey] which indicates who will have access (and management rights) to
     * the updated Thread
     * @param publicMeta          public (unencrypted) metadata
     * @param privateMeta         private (encrypted) metadata
     * @param version             current version of the updated Thread
     * @param force               force update (without checking version)
     * @param forceGenerateNewKey force to regenerate a key for the Thread
     * @param policies            additional container access policies
     * @param groups              Groups granted access to the Thread, with their verified epoch public keys.
     * The list is authoritative — an empty list revokes every Group grant the Thread had.
     * @throws IllegalStateException thrown when instance is closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun updateThread(
        threadId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        version: Long,
        force: Boolean = false,
        forceGenerateNewKey: Boolean = false,
        policies: ContainerPolicy? = null,
        groups: List<GroupGrantWithKey> = emptyList()
    )

    /**
     * Re-encrypts the Thread key for all current members without changing data, membership, or policy.
     *
     * Unlike [updateThread] this can be called by any Thread member (not just managers) when the default
     * `rotateKeys` policy of `"user"` is in effect.
     *
     * The Thread's key is re-wrapped to every one of its grantee Groups at that Group's current epoch, whether or
     * not it is named in [groups]: the grantee list comes from the Thread itself, and any epoch public key missing
     * from [groups] is read from the Bridge. A caller who belongs to none of the Thread's grantee Groups, and
     * cannot supply their epoch keys in [groups] either, gets `UnresolvedGroupGranteeException`.
     *
     * @param threadId ID of the Thread to re-key
     * @param users    current Thread users with their public keys
     * @param managers current Thread managers with their public keys
     * @param version  current Thread version (optimistic lock guard)
     * @param force    skip the version check when `true`
     * @param groups   epoch public keys of grantee Groups the caller has verified itself; optional, and Groups the
     * Thread does not grant are ignored — a re-key changes no grants
     * @throws IllegalStateException thrown when instance is closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun rotateThreadKeys(
        threadId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        version: Long,
        force: Boolean = false,
        groups: List<GroupGrantWithKey> = emptyList()
    )

    /**
     * Gets a Thread by given Thread ID.
     *
     * @param threadId ID of Thread to get
     * @return Information about the Thread
     * @throws IllegalStateException thrown when instance is closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun getThread(threadId: String): Thread

    /**
     * Gets a list of Threads in given Context.
     *
     * @param contextId ID of the Context to get the Threads from
     * @param skip      skip number of elements to skip from result
     * @param limit     limit of elements to return for query
     * @param sortOrder order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId    ID of the element from which query results should start
     * @param queryAsJson stringified JSON object with a custom field to filter result
     * @param sortBy      field name to sort elements by
     * @return list of Threads
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun listThreads(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String = "desc",
        lastId: String? = null,
        queryAsJson: String? = null,
        sortBy: String? = null
    ): PagingList<Thread>

    /**
     * Deletes a Thread by given Thread ID.
     *
     * @param threadId ID of the Thread to delete
     * @throws IllegalStateException thrown when instance is closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun deleteThread(threadId: String)

    /**
     * Sends a message in a Thread.
     *
     * @param threadId    ID of the Thread to send message to
     * @param publicMeta  public message metadata
     * @param privateMeta private message metadata
     * @param data        content of the message
     * @return ID of the new message
     * @throws IllegalStateException thrown when instance is closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun sendMessage(
        threadId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        data: ByteArray
    ): String

    /**
     * Gets a message by given message ID.
     *
     * @param messageId ID of the message to get
     * @return Message with matching id
     * @throws IllegalStateException thrown when instance is closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun getMessage(messageId: String): Message

    /**
     * Gets a list of messages from a Thread.
     *
     * @param threadId  ID of the Thread to list messages from
     * @param skip      skip number of elements to skip from result
     * @param limit     limit of elements to return for query
     * @param sortOrder order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId    ID of the element from which query results should start
     * @param queryAsJson stringified JSON object with a custom field to filter result
     * @param sortBy      field name to sort elements by
     * @return list of messages
     * @throws IllegalStateException thrown when instance is closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun listMessages(
        threadId: String,
        skip: Long,
        limit: Long,
        sortOrder: String = "desc",
        lastId: String? = null,
        queryAsJson: String? = null,
        sortBy: String? = null
    ): PagingList<Message>

    /**
     * Deletes a message by given message ID.
     *
     * @param messageId ID of the message to delete
     * @throws IllegalStateException thrown when instance is closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun deleteMessage(messageId: String)

    /**
     * Updates message in a Thread.
     *
     * @param messageId   ID of the message to update
     * @param publicMeta  public message metadata
     * @param privateMeta private message metadata
     * @param data        new content of the message
     * @throws IllegalStateException thrown when instance is closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun updateMessage(
        messageId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        data: ByteArray
    )

    /**
     * Subscribe for the Thread events on the given subscription query.
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
     * Generate subscription Query for the Thread events.
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
        eventType: ThreadEventType,
        selectorType: ThreadEventSelectorType,
        selectorId: String
    ): String

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed
     */
    override fun close()
}