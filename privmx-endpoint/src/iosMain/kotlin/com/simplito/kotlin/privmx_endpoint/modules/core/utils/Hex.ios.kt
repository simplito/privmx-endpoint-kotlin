package com.simplito.kotlin.privmx_endpoint.modules.core.utils

import cnames.structs.pson_value
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import com.simplito.kotlin.privmx_endpoint.utils.makeArgs
import com.simplito.kotlin.privmx_endpoint.utils.pson
import com.simplito.kotlin.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execUtils
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_free_value

@OptIn(ExperimentalForeignApi::class)
actual object Hex {
    /**
     * Encodes buffer to a string in Hex format.
     *
     * @param data buffer to encode
     * @return string in Hex format
     */
    actual fun encode(data: ByteArray): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(data.pson)
        try {
            privmx_endpoint_execUtils(_nativeUtils.value, 1, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Decodes string in Hex to buffer.
     *
     * @param hexData string to decode
     * @return buffer with decoded data
     */
    actual fun decode(hexData: String): ByteArray = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(hexData.pson)
        try {
            privmx_endpoint_execUtils(_nativeUtils.value, 2, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Checks if given string is in Hex format.
     *
     * @param data string to check
     * @return data check result
     */
    actual fun `is`(data: String): Boolean = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(data.pson)
        try {
            privmx_endpoint_execUtils(_nativeUtils.value, 3, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }
}