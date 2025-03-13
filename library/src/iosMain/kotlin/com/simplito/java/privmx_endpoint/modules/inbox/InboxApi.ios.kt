package com.simplito.java.privmx_endpoint.modules.inbox

import com.simplito.java.privmx_endpoint.model.ContainerPolicyWithoutItem
import com.simplito.java.privmx_endpoint.model.FilesConfig
import com.simplito.java.privmx_endpoint.model.Inbox
import com.simplito.java.privmx_endpoint.model.InboxEntry
import com.simplito.java.privmx_endpoint.model.InboxPublicView
import com.simplito.java.privmx_endpoint.model.PagingList
import com.simplito.java.privmx_endpoint.model.UserWithPubKey
import com.simplito.java.privmx_endpoint.model.exceptions.NativeException
import com.simplito.java.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.java.privmx_endpoint.modules.core.Connection
import com.simplito.java.privmx_endpoint.modules.store.StoreApi
import com.simplito.java.privmx_endpoint.modules.thread.ThreadApi
import com.simplito.java.privmx_endpoint.utils.KPSON_NULL
import com.simplito.java.privmx_endpoint.utils.PsonValue
import com.simplito.java.privmx_endpoint.utils.asResponse
import com.simplito.java.privmx_endpoint.utils.makeArgs
import com.simplito.java.privmx_endpoint.utils.mapOfWithNulls
import com.simplito.java.privmx_endpoint.utils.pson
import com.simplito.java.privmx_endpoint.utils.toInbox
import com.simplito.java.privmx_endpoint.utils.toInboxEntry
import com.simplito.java.privmx_endpoint.utils.toInboxPublicView
import com.simplito.java.privmx_endpoint.utils.toPagingList
import com.simplito.java.privmx_endpoint.utils.typedValue
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
import libprivmxendpoint.pson_new_array
import cnames.structs.pson_value

@OptIn(ExperimentalForeignApi::class)
actual class InboxApi actual constructor(
    connection: Connection, threadApi: ThreadApi?, storeApi: StoreApi?
) : AutoCloseable {
    private val nativeInboxApi = nativeHeap.allocPointerTo<cnames.structs.InboxApi>()

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
            nativeInboxApi.ptr
        )
        memScoped {
            val args = pson_new_array()
            val pson_result = allocPointerTo<pson_value>()
            privmx_endpoint_execInboxApi(nativeInboxApi.value, 0, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
        }

        tmpThreadApi?.let { privmx_endpoint_freeThreadApi(it.value) }
        tmpStoreApi?.let { privmx_endpoint_freeStoreApi(it.value) }
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun createInbox(
        contextId: String?,
        users: List<UserWithPubKey?>?,
        managers: List<UserWithPubKey?>?,
        publicMeta: ByteArray?,
        privateMeta: ByteArray?,
        filesConfig: FilesConfig?,
        policies: ContainerPolicyWithoutItem?
    ): String? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId?.pson,
            users?.map { it!!.pson }?.pson,
            managers?.map { it!!.pson }?.pson,
            publicMeta?.pson,
            privateMeta?.pson,
            filesConfig?.pson ?: KPSON_NULL,
            policies?.pson ?: KPSON_NULL
        )
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 1, args, pson_result.ptr)
        pson_result.value?.asResponse?.getResultOrThrow()?.typedValue<String>()
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun updateInbox(
        inboxId: String?,
        users: List<UserWithPubKey?>?,
        managers: List<UserWithPubKey?>?,
        publicMeta: ByteArray?,
        privateMeta: ByteArray?,
        filesConfig: FilesConfig?,
        version: Long,
        force: Boolean,
        forceGenerateNewKey: Boolean,
        policies: ContainerPolicyWithoutItem?
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            inboxId?.pson,
            inboxId?.pson,
            users!!.map { it!!.pson }.pson,
            managers!!.map { it!!.pson }.pson,
            publicMeta!!.pson,
            privateMeta!!.pson,
            version.pson,
            force.pson,
            forceGenerateNewKey.pson,
            policies?.pson ?: KPSON_NULL,
        )
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 2, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()
        Unit
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun getInbox(inboxId: String?): Inbox? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(inboxId?.pson)
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 3, args, pson_result.ptr)
        val result = pson_result.value?.asResponse?.getResultOrThrow() as PsonValue.PsonObject
        result.toInbox()
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun listInboxes(
        contextId: String?, skip: Long, limit: Long, sortOrder: String?, lastId: String?
    ): PagingList<Inbox?>? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId!!.pson, mapOfWithNulls(
                "skip" to skip.pson,
                "limit" to limit.pson,
                "sortOrder" to sortOrder!!.pson,
                lastId?.let { "lastId" to lastId.pson }).pson
        )
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 4, args, pson_result.ptr)
        val pagingList = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
        pagingList.toPagingList(PsonValue.PsonObject::toInbox)
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun getInboxPublicView(inboxId: String?): InboxPublicView? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(inboxId!!.pson)
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 5, args, pson_result.ptr)
        val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
        result.toInboxPublicView()
    }


    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun deleteInbox(inboxId: String?) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(inboxId!!.pson)
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 6, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()
        Unit
    }


    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun prepareEntry(
        inboxId: String?, data: ByteArray?, inboxFileHandles: List<Long?>?, userPrivKey: String?
    ): Long? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            inboxId?.pson,
            data?.pson,
            inboxFileHandles?.map { it!!.pson }?.pson,
            userPrivKey?.pson,
        )
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 7, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()?.typedValue()
    }


    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun sendEntry(inboxHandle: Long) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(inboxHandle.pson)
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 8, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()
        Unit
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun readEntry(inboxEntryId: String?): InboxEntry? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(inboxEntryId!!.pson)
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 9, args, pson_result.ptr)
        val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
        result.toInboxEntry()
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun listEntries(
        inboxId: String?, skip: Long, limit: Long, sortOrder: String?, lastId: String?
    ): PagingList<InboxEntry?>? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            inboxId!!.pson, mapOfWithNulls(
                "skip" to skip.pson,
                "limit" to limit.pson,
                "sortOrder" to sortOrder!!.pson,
                lastId?.let { "lastId" to lastId.pson }).pson
        )
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 10, args, pson_result.ptr)
        val pagingList = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
        pagingList.toPagingList(PsonValue.PsonObject::toInboxEntry)
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun deleteEntry(inboxEntryId: String?) {
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun createFileHandle(
        publicMeta: ByteArray?, privateMeta: ByteArray?, fileSize: Long
    ): Long? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            publicMeta?.pson, privateMeta?.pson, fileSize.pson
        )
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 12, args, pson_result.ptr)
        pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun writeToFile(
        inboxHandle: Long, inboxFileHandle: Long, dataChunk: ByteArray?
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            inboxHandle.pson, inboxFileHandle.pson, dataChunk?.pson
        )
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 13, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()
        Unit
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun openFile(fileId: String?): Long? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileId?.pson
        )
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 14, args, pson_result.ptr)
        pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun readFromFile(fileHandle: Long, length: Long): ByteArray? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileHandle.pson, length.pson
        )
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 15, args, pson_result.ptr)
        pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun seekInFile(fileHandle: Long, position: Long) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileHandle.pson, position.pson
        )
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 16, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()
        Unit
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun closeFile(fileHandle: Long): String? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileHandle.pson
        )
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 17, args, pson_result.ptr)
        pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun subscribeForInboxEvents() = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 18, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()
        Unit
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun unsubscribeFromInboxEvents() = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 19, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()
        Unit
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun subscribeForEntryEvents(inboxId: String?) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            inboxId?.pson
        )
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 20, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()
        Unit
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual fun unsubscribeFromEntryEvents(inboxId: String?) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            inboxId?.pson
        )
        privmx_endpoint_execInboxApi(nativeInboxApi.value, 21, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()
        Unit
    }

    actual override fun close() {
        if (nativeInboxApi.value == null) return
        privmx_endpoint_freeInboxApi(nativeInboxApi.value)
        nativeHeap.free(nativeInboxApi.rawPtr)
        nativeInboxApi.value = null
    }
}