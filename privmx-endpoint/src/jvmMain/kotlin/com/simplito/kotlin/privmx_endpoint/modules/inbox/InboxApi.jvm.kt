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

package com.simplito.kotlin.privmx_endpoint.modules.inbox

import com.simplito.kotlin.privmx_endpoint.LibLoader
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

/**
 * Manages PrivMX Bridge Inboxes and Entries.
 * @param connection active connection to PrivMX Bridge
 * @param threadApi  instance of [ThreadApi] created on passed Connection
 * @param storeApi   instance of [StoreApi] created on passed Connection
 * @throws IllegalStateException when one of the passed parameters is closed
 */
actual class InboxApi
@Throws(IllegalStateException::class)
@JvmOverloads
actual constructor(
    connection: Connection, threadApi: ThreadApi?, storeApi: StoreApi?
) : AutoCloseable {
    companion object {
        init {
            LibLoader.load()
        }
    }

    private var api: Long? = null

    init {
        val tmpThreadApi = if (threadApi == null) ThreadApi(connection) else null
        val tmpStoreApi = if (storeApi == null) StoreApi(connection) else null

        api = init(
            connection,
            threadApi ?: tmpThreadApi!!,
            storeApi ?: tmpStoreApi!!
        )

        tmpThreadApi?.close()
        tmpStoreApi?.close()
    }

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
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    @JvmOverloads
    actual external fun createInbox(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        filesConfig: FilesConfig?,
        policies: ContainerPolicyWithoutItem?
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
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    @JvmOverloads
    actual external fun updateInbox(
        inboxId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        filesConfig: FilesConfig?,
        version: Long,
        force: Boolean,
        forceGenerateNewKey: Boolean,
        policies: ContainerPolicyWithoutItem?
    )

    /**
     * Gets a single Inbox by given Inbox ID.
     *
     * @param inboxId ID of the Inbox to get
     * @return Information about the Inbox
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun getInbox(inboxId: String): Inbox

    /**
     * Gets a list of Inboxes in given Context.
     *
     * @param contextId ID of the Context to get Inboxes from
     * @param skip      skip number of elements to skip from result
     * @param limit     limit of elements to return for query
     * @param sortOrder order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId    ID of the element from which query results should start
     * @param queryAsJson stringified JSON object with a custom field to filter result
     * @return list of Inboxes
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    @JvmOverloads
    actual external fun listInboxes(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?
    ): PagingList<Inbox>

    /**
     * Gets public data of given Inbox.
     * You do not have to be logged in to call this function.
     *
     * @param inboxId ID of the Inbox to get
     * @return Public accessible information about the Inbox
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun getInboxPublicView(inboxId: String): InboxPublicView

    /**
     * Deletes an Inbox by given Inbox ID.
     *
     * @param inboxId ID of the Inbox to delete
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun deleteInbox(inboxId: String)

    /**
     * Prepares a request to send data to an Inbox.
     * You do not have to be logged in to call this function.
     *
     * @param inboxId          ID of the Inbox to which the request applies
     * @param data             entry data to send
     * @param inboxFileHandles optional list of file handles that will be sent with the request
     * @param userPrivKey sender can optionally provide a private key, which will be used:
     * 1) to sign the sent data,
     * 2) to derivation of the public key, which will then be transferred along with the sent data and can be used in the future for further secure communication with the sender
     * @return Inbox handle
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    @JvmOverloads
    actual external fun prepareEntry(
        inboxId: String, data: ByteArray, inboxFileHandles: List<Long>, userPrivKey: String?
    ): Long?

    /**
     * Sends data to an Inbox.
     * You do not have to be logged in to call this function.
     *
     * @param inboxHandle ID of the Inbox to which the request applies
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun sendEntry(inboxHandle: Long)

    /**
     * Gets an entry from an Inbox.
     *
     * @param inboxEntryId ID of an entry to read from the Inbox
     * @return Data of the selected entry stored in the Inbox
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun readEntry(inboxEntryId: String): InboxEntry

    /**
     * Gets list of entries of given Inbox.
     *
     * @param inboxId   ID of the Inbox
     * @param skip      skip number of elements to skip from result
     * @param limit     limit of elements to return for query
     * @param sortOrder order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId    ID of the element from which query results should start
     * @param queryAsJson stringified JSON object with a custom field to filter result
     * @return list of entries
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    @JvmOverloads
    actual external fun listEntries(
        inboxId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?
    ): PagingList<InboxEntry>

    /**
     * Deletes an entry from an Inbox.
     *
     * @param inboxEntryId ID of an entry to delete
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun deleteEntry(inboxEntryId: String)

    /**
     * Creates a file handle to send a file to an Inbox.
     * You do not have to be logged in to call this function.
     *
     * @param publicMeta  public file's metadata
     * @param privateMeta private file's metadata
     * @param fileSize    size of the file to send
     * @return File handle
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun /*inboxFileHandle*/createFileHandle(
        publicMeta: ByteArray, privateMeta: ByteArray, fileSize: Long
    ): Long?

    /**
     * Sends a file's data chunk to an Inbox.
     * To send the entire file - divide it into pieces of the desired size and call the function for each fragment.
     * You do not have to be logged in to call this function.
     *
     * @param inboxHandle     handle to the prepared Inbox entry
     * @param inboxFileHandle handle to the file where the uploaded chunk belongs
     * @param dataChunk       file chunk to send
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun writeToFile(
        inboxHandle: Long, inboxFileHandle: Long, dataChunk: ByteArray
    )

    /**
     * Opens a file to read.
     *
     * @param fileId ID of the file to read
     * @return Handle to read file data
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun openFile(fileId: String): Long?

    /**
     * Reads file data.
     *
     * @param fileHandle handle to the file
     * @param length     size of data to read
     * @return File data chunk
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun readFromFile(fileHandle: Long, length: Long): ByteArray

    /**
     * Moves file's read cursor.
     *
     * @param fileHandle handle to the file
     * @param position   sets new cursor position
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun seekInFile(fileHandle: Long, position: Long)

    /**
     * Closes a file by given handle.
     *
     * @param fileHandle handle to the file
     * @return ID of closed file
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun closeFile(fileHandle: Long): String

    /**
     * Subscribes for the Inbox module main events.
     *
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun subscribeForInboxEvents()

    /**
     * Subscribes for the Inbox module main events.
     *
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun unsubscribeFromInboxEvents()

    /**
     * Subscribes for events in given Inbox.
     *
     * @param inboxId ID of the Inbox to subscribe
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun subscribeForEntryEvents(inboxId: String)

    /**
     * Unsubscribes from events in given Inbox.
     *
     * @param inboxId ID of the Inbox to unsubscribe
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun unsubscribeFromEntryEvents(inboxId: String)

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed
     */
    actual override fun close() {
        deinit()
    }

    @Throws(IllegalStateException::class)
    private external fun init(
        connection: Connection,
        threadApi: ThreadApi,
        storeApi: StoreApi
    ): Long?

    @Throws(IllegalStateException::class)
    private external fun deinit()
}
