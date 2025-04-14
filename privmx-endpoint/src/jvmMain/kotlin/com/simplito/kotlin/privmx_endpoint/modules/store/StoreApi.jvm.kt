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

package com.simplito.kotlin.privmx_endpoint.modules.store

import com.simplito.kotlin.privmx_endpoint.LibLoader
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.File
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.Store
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection

actual class StoreApi actual constructor(connection: Connection) : AutoCloseable {
    companion object {
        init {
            LibLoader.load()
        }
    }

    private var api: Long? = null

    init {
        api = init(connection)
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    @JvmOverloads
    actual external fun createStore(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy?
    ): String

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    @JvmOverloads
    actual external fun updateStore(
        storeId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        version: Long,
        force: Boolean,
        forceGenerateNewKey: Boolean,
        policies: ContainerPolicy?
    )

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual external fun getStore(storeId: String): Store

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    @JvmOverloads
    actual external fun listStores(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?
    ): PagingList<Store>

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual external fun deleteStore(storeId: String)

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual external fun createFile(
        storeId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        size: Long
    ): Long?

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual external fun updateFile(
        fileId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        size: Long
    ): Long?

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual external fun updateFileMeta(
        fileId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray
    )

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual external fun writeToFile(fileHandle: Long, dataChunk: ByteArray)

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual external fun deleteFile(fileId: String)

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual external fun getFile(fileId: String): File

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    @JvmOverloads
    actual external fun listFiles(
        storeId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?
    ): PagingList<File>

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual external fun openFile(fileId: String): Long?

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual external fun readFromFile(fileHandle: Long, length: Long): ByteArray

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual external fun seekInFile(fileHandle: Long, position: Long)

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual external fun closeFile(fileHandle: Long): String

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual external fun subscribeForStoreEvents()

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual external fun unsubscribeFromStoreEvents()


    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual external fun subscribeForFileEvents(storeId: String)

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual external fun unsubscribeFromFileEvents(storeId: String)

    @Throws(IllegalStateException::class)
    private external fun init(connection: Connection): Long?

    @Throws(IllegalStateException::class)
    private external fun deinit()

    actual override fun close() {
        deinit()
    }
}