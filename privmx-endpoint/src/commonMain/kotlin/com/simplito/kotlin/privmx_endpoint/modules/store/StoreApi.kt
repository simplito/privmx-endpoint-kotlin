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
package com.simplito.kotlin.privmx_endpoint.modules.store

import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.File
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.Store
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection

/**
 * Manages PrivMX Bridge Stores and Files.
 * @param connection active connection to PrivMX Bridge
 * @throws IllegalStateException when given [Connection] is not connected
 * @category store
 */
expect class StoreApi
@Throws(IllegalStateException::class)
constructor(connection: Connection) : AutoCloseable {
    /**
     * Creates a new Store in given Context.
     *
     * @param contextId   ID of the Context to create the Store in
     * @param users       list of [UserWithPubKey] which indicates who will have access to the created Store
     * @param managers    list of [UserWithPubKey] which indicates who will have access (and management rights) to the
     * created Store
     * @param publicMeta  public (unencrypted) metadata
     * @param privateMeta private (encrypted) metadata
     * @return Created Store ID
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @event type: storeCreated
     * channel: store
     * payload: [Store]
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun createStore(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy? = null
    ): String

    /**
     * Updates an existing Store.
     *
     * @param storeId     ID of the Store to update
     * @param users       list of [UserWithPubKey] which indicates who will have access to the updated Store
     * @param managers    list of [UserWithPubKey] which indicates who will have access (and management rights) to the
     * updated Store
     * @param publicMeta  public (unencrypted) metadata
     * @param privateMeta private (encrypted) metadata
     * @param version     current version of the updated Store
     * @param force       force update (without checking version)
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @event type: storeUpdated
     * channel: store
     * payload: [Store]
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun updateStore(
        storeId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        version: Long,
        force: Boolean = false,
        forceGenerateNewKey: Boolean = false,
        policies: ContainerPolicy? = null
    )

    /**
     * Gets a single Store by given Store ID.
     *
     * @param storeId ID of the Store to get
     * @return Information about the Store
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun getStore(storeId: String): Store

    /**
     * Gets a list of Stores in given Context.
     *
     * @param contextId ID of the Context to get the Stores from
     * @param skip      skip number of elements to skip from result
     * @param limit     limit of elements to return for query
     * @param sortOrder order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId    ID of the element from which query results should start
     * @return list of Stores
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun listStores(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String = "desc",
        lastId: String? = null
    ): PagingList<Store>

    /**
     * Deletes a Store by given Store ID.
     *
     * @param storeId ID of the Store to delete
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @event type: storeDeleted
     * channel: store
     * payload: [StoreDeletedEventData]
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun deleteStore(storeId: String)

    /**
     * Creates a new file in a Store.
     *
     * @param storeId     ID of the Store to create the file in
     * @param publicMeta  public file metadata
     * @param privateMeta private file metadata
     * @param size        size of the file
     * @return Handle to write data
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun createFile(
        storeId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        size: Long
    ): Long?

    /**
     * Updates an existing file in a Store.
     *
     * @param fileId      ID of the file to update
     * @param publicMeta  public file metadata
     * @param privateMeta private file metadata
     * @param size        size of the file
     * @return Handle to write file data
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun updateFile(
        fileId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        size: Long
    ): Long?

    /**
     * Updates metadata of an existing file in a Store.
     *
     * @param fileId      ID of the file to update
     * @param publicMeta  public file metadata
     * @param privateMeta private file metadata
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @event type: storeFileUpdated
     * channel: store/&lt;storeId&gt;/files
     * payload: [File]
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun updateFileMeta(fileId: String, publicMeta: ByteArray, privateMeta: ByteArray)

    /**
     * Writes a file data.
     *
     * @param fileHandle handle to write file data
     * @param dataChunk  file data chunk
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun writeToFile(fileHandle: Long, dataChunk: ByteArray)

    /**
     * Deletes a file by given ID.
     *
     * @param fileId ID of the file to delete
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @event type: storeFileDeleted
     * channel: store/&lt;storeId&gt;/files
     * payload: [StoreFileDeletedEventData]
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun deleteFile(fileId: String)

    /**
     * Gets a single file by the given file ID.
     *
     * @param fileId ID of the file to get
     * @return Information about the file
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun getFile(fileId: String): File

    /**
     * Gets a list of files in given Store.
     *
     * @param storeId   ID of the Store to get files from
     * @param skip      skip number of elements to skip from result
     * @param limit     limit of elements to return for query
     * @param sortOrder order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId    ID of the element from which query results should start
     * @return list of files
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun listFiles(
        storeId: String,
        skip: Long,
        limit: Long,
        sortOrder: String = "desc",
        lastId: String? = null
    ): PagingList<File>

    /**
     * Opens a file to read.
     *
     * @param fileId ID of the file to read
     * @return Handle to read file data
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun openFile(fileId: String): Long?

    /**
     * Reads file data.
     *
     * @param fileHandle handle to read file data
     * @param length     size of data to read
     * @return File data chunk
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun readFromFile(fileHandle: Long, length: Long): ByteArray

    /**
     * Moves read cursor.
     *
     * @param fileHandle handle to read/write file data
     * @param position   new cursor position
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun seekInFile(fileHandle: Long, position: Long)

    /**
     * Closes the file handle.
     *
     * @param fileHandle handle to read/write file data
     * @return ID of closed file
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     * @event type: storeStatsChanged
     * channel: store
     * payload: [StoreStatsChangedEventData]
     * @event type: storeFileCreated
     * channel: store/&lt;storeId&gt;/files
     * payload: [File]
     * @event type: storeFileUpdated
     * channel: store/&lt;storeId&gt;/files
     * payload: [File]
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun closeFile(fileHandle: Long): String

    /**
     * Subscribes for the Store module main events.
     *
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun subscribeForStoreEvents()

    /**
     * Unsubscribes from the Store module main events.
     *
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun unsubscribeFromStoreEvents()

    /**
     * Subscribes for events in given Store.
     *
     * @param storeId ID of the Store to subscribe
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun subscribeForFileEvents(storeId: String)

    /**
     * Unsubscribes from events in given Store.
     *
     * @param storeId ID of the `Store` to unsubscribe
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun unsubscribeFromFileEvents(storeId: String)

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed.
     */
    override fun close()
}