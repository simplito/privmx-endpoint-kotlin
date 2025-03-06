package com.simplito.java.privmx_endpoint.modules.thread

import com.simplito.java.privmx_endpoint.model.ContainerPolicy
import com.simplito.java.privmx_endpoint.model.Message
import com.simplito.java.privmx_endpoint.model.PagingList
import com.simplito.java.privmx_endpoint.model.Thread
import com.simplito.java.privmx_endpoint.model.UserWithPubKey
import com.simplito.java.privmx_endpoint.model.exceptions.NativeException
import com.simplito.java.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.java.privmx_endpoint.modules.core.Connection
import com.simplito.java.privmx_endpoint.utils.KPSON_NULL
import com.simplito.java.privmx_endpoint.utils.PsonValue
import com.simplito.java.privmx_endpoint.utils.asResponse
import com.simplito.java.privmx_endpoint.utils.makeArgs
import com.simplito.java.privmx_endpoint.utils.pson
import com.simplito.java.privmx_endpoint.utils.toMessage
import com.simplito.java.privmx_endpoint.utils.toPagingList
import com.simplito.java.privmx_endpoint.utils.toThread
import com.simplito.java.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.*


@OptIn(ExperimentalForeignApi::class)
actual class ThreadApi actual constructor(connection: Connection) : AutoCloseable {
    private val nativeThreadApi = nativeHeap.allocPointerTo<libprivmxendpoint.ThreadApi>()

    internal fun getThreadPtr() = nativeThreadApi.value

    init {
        privmx_endpoint_newThreadApi(connection.getConnectionPtr(), nativeThreadApi.ptr)
        memScoped {
            val args = pson_new_array()
            val pson_result = allocPointerTo<pson_value>()
            privmx_endpoint_execThreadApi(nativeThreadApi.value, 0, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
        }
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun createThread(
        contextId: String?,
        users: List<UserWithPubKey?>?,
        managers: List<UserWithPubKey?>?,
        publicMeta: ByteArray?,
        privateMeta: ByteArray?,
        policies: ContainerPolicy?
    ): String? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId?.pson,
            users?.map { it!!.pson }?.pson,
            managers?.map { it!!.pson }?.pson,
            publicMeta?.pson,
            privateMeta?.pson,
            policies?.pson ?: KPSON_NULL
        )
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 1, args, pson_result.ptr)
        pson_result.value?.asResponse?.getResultOrThrow()?.typedValue<String>()
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun updateThread(
        threadId: String?,
        users: List<UserWithPubKey?>?,
        managers: List<UserWithPubKey?>?,
        publicMeta: ByteArray?,
        privateMeta: ByteArray?,
        version: Long,
        force: Boolean,
        forceGenerateNewKey: Boolean,
        policies: ContainerPolicy?
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            threadId?.pson,
            threadId?.pson,
            users!!.map { it!!.pson }.pson,
            managers!!.map { it!!.pson }.pson,
            publicMeta!!.pson,
            privateMeta!!.pson,
            version.pson,
            force.pson,
            forceGenerateNewKey.pson,
            policies?.pson ?: KPSON_NULL,
        )
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 2, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()
        Unit
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getThread(threadId: String?): Thread? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(threadId?.pson)
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 4, args, pson_result.ptr)
        val result = pson_result.value?.asResponse?.getResultOrThrow() as PsonValue.PsonObject
        result.toThread()
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun listThreads(
        contextId: String?,
        skip: Long,
        limit: Long,
        sortOrder: String?,
        lastId: String?
    ): PagingList<Thread?>? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId!!.pson,
            mapOfWithNulls(
                "skip" to skip.pson,
                "limit" to limit.pson,
                "sortOrder" to sortOrder!!.pson,
                lastId?.let { "lastId" to lastId.pson }
            ).pson
        )
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 5, args, pson_result.ptr)
        val pagingList = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
        pagingList.toPagingList(PsonValue.PsonObject::toThread)
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun deleteThread(threadId: String?) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(threadId!!.pson)
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 3, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()
        Unit
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun sendMessage(
        threadId: String?,
        publicMeta: ByteArray?,
        privateMeta: ByteArray?,
        data: ByteArray?
    ): String? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            threadId?.pson,
            publicMeta?.pson,
            privateMeta?.pson,
            data?.pson,
        )
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 8, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()?.typedValue<String>()
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getMessage(messageId: String?): Message? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(PsonValue.PsonString(messageId!!))
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 6, args, pson_result.ptr)
        val psonObject = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
        psonObject.toMessage()
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun listMessages(
        threadId: String?,
        skip: Long,
        limit: Long,
        sortOrder: String?,
        lastId: String?
    ): PagingList<Message?>? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            threadId!!.pson,
            mapOfWithNulls(
                "skip" to skip.pson,
                "limit" to limit.pson,
                "sortOrder" to sortOrder!!.pson,
                lastId?.let { "lastId" to lastId.pson }
            ).pson
        )
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 7, args, pson_result.ptr)
        val pagingList = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
        pagingList.toPagingList(PsonValue.PsonObject::toMessage)
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun deleteMessage(messageId: String?) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(PsonValue.PsonString(messageId!!))
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 9, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()
        Unit
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun updateMessage(
        messageId: String?,
        publicMeta: ByteArray?,
        privateMeta: ByteArray?,
        data: ByteArray?
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            PsonValue.PsonString(messageId!!),
            PsonValue.PsonBinary(publicMeta!!),
            PsonValue.PsonBinary(privateMeta!!),
            PsonValue.PsonBinary(data!!)
        )
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 10, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()
        Unit
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun subscribeForThreadEvents() = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 11, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()
        Unit
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun unsubscribeFromThreadEvents() = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 12, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()
        Unit
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun subscribeForMessageEvents(threadId: String?) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            PsonValue.PsonString(threadId!!)
        )
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 12, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()
        Unit
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun unsubscribeFromMessageEvents(threadId: String?) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            PsonValue.PsonString(threadId!!)
        )
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 12, args, pson_result.ptr)
        pson_result.value!!.asResponse?.getResultOrThrow()
        Unit
    }

    actual override fun close() {
        if(nativeThreadApi.value == null) return
        privmx_endpoint_freeThreadApi(nativeThreadApi.value)
        nativeHeap.free(nativeThreadApi.rawPtr)
        nativeThreadApi.value = null
    }
}

fun <K, V> mapOfWithNulls(vararg pairs: Pair<K, V>?): Map<K, V> =
    mapOf(*(pairs.filterNotNull().toTypedArray()))