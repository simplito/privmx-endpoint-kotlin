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

import cnames.structs.pson_value
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.File
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.Store
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.utils.KPSON_NULL
import com.simplito.kotlin.privmx_endpoint.utils.PsonValue
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import com.simplito.kotlin.privmx_endpoint.utils.makeArgs
import com.simplito.kotlin.privmx_endpoint.utils.mapOfWithNulls
import com.simplito.kotlin.privmx_endpoint.utils.pson
import com.simplito.kotlin.privmx_endpoint.utils.toFile
import com.simplito.kotlin.privmx_endpoint.utils.toPagingList
import com.simplito.kotlin.privmx_endpoint.utils.toStore
import com.simplito.kotlin.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execStoreApi
import libprivmxendpoint.privmx_endpoint_freeStoreApi
import libprivmxendpoint.privmx_endpoint_newStoreApi
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_free_value
import libprivmxendpoint.pson_new_array

@OptIn(ExperimentalForeignApi::class)
actual class StoreApi actual constructor(connection: Connection) :
    AutoCloseable {
    private val _nativeStoreApi = nativeHeap.allocPointerTo<cnames.structs.StoreApi>()
    private val nativeStoreApi
        get() = _nativeStoreApi.value?.let { _nativeStoreApi }
            ?: throw IllegalStateException("StoreApi has been closed.")

    internal fun getStorePtr() = nativeStoreApi.value

    init {
        privmx_endpoint_newStoreApi(connection.getConnectionPtr(), _nativeStoreApi.ptr)
        memScoped {
            val args = pson_new_array()
            val pson_result = allocPointerTo<pson_value>()
            try{
                privmx_endpoint_execStoreApi(nativeStoreApi.value, 0, args, pson_result.ptr)
                pson_result.value!!.asResponse?.getResultOrThrow()
            } finally {
                pson_free_value(args)
                pson_free_result(pson_result.value)
            }
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun createStore(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy?
    ): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId.pson,
            users.map { it.pson }.pson,
            managers.map { it.pson }.pson,
            publicMeta.pson,
            privateMeta.pson,
            policies?.pson
        )
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 1, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun updateStore(
        storeId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        version: Long,
        force: Boolean,
        forceGenerateNewKey: Boolean,
        policies: ContainerPolicy?
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            storeId.pson,
            users.map { it.pson }.pson,
            managers.map { it.pson }.pson,
            publicMeta.pson,
            privateMeta.pson,
            version.pson,
            force.pson,
            forceGenerateNewKey.pson,
            policies?.pson ?: KPSON_NULL
        )
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 2, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun getStore(storeId: String): Store = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(storeId.pson)
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 4, args, pson_result.ptr)
            val result = pson_result.value?.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toStore()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun listStores(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?
    ): PagingList<Store> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId.pson,
            mapOfWithNulls(
                "skip" to skip.pson,
                "limit" to limit.pson,
                "sortOrder" to sortOrder.pson,
                lastId?.let { "lastId" to lastId.pson }
            ).pson
        )
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 5, args, pson_result.ptr)
            val pagingList =
                pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            pagingList.toPagingList(PsonValue.PsonObject::toStore)
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun deleteStore(storeId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(storeId.pson)
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 3, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun createFile(
        storeId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        size: Long
    ): Long? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            storeId.pson,
            publicMeta.pson,
            privateMeta.pson,
            size.pson
        )
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 6, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()?.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun updateFile(
        fileId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        size: Long
    ): Long? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileId.pson,
            publicMeta.pson,
            privateMeta.pson,
            size.pson
        )
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 7, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()?.typedValue<Long>()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun updateFileMeta(
        fileId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileId.pson,
            publicMeta.pson,
            privateMeta.pson,
        )
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 8, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun writeToFile(fileHandle: Long, dataChunk: ByteArray) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileHandle.pson,
            dataChunk.pson
        )
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 9, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun deleteFile(fileId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileId.pson
        )
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 10, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun getFile(fileId: String): File = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileId.pson
        )
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 11, args, pson_result.ptr)
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toFile()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun listFiles(
        storeId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?
    ): PagingList<File> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            storeId.pson,
            mapOfWithNulls(
                "skip" to skip.pson,
                "limit" to limit.pson,
                "sortOrder" to sortOrder.pson,
                lastId?.let { "lastId" to lastId.pson }
            ).pson
        )
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 12, args, pson_result.ptr)
            val pagingList =
                pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            pagingList.toPagingList(PsonValue.PsonObject::toFile)
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun openFile(fileId: String): Long? = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileId.pson
        )
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 13, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun readFromFile(fileHandle: Long, length: Long): ByteArray = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileHandle.pson,
            length.pson
        )
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 14, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun seekInFile(fileHandle: Long, position: Long) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileHandle.pson,
            position.pson
        )
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 15, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun closeFile(fileHandle: Long): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileHandle.pson,
        )
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 16, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun subscribeForStoreEvents() = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 17, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun unsubscribeFromStoreEvents() = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 18, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun subscribeForFileEvents(storeId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            storeId.pson
        )
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 19, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    actual fun unsubscribeFromFileEvents(storeId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            storeId.pson
        )
        try {
            privmx_endpoint_execStoreApi(nativeStoreApi.value, 20, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    actual override fun close() {
        if (_nativeStoreApi.value == null) return
        privmx_endpoint_freeStoreApi(_nativeStoreApi.value)
        _nativeStoreApi.value = null
    }
}