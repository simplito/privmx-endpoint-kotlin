//
// PrivMX Endpoint Kotlin.
// Copyright © 2025 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//

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

/**
 * 'BackendRequester' provides functions to call PrivMX Bridge API.
 */
@OptIn(ExperimentalForeignApi::class)
actual object BackendRequester {
    private val nativeBackendRequester =
        nativeHeap.allocPointerTo<cnames.structs.BackendRequester>()

    init {
        privmx_endpoint_newBackendRequester(nativeBackendRequester.ptr)
    }

    /**
     * Sends a request to PrivMX Bridge API using access token for authorization.
     *
     * @param serverUrl PrivMX Bridge server URL
     * @param accessToken token for authorization (see PrivMX Bridge API for more details)
     * @param method API method to call
     * @param paramsAsJson API method's parameters in JSON format
     *
     * @return JSON String representing raw server response
     * @throws PrivmxException thrown when method encounters an exception.
     * @throws NativeException thrown when method encounters an unknown exception.
     */
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

    /**
     * Sends request to PrivMX Bridge API.
     *
     * @param serverUrl PrivMX Bridge server URL
     * @param method API method to call
     * @param paramsAsJson API method's parameters in JSON format
     *
     * @return JSON String representing raw server response
     */
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

    /**
     * Sends a request to PrivMX Bridge API using pair of API KEY ID and API KEY SECRET for authorization.
     *
     * @param serverUrl PrivMX Bridge server URL
     * @param apiKeyId API KEY ID (see PrivMX Bridge API for more details)
     * @param apiKeySecret API KEY SECRET (see PrivMX Bridge API for more details)
     * @param mode allows you to set whether the request should be signed (mode = 1) or plain (mode = 0)
     * @param method API method to call
     * @param paramsAsJson API method's parameters in JSON format
     *
     * @return JSON String representing raw server response
     */
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