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
actual object Base32 {
    actual fun encode(data: ByteArray): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(data.pson)
        try {
            privmx_endpoint_execUtils(_nativeUtils.value, 4, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    actual fun decode(base32Data: String): ByteArray = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(base32Data.pson)
        try {
            privmx_endpoint_execUtils(_nativeUtils.value, 5, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    actual fun `is`(data: String): Boolean = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(data.pson)
        try {
            privmx_endpoint_execUtils(_nativeUtils.value, 6, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }
}