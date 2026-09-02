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

import com.simplito.kotlin.privmx_endpoint.model.LockLevel
import com.simplito.kotlin.privmx_endpoint.model.LockOperationResult
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection

/**
 * Provides distributed locking of arbitrary resources identified by a string ID.
 * Lock levels follow the SQLite locking model:
 * [LockLevel.NONE] < [LockLevel.SHARED] < [LockLevel.RESERVED] < [LockLevel.PENDING] < [LockLevel.EXCLUSIVE].
 *
 * @param connection active connection to PrivMX Bridge
 * @throws IllegalStateException when given [Connection] is not connected
 */
expect class LockApi
@Throws(IllegalStateException::class)
constructor(connection: Connection) : AutoCloseable {

    /**
     * Attempts to acquire a lock on a resource at the requested level.
     *
     * @param resourceId identifier of the resource to lock
     * @param uuid       caller-unique identifier used to track lock ownership
     * @param lockLevel  desired lock level ([LockLevel.SHARED], [LockLevel.RESERVED], [LockLevel.PENDING] or [LockLevel.EXCLUSIVE])
     * @return result indicating success and the current lock level held by the caller
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun lock(
        resourceId: String,
        uuid: String,
        lockLevel: LockLevel
    ): LockOperationResult

    /**
     * Releases or downgrades a lock held on a resource.
     *
     * @param resourceId identifier of the resource to unlock
     * @param uuid       caller-unique identifier matching the one used during lock acquisition
     * @param lockLevel  target level to downgrade to ([LockLevel.NONE] releases fully, [LockLevel.SHARED] keeps a reader lock)
     * @return result indicating success and the current lock level held by the caller
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun unlock(
        resourceId: String,
        uuid: String,
        lockLevel: LockLevel
    ): LockOperationResult

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
    fun checkReservedLock(
        resourceId: String,
        uuid: String
    ): Boolean

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed.
     */
    override fun close()
}
