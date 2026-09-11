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

import cnames.structs.pson_value
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
import com.simplito.kotlin.privmx_endpoint.utils.KPSON_NULL
import com.simplito.kotlin.privmx_endpoint.utils.PsonValue
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import com.simplito.kotlin.privmx_endpoint.utils.makeArgs
import com.simplito.kotlin.privmx_endpoint.utils.mapOfWithNulls
import com.simplito.kotlin.privmx_endpoint.utils.pson
import com.simplito.kotlin.privmx_endpoint.utils.toDocument
import com.simplito.kotlin.privmx_endpoint.utils.toPagingList
import com.simplito.kotlin.privmx_endpoint.utils.toSearchIndex
import com.simplito.kotlin.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execSearchApi
import libprivmxendpoint.privmx_endpoint_freeSearchApi
import libprivmxendpoint.privmx_endpoint_newSearchApi
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_free_value
import libprivmxendpoint.pson_new_array

/**
 * Manages PrivMX Bridge Search Indexes and their Documents.
 */
@OptIn(ExperimentalForeignApi::class)
actual class SearchApi
@Throws(IllegalStateException::class)
actual constructor(
    connection: Connection,
    storeApi: StoreApi,
    kvdbApi: KvdbApi,
    lockApi: LockApi
) : AutoCloseable {
    private val _nativeSearchApi = nativeHeap.allocPointerTo<cnames.structs.SearchApi>()
    private val nativeSearchApi
        get() = _nativeSearchApi.value?.let { _nativeSearchApi }
            ?: throw IllegalStateException("SearchApi has been closed.")


    init {
        privmx_endpoint_newSearchApi(
            connection.getConnectionPtr(),
            storeApi.getStorePtr(),
            kvdbApi.getKvdbPtr(),
            lockApi.getLockPtr(),
            _nativeSearchApi.ptr
        )
        memScoped {
            val args = pson_new_array()
            val pson_result = allocPointerTo<pson_value>()
            try {
                privmx_endpoint_execSearchApi(nativeSearchApi.value, 0, args, pson_result.ptr)
                pson_result.value!!.asResponse?.getResultOrThrow()
            } finally {
                pson_free_value(args)
                pson_free_result(pson_result.value)
            }
        }
    }

    private fun pagingQuery(
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?,
        sortBy: String?
    ): PsonValue.PsonObject = mapOfWithNulls(
        "skip" to skip.pson,
        "limit" to limit.pson,
        "sortOrder" to sortOrder.pson,
        lastId?.let { "lastId" to it.pson },
        queryAsJson?.let { "queryAsJson" to it.pson },
        sortBy?.let { "sortBy" to it.pson }
    ).pson

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
    actual fun createSearchIndex(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        mode: IndexMode,
        policies: ContainerPolicy?
    ): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId.pson,
            users.map { it.pson }.pson,
            managers.map { it.pson }.pson,
            publicMeta.pson,
            privateMeta.pson,
            mode.pson,
            policies?.pson ?: KPSON_NULL,
        )
        try {
            privmx_endpoint_execSearchApi(nativeSearchApi.value, 1, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()!!.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    ): Unit = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            indexId.pson,
            users.map { it.pson }.pson,
            managers.map { it.pson }.pson,
            publicMeta.pson,
            privateMeta.pson,
            version.pson,
            force.pson,
            forceGenerateNewKey.pson,
            policies?.pson ?: KPSON_NULL,
        )
        try {
            privmx_endpoint_execSearchApi(nativeSearchApi.value, 2, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Deletes a Search Index by given Search Index ID.
     *
     * @param indexId ID of the Search Index to delete
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun deleteSearchIndex(indexId: String): Unit = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(indexId.pson)
        try {
            privmx_endpoint_execSearchApi(nativeSearchApi.value, 3, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun getSearchIndex(indexId: String): SearchIndex = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(indexId.pson)
        try {
            privmx_endpoint_execSearchApi(nativeSearchApi.value, 4, args, pson_result.ptr)
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toSearchIndex()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Gets a list of Search Indexes in given Context.
     *
     * @param contextId   ID of the Context to get the Search Indexes from
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
    actual fun listSearchIndexes(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?,
        sortBy: String?
    ): PagingList<SearchIndex> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId.pson,
            pagingQuery(skip, limit, sortOrder, lastId, queryAsJson, sortBy)
        )
        try {
            privmx_endpoint_execSearchApi(nativeSearchApi.value, 5, args, pson_result.ptr)
            val pagingList =
                pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            pagingList.toPagingList(PsonValue.PsonObject::toSearchIndex)
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun openSearchIndex(indexId: String): Long = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(indexId.pson)
        try {
            privmx_endpoint_execSearchApi(nativeSearchApi.value, 6, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()!!.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Closes the Search Index associated with the given handle.
     *
     * @param indexHandle handle of the Search Index to close
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun closeSearchIndex(indexHandle: Long): Unit = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(indexHandle.pson)
        try {
            privmx_endpoint_execSearchApi(nativeSearchApi.value, 7, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Begins a SQLite transaction on the Search Index.
     *
     * @param indexHandle handle of the Search Index to begin the transaction on
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun beginTransaction(indexHandle: Long): Unit = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(indexHandle.pson)
        try {
            privmx_endpoint_execSearchApi(nativeSearchApi.value, 14, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Commits the active transaction on the Search Index.
     *
     * @param indexHandle handle of the Search Index to commit the transaction on
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun commit(indexHandle: Long): Unit = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(indexHandle.pson)
        try {
            privmx_endpoint_execSearchApi(nativeSearchApi.value, 15, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Rolls back the active transaction on the Search Index.
     *
     * @param indexHandle handle of the Search Index to roll back the transaction on
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun rollback(indexHandle: Long): Unit = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(indexHandle.pson)
        try {
            privmx_endpoint_execSearchApi(nativeSearchApi.value, 16, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    ): Long = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            indexHandle.pson,
            name.pson,
            content.pson
        )
        try {
            privmx_endpoint_execSearchApi(nativeSearchApi.value, 8, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()!!.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    ): Unit = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            indexHandle.pson,
            document.pson
        )
        try {
            privmx_endpoint_execSearchApi(nativeSearchApi.value, 9, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    ): Unit = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            indexHandle.pson,
            documentId.pson
        )
        try {
            privmx_endpoint_execSearchApi(nativeSearchApi.value, 10, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    ): Document = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            indexHandle.pson,
            documentId.pson
        )
        try {
            privmx_endpoint_execSearchApi(nativeSearchApi.value, 11, args, pson_result.ptr)
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toDocument()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun listDocuments(
        indexHandle: Long,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?,
        sortBy: String?
    ): PagingList<Document> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            indexHandle.pson,
            pagingQuery(skip, limit, sortOrder, lastId, queryAsJson, sortBy)
        )
        try {
            privmx_endpoint_execSearchApi(nativeSearchApi.value, 12, args, pson_result.ptr)
            val pagingList =
                pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            pagingList.toPagingList(PsonValue.PsonObject::toDocument)
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun searchDocuments(
        indexHandle: Long,
        searchQuery: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?,
        sortBy: String?
    ): PagingList<Document> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            indexHandle.pson,
            searchQuery.pson,
            pagingQuery(skip, limit, sortOrder, lastId, queryAsJson, sortBy)
        )
        try {
            privmx_endpoint_execSearchApi(nativeSearchApi.value, 13, args, pson_result.ptr)
            val pagingList =
                pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            pagingList.toPagingList(PsonValue.PsonObject::toDocument)
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed.
     */
    actual override fun close() {
        privmx_endpoint_freeSearchApi(nativeSearchApi.value)
        _nativeSearchApi.value = null
    }
}
