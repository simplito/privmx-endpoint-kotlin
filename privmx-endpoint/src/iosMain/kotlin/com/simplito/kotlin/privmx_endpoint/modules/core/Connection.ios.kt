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
import com.simplito.kotlin.privmx_endpoint.model.Context
import com.simplito.kotlin.privmx_endpoint.model.PKIVerificationOptions
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.UserInfo
import com.simplito.kotlin.privmx_endpoint.model.UserVerifierInterface
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.utils.KPSON_NULL
import com.simplito.kotlin.privmx_endpoint.utils.PsonResponse
import com.simplito.kotlin.privmx_endpoint.utils.PsonValue
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import com.simplito.kotlin.privmx_endpoint.utils.makeArgs
import com.simplito.kotlin.privmx_endpoint.utils.mapOfWithNulls
import com.simplito.kotlin.privmx_endpoint.utils.pson
import com.simplito.kotlin.privmx_endpoint.utils.psonMapper
import com.simplito.kotlin.privmx_endpoint.utils.toContext
import com.simplito.kotlin.privmx_endpoint.utils.toPagingList
import com.simplito.kotlin.privmx_endpoint.utils.toUserInfo
import com.simplito.kotlin.privmx_endpoint.utils.toVerificationRequest
import com.simplito.kotlin.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execConnection
import libprivmxendpoint.privmx_endpoint_freeConnection
import libprivmxendpoint.privmx_endpoint_newConnection
import libprivmxendpoint.privmx_endpoint_setCertsPath
import libprivmxendpoint.privmx_endpoint_setUserVerifier
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_free_value
import libprivmxendpoint.pson_new_object

/**
 * Manages a connection between the PrivMX Endpoint and PrivMX Bridge server
 */
@OptIn(ExperimentalForeignApi::class)
actual class Connection private constructor() : AutoCloseable {
    private val _nativeConnection = nativeHeap.allocPointerTo<cnames.structs.Connection>()
    private val nativeConnection
        get() = _nativeConnection.value?.let { _nativeConnection }
            ?: throw IllegalStateException("Connection has been closed.")

    private var userVerifierInterface: UserVerifierInterface? = null
    internal fun getConnectionPtr() = nativeConnection.value

    actual companion object {
        /**
         * Connects to PrivMX Bridge server.
         *
         * @param userPrivKey user's private key
         * @param solutionId  ID of the Solution
         * @param bridgeUrl   PrivMX Bridge server URL
         * @param verificationOptions PrivMX Bridge server instance verification options using a PKI server
         * @return Connection object
         * @throws PrivmxException thrown when method encounters an exception
         * @throws NativeException thrown when method encounters an unknown exception
         */
        @Throws(PrivmxException::class, NativeException::class)
        actual fun connect(
            userPrivKey: String,
            solutionId: String,
            bridgeUrl: String,
            verificationOptions: PKIVerificationOptions?
        ): Connection =
            Connection().apply {
                memScoped {
                    val args = makeArgs(
                        userPrivKey.pson,
                        solutionId.pson,
                        bridgeUrl.pson,
                        verificationOptions?.pson ?: KPSON_NULL
                    )
                    val result = allocPointerTo<pson_value>().apply {
                        value = pson_new_object()
                    }
                    try {
                        privmx_endpoint_newConnection(_nativeConnection.ptr)
                        privmx_endpoint_execConnection(nativeConnection.value, 0, args, result.ptr)
                        PsonResponse(psonMapper(result.value!!) as PsonValue.PsonObject).getResultOrThrow()
                    } finally {
                        pson_free_value(args)
                        pson_free_result(result.value)
                    }
                }
            }

        /**
         * Connects to PrivMX Bridge server as a guest user.
         *
         * @param solutionId ID of the Solution
         * @param bridgeUrl  PrivMX Bridge server URL
         * @param verificationOptions PrivMX Bridge server instance verification options using a PKI server
         * @return Connection object
         * @throws PrivmxException thrown when method encounters an exception
         * @throws NativeException thrown when method encounters an unknown exception
         */
        @Throws(PrivmxException::class, NativeException::class)
        actual fun connectPublic(
            solutionId: String,
            bridgeUrl: String,
            verificationOptions: PKIVerificationOptions?
        ): Connection = Connection().apply {
            memScoped {
                val result = allocPointerTo<pson_value>()
                val args = makeArgs(
                    solutionId.pson,
                    bridgeUrl.pson,
                    verificationOptions?.pson ?: KPSON_NULL
                )
                try {
                    privmx_endpoint_newConnection(_nativeConnection.ptr)
                    privmx_endpoint_execConnection(nativeConnection.value, 1, args, result.ptr)
                    result.value!!.asResponse?.getResultOrThrow()
                } finally {
                    pson_free_result(result.value)
                    pson_free_value(args)
                }
            }
        }

        /**
         * Allows to set path to the SSL certificate file.
         *
         * @param certsPath path to file
         * @throws PrivmxException thrown when method encounters an exception
         * @throws NativeException thrown when method encounters an unknown exception
         */
        @Throws(PrivmxException::class, NativeException::class)
        actual fun setCertsPath(certsPath: String): Unit = memScoped {
            privmx_endpoint_setCertsPath(certsPath)
        }
    }

    /**
     * Gets a list of Contexts available for the user.
     *
     * @param skip      skip number of elements to skip from result
     * @param limit     limit of elements to return for query
     * @param sortOrder order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId    ID of the element from which query results should start
     * @param queryAsJson stringified JSON object with a custom field to filter result
     * @return list of Contexts
     * @throws IllegalStateException thrown when instance is not connected
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun listContexts(
        skip: Long, limit: Long, sortOrder: String, lastId: String?, queryAsJson: String?
    ): PagingList<Context> = memScoped {
        val args = makeArgs(
            mapOfWithNulls(
                "skip" to skip.pson,
                "limit" to limit.pson,
                "sortOrder" to sortOrder.pson,
                lastId?.let { "lastId" to lastId.pson },
                queryAsJson?.let { "queryAsJson" to queryAsJson.pson }
            ).pson
        )
        val result = allocPointerTo<pson_value>()
        try {
            privmx_endpoint_execConnection(nativeConnection.value, 3, args, result.ptr)
            val pagingList = result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            pagingList.toPagingList(PsonValue.PsonObject::toContext)
        } finally {
            pson_free_value(args)
            pson_free_result(result.value)
        }
    }

    /**
     * Disconnects from PrivMX Bridge server.
     *
     * @throws IllegalStateException thrown when instance is not connected or closed
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun disconnect() = memScoped {
        val args = pson_new_object()
        val result = allocPointerTo<pson_value>().apply {
            value = pson_new_object()
        }
        try {
            privmx_endpoint_execConnection(nativeConnection.value, 4, args, result.ptr)
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(result.value)
        }
    }

    /**
     * Gets the ID of the current connection.
     *
     * @return ID of the connection
     * @throws IllegalStateException thrown when instance is not connected or closed
     */
    @Throws(IllegalStateException::class)
    actual fun getConnectionId(): Long? = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execConnection(nativeConnection.value, 2, args, result.ptr)
            result.value!!.asResponse?.getResultOrThrow()?.typedValue()
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }

    private fun verifier(
        args: CPointer<pson_value>?,
        res: CPointer<CPointerVarOf<CPointer<pson_value>>>?
    ): Int = memScoped {
        try {
            var args_k = (args as PsonValue.PsonArray<*>)
                .getValue()
                .map { (it as PsonValue.PsonObject).toVerificationRequest() }

            var res_k = userVerifierInterface?.verify(args_k)
            var list = res_k?.map { it.pson } ?: emptyList()
            var res_c = allocArray<CPointerVar<pson_value>>(list.size)

            (0 until list.size).forEach { i -> res_c[i] = list[i].typedValue() }
            res?.pointed?.value = res_c.reinterpret()

            return@memScoped 0
        } catch (e: Exception) {
            return@memScoped 1
        }
    }

    @Throws(exceptionClasses = [PrivmxException::class, NativeException::class, IllegalStateException::class])
    actual fun setUserVerifier(userVerifier: UserVerifierInterface): Unit = memScoped {
        val result = allocPointerTo<pson_value>()
        userVerifierInterface = userVerifier

        try {
            privmx_endpoint_setUserVerifier(
                nativeConnection.value,
                staticCFunction(::verifier),
                res = result.ptr,
            )
        } finally {
            pson_free_result(result.value)
        }
    }

    @Throws(exceptionClasses = [PrivmxException::class, NativeException::class, IllegalStateException::class])
    actual fun getContextUsers(contextId: String): List<UserInfo> = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(contextId.pson)
        try {
            privmx_endpoint_execConnection(nativeConnection.value, 5, args, result.ptr)
            val contextUsersList =
                result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonArray<*>
            contextUsersList.getValue().map { (it as PsonValue.PsonObject).toUserInfo() }
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }

    /**
     * If there is an active connection then it
     * disconnects from PrivMX Bridge and frees memory making this instance not reusable.
     */
    actual override fun close() {
        if (_nativeConnection.value == null) return
        disconnect()
        privmx_endpoint_freeConnection(nativeConnection.value)
        _nativeConnection.value = null
    }
}