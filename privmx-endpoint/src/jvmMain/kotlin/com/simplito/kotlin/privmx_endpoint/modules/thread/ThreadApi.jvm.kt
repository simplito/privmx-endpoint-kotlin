package com.simplito.kotlin.privmx_endpoint.modules.thread

import com.simplito.kotlin.privmx_endpoint.LibLoader
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.Message
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.Thread
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import java.lang.AutoCloseable

actual class ThreadApi actual constructor(connection: Connection): AutoCloseable {
    companion object {
        init {
            LibLoader.load()
        }
    }
    private var api: Long? = null

    init {
        api = init(connection)
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual external fun createThread(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy?
    ): String
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
        policies: ContainerPolicy?
    )
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun getThread(threadId: String): Thread

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual external fun listThreads(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?
    ): PagingList<Thread>
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun deleteThread(threadId: String)
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun sendMessage(
        threadId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        data: ByteArray
    ): String
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun getMessage(messageId: String): Message

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual external fun listMessages(
        threadId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?
    ): PagingList<Message>

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun deleteMessage(messageId: String)

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun updateMessage(
        messageId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        data: ByteArray
    )
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun subscribeForThreadEvents()
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun unsubscribeFromThreadEvents()
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun subscribeForMessageEvents(threadId: String)
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun unsubscribeFromMessageEvents(threadId: String)

    @Throws(IllegalStateException::class)
    private external fun init(connection: Connection): Long?

    @Throws(IllegalStateException::class)
    private external fun deinit()

    actual override fun close(){
        deinit()
    }
}