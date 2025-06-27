package com.simplito.kotlin.privmx_endpoint.modules.kvdb

import cnames.structs.pson_value
import com.simplito.kotlin.privmx_endpoint.model.Kvdb
import com.simplito.kotlin.privmx_endpoint.model.KvdbEntry
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.PagingList
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
import com.simplito.kotlin.privmx_endpoint.utils.toPagingList
import com.simplito.kotlin.privmx_endpoint.utils.toKvdb
import com.simplito.kotlin.privmx_endpoint.utils.toKvdbEntry
import com.simplito.kotlin.privmx_endpoint.utils.toMap
import com.simplito.kotlin.privmx_endpoint.utils.toValuePagingList
import com.simplito.kotlin.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execKvdbApi
import libprivmxendpoint.privmx_endpoint_freeKvdbApi
import libprivmxendpoint.privmx_endpoint_newKvdbApi
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_free_value
import libprivmxendpoint.pson_new_array

/**
 * Manages PrivMX Bridge  Kvdbs and their messages.
 *
 * @category kvdb
 */
@OptIn(ExperimentalForeignApi::class)
actual class KvdbApi
actual constructor(connection: Connection) : AutoCloseable {
    private val _nativeKvdbApi = nativeHeap.allocPointerTo<cnames.structs.KvdbApi>()
    private val nativeKvdbApi
        get() = _nativeKvdbApi.value?.let { _nativeKvdbApi }
            ?: throw IllegalStateException("StoreApi has been closed.")

    internal fun getKvdbPtr() = nativeKvdbApi.value

    init {
        privmx_endpoint_newKvdbApi(connection.getConnectionPtr(), _nativeKvdbApi.ptr)
        memScoped {
            val args = pson_new_array()
            val pson_result = allocPointerTo<pson_value>()
            try {
                privmx_endpoint_execKvdbApi(nativeKvdbApi.value, 0, args, pson_result.ptr)
                pson_result.value!!.asResponse?.getResultOrThrow()
            } finally {
                pson_free_value(args)
                pson_free_result(pson_result.value)
            }
        }
    }

    /**
     * Creates a new KVDB in given Context.
     *
     * @param contextId   ID of the Context to create the KVDB in
     * @param users       list of [UserWithPubKey] which indicates who will have access to the created KVDB
     * @param managers    list of [UserWithPubKey] which indicates who will have access (and management rights) to the created KVDB
     * @param publicMeta  public (unencrypted) metadata
     * @param privateMeta private (encrypted) metadata
     * @param policies    KVDB's policies
     * @return ID of the created KVDB
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun createKvdb(
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
            policies?.pson ?: KPSON_NULL
        )
        try {
            privmx_endpoint_execKvdbApi(nativeKvdbApi.value, 1, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()!!.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }


    /**
     * Updates an existing KVDB.
     *
     * @param kvdbId              ID of the KVDB to update
     * @param users               list of [UserWithPubKey] which indicates who will have access to the created KVDB
     * @param managers            list of [UserWithPubKey] which indicates who will have access (and management rights) to the created KVDB
     * @param publicMeta          public (unencrypted) metadata
     * @param privateMeta         private (encrypted) metadata
     * @param version             current version of the updated KVDB
     * @param force               force update (without checking version)
     * @param forceGenerateNewKey force to regenerate a key for the KVDB
     * @param policies            KVDB's policies
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(exceptionClasses = [PrivmxException::class, NativeException::class, IllegalStateException::class])
    actual fun updateKvdb(
        kvdbId: String,
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
            kvdbId.pson,
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
            privmx_endpoint_execKvdbApi(nativeKvdbApi.value, 2, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }


    /**
     * Deletes a KVDB by given KVDB ID.
     *
     * @param kvdbId ID of the KVDB to delete
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun deleteKvdb(kvdbId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(kvdbId.pson)
        try {
            privmx_endpoint_execKvdbApi(nativeKvdbApi.value, 3, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Gets a KVDB by given KVDB ID.
     *
     * @param kvdbId ID of KVDB to get
     * @return object containing info about the KVDB
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getKvdb(kvdbId: String): Kvdb = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(kvdbId.pson)
        try {
            privmx_endpoint_execKvdbApi(nativeKvdbApi.value, 4, args, pson_result.ptr)
            pson_result.value?.asResponse?.getResultOrThrow()!!.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Gets a list of Kvdbs in given Context.
     *
     * @param contextId   ID of the Context to get the Kvdbs from
     * @param skip        skip number of elements to skip from result
     * @param limit       limit of elements to return for query
     * @param sortOrder   order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId      ID of the element from which query results should start
     * @param queryAsJson stringified JSON object with a custom field to filter result
     * @param sortBy      field by elements are sorted in result
     * @return list of Kvdbs
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun listKvdbs(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?,
        sortBy: String?
    ): PagingList<Kvdb> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId.pson,
            mapOfWithNulls(
                "skip" to skip.pson,
                "limit" to limit.pson,
                "sortOrder" to sortOrder.pson,
                lastId?.let { "lastId" to lastId.pson },
                queryAsJson?.let { "queryAsJson" to queryAsJson.pson },
                sortBy?.let { "sortBy" to sortBy.pson }
            ).pson
        )
        try {
            privmx_endpoint_execKvdbApi(nativeKvdbApi.value, 5, args, pson_result.ptr)
            val pagingList =
                pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            pagingList.toPagingList(PsonValue.PsonObject::toKvdb)
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }


    /**
     * Gets a KVDB entry by given KVDB entry key and KVDB ID.
     *
     * @param kvdbId KVDB ID of the KVDB entry to get
     * @param key    key of the KVDB entry to get
     * @return object containing the KVDB entry
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getEntry(
        kvdbId: String,
        key: String
    ): KvdbEntry = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            kvdbId.pson,
            key.pson
        )
        try {
            privmx_endpoint_execKvdbApi(nativeKvdbApi.value, 6, args, pson_result.ptr)
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toKvdbEntry()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Gets a list of KVDB entries keys from a KVDB.
     *
     * @param kvdbId      ID of the KVDB to list KVDB entries from
     * @param skip        skip number of elements to skip from result
     * @param limit       limit of elements to return for query
     * @param sortOrder   order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId      ID of the element from which query results should start
     * @param queryAsJson stringified JSON object with a custom field to filter result
     * @param sortBy      field by elements are sorted in result
     * @return list of KVDB entries
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun listEntriesKeys(
        kvdbId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?,
        sortBy: String?
    ): PagingList<String> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            kvdbId.pson,
            mapOfWithNulls(
                "skip" to skip.pson,
                "limit" to limit.pson,
                "sortOrder" to sortOrder.pson,
                lastId?.let { "lastId" to lastId.pson },
                queryAsJson?.let { "queryAsJson" to queryAsJson.pson },
                sortBy?.let { "sortBy" to sortBy.pson }
            ).pson
        )
        try {
            privmx_endpoint_execKvdbApi(nativeKvdbApi.value, 7, args, pson_result.ptr)
            val pagingList =
                pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            pagingList.toValuePagingList()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Gets a list of KVDB entries from a KVDB.
     *
     * @param kvdbId      ID of the KVDB to list KVDB entries from
     * @param skip        skip number of elements to skip from result
     * @param limit       limit of elements to return for query
     * @param sortOrder   order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId      ID of the element from which query results should start
     * @param queryAsJson stringified JSON object with a custom field to filter result
     * @param sortBy      field by elements are sorted in result
     * @return list of KVDB entries
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun listEntries(
        kvdbId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?,
        sortBy: String?
    ): PagingList<KvdbEntry> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            kvdbId.pson,
            mapOfWithNulls(
                "skip" to skip.pson,
                "limit" to limit.pson,
                "sortOrder" to sortOrder.pson,
                lastId?.let { "lastId" to lastId.pson },
                queryAsJson?.let { "queryAsJson" to queryAsJson.pson },
                sortBy?.let { "sortBy" to sortBy.pson }
            ).pson
        )
        try {
            privmx_endpoint_execKvdbApi(nativeKvdbApi.value, 8, args, pson_result.ptr)
            val pagingList =
                pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            pagingList.toPagingList(PsonValue.PsonObject::toKvdbEntry)
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }


    /**
     * Sets a KVDB entry in the given KVDB.
     *
     * @param kvdbId      ID of the KVDB to set the entry to
     * @param key         KVDB entry key
     * @param publicMeta  public KVDB entry metadata
     * @param privateMeta private KVDB entry metadata
     * @param data        content of the KVDB entry
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun setEntry(
        kvdbId: String,
        key: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        data: ByteArray,
        version: Long
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            kvdbId.pson,
            key.pson,
            publicMeta.pson,
            privateMeta.pson,
            data.pson,
            version.pson
        )
        try {
            privmx_endpoint_execKvdbApi(nativeKvdbApi.value, 9, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Deletes a KVDB entry by given KVDB entry ID.
     *
     * @param kvdbId KVDB ID of the KVDB entry to delete
     * @param key    key of the KVDB entry to delete
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun deleteEntry(
        kvdbId: String,
        key: String
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            kvdbId.pson,
            key.pson
        )
        try {
            privmx_endpoint_execKvdbApi(nativeKvdbApi.value, 10, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Deletes KVDB entries by given KVDB IDs and the list of entry keys.
     *
     * @param kvdbId ID of the KVDB database to delete from
     * @param keys   vector of the keys of the KVDB entries to delete
     * @return map with the statuses of deletion for every key
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun deleteEntries(
        kvdbId: String,
        keys: List<String>
    ): Map<String, Boolean> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            kvdbId.pson,
            keys.map { it.pson }.pson
        )
        try {
            privmx_endpoint_execKvdbApi(nativeKvdbApi.value, 11, args, pson_result.ptr)
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            println(result.getDebugString())
            result
            println(result)
            result.toMap()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
//        TODO("Implement this function")
    }

    /**
     * Subscribes for the KVDB module main events.
     *
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun subscribeForKvdbEvents() = memScoped{
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execKvdbApi(nativeKvdbApi.value, 12, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Unsubscribes from the KVDB module main events.
     *
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun unsubscribeFromKvdbEvents() = memScoped{
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execKvdbApi(nativeKvdbApi.value, 13, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Subscribes for events in given KVDB.
     *
     * @param kvdbId ID of the KVDB to subscribe
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun subscribeForEntryEvents(kvdbId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            kvdbId.pson
        )
        try {
            privmx_endpoint_execKvdbApi(nativeKvdbApi.value, 14, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Unsubscribes from events in given KVDB.
     *
     * @param kvdbId ID of the KVDB to unsubscribe
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun unsubscribeFromEntryEvents(kvdbId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            kvdbId.pson
        )
        try {
            privmx_endpoint_execKvdbApi(nativeKvdbApi.value, 15, args, pson_result.ptr)
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    //TODO: Implement "hasEntry"

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed.
     */
    actual override fun close() {
        privmx_endpoint_freeKvdbApi(nativeKvdbApi.value)
        _nativeKvdbApi.value = null
    }
}