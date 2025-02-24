package com.simplito.java.privmx_endpoint.modules.core

import com.simplito.java.privmx_endpoint.model.Context
import com.simplito.java.privmx_endpoint.model.PagingList
import com.simplito.java.privmx_endpoint.model.exceptions.NativeException
import com.simplito.java.privmx_endpoint.model.exceptions.PrivmxException
import kotlinx.cinterop.*
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.*

@OptIn(ExperimentalForeignApi::class)
actual class Connection() : AutoCloseable {
    private val nativeConnection = nativeHeap.allocPointerTo<libprivmxendpoint.Connection>()

    actual companion object {
        actual fun connect(userPrivKey: String, solutionId: String, host: String): Connection =
            Connection().apply {
                memScoped {
                    val args = pson_new_array()
                    val result = allocPointerTo<pson_value>().apply {
                        value = pson_new_object()
                    }

                    pson_add_array_value(args, pson_new_string(userPrivKey))
                    pson_add_array_value(args, pson_new_string(solutionId))
                    pson_add_array_value(args, pson_new_string(host))
                    privmx_endpoint_newConnection(nativeConnection.ptr)
                    privmx_endpoint_execConnection(nativeConnection.value, 0, args, result.ptr)
                    PsonResponse(psonMapper(result.value!!) as PsonValue.PsonObject).getResultOrThrow()
                }
            }
    }

    actual fun listContexts(
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?
    ): PagingList<Context> = memScoped {
        val args = pson_new_array()
        val result = allocPointerTo<pson_value>()
        val pagingQuery = pson_new_object();

        pson_set_object_value(pagingQuery, "skip", pson_new_int64(skip))
        pson_set_object_value(pagingQuery, "limit", pson_new_int64(limit))
        pson_set_object_value(pagingQuery, "sortOrder", pson_new_string(sortOrder))
        pson_add_array_value(args, pagingQuery)

        privmx_endpoint_execConnection(nativeConnection.value, 3, args, result.ptr)
        return PsonResponse(psonMapper(result.value!!) as PsonValue.PsonObject).getResultOrThrow()
            .let { result ->
                (result as PsonValue.PsonObject).let { pagingList->
                    PagingList(
                        (pagingList["totalAvailable"] as PsonValue.PsonLong).getValue(),
                        (pagingList["readItems"] as PsonValue.PsonArray<PsonValue.PsonObject>).getValue().map { context->
                            Context(
                                (context["userId"] as PsonValue.PsonString ).getValue(),
                                (context["contextId"] as PsonValue.PsonString ).getValue(),
                            )
                        }
                    )
                }
            }
    }

    actual fun disconnect() = memScoped {
        val args = pson_new_object()
        val result = allocPointerTo<pson_value>().apply {
            value = pson_new_object()
        }
        privmx_endpoint_execConnection(nativeConnection.value, 4, args, result.ptr)
        privmx_endpoint_freeConnection(nativeConnection.value)
        Unit
    }

    actual override fun close() {
        disconnect()
        nativeHeap.free(nativeConnection)
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun psonMapper(psonValue: CPointer<pson_value>): PsonValue<out Any>? = memScoped {
    when (pson_value_type(psonValue)) {
        PSON_BOOL -> {
            val psonBool = alloc<IntVar>()
            pson_get_bool(psonValue, psonBool.ptr)
            return PsonValue.PsonBoolean(psonBool.value == 1)
        }

        PSON_BINARY -> {
            val psonBinaryData = allocPointerTo<ByteVar>()
            val psonBinarySize = alloc<ULongVar>()
            pson_inspect_binary(psonValue, psonBinaryData.ptr, psonBinarySize.ptr)
            return PsonValue.PsonBinary(
                psonBinaryData.value?.readBytes(psonBinarySize.value.toInt()) ?: ByteArray(0)
            )
        }

        PSON_INT32 -> {
            val psonInt = alloc<IntVar>()
            pson_get_int32(psonValue, psonInt.ptr)
            return PsonValue.PsonInt(psonInt.value)
        }

        PSON_INT64 -> {
            val psonLong = alloc<LongVar>()
            pson_get_int64(psonValue, psonLong.ptr)
            return PsonValue.PsonLong(psonLong.value)
        }

        PSON_FLOAT32 -> {
            val psonFloat = alloc<FloatVar>()
            pson_get_float32(psonValue, psonFloat.ptr)
            return PsonValue.PsonFloat(psonFloat.value)
        }

        PSON_FLOAT64 -> {
            val psonDouble = alloc<DoubleVar>()
            pson_get_float64(psonValue, psonDouble.ptr)
            return PsonValue.PsonDouble(psonDouble.value)
        }

        PSON_STRING -> {
            return PsonValue.PsonString(pson_get_cstring(psonValue)?.toKStringFromUtf8() ?: "")
        }

        PSON_ARRAY -> {
            val psonBinarySize = alloc<ULongVar>()
            pson_get_array_size(psonValue, psonBinarySize.ptr)
            return PsonValue.PsonArray<PsonValue<Any>>(
                (0uL..<psonBinarySize.value).map { offset ->
                    psonMapper(pson_get_array_value(psonValue, offset)!!)
                }.toList() as List<PsonValue<Any>>
            )
        }

        PSON_OBJECT -> {
            val objectMap = mutableMapOf<String, PsonValue<*>?>()
            val pson_object_iterator = allocPointerTo<pson_object_iterator>()
            val current_pson_value = allocPointerTo<pson_value>()
            val current_pson_value_key = allocPointerTo<ByteVar>()
            pson_open_object_iterator(psonValue, pson_object_iterator.ptr)
            while (
                pson_object_iterator_next(
                    pson_object_iterator.value,
                    current_pson_value_key.ptr,
                    current_pson_value.ptr
                ) != 0
            ) {
                current_pson_value_key.value?.toKStringFromUtf8()?.let { key ->
                    objectMap[key] = psonMapper(current_pson_value.value!!)
                }
            }
            return PsonValue.PsonObject(objectMap as Map<String, PsonValue<Any>?>)
        }
    }
    return null
}

@OptIn(ExperimentalForeignApi::class)
internal class PsonResponse(
    fromValue: PsonValue.PsonObject
) {
    val __type = fromValue["__type"] as PsonValue.PsonString
    val result = fromValue["result"] as PsonValue.PsonObject?
    val error = fromValue["error"] as PsonValue.PsonObject?
    val status = fromValue["status"] as PsonValue.PsonBoolean

    @Throws(
        PrivmxException::class,
        NativeException::class
    )
    internal fun getResultOrThrow(): PsonValue<*>? {
        if (error == null && status.getValue()) {
            return result
        } else if (error != null && !status.getValue()) {
            throw if (error["description"] != null) {
                PrivmxException(
                    "",
                    (error["description"] as PsonValue.PsonString).getValue(),
                    (error["scope"] as PsonValue.PsonString).getValue(),
                    (error["code"] as PsonValue.PsonLong).getValue().toInt()
                )
            } else NativeException(
                (error["message"] as PsonValue.PsonString?)?.getValue() ?: "Unknown exception"
            )
        } else {
            throw NativeException("Unexpected state of response")
        }
    }
}

internal sealed class PsonValue<T> {
    protected abstract val value: T

    fun getValue(): T = value

    class PsonBoolean internal constructor(override val value: Boolean) : PsonValue<Boolean>()
    class PsonLong internal constructor(override val value: Long) : PsonValue<Long>()
    class PsonInt internal constructor(override val value: Int) : PsonValue<Int>()
    class PsonDouble internal constructor(override val value: Double) : PsonValue<Double>()
    class PsonFloat internal constructor(override val value: Float) : PsonValue<Float>()
    class PsonBinary internal constructor(override val value: ByteArray) : PsonValue<ByteArray>()
    class PsonString internal constructor(override val value: String) : PsonValue<String>()
    class PsonArray<T : PsonValue<Any>> internal constructor(override val value: List<T>) :
        PsonValue<List<T>>() {
        operator fun get(index: Int): T = this.value[index]
    }

    class PsonObject internal constructor(override val value: Map<String, PsonValue<Any>?>) :
        PsonValue<Map<String, PsonValue<Any>?>>() {
        operator fun get(key: String): PsonValue<Any>? = this.value[key]
    }
}
