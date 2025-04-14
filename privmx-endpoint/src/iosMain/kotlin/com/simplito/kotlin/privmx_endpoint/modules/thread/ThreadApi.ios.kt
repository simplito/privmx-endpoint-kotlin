package com.simplito.kotlin.privmx_endpoint.modules.thread

import cnames.structs.pson_value
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.Message
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.Thread
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.utils.KPSON_NULL
import com.simplito.kotlin.privmx_endpoint.utils.PsonValue
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import com.simplito.kotlin.privmx_endpoint.utils.makeArgs
import com.simplito.kotlin.privmx_endpoint.utils.mapOfWithNulls
import com.simplito.kotlin.privmx_endpoint.utils.pson
import com.simplito.kotlin.privmx_endpoint.utils.toMessage
import com.simplito.kotlin.privmx_endpoint.utils.toPagingList
import com.simplito.kotlin.privmx_endpoint.utils.toThread
import com.simplito.kotlin.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execThreadApi
import libprivmxendpoint.privmx_endpoint_freeThreadApi
import libprivmxendpoint.privmx_endpoint_newThreadApi
import libprivmxendpoint.pson_new_array
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_free_value

/**
 * Manages Threads and messages.
 * @param connection active connection to PrivMX Bridge
 * @throws IllegalStateException when given [Connection] is not connected
 * @category thread
 */
@OptIn(ExperimentalForeignApi::class)
actual class ThreadApi
@Throws(IllegalStateException::class)
actual constructor(connection: Connection) : AutoCloseable {
    private val _nativeThreadApi = nativeHeap.allocPointerTo<cnames.structs.ThreadApi>()
    private val nativeThreadApi
        get() = _nativeThreadApi.value?.let { _nativeThreadApi }
            ?: throw IllegalStateException("ThreadApi has been closed.")

    internal fun getThreadPtr() = nativeThreadApi.value

    init {
        privmx_endpoint_newThreadApi(connection.getConnectionPtr(), _nativeThreadApi.ptr)
        memScoped {
            val args = pson_new_array()
            val pson_result = allocPointerTo<pson_value>()
            try{
                privmx_endpoint_execThreadApi(nativeThreadApi.value, 0, args, pson_result.ptr)
                pson_result.value!!.asResponse?.getResultOrThrow()
            } finally {
                pson_free_value(args)
                pson_free_result(pson_result.value)
            }
        }
    }

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
    actual fun createThread(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy?
    ): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId.pson,
            users.map { it.pson }.pson,
            managers.map { it.pson }.pson,
            publicMeta.pson,
            privateMeta.pson,
            policies?.pson ?: KPSON_NULL
        )
        try {
            privmx_endpoint_execThreadApi(nativeThreadApi.value, 1, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun updateThread(
        threadId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        version: Long,
        force: Boolean,
        forceGenerateNewKey: Boolean,
        policies: ContainerPolicy?
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            threadId.pson,
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
            privmx_endpoint_execThreadApi(nativeThreadApi.value, 2, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun getThread(threadId: String): Thread = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(threadId.pson)
        try {
            privmx_endpoint_execThreadApi(nativeThreadApi.value, 4, args, pson_result.ptr)
            val result = pson_result.value?.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toThread()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun listThreads(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?
    ): PagingList<Thread> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId.pson,
            mapOfWithNulls(
                "skip" to skip.pson,
                "limit" to limit.pson,
                "sortOrder" to sortOrder.pson,
                lastId?.let { "lastId" to lastId.pson }
            ).pson
        )
        try {
            privmx_endpoint_execThreadApi(nativeThreadApi.value, 5, args, pson_result.ptr)
            val pagingList =
                pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            pagingList.toPagingList(PsonValue.PsonObject::toThread)
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun deleteThread(threadId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(threadId.pson)
        try {
            privmx_endpoint_execThreadApi(nativeThreadApi.value, 3, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun sendMessage(
        threadId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        data: ByteArray
    ): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            threadId.pson,
            publicMeta.pson,
            privateMeta.pson,
            data.pson,
        )
        try {
            privmx_endpoint_execThreadApi(nativeThreadApi.value, 8, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun getMessage(messageId: String): Message = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(PsonValue.PsonString(messageId))
        try {
            privmx_endpoint_execThreadApi(nativeThreadApi.value, 6, args, pson_result.ptr)
            val psonObject =
                pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            psonObject.toMessage()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun listMessages(
        threadId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?
    ): PagingList<Message> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            threadId.pson,
            mapOfWithNulls(
                "skip" to skip.pson,
                "limit" to limit.pson,
                "sortOrder" to sortOrder.pson,
                lastId?.let { "lastId" to lastId.pson }
            ).pson
        )
        try {
            privmx_endpoint_execThreadApi(nativeThreadApi.value, 7, args, pson_result.ptr)
            val pagingList =
                pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            pagingList.toPagingList(PsonValue.PsonObject::toMessage)
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun deleteMessage(messageId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(PsonValue.PsonString(messageId))
        try {
            privmx_endpoint_execThreadApi(nativeThreadApi.value, 9, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

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
    actual fun updateMessage(
        messageId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        data: ByteArray
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            PsonValue.PsonString(messageId),
            PsonValue.PsonBinary(publicMeta),
            PsonValue.PsonBinary(privateMeta),
            PsonValue.PsonBinary(data)
        )
        try {
            privmx_endpoint_execThreadApi(nativeThreadApi.value, 10, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Subscribes for the Thread module main events.
     *
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun subscribeForThreadEvents() = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execThreadApi(nativeThreadApi.value, 11, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Unsubscribes from the Thread module main events.
     *
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun unsubscribeFromThreadEvents() = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execThreadApi(nativeThreadApi.value, 12, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Subscribes for events in given Thread.
     *
     * @param threadId ID of the Thread to subscribe
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun subscribeForMessageEvents(threadId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            PsonValue.PsonString(threadId)
        )
        try {
            privmx_endpoint_execThreadApi(nativeThreadApi.value, 13, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Unsubscribes from events in given Thread.
     *
     * @param threadId ID of the Thread to unsubscribe
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun unsubscribeFromMessageEvents(threadId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(PsonValue.PsonString(threadId))
        try {
            privmx_endpoint_execThreadApi(nativeThreadApi.value, 14, args, pson_result.ptr)
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
     * @throws Exception when instance is currently closed.
     */
    actual override fun close() {
        if (_nativeThreadApi.value == null) return
        privmx_endpoint_freeThreadApi(_nativeThreadApi.value)
        _nativeThreadApi.value = null
    }
}

