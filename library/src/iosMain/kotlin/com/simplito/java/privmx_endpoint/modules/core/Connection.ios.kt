package com.simplito.java.privmx_endpoint.modules.core

import com.simplito.java.privmx_endpoint.model.Context
import com.simplito.java.privmx_endpoint.model.PagingList
import com.simplito.java.privmx_endpoint.model.exceptions.NativeException
import com.simplito.java.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.java.privmx_endpoint.utils.PsonResponse
import com.simplito.java.privmx_endpoint.utils.PsonValue
import com.simplito.java.privmx_endpoint.utils.asResponse
import com.simplito.java.privmx_endpoint.utils.makeArgs
import com.simplito.java.privmx_endpoint.utils.pson
import com.simplito.java.privmx_endpoint.utils.psonMapper
import com.simplito.java.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execConnection
import libprivmxendpoint.privmx_endpoint_freeConnection
import libprivmxendpoint.privmx_endpoint_newConnection
import libprivmxendpoint.privmx_endpoint_setCertsPath
import libprivmxendpoint.pson_add_array_value
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_free_value
import libprivmxendpoint.pson_new_array
import libprivmxendpoint.pson_new_int64
import libprivmxendpoint.pson_new_object
import libprivmxendpoint.pson_new_string
import libprivmxendpoint.pson_set_object_value
import libprivmxendpoint.pson_value

@OptIn(ExperimentalForeignApi::class)
actual class Connection() : AutoCloseable {
    private val nativeConnection = nativeHeap.allocPointerTo<libprivmxendpoint.Connection>()

    internal fun getConnectionPtr() = nativeConnection.value

    actual companion object {
        @Throws(PrivmxException::class, NativeException::class)
        actual fun connect(userPrivKey: String, solutionId: String, bridgeUrl: String): Connection =
            Connection().apply {
                memScoped {
                    val args = pson_new_array()
                    val result = allocPointerTo<pson_value>().apply {
                        value = pson_new_object()
                    }
                    try {
                        pson_add_array_value(args, pson_new_string(userPrivKey))
                        pson_add_array_value(args, pson_new_string(solutionId))
                        pson_add_array_value(args, pson_new_string(bridgeUrl))
                        privmx_endpoint_newConnection(nativeConnection.ptr)
                        privmx_endpoint_execConnection(nativeConnection.value, 0, args, result.ptr)
                        PsonResponse(psonMapper(result.value!!) as PsonValue.PsonObject).getResultOrThrow()
                    } finally {
                        pson_free_value(args)
                        pson_free_result(result.value)
                    }
                }
            }

        @Throws(PrivmxException::class, NativeException::class)
        actual fun connectPublic(
            solutionId: String,
            bridgeUrl: String,
        ): Connection = Connection().apply {
            memScoped {
                val result = allocPointerTo<pson_value>()
                val args = makeArgs(solutionId.pson, bridgeUrl.pson)
                try {
                    privmx_endpoint_newConnection(nativeConnection.ptr)
                    privmx_endpoint_execConnection(nativeConnection.value, 1, args, result.ptr)
                    result.value!!.asResponse?.getResultOrThrow()
                } finally {
                    pson_free_result(result.value)
                    pson_free_value(args)
                }
            }
        }

        @Throws(PrivmxException::class, NativeException::class)
        actual fun setCertsPath(certsPath: String?): Unit = memScoped {
            privmx_endpoint_setCertsPath(certsPath)
        }
    }

    @Throws(IllegalStateException::class, PrivmxException::class, NativeException::class)
    actual fun listContexts(
        skip: Long, limit: Long, sortOrder: String, lastId: String?
    ): PagingList<Context> = memScoped {
        val args = pson_new_array()
        val result = allocPointerTo<pson_value>()
        val pagingQuery = pson_new_object()
        try {
            pson_set_object_value(pagingQuery, "skip", pson_new_int64(skip))
            pson_set_object_value(pagingQuery, "limit", pson_new_int64(limit))
            pson_set_object_value(pagingQuery, "sortOrder", pson_new_string(sortOrder))
            lastId?.let {
                pson_set_object_value(pagingQuery, "lastId", pson_new_string(lastId))
            }
            pson_add_array_value(args, pagingQuery)

            privmx_endpoint_execConnection(nativeConnection.value, 3, args, result.ptr)
            return PsonResponse(psonMapper(result.value!!) as PsonValue.PsonObject).getResultOrThrow()
                .let { result ->
                    (result as PsonValue.PsonObject).let { pagingList ->
                        PagingList(
                            (pagingList["totalAvailable"] as PsonValue.PsonLong).getValue(),
                            (pagingList["readItems"] as PsonValue.PsonArray<PsonValue.PsonObject>).getValue()
                                .map { context ->
                                    Context(
                                        (context["userId"] as PsonValue.PsonString).getValue(),
                                        (context["contextId"] as PsonValue.PsonString).getValue(),
                                    )
                                }
                        )
                    }
                }
        } finally {
            pson_free_value(args)
            pson_free_result(result.value)
            pson_free_value(pagingQuery)
        }
    }

    @Throws(IllegalStateException::class, PrivmxException::class, NativeException::class)
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

    actual override fun close() {
        try {
            disconnect()
        } finally {
            privmx_endpoint_freeConnection(nativeConnection.value)
        }
    }
}
