//
// PrivMX Endpoint Java.
// Copyright © 2024 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.simplito.java.privmx_endpoint.modules.thread

import com.simplito.java.privmx_endpoint.model.ContainerPolicy
import com.simplito.java.privmx_endpoint.model.Message
import com.simplito.java.privmx_endpoint.model.PagingList
import com.simplito.java.privmx_endpoint.model.Thread
import com.simplito.java.privmx_endpoint.model.UserWithPubKey
import com.simplito.java.privmx_endpoint.model.exceptions.NativeException
import com.simplito.java.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.java.privmx_endpoint.modules.core.Connection

/**
 * Manages Threads and messages.
 *
 * @category thread
 */
expect class ThreadApi(connection: Connection) : AutoCloseable {

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
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @event type: threadCreated
     * channel: thread
     * payload: [Thread]
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun createThread(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy? = null
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
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @event type: threadUpdated
     * channel: thread
     * payload: [Thread]
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun updateThread(
        threadId: String,
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
     * Gets a Thread by given Thread ID.
     *
     * @param threadId ID of Thread to get
     * @return Information about the Thread
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
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
        sortOrder: String,
        lastId: String? = null
    ): PagingList<Thread>

    /**
     * Deletes a Thread by given Thread ID.
     *
     * @param threadId ID of the Thread to delete
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @event type: threadDeleted
     * channel: thread
     * payload: [ThreadDeletedEventData]
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
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @event type: threadNewMessage
     * channel: thread/&lt;threadId&gt;/messages
     * payload: [Message]
     * @event type: threadStats
     * channel: thread
     * payload: [ThreadStatsEventData]
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
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
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
     * @return list of messages
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun listMessages(
        threadId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?
    ): PagingList<Message>


    /**
     * Deletes a message by given message ID.
     *
     * @param messageId ID of the message to delete
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @event type: threadMessageDeleted
     * channel: thread/&lt;threadId&gt;/messages
     * payload: [ThreadDeletedMessageEventData]
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
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @event type: threadUpdatedMessage
     * channel: thread/&lt;threadId&gt;/messages
     * payload: [Message]
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun updateMessage(
        messageId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        data: ByteArray
    )

    /**
     * Subscribes for the Thread module main events.
     *
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun subscribeForThreadEvents()

    /**
     * Unsubscribes from the Thread module main events.
     *
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun unsubscribeFromThreadEvents()

    /**
     * Subscribes for events in given Thread.
     *
     * @param threadId ID of the Thread to subscribe
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun subscribeForMessageEvents(threadId: String)

    /**
     * Unsubscribes from events in given Thread.
     *
     * @param threadId ID of the Thread to unsubscribe
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun unsubscribeFromMessageEvents(threadId: String)

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed.
     */
    override fun close()
}
