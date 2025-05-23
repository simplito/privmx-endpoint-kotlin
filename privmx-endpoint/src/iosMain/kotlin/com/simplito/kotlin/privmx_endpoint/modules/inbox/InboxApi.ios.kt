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

import cnames.structs.pson_value
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
import com.simplito.kotlin.privmx_endpoint.utils.KPSON_NULL
import com.simplito.kotlin.privmx_endpoint.utils.PsonValue
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import com.simplito.kotlin.privmx_endpoint.utils.makeArgs
import com.simplito.kotlin.privmx_endpoint.utils.mapOfWithNulls
import com.simplito.kotlin.privmx_endpoint.utils.pson
import com.simplito.kotlin.privmx_endpoint.utils.toInbox
import com.simplito.kotlin.privmx_endpoint.utils.toInboxEntry
import com.simplito.kotlin.privmx_endpoint.utils.toInboxPublicView
import com.simplito.kotlin.privmx_endpoint.utils.toPagingList
import com.simplito.kotlin.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execInboxApi
import libprivmxendpoint.privmx_endpoint_freeInboxApi
import libprivmxendpoint.privmx_endpoint_freeStoreApi
import libprivmxendpoint.privmx_endpoint_freeThreadApi
import libprivmxendpoint.privmx_endpoint_newInboxApi
import libprivmxendpoint.privmx_endpoint_newStoreApi
import libprivmxendpoint.privmx_endpoint_newThreadApi
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_free_value
import libprivmxendpoint.pson_new_array

/**
 * Manages PrivMX Bridge Inboxes and Entries.
 * @param connection active connection to PrivMX Bridge
 * @param threadApi  instance of [ThreadApi] created on passed Connection
 * @param storeApi   instance of [StoreApi] created on passed Connection
 * @throws IllegalStateException when one of the passed parameters is closed
 */
@OptIn(ExperimentalForeignApi::class)
actual class InboxApi
@Throws(IllegalStateException::class)
actual constructor(
    connection: Connection, threadApi: ThreadApi?, storeApi: StoreApi?
) : AutoCloseable {
    private val _nativeInboxApi = nativeHeap.allocPointerTo<cnames.structs.InboxApi>()
    private val nativeInboxApi
        get() = _nativeInboxApi.value?.let { _nativeInboxApi }
            ?: throw IllegalStateException("InboxApi has been closed.")

    init {
        val tmpThreadApi = if (threadApi == null) {
            memScoped {
                allocPointerTo<cnames.structs.ThreadApi>().apply {
                    privmx_endpoint_newThreadApi(
                        connection.getConnectionPtr(),
                        ptr,
                    )
                }
            }
        } else null

        val tmpStoreApi = if (storeApi == null) {
            memScoped {
                allocPointerTo<cnames.structs.StoreApi>().apply {
                    privmx_endpoint_newStoreApi(
                        connection.getConnectionPtr(),
                        ptr,
                    )
                }
            }
        } else null

        privmx_endpoint_newInboxApi(
            connection.getConnectionPtr(),
            threadApi?.getThreadPtr(),
            storeApi?.getStorePtr(),
            _nativeInboxApi.ptr
        )

        memScoped {
            val args = pson_new_array()
            val pson_result = allocPointerTo<pson_value>()
            try {
                privmx_endpoint_execInboxApi(nativeInboxApi.value, 0, args, pson_result.ptr)
                pson_result.value!!.asResponse?.getResultOrThrow()
            } finally {
                pson_free_value(args)
                pson_free_result(pson_result.value)
                tmpThreadApi?.let { privmx_endpoint_freeThreadApi(it.value) }
                tmpStoreApi?.let { privmx_endpoint_freeStoreApi(it.value) }
            }
        }
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
    actual fun createInbox(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        filesConfig: FilesConfig?,
        policies: ContainerPolicyWithoutItem?
    ): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId.pson,
            users.map { it.pson }.pson,
            managers.map { it.pson }.pson,
            publicMeta.pson,
            privateMeta.pson,
            filesConfig?.pson ?: KPSON_NULL,
            policies?.pson ?: KPSON_NULL
        )
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 1, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    actual fun updateInbox(
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
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            inboxId.pson,
            inboxId.pson,
            users.map { it.pson }.pson,
            managers.map { it.pson }.pson,
            publicMeta.pson,
            privateMeta.pson,
            filesConfig?.pson ?: KPSON_NULL,
            version.pson,
            force.pson,
            forceGenerateNewKey.pson,
            policies?.pson ?: KPSON_NULL,
        )
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 2, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun getInbox(inboxId: String): Inbox = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(inboxId.pson)
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 3, args, pson_result.ptr)
            val result = pson_result.value?.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toInbox()
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

    /**
     * Gets a list of Inboxes in given Context.
     *
     * @param contextId ID of the Context to get Inboxes from
     * @param skip      skip number of elements to skip from result
     * @param limit     limit of elements to return for query
     * @param sortOrder order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId    ID of the element from which query results should start
     * @return list of Inboxes
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun listInboxes(
        contextId: String, skip: Long, limit: Long, sortOrder: String, lastId: String?, queryAsJson: String?
    ): PagingList<Inbox> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId.pson, mapOfWithNulls(
                "skip" to skip.pson,
                "limit" to limit.pson,
                "sortOrder" to sortOrder.pson,
                lastId?.let { "lastId" to lastId.pson },
                queryAsJson?.let { "queryAsJson" to queryAsJson.pson }
            ).pson
        )
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 4, args, pson_result.ptr)
            val pagingList =
                pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            pagingList.toPagingList(PsonValue.PsonObject::toInbox)
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    actual fun getInboxPublicView(inboxId: String): InboxPublicView = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(inboxId.pson)
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 5, args, pson_result.ptr)
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toInboxPublicView()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun deleteInbox(inboxId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(inboxId.pson)
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 6, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Prepares a request to send data to an Inbox.
     * You do not have to be logged in to call this function.
     *
     * @param inboxId          ID of the Inbox to which the request applies
     * @param data             entry data to send
     * @param inboxFileHandles optional list of file handles that will be sent with the request
     * @return Inbox handle
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun prepareEntry(
        inboxId: String, data: ByteArray, inboxFileHandles: List<Long>, userPrivKey: String?
    ): Long? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            inboxId.pson,
            data.pson,
            inboxFileHandles.map { it.pson }.pson,
            userPrivKey?.pson,
        )
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 7, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()?.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun sendEntry(inboxHandle: Long) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(inboxHandle.pson)
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 8, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun readEntry(inboxEntryId: String): InboxEntry = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(inboxEntryId.pson)
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 9, args, pson_result.ptr)
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toInboxEntry()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun listEntries(
        inboxId: String, skip: Long, limit: Long, sortOrder: String, lastId: String?, queryAsJson: String?
    ): PagingList<InboxEntry> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            inboxId.pson, mapOfWithNulls(
                "skip" to skip.pson,
                "limit" to limit.pson,
                "sortOrder" to sortOrder.pson,
                lastId?.let { "lastId" to lastId.pson },
                queryAsJson?.let { "queryAsJson" to queryAsJson.pson }
            ).pson
        )
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 10, args, pson_result.ptr)
            val pagingList =
                pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            pagingList.toPagingList(PsonValue.PsonObject::toInboxEntry)
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun deleteEntry(inboxEntryId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(inboxEntryId.pson)
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 11, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

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
    actual fun createFileHandle(
        publicMeta: ByteArray, privateMeta: ByteArray, fileSize: Long
    ): Long? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            publicMeta.pson, privateMeta.pson, fileSize.pson
        )
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 12, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun writeToFile(
        inboxHandle: Long, inboxFileHandle: Long, dataChunk: ByteArray
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            inboxHandle.pson, inboxFileHandle.pson, dataChunk.pson
        )
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 13, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun openFile(fileId: String): Long? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileId.pson
        )
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 14, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun readFromFile(fileHandle: Long, length: Long): ByteArray = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileHandle.pson, length.pson
        )
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 15, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun seekInFile(fileHandle: Long, position: Long) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileHandle.pson, position.pson
        )
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 16, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun closeFile(fileHandle: Long): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileHandle.pson
        )
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 17, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun subscribeForInboxEvents() = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 18, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun unsubscribeFromInboxEvents() = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 19, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun subscribeForEntryEvents(inboxId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            inboxId.pson
        )
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 20, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun unsubscribeFromEntryEvents(inboxId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(inboxId.pson)
        try {
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 21, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed
     */
    actual override fun close() {
        if (_nativeInboxApi.value == null) return
        privmx_endpoint_freeInboxApi(_nativeInboxApi.value)
        _nativeInboxApi.value = null
    }
}