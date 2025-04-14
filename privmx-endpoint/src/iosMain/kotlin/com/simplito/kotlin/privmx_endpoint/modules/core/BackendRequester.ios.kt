package com.simplito.kotlin.privmx_endpoint.modules.core

import cnames.structs.pson_value
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import com.simplito.kotlin.privmx_endpoint.utils.makeArgs
import com.simplito.kotlin.privmx_endpoint.utils.pson
import com.simplito.kotlin.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execBackendRequester
import libprivmxendpoint.privmx_endpoint_newBackendRequester
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_free_value

@OptIn(ExperimentalForeignApi::class)
actual object BackendRequester {
    private val nativeBackendRequester =
        nativeHeap.allocPointerTo<cnames.structs.BackendRequester>()

    init {
        privmx_endpoint_newBackendRequester(nativeBackendRequester.ptr)
    }

    @Throws(PrivmxException::class, NativeException::class)
    actual fun backendRequest(
        serverUrl: String, accessToken: String, method: String, paramsAsJson: String
    ): String = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(
            serverUrl.pson,
            accessToken.pson,
            method.pson,
            paramsAsJson.pson,
        )
        try {
            privmx_endpoint_execBackendRequester(
                nativeBackendRequester.value, 0, args, result.ptr
            )
            result.value!!.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }


    @Throws(PrivmxException::class, NativeException::class)
    actual fun backendRequest(
        serverUrl: String, method: String, paramsAsJson: String
    ): String = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(
            serverUrl.pson,
            method.pson,
            paramsAsJson.pson,
        )
        try {
            privmx_endpoint_execBackendRequester(
                nativeBackendRequester.value, 0, args, result.ptr
            )
            result.value!!.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }


    @Throws(PrivmxException::class, NativeException::class)
    actual fun backendRequest(
        serverUrl: String,
        apiKeyId: String,
        apiKeySecret: String,
        mode: Long,
        method: String,
        paramsAsJson: String
    ): String = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(
            serverUrl.pson,
            apiKeyId.pson,
            apiKeySecret.pson,
            mode.pson,
            method.pson,
            paramsAsJson.pson,
        )
        try {
            privmx_endpoint_execBackendRequester(
                nativeBackendRequester.value, 0, args, result.ptr
            )
            result.value!!.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }
}