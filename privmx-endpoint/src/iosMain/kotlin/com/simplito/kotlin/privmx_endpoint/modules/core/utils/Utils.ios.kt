package com.simplito.kotlin.privmx_endpoint.modules.core.utils

import cnames.structs.Utils
import cnames.structs.pson_value
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import com.simplito.kotlin.privmx_endpoint.utils.makeArgs
import com.simplito.kotlin.privmx_endpoint.utils.pson
import com.simplito.kotlin.privmx_endpoint.utils.typedList
import com.simplito.kotlin.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execUtils
import libprivmxendpoint.privmx_endpoint_newUtils
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_free_value

@OptIn(ExperimentalForeignApi::class)
internal val _nativeUtils by lazy {
    nativeHeap.allocPointerTo<Utils>().apply {
        privmx_endpoint_newUtils(this.ptr)
    }
}

@OptIn(ExperimentalForeignApi::class)
actual object Utils {
    /**
     * Removes all trailing whitespace.
     *
     * @param data
     * @return copy of string with removed trailing whitespace
     */
    actual fun trim(data: String): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(data.pson)
        try {
            privmx_endpoint_execUtils(_nativeUtils.value, 10, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Splits a string using the provided delimiter.
     *
     * @param data      the string to be split
     * @param delimiter string which will be split
     * @return split parts
     */
    actual fun split(
        data: String,
        delimiter: String
    ): List<String> = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(
            data.pson,
            delimiter.pson
        )
        try {
            privmx_endpoint_execUtils(_nativeUtils.value, 11, args, result.ptr)
            val list = result.value!!.asResponse?.getResultOrThrow()!!
            list.typedList().map { (it.typedValue()) }
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }

    /**
     * Removes all whitespace from the left of given string.
     *
     * @param data reference to string
     * @return copy of string without whitespace at the beginning
     */
    actual fun ltrim(data: String): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(data.pson)
        try {
            privmx_endpoint_execUtils(_nativeUtils.value, 12, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Removes all whitespace from the right of given string.
     *
     * @param data string to check
     * @return copy of string without whitespace at the end
     */
    actual fun rtrim(data: String): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(data.pson)
        try {
            privmx_endpoint_execUtils(_nativeUtils.value, 13, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }
}