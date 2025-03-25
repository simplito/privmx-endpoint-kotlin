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
package com.simplito.kotlin.privmx_endpoint.modules.inbox

import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicyWithoutItem
import com.simplito.kotlin.privmx_endpoint.model.FilesConfig
import com.simplito.kotlin.privmx_endpoint.model.Inbox
import com.simplito.kotlin.privmx_endpoint.model.InboxEntry
import com.simplito.kotlin.privmx_endpoint.model.InboxPublicView
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.store.StoreApi
import com.simplito.kotlin.privmx_endpoint.modules.thread.ThreadApi
import kotlin.jvm.JvmOverloads

/**
 * Manages PrivMX Bridge Inboxes and Entries.
 *
 * @category inbox
 */
expect class InboxApi(
    connection: Connection,
    threadApi: ThreadApi? = null,
    storeApi: StoreApi? = null
) :
    AutoCloseable {
    /**
     * Creates a new Inbox.
     *
     * @param contextId   ID of the Context of the new Inbox
     * @param users       list of [UserWithPubKey] structs which indicates who will have access to the created Inbox
     * @param managers    list of [UserWithPubKey] structs which indicates who will have access (and management rights) to
     * the created Inbox
     * @param publicMeta  public (unencrypted) metadata
     * @param privateMeta private (encrypted) metadata
     * @param filesConfig overrides default file configuration
     * @param policies    additional container access policies
     * @return ID of the created Inbox
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     * @event type: inboxCreated
     * channel: inbox
     * payload: [Inbox]
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun createInbox(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        filesConfig: FilesConfig? = null,
        policies: ContainerPolicyWithoutItem? = null
    ): String

    /**
     * Updates an existing Inbox.
     *
     * @param inboxId             ID of the Inbox to update
     * @param users               list of [UserWithPubKey] structs which indicates who will have access to the created Inbox
     * @param managers            list of [UserWithPubKey] structs which indicates who will have access (and management rights) to
     * the created Inbox
     * @param publicMeta          public (unencrypted) metadata
     * @param privateMeta         private (encrypted) metadata
     * @param filesConfig         overrides default file configuration
     * @param version             current version of the updated Inbox
     * @param force               force update (without checking version)
     * @param forceGenerateNewKey force to regenerate a key for the Inbox
     * @param policies            additional container access policies
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     * @event type: inboxUpdated
     * channel: inbox
     * payload: [Inbox]
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun updateInbox(
        inboxId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        filesConfig: FilesConfig? = null,
        version: Long,
        force: Boolean = false,
        forceGenerateNewKey: Boolean = false,
        policies: ContainerPolicyWithoutItem? = null
    )

    /**
     * Gets a single Inbox by given Inbox ID.
     *
     * @param inboxId ID of the Inbox to get
     * @return Information about the Inbox
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun getInbox(inboxId: String): Inbox

    /**
     * Gets s list of Inboxes in given Context.
     *
     * @param contextId ID of the Context to get Inboxes from
     * @param skip      skip number of elements to skip from result
     * @param limit     limit of elements to return for query
     * @param sortOrder order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId    ID of the element from which query results should start
     * @return list of Inboxes
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun listInboxes(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String = "desc",
        lastId: String? = null
    ): PagingList<Inbox>


    /**
     * Gets public data of given Inbox.
     * You do not have to be logged in to call this function.
     *
     * @param inboxId ID of the Inbox to get
     * @return Public accessible information about the Inbox
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun getInboxPublicView(inboxId: String): InboxPublicView

    /**
     * Deletes an Inbox by given Inbox ID.
     *
     * @param inboxId ID of the Inbox to delete
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     * @event type: inboxDeleted
     * channel: inbox
     * payload: [InboxDeletedEventData]
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun deleteInbox(inboxId: String)

    /**
     * Prepares a request to send data to an Inbox.
     * You do not have to be logged in to call this function.
     *
     * @param inboxId          ID of the Inbox to which the request applies
     * @param data             entry data to send
     * @param inboxFileHandles optional list of file handles that will be sent with the request
     * @return Inbox handle
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun prepareEntry(
        inboxId: String,
        data: ByteArray,
        inboxFileHandles: List<Long> = emptyList<Long>(),
        userPrivKey: String? = null
    ): Long?

    /**
     * Sends data to an Inbox.
     * You do not have to be logged in to call this function.
     *
     * @param inboxHandle ID of the Inbox to which the request applies
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     * @event type: inboxEntryCreated
     * channel: inbox/&lt;inboxId&gt;/entries
     * payload: [InboxEntry]
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun sendEntry(inboxHandle: Long)

    /**
     * Gets an entry from an Inbox.
     *
     * @param inboxEntryId ID of an entry to read from the Inbox
     * @return Data of the selected entry stored in the Inbox
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun readEntry(inboxEntryId: String): InboxEntry

    /**
     * Gets list of entries of given Inbox.
     *
     * @param inboxId   ID of the Inbox
     * @param skip      skip number of elements to skip from result
     * @param limit     limit of elements to return for query
     * @param sortOrder order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId    ID of the element from which query results should start
     * @return list of entries
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun listEntries(
        inboxId: String,
        skip: Long,
        limit: Long,
        sortOrder: String = "desc",
        lastId: String? = null
    ): PagingList<InboxEntry>

    /**
     * Deletes an entry from an Inbox.
     *
     * @param inboxEntryId ID of an entry to delete
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     * @event type: inboxEntryDeleted
     * channel: inbox/&lt;inboxId&gt;/entries
     * payload: [InboxEntryDeletedEventData]
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun deleteEntry(inboxEntryId: String)

    /**
     * Creates a file handle to send a file to an Inbox.
     * You do not have to be logged in to call this function.
     *
     * @param publicMeta  public file's metadata
     * @param privateMeta private file's metadata
     * @param fileSize    size of the file to send
     * @return File handle
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun /*inboxFileHandle*/createFileHandle(
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        fileSize: Long
    ): Long?

    /**
     * Sends a file's data chunk to an Inbox.
     * To send the entire file - divide it into pieces of the desired size and call the function for each fragment.
     * You do not have to be logged in to call this function.
     *
     * @param inboxHandle     handle to the prepared Inbox entry
     * @param inboxFileHandle handle to the file where the uploaded chunk belongs
     * @param dataChunk       file chunk to send
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun writeToFile(
        inboxHandle: Long,
        inboxFileHandle: Long,
        dataChunk: ByteArray
    )


    /**
     * Opens a file to read.
     *
     * @param fileId ID of the file to read
     * @return Handle to read file data
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun openFile(fileId: String): Long?

    /**
     * Reads file data.
     *
     * @param fileHandle handle to the file
     * @param length     size of data to read
     * @return File data chunk
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun readFromFile(fileHandle: Long, length: Long): ByteArray

    /**
     * Moves file's read cursor.
     *
     * @param fileHandle handle to the file
     * @param position   sets new cursor position
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun seekInFile(fileHandle: Long, position: Long)

    /**
     * Closes a file by given handle.
     *
     * @param fileHandle handle to the file
     * @return ID of closed file
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun closeFile(fileHandle: Long): String

    /**
     * Subscribes for the Inbox module main events.
     *
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun subscribeForInboxEvents()

    /**
     * Subscribes for the Inbox module main events.
     *
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun unsubscribeFromInboxEvents()

    /**
     * Subscribes for events in given Inbox.
     *
     * @param inboxId ID of the Inbox to subscribe
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun subscribeForEntryEvents(inboxId: String)

    /**
     * Unsubscribes from events in given Inbox.
     *
     * @param inboxId ID of the Inbox to unsubscribe
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @throws IllegalStateException thrown when instance is closed.
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun unsubscribeFromEntryEvents(inboxId: String)

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed.
     */
    override fun close()
}
