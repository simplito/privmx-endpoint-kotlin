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

import com.simplito.kotlin.privmx_endpoint.LibLoader
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
import java.lang.AutoCloseable

/**
 * Manages Threads and messages.
 * @param connection active connection to PrivMX Bridge
 * @param groupApi instance of [GroupApi], required to read and write Threads granted to Groups
 * @throws IllegalStateException when given [Connection] is not connected
 */
actual class ThreadApi
@Throws(IllegalStateException::class)
@JvmOverloads
actual constructor(connection: Connection, groupApi: GroupApi?) : AutoCloseable {
    companion object {
        init {
            LibLoader.loadPrivmxLibraries()
        }
    }

    private var api: Long? = null

    init {
        api = init(connection, groupApi)
    }

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
     * @return ID of the created Thread
     * @throws IllegalStateException thrown when instance is closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual external fun createThread(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy?,
        groups: List<GroupGrantWithKey>
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
     * @throws IllegalStateException thrown when instance is closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual external fun updateThread(
        threadId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        version: Long,
        force: Boolean,
        forceGenerateNewKey: Boolean,
        policies: ContainerPolicy?,
        groups: List<GroupGrantWithKey>
    )

    /**
     * Re-encrypts the Thread key for all current members without changing data, membership, or policy.
     *
     * @param threadId ID of the Thread to re-key
     * @param users    current Thread users with their public keys
     * @param managers current Thread managers with their public keys
     * @param version  current Thread version (optimistic lock guard)
     * @param force    skip the version check when `true`
     * @param groups   epoch public keys of grantee Groups the caller has verified itself
     * @throws IllegalStateException thrown when instance is closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual external fun rotateThreadKeys(
        threadId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        version: Long,
        force: Boolean,
        groups: List<GroupGrantWithKey>
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
    actual external fun getThread(threadId: String): Thread

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
    @JvmOverloads
    actual external fun listThreads(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?,
        sortBy: String?
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
    actual external fun deleteThread(threadId: String)

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
    actual external fun sendMessage(
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
    actual external fun getMessage(messageId: String): Message

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
    @JvmOverloads
    actual external fun listMessages(
        threadId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?,
        sortBy: String?
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
    actual external fun deleteMessage(messageId: String)

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
    actual external fun updateMessage(
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
    actual external fun subscribeFor(subscriptionQueries: List<String>): List<String>

    /**
     * Unsubscribe from events with the given subscriptionId.
     *
     * @param subscriptionIds list of subscriptionId
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun unsubscribeFrom(subscriptionIds: List<String>)

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    private external fun buildSubscriptionQuery(
        eventType: Long,
        selectorType: Long,
        selectorId: String
    ): String

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
    actual fun buildSubscriptionQuery(
        eventType: ThreadEventType,
        selectorType: ThreadEventSelectorType,
        selectorId: String
    ): String {
        return buildSubscriptionQuery(
            eventType.ordinal.toLong(),
            selectorType.ordinal.toLong(),
            selectorId
        )
    }

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed
     */
    actual override fun close() {
        deinit()
    }

    @Throws(IllegalStateException::class)
    private external fun init(connection: Connection, groupApi: GroupApi?): Long?

    @Throws(IllegalStateException::class)
    private external fun deinit()
}