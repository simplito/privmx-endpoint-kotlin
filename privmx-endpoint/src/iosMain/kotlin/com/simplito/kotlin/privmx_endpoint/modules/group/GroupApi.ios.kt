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

package com.simplito.kotlin.privmx_endpoint.modules.group

import cnames.structs.pson_value
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.DecryptedEnvelope
import com.simplito.kotlin.privmx_endpoint.model.DecryptedFileInfo
import com.simplito.kotlin.privmx_endpoint.model.Group
import com.simplito.kotlin.privmx_endpoint.model.GroupMemberToAdd
import com.simplito.kotlin.privmx_endpoint.model.GroupSummary
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.GroupEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.GroupEventType
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.utils.KPSON_NULL
import com.simplito.kotlin.privmx_endpoint.utils.PsonValue
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import com.simplito.kotlin.privmx_endpoint.utils.makeArgs
import com.simplito.kotlin.privmx_endpoint.utils.mapOfWithNulls
import com.simplito.kotlin.privmx_endpoint.utils.pson
import com.simplito.kotlin.privmx_endpoint.utils.toDecryptedEnvelope
import com.simplito.kotlin.privmx_endpoint.utils.toDecryptedFileInfo
import com.simplito.kotlin.privmx_endpoint.utils.toGroup
import com.simplito.kotlin.privmx_endpoint.utils.toGroupSummary
import com.simplito.kotlin.privmx_endpoint.utils.toPagingList
import com.simplito.kotlin.privmx_endpoint.utils.typedList
import com.simplito.kotlin.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execGroupApi
import libprivmxendpoint.privmx_endpoint_freeGroupApi
import libprivmxendpoint.privmx_endpoint_newGroupApi
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_free_value
import libprivmxendpoint.pson_new_array

/**
 * Manages PrivMX Bridge Groups and the content sealed for them.
 */
@OptIn(ExperimentalForeignApi::class)
actual class GroupApi actual constructor(connection: Connection) : AutoCloseable {

    private val _nativeGroupApi = nativeHeap.allocPointerTo<cnames.structs.GroupApi>()
    private val nativeGroupApi
        get() = _nativeGroupApi.value?.let { _nativeGroupApi }
            ?: throw IllegalStateException("GroupApi has been closed.")

    internal fun getGroupPtr() = nativeGroupApi.value

    init {
        privmx_endpoint_newGroupApi(connection.getConnectionPtr(), _nativeGroupApi.ptr)
        memScoped {
            val args = pson_new_array()
            val pson_result = allocPointerTo<pson_value>()
            try {
                privmx_endpoint_execGroupApi(
                    nativeGroupApi.value,
                    0,
                    args,
                    pson_result.ptr
                )
                pson_result.value!!.asResponse?.getResultOrThrow()
            } finally {
                pson_free_value(args)
                pson_free_result(pson_result.value)
            }
        }
    }

    /**
     * Creates a new Group whose key distribution is backed by a hidden key tree.
     *
     * Removing a member is proportional to the logarithm of the group size instead of to the group size, and
     * adding one does not advance the group's key epoch, so no container the group can read has to be re-keyed.
     *
     * @param contextId   ID of the Context to create the Group in
     * @param users       list of [UserWithPubKey] which indicates who will have access to the created Group
     * @param managers    list of [UserWithPubKey] which indicates who will have access (and management rights) to the created Group
     * @param publicMeta  public (unencrypted) metadata
     * @param privateMeta private (encrypted) metadata
     * @param policies    Group's policies
     * @return ID of the created Group
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun createGroup(
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
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                1,
                args,
                pson_result.ptr
            )
            pson_result.value?.asResponse?.getResultOrThrow()!!.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Adds members to a tree-backed Group, without advancing its key epoch.
     *
     * Incremental: only the newcomers are named. The resulting roster is derived from the Group's own verified
     * history, and its metadata carries through untouched.
     *
     * @param groupId    ID of the Group
     * @param newMembers the members to add, each with their public key and the role they take
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun addGroupMembers(groupId: String, newMembers: List<GroupMemberToAdd>) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            groupId.pson,
            newMembers.map { it.pson }.pson
        )
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                2,
                args,
                pson_result.ptr
            )
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Removes several members at once, advancing the key epoch **once**.
     *
     * Removing them one at a time advances the epoch per member; a batch costs one epoch, one set of archive
     * rungs and one metadata re-wrap however many members leave.
     *
     * @param groupId ID of the Group
     * @param userIds IDs of the members to remove
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun removeGroupMembers(groupId: String, userIds: List<String>) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            groupId.pson,
            userIds.map { it.pson }.pson
        )
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                3,
                args,
                pson_result.ptr
            )
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Updates a Group's public (unencrypted) metadata, and nothing else.
     *
     * Cannot touch the private metadata or the policies: the request has no field to carry them in. The
     * version checked is [Group.publicMetaVersion] alone, so a concurrent private-metadata write cannot
     * make this one lose.
     *
     * @param groupId    ID of the Group to update
     * @param publicMeta public (unencrypted) metadata
     * @param version    current [Group.publicMetaVersion] of the updated Group
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    actual fun updateGroupPublicMeta(
        groupId: String,
        publicMeta: ByteArray,
        version: Long
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            groupId.pson,
            publicMeta.pson,
            version.pson
        )
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                24,
                args,
                pson_result.ptr
            )
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Updates a Group's private (encrypted) metadata, and nothing else.
     *
     * The counterpart of [updateGroupPublicMeta], with its own version counter: the version checked is
     * [Group.privateMetaVersion] alone.
     *
     * @param groupId     ID of the Group to update
     * @param privateMeta private (encrypted) metadata
     * @param version     current [Group.privateMetaVersion] of the updated Group
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    actual fun updateGroupPrivateMeta(
        groupId: String,
        privateMeta: ByteArray,
        version: Long
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            groupId.pson,
            privateMeta.pson,
            version.pson
        )
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                25,
                args,
                pson_result.ptr
            )
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Sets a Group's policies, and nothing else.
     *
     * Takes no version and performs no version check, unlike the two metadata calls: the policies live
     * outside the Group's encrypted, signed metadata, so there is no counter to know and nothing to
     * re-verify. Two callers racing here means the later write wins.
     *
     * [policies] is required, not optional — on a call that does nothing else, an absent policy would be a
     * request that asks for nothing.
     *
     * @param groupId  ID of the Group to update
     * @param policies Group's policies
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    actual fun updateGroupPolicy(groupId: String, policies: ContainerPolicy) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            groupId.pson,
            policies.pson
        )
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                26,
                args,
                pson_result.ptr
            )
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Deletes a Group by given Group ID.
     *
     * @param groupId ID of the Group to delete
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun deleteGroup(groupId: String) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(groupId.pson)
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                5,
                args,
                pson_result.ptr
            )
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Gets a Group by given Group ID.
     *
     * @param groupId ID of the Group to get
     * @return object containing info about the Group
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getGroup(groupId: String): Group = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(groupId.pson)
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                6,
                args,
                pson_result.ptr
            )
            (pson_result.value?.asResponse?.getResultOrThrow()!! as PsonValue.PsonObject).toGroup()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Gets a list of Groups in given Context.
     *
     * The listing carries no per-Group metadata: a page holds identity, roster, epoch and policies only.
     * Call [getGroup] for a Group's `publicMeta`/`privateMeta` and for its verified status.
     *
     * @param contextId   ID of the Context to get the Groups from
     * @param skip        skip number of elements to skip from result
     * @param limit       limit of elements to return for query
     * @param sortOrder   order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId      ID of the element from which query results should start
     * @param queryAsJson stringified JSON object with a custom field to filter result
     * @param sortBy      field by elements are sorted in result
     * @return list of Group summaries
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun listGroups(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?,
        sortBy: String?
    ): PagingList<GroupSummary> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            contextId.pson,
            mapOfWithNulls(
                "skip" to skip.pson,
                "limit" to limit.pson,
                "sortOrder" to sortOrder.pson,
                lastId?.let { "lastId" to it.pson },
                queryAsJson?.let { "queryAsJson" to it.pson },
                sortBy?.let { "sortBy" to it.pson }
            ).pson
        )
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                7,
                args,
                pson_result.ptr
            )
            val pagingList =
                pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            pagingList.toPagingList(PsonValue.PsonObject::toGroupSummary)
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Seals content for a Group, as one of its members.
     *
     * The returned envelope is self-contained: it names the Group and the key version it was sealed under, so
     * any member can [decrypt] it later — including after the Group's key has rotated. It is signed with your
     * own key, so a reader also learns that you wrote it.
     *
     * Requires membership. To seal for a Group you are not in, use [encryptAnonymously].
     *
     * @param groupId ID of the Group to seal for
     * @param content data to encrypt
     * @return the envelope
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun encrypt(groupId: String, content: ByteArray): ByteArray = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            groupId.pson,
            content.pson
        )
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                11,
                args,
                pson_result.ptr
            )
            pson_result.value?.asResponse?.getResultOrThrow()!!.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Seals content for a Group without revealing, or proving, who sent it.
     *
     * Needs only public information — the Group's ID and its identity public key, both readable from
     * [Group.groupPubKey] — so it works whether or not you are a member, and makes no server call.
     *
     * You cannot read back what you sealed here. Only the Group can.
     *
     * @param groupId     ID of the Group to seal for
     * @param groupPubKey the Group's identity public key (base58-DER encoded)
     * @param content     data to encrypt
     * @return the envelope
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun encryptAnonymously(
        groupId: String,
        groupPubKey: String,
        content: ByteArray
    ): ByteArray = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            groupId.pson,
            groupPubKey.pson,
            content.pson
        )
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                13,
                args,
                pson_result.ptr
            )
            pson_result.value?.asResponse?.getResultOrThrow()!!.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Opens an envelope sealed by [encrypt] or by [encryptAnonymously].
     *
     * Which of the two it was is reported as [DecryptedEnvelope.type], and that decides what the result's
     * [DecryptedEnvelope.authorPubKey] is worth. Branch on the type, not on the field being non-empty.
     *
     * A file envelope is not accepted here — open one with [beginFileDecryption].
     *
     * @param envelope envelope to open
     * @return the content, and what could be established about its author
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun decrypt(envelope: ByteArray): DecryptedEnvelope = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(envelope.pson)
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                12,
                args,
                pson_result.ptr
            )
            (pson_result.value?.asResponse?.getResultOrThrow()!! as PsonValue.PsonObject)
                .toDecryptedEnvelope()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Begins sealing a file for a Group, as one of its members.
     *
     * Keep **both** outputs: the ciphertext returned by [encryptFileChunk] is the file, and the envelope
     * returned by [finishFileEncryption] is a small header without which nobody can open the ciphertext.
     *
     * The size is declared up front and enforced at [finishFileEncryption]: supplying less than you promised
     * is an error, because it cannot be told apart from a file cut short.
     *
     * A handle must not be driven from two threads at once.
     *
     * @param groupId ID of the Group to seal for
     * @param size    total size of the plaintext file, in bytes
     * @return handle to seal file data with
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun beginFileEncryption(groupId: String, size: Long): Long = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            groupId.pson,
            size.pson
        )
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                14,
                args,
                pson_result.ptr
            )
            pson_result.value?.asResponse?.getResultOrThrow()!!.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Begins sealing a file for a Group without revealing, or proving, who sent it.
     *
     * The file counterpart of [encryptAnonymously]. Driven exactly like [beginFileEncryption]. Needs public
     * information only — no membership, no server call — and the result is therefore unreadable to you
     * afterwards. Only the Group can open it.
     *
     * @param groupId     ID of the Group to seal for
     * @param groupPubKey the Group's identity public key (base58-DER encoded)
     * @param size        total size of the plaintext file, in bytes
     * @return handle to seal file data with
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun beginFileEncryptionAnonymously(
        groupId: String,
        groupPubKey: String,
        size: Long
    ): Long = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            groupId.pson,
            groupPubKey.pson,
            size.pson
        )
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                20,
                args,
                pson_result.ptr
            )
            pson_result.value?.asResponse?.getResultOrThrow()!!.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Seals the next piece of a file: takes plaintext, returns ciphertext.
     *
     * The return covers whichever internal chunks this call completed, so it is **empty whenever your block
     * did not finish one**, and larger than your block when it finished several. Neither is an error. Write
     * whatever comes back straight out, in the order it comes back.
     *
     * @param fileHandle handle from [beginFileEncryption] or [beginFileEncryptionAnonymously]
     * @param plainChunk plaintext to append, at most 4 MiB
     * @return ciphertext to store, possibly empty
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun encryptFileChunk(fileHandle: Long, plainChunk: ByteArray): ByteArray = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileHandle.pson,
            plainChunk.pson
        )
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                15,
                args,
                pson_result.ptr
            )
            pson_result.value?.asResponse?.getResultOrThrow()!!.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Finishes sealing a file and releases its handle.
     *
     * Returns the envelope — the one piece without which the ciphertext is unopenable. Store it.
     *
     * Throws if less plaintext arrived than was declared. The handle is released either way.
     *
     * @param fileHandle handle from [beginFileEncryption] or [beginFileEncryptionAnonymously]
     * @return the envelope, needed to read the file back
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun finishFileEncryption(fileHandle: Long): ByteArray = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(fileHandle.pson)
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                18,
                args,
                pson_result.ptr
            )
            pson_result.value?.asResponse?.getResultOrThrow()!!.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Begins opening a sealed file.
     *
     * Feed the ciphertext in the order it was produced. Block sizes need not match the ones used when sealing.
     * To read only part of a file rather than all of it, see [seekInEncryptedFile].
     *
     * @param envelope envelope returned by [finishFileEncryption]
     * @return handle to open file data with
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun beginFileDecryption(envelope: ByteArray): Long = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(envelope.pson)
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                16,
                args,
                pson_result.ptr
            )
            pson_result.value?.asResponse?.getResultOrThrow()!!.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Opens the next piece of a file: takes ciphertext, returns plaintext.
     *
     * Note the difference from [com.simplito.kotlin.privmx_endpoint.modules.store.StoreApi.readFromFile], which
     * fetches for you and takes a *length* — here the second argument is the data itself, and nothing is fetched.
     *
     * @param fileHandle  handle from [beginFileDecryption]
     * @param cipherChunk ciphertext to append, at most 4 MiB
     * @return plaintext, possibly empty
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun decryptFileChunk(fileHandle: Long, cipherChunk: ByteArray): ByteArray = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileHandle.pson,
            cipherChunk.pson
        )
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                17,
                args,
                pson_result.ptr
            )
            pson_result.value?.asResponse?.getResultOrThrow()!!.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Moves the read cursor to [position] in the plaintext, and returns where to resume feeding ciphertext.
     *
     * The offset you get back points at the start of the chunk *containing* [position], never at [position]
     * itself. The head of that chunk is then discarded for you, so what comes out of the next
     * [decryptFileChunk] still begins exactly at [position]; the tail may overshoot, so trim it yourself.
     *
     * Seeking gives up the truncation guarantee for this handle, and [DecryptedFileInfo.complete] comes back
     * false to say so. Everything you do read stays fully authenticated.
     *
     * @param fileHandle handle from [beginFileDecryption]
     * @param position   new cursor position in the plaintext, from 0 to the file size
     * @return ciphertext byte offset to resume feeding from
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun seekInEncryptedFile(fileHandle: Long, position: Long): Long = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            fileHandle.pson,
            position.pson
        )
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                21,
                args,
                pson_result.ptr
            )
            pson_result.value?.asResponse?.getResultOrThrow()!!.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Finishes opening a file, reports where it came from, and releases its handle.
     *
     * Call it even when you are sure you are done: this is the only point at which "the file ended early" can
     * be distinguished from "the file ended".
     *
     * Throws if less ciphertext arrived than the envelope declares — unless you seeked, in which case it
     * reports [DecryptedFileInfo.complete] as false instead of throwing. The handle is released either way.
     *
     * @param fileHandle handle from [beginFileDecryption]
     * @return which Group the file was sealed for, who — if anyone — is provably its author, and whether all
     * of it arrived
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun finishFileDecryption(fileHandle: Long): DecryptedFileInfo = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(fileHandle.pson)
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                19,
                args,
                pson_result.ptr
            )
            (pson_result.value?.asResponse?.getResultOrThrow()!! as PsonValue.PsonObject)
                .toDecryptedFileInfo()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Subscribe for the Group events on the given subscription query.
     *
     * @param subscriptionQueries list of queries
     * @return list of subscriptionIds in matching order to subscriptionQueries
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun subscribeFor(subscriptionQueries: List<String>): List<String> = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(subscriptionQueries.map { it.pson }.pson)
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                8,
                args,
                pson_result.ptr
            )
            val list = pson_result.value!!.asResponse?.getResultOrThrow()!!
            list.typedList().map { it.typedValue() }
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Unsubscribe from events with the given subscriptionId.
     *
     * @param subscriptionIds list of subscriptionId
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun unsubscribeFrom(subscriptionIds: List<String>) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(subscriptionIds.map { it.pson }.pson)
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                9,
                args,
                pson_result.ptr
            )
            pson_result.value!!.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Generate subscription Query for the Group events.
     *
     * @param eventType    type of event you listen for
     * @param selectorType scope on which you listen for events
     * @param selectorId   ID of the selector
     * @return Query for subscribing event
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun buildSubscriptionQuery(
        eventType: GroupEventType,
        selectorType: GroupEventSelectorType,
        selectorId: String
    ): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            eventType.ordinal.toLong().pson,
            selectorType.ordinal.toLong().pson,
            selectorId.pson
        )
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                10,
                args,
                pson_result.ptr
            )
            val query = pson_result.value!!.asResponse?.getResultOrThrow()!!
            query.typedValue()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Sends an ephemeral notification to the Group's members — "I am typing", "I moved the cursor",
     * "the call has started".
     *
     * The content is sealed with the Group's own key, exactly as [encrypt] would seal it, so one call
     * sends one request no matter how large the Group is. Members receive it as a `GroupCustomEvent`
     * with the payload already opened and the sender's signature already verified.
     *
     * A notification is not a record: whoever is not connected and subscribed at the time misses it.
     *
     * Requires membership. [eventData] is capped at about 11 KB after sealing and encoding.
     *
     * @param groupId     ID of the Group to notify
     * @param channelName name of the channel, chosen by you; recipients subscribe to it with
     *        [buildCustomEventSubscriptionQuery]. Must not contain '/', '|', ',' or '='.
     * @param eventData   payload to send
     * @param users       IDs of the members to reach; empty means every member
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    actual fun sendCustomEvent(
        groupId: String,
        channelName: String,
        eventData: ByteArray,
        users: List<String>
    ) = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            groupId.pson,
            channelName.pson,
            eventData.pson,
            users.map { it.pson }.pson
        )
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                22,
                args,
                pson_result.ptr
            )
            pson_result.value?.asResponse?.getResultOrThrow()
            Unit
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    /**
     * Generate subscription Query for custom notifications sent with [sendCustomEvent].
     *
     * @param channelName  name of the channel to listen on — the same name the sender passed
     * @param selectorType scope on which you listen for events
     * @param selectorId   ID of the selector
     * @return subscription query string
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    actual fun buildCustomEventSubscriptionQuery(
        channelName: String,
        selectorType: GroupEventSelectorType,
        selectorId: String
    ): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            channelName.pson,
            selectorType.ordinal.toLong().pson,
            selectorId.pson
        )
        try {
            privmx_endpoint_execGroupApi(
                nativeGroupApi.value,
                23,
                args,
                pson_result.ptr
            )
            val query = pson_result.value!!.asResponse?.getResultOrThrow()!!
            query.typedValue()
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
        privmx_endpoint_freeGroupApi(nativeGroupApi.value)
        _nativeGroupApi.value = null
    }
}
