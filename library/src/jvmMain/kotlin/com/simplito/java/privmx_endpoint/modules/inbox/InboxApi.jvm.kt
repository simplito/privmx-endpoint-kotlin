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
import java.util.Optional

actual class InboxApi actual constructor(
    connection: Connection, threadApi: ThreadApi?, storeApi: StoreApi?
) : AutoCloseable {
    private var api: Long? = null

    init {
        val tmpThreadApi = if(threadApi == null) ThreadApi(connection) else null
        val tmpStoreApi = if(storeApi == null) StoreApi(connection) else null

        api = init(
            connection,
            threadApi ?: tmpThreadApi!!,
            storeApi ?: tmpStoreApi!!
        )

        tmpThreadApi?.close()
        tmpStoreApi?.close()
    }

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun createInbox(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        filesConfig: FilesConfig?,
        policies: ContainerPolicyWithoutItem?
    ): String

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
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

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun getInbox(inboxId: String): Inbox

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun listInboxes(
        contextId: String, skip: Long, limit: Long, sortOrder: String, lastId: String?
    ): PagingList<Inbox>

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun getInboxPublicView(inboxId: String): InboxPublicView

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun deleteInbox(inboxId: String)

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun prepareEntry(
        inboxId: String, data: ByteArray, inboxFileHandles: List<Long>, userPrivKey: String?
    ): Long

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun sendEntry(inboxHandle: Long)

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun readEntry(inboxEntryId: String): InboxEntry

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun listEntries(
        inboxId: String, skip: Long, limit: Long, sortOrder: String, lastId: String?
    ): PagingList<InboxEntry>

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun deleteEntry(inboxEntryId: String)

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun /*inboxFileHandle*/createFileHandle(
        publicMeta: ByteArray, privateMeta: ByteArray, fileSize: Long
    ): Long

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun writeToFile(
        inboxHandle: Long, inboxFileHandle: Long, dataChunk: ByteArray
    )

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun openFile(fileId: String): Long

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun readFromFile(fileHandle: Long, length: Long): ByteArray

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun seekInFile(fileHandle: Long, position: Long)

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun closeFile(fileHandle: Long): String

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun subscribeForInboxEvents()

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun unsubscribeFromInboxEvents()

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun subscribeForEntryEvents(inboxId: String)

    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    actual external fun unsubscribeFromEntryEvents(inboxId: String)

    @Throws(IllegalStateException::class)
    private external fun init(connection: Connection?, orElse: ThreadApi, orElse1: StoreApi): Long?

    @Throws(IllegalStateException::class)
    private external fun deinit()

    actual override fun close() {
        deinit()
    }
}
