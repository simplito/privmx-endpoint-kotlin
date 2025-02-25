package com.simplito.java.privmx_endpoint.modules.core

import com.simplito.java.privmx_endpoint.model.Context
import com.simplito.java.privmx_endpoint.model.PagingList
import com.simplito.java.privmx_endpoint.utils.PsonValue
import com.simplito.java.privmx_endpoint.utils.psonMapper
import com.simplito.java.privmx_endpoint.utils.PsonResponse
import kotlinx.cinterop.*
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.*

@OptIn(ExperimentalForeignApi::class)
actual class Connection() : AutoCloseable {
    private val nativeConnection = nativeHeap.allocPointerTo<libprivmxendpoint.Connection>()

    internal fun getConnectionPtr() = nativeConnection.value

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
        lastId?.let {
            pson_set_object_value(pagingQuery, "lastId", pson_new_string(lastId))
        }
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
        privmx_endpoint_freeConnection(nativeConnection.value)
    }
}
