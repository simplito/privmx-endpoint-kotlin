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

package com.simplito.kotlin.privmx_endpoint.modules.search

import com.simplito.kotlin.privmx_endpoint.LibLoader
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.Document
import com.simplito.kotlin.privmx_endpoint.model.IndexMode
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.SearchIndex
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.kvdb.KvdbApi
import com.simplito.kotlin.privmx_endpoint.modules.lock.LockApi
import com.simplito.kotlin.privmx_endpoint.modules.store.StoreApi
import kotlin.IllegalStateException
import kotlin.Throws

/**
 * Manages PrivMX Bridge Search Indexes and their Documents.
 */
actual class SearchApi
@Throws(IllegalStateException::class)
actual constructor(
    connection: Connection,
    storeApi: StoreApi,
    kvdbApi: KvdbApi,
    lockApi: LockApi
) : AutoCloseable {
    companion object {
        init {
            LibLoader.loadPrivmxLibraries()
        }
    }

    /**
     * Creates an instance of `SearchApi`.
     *
     * @param connection instance of 'Connection'
     * @param storeApi   instance of 'StoreApi', holds the Search Index's documents
     * @param kvdbApi    instance of 'KvdbApi', holds the Search Index's metadata
     * @param lockApi    instance of 'LockApi', serializes concurrent writes to the Search Index
     * @throws IllegalStateException when one of the passed parameters is closed
     */
    init {
        // TODO(Not implemented yet)
    }

    /**
     * Creates a new Search Index in a given Context.
     *
     * @param contextId   ID of the Context to create the Search Index in
     * @param users       list of [UserWithPubKey] which indicates who will have access to the created Search Index
     * @param managers    list of [UserWithPubKey] which indicates who will have access (and management rights) to the created Search Index
     * @param publicMeta  public (unencrypted) metadata
     * @param privateMeta private (encrypted) metadata
     * @param mode        the operational mode of the Search Index
     * @param policies    Search Index's policies
     * @return ID of the created Search Index
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual fun createSearchIndex(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        mode: IndexMode,
        policies: ContainerPolicy?
    ): String = TODO("Not implemented yet")

    /**
     * Updates an existing Search Index.
     *
     * @param indexId             ID of the Search Index to update
     * @param users               list of [UserWithPubKey] which indicates who will have access to the Search Index
     * @param managers            list of [UserWithPubKey] which indicates who will have access (and management rights) to the Search Index
     * @param publicMeta          public (unencrypted) metadata
     * @param privateMeta         private (encrypted) metadata
     * @param version             current version of the updated Search Index
     * @param force               force update (without checking version)
     * @param forceGenerateNewKey force to regenerate a key for the Search Index
     * @param policies            Search Index's policies
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual fun updateSearchIndex(
        indexId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        version: Long,
        force: Boolean,
        forceGenerateNewKey: Boolean,
        policies: ContainerPolicy?
    ): Unit = TODO("Not implemented yet")

    /**
     * Deletes a Search Index by given Search Index ID.
     *
     * @param indexId ID of the Search Index to delete
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun deleteSearchIndex(indexId: String): Unit = TODO("Not implemented yet")

    /**
     * Gets a Search Index by given Search Index ID.
     *
     * @param indexId ID of the Search Index to get
     * @return object containing info about the Search Index
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getSearchIndex(indexId: String): SearchIndex = TODO("Not implemented yet")

    /**
     * Gets a list of Search Indexes in given Context.
     *
     * @param contextId   ID of the Context to get the Indexes from
     * @param skip        skip number of elements to skip from result
     * @param limit       limit of elements to return for query
     * @param sortOrder   order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId      ID of the element from which query results should start
     * @param queryAsJson stringified JSON object with a custom field to filter result
     * @param sortBy      field by elements are sorted in result
     * @return list of Search Indexes
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual fun listSearchIndexes(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?,
        sortBy: String?
    ): PagingList<SearchIndex> = TODO("Not implemented yet")

    /**
     * Opens a Search Index for use and returns a handle.
     *
     * @param indexId ID of the Search Index to open
     * @return handle to the opened Search Index
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun openSearchIndex(indexId: String): Long = TODO("Not implemented yet")

    /**
     * Closes the Search Index associated with the given handle.
     *
     * @param indexHandle handle of the Search Index to close
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun closeSearchIndex(indexHandle: Long): Unit = TODO("Not implemented yet")

    /**
     * Begins a SQLite transaction on the Search Index.
     *
     * @param indexHandle handle of the Search Index to begin the transaction on
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun beginTransaction(indexHandle: Long): Unit = TODO("Not implemented yet")

    /**
     * Commits the active transaction on the Search Index.
     *
     * @param indexHandle handle of the Search Index to commit the transaction on
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun commit(indexHandle: Long): Unit = TODO("Not implemented yet")

    /**
     * Rolls back the active transaction on the Search Index.
     *
     * @param indexHandle handle of the Search Index to roll back the transaction on
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun rollback(indexHandle: Long): Unit = TODO("Not implemented yet")

    /**
     * Adds a new document to the Search Index.
     *
     * @param indexHandle handle of the Search Index to add the document to
     * @param name        name of the document
     * @param content     content of the document
     * @return ID of the newly added document
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun addDocument(
        indexHandle: Long,
        name: String,
        content: String
    ): Long = TODO("Not implemented yet")

    /**
     * Updates an existing document in the Search Index.
     *
     * @param indexHandle handle of the Search Index containing the document
     * @param document    [Document] with data for update
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun updateDocument(
        indexHandle: Long,
        document: Document
    ): Unit = TODO("Not implemented yet")

    /**
     * Deletes a document by given document ID from the Search Index.
     *
     * @param indexHandle handle of the Search Index to delete the document from
     * @param documentId  ID of the document to delete
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun deleteDocument(
        indexHandle: Long,
        documentId: Long
    ): Unit = TODO("Not implemented yet")

    /**
     * Gets a document by given document ID from the Search Index.
     *
     * @param indexHandle handle of the Search Index containing the document
     * @param documentId  ID of the document to get
     * @return object containing the document data
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getDocument(
        indexHandle: Long,
        documentId: Long
    ): Document = TODO("Not implemented yet")

    /**
     * Gets a list of documents (e.g. messages, threads or custom documents) from a Search Index.
     *
     * @param indexHandle handle of the Search Index containing documents
     * @param skip        skip number of elements to skip from result
     * @param limit       limit of elements to return for query
     * @param sortOrder   order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId      ID of the element from which query results should start
     * @param queryAsJson stringified JSON object with a custom field to filter result
     * @param sortBy      field by elements are sorted in result
     * @return list of documents
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual fun listDocuments(
        indexHandle: Long,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?,
        sortBy: String?
    ): PagingList<Document> = TODO("Not implemented yet")

    /**
     * Searches for documents in the Search Index.
     *
     * @param indexHandle handle of the Search Index to search
     * @param searchQuery search query
     * @param skip        skip number of elements to skip from result
     * @param limit       limit of elements to return for query
     * @param sortOrder   order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId      ID of the element from which query results should start
     * @param queryAsJson stringified JSON object with a custom field to filter result
     * @param sortBy      field by elements are sorted in result
     * @return list of matching documents
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual fun searchDocuments(
        indexHandle: Long,
        searchQuery: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?,
        sortBy: String?
    ): PagingList<Document> = TODO("Not implemented yet")

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed.
     */
    actual override fun close() {
        // TODO(Not implemented yet)
    }
}
