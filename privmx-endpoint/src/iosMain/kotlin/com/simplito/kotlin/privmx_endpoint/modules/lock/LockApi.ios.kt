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

package com.simplito.kotlin.privmx_endpoint.modules.lock

import cnames.structs.pson_value
import com.simplito.kotlin.privmx_endpoint.model.LockLevel
import com.simplito.kotlin.privmx_endpoint.model.LockOperationResult
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.utils.PsonValue
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import com.simplito.kotlin.privmx_endpoint.utils.makeArgs
import com.simplito.kotlin.privmx_endpoint.utils.pson
import com.simplito.kotlin.privmx_endpoint.utils.toLockOperationResult
import com.simplito.kotlin.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execLockApi
import libprivmxendpoint.privmx_endpoint_freeLockApi
import libprivmxendpoint.privmx_endpoint_newLockApi
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_free_value
import libprivmxendpoint.pson_new_array

/**
 * Provides distributed locking of arbitrary resources identified by a string ID.
 */
@OptIn(ExperimentalForeignApi::class)
actual class LockApi
@Throws(IllegalStateException::class)
actual constructor(connection: Connection) : AutoCloseable {
    private val _nativeLockApi = nativeHeap.allocPointerTo<cnames.structs.LockApi>()
    private val nativeLockApi
        get() = _nativeLockApi.value?.let { _nativeLockApi }
            ?: throw IllegalStateException("LockApi has been closed.")

    internal fun getLockPtr() = nativeLockApi.value

    init {
        privmx_endpoint_newLockApi(connection.getConnectionPtr(), _nativeLockApi.ptr)
        memScoped {
            val args = pson_new_array()
            val pson_result = allocPointerTo<pson_value>()
            try {
                privmx_endpoint_execLockApi(nativeLockApi.value, 0, args, pson_result.ptr)
                pson_result.value!!.asResponse?.getResultOrThrow()
            } finally {
                pson_free_value(args)
                pson_free_result(pson_result.value)
            }
        }
    }

    /**
     * Attempts to acquire a lock on a resource at the requested level.
     *
     * @param resourceId identifier of the resource to lock
     * @param uuid       caller-unique identifier used to track lock ownership
     * @param lockLevel  desired lock level
     * @return result indicating success and the current lock level held by the caller
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun lock(
        resourceId: String,
        uuid: String,
        lockLevel: LockLevel
    ): LockOperationResult = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            resourceId.pson,
            uuid.pson,
            lockLevel.pson
        )
        try {
            privmx_endpoint_execLockApi(nativeLockApi.value, 1, args, pson_result.ptr)
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toLockOperationResult()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Releases or downgrades a lock held on a resource.
     *
     * @param resourceId identifier of the resource to unlock
     * @param uuid       caller-unique identifier matching the one used during lock acquisition
     * @param lockLevel  target level to downgrade to
     * @return result indicating success and the current lock level held by the caller
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun unlock(
        resourceId: String,
        uuid: String,
        lockLevel: LockLevel
    ): LockOperationResult = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            resourceId.pson,
            uuid.pson,
            lockLevel.pson
        )
        try {
            privmx_endpoint_execLockApi(nativeLockApi.value, 2, args, pson_result.ptr)
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toLockOperationResult()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Checks whether any connection (including the caller) holds a [LockLevel.RESERVED] or higher lock on the resource.
     *
     * @param resourceId identifier of the resource to check
     * @param uuid       caller-unique identifier
     * @return 'true' if a [LockLevel.RESERVED] or higher lock exists, 'false' otherwise
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun checkReservedLock(
        resourceId: String,
        uuid: String
    ): Boolean = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            resourceId.pson,
            uuid.pson
        )
        try {
            privmx_endpoint_execLockApi(nativeLockApi.value, 3, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()!!.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed.
     */
    actual override fun close() {
        privmx_endpoint_freeLockApi(nativeLockApi.value)
        _nativeLockApi.value = null
    }
}
