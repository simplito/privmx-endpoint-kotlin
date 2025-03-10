package com.simplito.java.privmx_endpoint.utils

import com.simplito.java.privmx_endpoint.model.exceptions.NativeException
import com.simplito.java.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.java.privmx_endpoint.utils.PsonValue.PsonArray
import com.simplito.java.privmx_endpoint.utils.PsonValue.PsonBinary
import com.simplito.java.privmx_endpoint.utils.PsonValue.PsonBoolean
import com.simplito.java.privmx_endpoint.utils.PsonValue.PsonDouble
import com.simplito.java.privmx_endpoint.utils.PsonValue.PsonFloat
import com.simplito.java.privmx_endpoint.utils.PsonValue.PsonInt
import com.simplito.java.privmx_endpoint.utils.PsonValue.PsonLong
import com.simplito.java.privmx_endpoint.utils.PsonValue.PsonObject
import com.simplito.java.privmx_endpoint.utils.PsonValue.PsonString
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.DoubleVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.FloatVar
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.LongVar
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import libprivmxendpoint.*
import kotlin.reflect.KClass

internal typealias KPSON_NULL = PsonValue.PsonNull

@OptIn(ExperimentalForeignApi::class)
internal fun psonMapper(psonValue: CPointer<pson_value>): PsonValue<Any>? = memScoped {
    when (pson_value_type(psonValue)) {
        PSON_BOOL -> {
            val psonBool = alloc<IntVar>()
            pson_get_bool(psonValue, psonBool.ptr)
            return PsonBoolean(psonBool.value == 1)
        }

        PSON_BINARY -> {
            val psonBinaryData = allocPointerTo<ByteVar>()
            val psonBinarySize = alloc<ULongVar>()
            pson_inspect_binary(psonValue, psonBinaryData.ptr, psonBinarySize.ptr)
            return PsonBinary(
                psonBinaryData.value?.readBytes(psonBinarySize.value.toInt()) ?: ByteArray(0)
            )
        }

        PSON_INT32 -> {
            val psonInt = alloc<IntVar>()
            pson_get_int32(psonValue, psonInt.ptr)
            return PsonInt(psonInt.value)
        }

        PSON_INT64 -> {
            val psonLong = alloc<LongVar>()
            pson_get_int64(psonValue, psonLong.ptr)
            return PsonLong(psonLong.value)
        }

        PSON_FLOAT32 -> {
            val psonFloat = alloc<FloatVar>()
            pson_get_float32(psonValue, psonFloat.ptr)
            return PsonFloat(psonFloat.value)
        }

        PSON_FLOAT64 -> {
            val psonDouble = alloc<DoubleVar>()
            pson_get_float64(psonValue, psonDouble.ptr)
            return PsonDouble(psonDouble.value)
        }

        PSON_STRING -> {
            return PsonString(pson_get_cstring(psonValue)?.toKStringFromUtf8() ?: "")
        }

        PSON_ARRAY -> {
            val psonBinarySize = alloc<ULongVar>()
            pson_get_array_size(psonValue, psonBinarySize.ptr)
            return PsonArray<PsonValue<Any>>(
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
                    current_pson_value.value?.let { value ->
                        objectMap[key] = psonMapper(value)
                    }
                }
            }
            @Suppress("UNCHECKED_CAST")
            return PsonObject(objectMap as Map<String, PsonValue<Any>>)
        }
    }
    return null
}

@OptIn(ExperimentalForeignApi::class)
internal class PsonResponse(
    fromValue: PsonValue.PsonObject
) {
    val __type = fromValue["__type"] as PsonString
    val result = fromValue["result"]
    val error = fromValue["error"] as PsonObject?
    val status = fromValue["status"] as PsonBoolean

    @Throws(
        PrivmxException::class,
        NativeException::class
    )
    internal fun getResultOrThrow(): PsonValue<Any>? {
        if (error == null && status.getValue()) {
            return result
        } else if (error != null && !status.getValue()) {
            throw if (error["description"] != null) {
                PrivmxException(
                    "",
                    (error["description"] as PsonString).getValue(),
                    (error["scope"] as PsonString).getValue(),
                    (error["code"] as PsonLong).getValue().toInt(),
                    (error["name"] as PsonString).getValue()
                )
            } else NativeException(
                (error["message"] as PsonString?)?.getValue() ?: "Unknown exception"
            )
        } else {
            throw NativeException("Unexpected state of response")
        }
    }
}

internal sealed class PsonValue<out T> {
    protected abstract val value: T

    //TODO: remove redundant getValue
    fun getValue(): T = value

    class PsonBoolean internal constructor(override val value: Boolean) : PsonValue<Boolean>()
    class PsonLong internal constructor(override val value: Long) : PsonValue<Long>()
    class PsonInt internal constructor(override val value: Int) : PsonValue<Int>()
    class PsonDouble internal constructor(override val value: Double) : PsonValue<Double>()
    class PsonFloat internal constructor(override val value: Float) : PsonValue<Float>()
    class PsonBinary internal constructor(override val value: ByteArray) : PsonValue<ByteArray>()
    class PsonString internal constructor(override val value: String) : PsonValue<String>()
    data object PsonNull: PsonValue<Unit>(){
        override val value = Unit
    }
    class PsonArray<T : PsonValue<Any>> internal constructor(override val value: List<T>) :
        PsonValue<List<T>>() {
        operator fun get(index: Int): T = this.value[index]
    }

    class PsonObject internal constructor(override val value: Map<String, PsonValue<Any>>) :
        PsonValue<Map<String, PsonValue<Any>>>() {
        operator fun get(key: String): PsonValue<Any>? = this.value[key]
        internal fun getDebugString() = value.entries.joinToString(",") { entry ->
            "${entry.key}: ${
                entry.value.getValue()
            }"
        }

    }
}

@Throws(IllegalArgumentException::class)
@OptIn(ExperimentalForeignApi::class)
internal fun makeArgs(vararg args: PsonValue<out Any>?): CPointer<pson_value> = memScoped {
    pson_new_array()!!.run {
        args.filterNotNull().forEach { arg ->
            pson_add_array_value(this,convertToPson(arg))
        }
        this
    }
}

@OptIn(ExperimentalForeignApi::class)
@Throws(IllegalArgumentException::class)
private fun convertToPson(value: PsonValue<Any>): CPointer<pson_value> = memScoped {
    val psonValue: CPointer<pson_value>? = when (value) {
        is PsonBoolean -> pson_new_bool(if (value.getValue()) 1 else 0)
        is PsonLong -> pson_new_int64(value.getValue())
        is PsonInt -> pson_new_int32(value.getValue())
        is PsonDouble -> pson_new_float64(value.getValue())
        is PsonFloat -> pson_new_float32(value.getValue())
        is PsonValue.PsonNull -> pson_new_null()
        is PsonBinary -> {
            value.getValue().usePinned { pinned ->
                pson_new_binary(pinned.addressOf(0), value.getValue().size.toULong())
            }
        }

        is PsonString -> pson_new_string(value.getValue())
        is PsonArray<*> -> {
            pson_new_array().also { psonArray ->
                value.getValue().forEach { item ->
                    pson_add_array_value(psonArray, convertToPson(item))
                }
            }
        }

        is PsonObject -> {
            pson_new_object().also { psonObject ->
                value.getValue().entries.forEach { (k, v) ->
                    pson_set_object_value(psonObject, k, convertToPson(v))
                }
            }
        }
    }
    psonValue!!
}

@OptIn(ExperimentalForeignApi::class)
internal val CPointer<pson_value>.asResponse: PsonResponse?
    get() = (psonMapper(this) as? PsonObject)?.let { PsonResponse(it) }

internal fun <K, V> mapOfWithNulls(vararg pairs: Pair<K, V>?): Map<K, V> =
    mapOf(*(pairs.filterNotNull().toTypedArray()))