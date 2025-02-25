package com.simplito.java.privmx_endpoint.modules.thread

import com.simplito.java.privmx_endpoint.model.ContainerPolicy
import com.simplito.java.privmx_endpoint.model.ItemPolicy
import com.simplito.java.privmx_endpoint.model.Message
import com.simplito.java.privmx_endpoint.model.PagingList
import com.simplito.java.privmx_endpoint.model.ServerMessageInfo
import com.simplito.java.privmx_endpoint.model.Thread
import com.simplito.java.privmx_endpoint.model.UserWithPubKey
import com.simplito.java.privmx_endpoint.model.exceptions.NativeException
import com.simplito.java.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.java.privmx_endpoint.modules.core.Connection
import com.simplito.java.privmx_endpoint.utils.PsonResponse
import com.simplito.java.privmx_endpoint.utils.PsonValue
import com.simplito.java.privmx_endpoint.utils.makeArgs
import com.simplito.java.privmx_endpoint.utils.psonMapper
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

    init {
        privmx_endpoint_newThreadApi(connection.getConnectionPtr(), nativeThreadApi.ptr)
        memScoped {
            val args = pson_new_array()
            val pson_result = allocPointerTo<pson_value>()
            //TODO: Check exception
            privmx_endpoint_execThreadApi(nativeThreadApi.value, 0, args, pson_result.ptr)
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
            PsonValue.PsonString(contextId!!),
            PsonValue.PsonArray<PsonValue.PsonObject>(users!!.map { parseUserWithPubKey2Pson(it!!) }),
            PsonValue.PsonArray<PsonValue.PsonObject>(managers!!.map { parseUserWithPubKey2Pson(it!!) }),
            PsonValue.PsonBinary(publicMeta!!),
            PsonValue.PsonBinary(privateMeta!!),
            policies?.let { parseContainerPolicy2Pson(policies) } ?: PsonValue.PsonNull()
        )
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 1, args, pson_result.ptr)
        (PsonResponse(psonMapper(pson_result.value!!) as PsonValue.PsonObject).getResultOrThrow() as PsonValue.PsonString).getValue()
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
            PsonValue.PsonString(threadId!!),
            PsonValue.PsonArray<PsonValue.PsonObject>(users!!.map { parseUserWithPubKey2Pson(it!!) }),
            PsonValue.PsonArray<PsonValue.PsonObject>(managers!!.map { parseUserWithPubKey2Pson(it!!) }),
            PsonValue.PsonBinary(publicMeta!!),
            PsonValue.PsonBinary(privateMeta!!),
            PsonValue.PsonLong(version),
            PsonValue.PsonBoolean(force),
            PsonValue.PsonBoolean(forceGenerateNewKey),
            policies?.let { parseContainerPolicy2Pson(policies) }
        )
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 2, args, pson_result.ptr)
        PsonResponse(psonMapper(pson_result.value!!) as PsonValue.PsonObject).getResultOrThrow()
        Unit
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getThread(threadId: String?): Thread? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(PsonValue.PsonString(threadId!!))
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 4, args, pson_result.ptr)
        val result =
            PsonResponse(psonMapper(pson_result.value!!) as PsonValue.PsonObject).getResultOrThrow() as PsonValue.PsonObject
        parseThread(result)
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
            PsonValue.PsonString(contextId!!),
            PsonValue.PsonObject(
                mapOfWithNulls(
                    "skip" to PsonValue.PsonLong(skip),
                    "limit" to PsonValue.PsonLong(limit),
                    "sortOrder" to PsonValue.PsonString(sortOrder!!),
                    lastId?.let { "lastId" to PsonValue.PsonString(lastId) }
                ))
        )
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 5, args, pson_result.ptr)
        val pagingList =
            PsonResponse(psonMapper(pson_result.value!!) as PsonValue.PsonObject).getResultOrThrow() as PsonValue.PsonObject
        PagingList(
            (pagingList["totalAvailable"] as PsonValue.PsonLong).getValue(),
            (pagingList["readItems"] as PsonValue.PsonArray<PsonValue.PsonObject>).getValue()
                .map { thread ->
                    parseThread(thread)
                }
        )
    }


    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun deleteThread(threadId: String?) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(PsonValue.PsonString(threadId!!))
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 3, args, pson_result.ptr)
        val result =
            PsonResponse(psonMapper(pson_result.value!!) as PsonValue.PsonObject).getResultOrThrow()
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
            PsonValue.PsonString(threadId!!),
            PsonValue.PsonBinary(publicMeta!!),
            PsonValue.PsonBinary(privateMeta!!),
            PsonValue.PsonBinary(data!!),
        )
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 8, args, pson_result.ptr)
        (PsonResponse(psonMapper(pson_result.value!!) as PsonValue.PsonObject).getResultOrThrow() as PsonValue.PsonString).getValue()
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getMessage(messageId: String?): Message? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(PsonValue.PsonString(messageId!!))
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 6, args, pson_result.ptr)
        val psonObject = PsonResponse(psonMapper(pson_result.value!!) as PsonValue.PsonObject).getResultOrThrow() as PsonValue.PsonObject
        parseMessage(psonObject)
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
            PsonValue.PsonString(threadId!!),
            PsonValue.PsonObject(
                mapOfWithNulls(
                    "skip" to PsonValue.PsonLong(skip),
                    "limit" to PsonValue.PsonLong(limit),
                    "sortOrder" to PsonValue.PsonString(sortOrder!!),
                    lastId?.let { "lastId" to PsonValue.PsonString(lastId) }
                ))
        )
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 7, args, pson_result.ptr)
        val pagingList =
            PsonResponse(psonMapper(pson_result.value!!) as PsonValue.PsonObject).getResultOrThrow() as PsonValue.PsonObject
        PagingList(
            (pagingList["totalAvailable"] as PsonValue.PsonLong).getValue(),
            (pagingList["readItems"] as PsonValue.PsonArray<PsonValue.PsonObject>).getValue()
                .map { message ->
                    parseMessage(message)
                }
        )
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun deleteMessage(messageId: String?) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(PsonValue.PsonString(messageId!!))
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 9, args, pson_result.ptr)
        PsonResponse(psonMapper(pson_result.value!!) as PsonValue.PsonObject).getResultOrThrow()
        Unit
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun updateMessage(
        messageId: String?,
        publicMeta: ByteArray?,
        privateMeta: ByteArray?,
        data: ByteArray?
    ) = memScoped{
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            PsonValue.PsonString(messageId!!),
            PsonValue.PsonBinary(publicMeta!!),
            PsonValue.PsonBinary(privateMeta!!),
            PsonValue.PsonBinary(data!!)
        )
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 10, args, pson_result.ptr)
        PsonResponse(psonMapper(pson_result.value!!) as PsonValue.PsonObject).getResultOrThrow()
        Unit
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun subscribeForThreadEvents() = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 11, args, pson_result.ptr)
        PsonResponse(psonMapper(pson_result.value!!) as PsonValue.PsonObject).getResultOrThrow()
        Unit
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun unsubscribeFromThreadEvents() = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 12, args, pson_result.ptr)
        PsonResponse(psonMapper(pson_result.value!!) as PsonValue.PsonObject).getResultOrThrow()
        Unit
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun subscribeForMessageEvents(threadId: String?) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            PsonValue.PsonString(threadId!!)
        )
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 12, args, pson_result.ptr)
        PsonResponse(psonMapper(pson_result.value!!) as PsonValue.PsonObject).getResultOrThrow()
        Unit
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun unsubscribeFromMessageEvents(threadId: String?) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            PsonValue.PsonString(threadId!!)
        )
        privmx_endpoint_execThreadApi(nativeThreadApi.value, 12, args, pson_result.ptr)
        PsonResponse(psonMapper(pson_result.value!!) as PsonValue.PsonObject).getResultOrThrow()
        Unit
    }


    actual override fun close() {
        privmx_endpoint_freeThreadApi(nativeThreadApi.value)
    }

    private fun parseContainerPolicy(policy: PsonValue.PsonObject): ContainerPolicy =
        ContainerPolicy(
            (policy["get"] as PsonValue.PsonString?)?.getValue(),
            (policy["update"] as PsonValue.PsonString?)?.getValue(),
            (policy["delete"] as PsonValue.PsonString?)?.getValue(),
            (policy["updatePolicy"] as PsonValue.PsonString?)?.getValue(),
            (policy["updaterCanBeRemovedFromManagers"] as PsonValue.PsonString?)?.getValue(),
            (policy["ownerCanBeRemovedFromManagers"] as PsonValue.PsonString?)?.getValue(),
            (policy["item"] as PsonValue.PsonObject?)?.let { parseItemPolicy(it) }
        )

    private fun parseItemPolicy(policy: PsonValue.PsonObject): ItemPolicy = ItemPolicy(
        (policy["get"] as PsonValue.PsonString?)?.getValue(),
        (policy["listMy"] as PsonValue.PsonString?)?.getValue(),
        (policy["listAll"] as PsonValue.PsonString?)?.getValue(),
        (policy["create"] as PsonValue.PsonString?)?.getValue(),
        (policy["update"] as PsonValue.PsonString?)?.getValue(),
        (policy["delete"] as PsonValue.PsonString?)?.getValue()
    )

    private fun parseThread(thread: PsonValue.PsonObject) = Thread(
        (thread["contextId"] as PsonValue.PsonString).getValue(),
        (thread["threadId"] as PsonValue.PsonString).getValue(),
        (thread["createDate"] as PsonValue.PsonLong).getValue(),
        (thread["creator"] as PsonValue.PsonString).getValue(),
        (thread["lastModificationDate"] as PsonValue.PsonLong).getValue(),
        (thread["lastModifier"] as PsonValue.PsonString).getValue(),
        (thread["users"] as PsonValue.PsonArray<PsonValue.PsonString>).getValue()
            .map { it.getValue() },
        (thread["managers"] as PsonValue.PsonArray<PsonValue.PsonString>).getValue()
            .map { it.getValue() },
        (thread["version"] as PsonValue.PsonLong).getValue(),
        (thread["lastMsgDate"] as PsonValue.PsonLong).getValue(),
        (thread["publicMeta"] as PsonValue.PsonBinary).getValue(),
        (thread["privateMeta"] as PsonValue.PsonBinary).getValue(),
        (thread["policy"] as PsonValue.PsonObject?)?.let { parseContainerPolicy(it) },
        (thread["messagesCount"] as PsonValue.PsonLong).getValue(),
        (thread["statusCode"] as PsonValue.PsonLong).getValue(),
    )

    private fun parseMessage(message: PsonValue.PsonObject) = Message(
        parseServerMessageInfo(message["info"] as PsonValue.PsonObject),
        (message["publicMeta"] as PsonValue.PsonBinary).getValue(),
        (message["privateMeta"] as PsonValue.PsonBinary).getValue(),
        (message["data"] as PsonValue.PsonBinary).getValue(),
        (message["authorPubKey"] as PsonValue.PsonString).getValue(),
        (message["statusCode"] as PsonValue.PsonLong).getValue()
    )

    private fun parseServerMessageInfo(message: PsonValue.PsonObject) = ServerMessageInfo(
        (message["threadId"] as PsonValue.PsonString).getValue(),
        (message["messageId"] as PsonValue.PsonString).getValue(),
        (message["createDate"] as PsonValue.PsonLong).getValue(),
        (message["author"] as PsonValue.PsonString).getValue()
    )

    private fun parseUserWithPubKey2Pson(userWithPubKey: UserWithPubKey) = PsonValue.PsonObject(
        mapOfWithNulls(
            "userId" to PsonValue.PsonString(userWithPubKey.userId!!),
            "pubKey" to PsonValue.PsonString(userWithPubKey.pubKey!!),
        )
    )

    private fun parseContainerPolicy2Pson(policies: ContainerPolicy) = PsonValue.PsonObject(
        mapOfWithNulls(
            policies.get?.let { "get" to PsonValue.PsonString(policies.get) },
            policies.update?.let { "update" to PsonValue.PsonString(policies.update) },
            policies.delete?.let { "delete" to PsonValue.PsonString(policies.delete) },
            policies.updatePolicy?.let { "updatePolicy" to PsonValue.PsonString(policies.updatePolicy) },
            policies.updaterCanBeRemovedFromManagers?.let {
                "updaterCanBeRemovedFromManagers" to PsonValue.PsonString(
                    policies.updaterCanBeRemovedFromManagers
                )
            },
            policies.ownerCanBeRemovedFromManagers?.let {
                "ownerCanBeRemovedFromManagers" to PsonValue.PsonString(
                    policies.ownerCanBeRemovedFromManagers
                )
            },
            policies.item?.let { "item" to parseItemPolicy2Pson(policies.item) }
        )
    )

    private fun parseItemPolicy2Pson(policies: ItemPolicy) = PsonValue.PsonObject(
        mapOfWithNulls(
            policies.get?.let { "get" to PsonValue.PsonString(policies.get) },
            policies.listMy?.let { "listMy" to PsonValue.PsonString(policies.listMy) },
            policies.listAll?.let { "listAll" to PsonValue.PsonString(policies.listAll) },
            policies.create?.let { "create" to PsonValue.PsonString(policies.create) },
            policies.update?.let { "update" to PsonValue.PsonString(policies.update) },
            policies.delete?.let { "delete" to PsonValue.PsonString(policies.delete) },
        )
    )

}

fun <K, V> mapOfWithNulls(vararg pairs: Pair<K, V>?): Map<K, V> =
    mapOf(*(pairs.filterNotNull().toTypedArray()))