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

/**
 * Manages PrivMX Bridge Groups and the content sealed for them.
 *
 * **Not implemented on Apple targets yet.** The shipped `privmx-endpoint.xcframework` carries no Group
 * C interface (`privmx/endpoint/group/cinterface/group.h`) and no Group symbols, so there is nothing to bind
 * to. Every member throws [NotImplementedError].
 *
 * The native ABI is already fixed, so filling this in is mechanical once the framework is rebuilt from a
 * source tree that includes the Group module: `privmx_endpoint_newGroupApi` / `privmx_endpoint_execGroupApi`,
 * with `GroupApiVarInterface::METHOD` ordinals — Create=0, CreateGroup=1, AddGroupMembers=2,
 * RemoveGroupMembers=3, UpdateGroup=4, DeleteGroup=5, GetGroup=6, ListGroups=7, SubscribeFor=8,
 * UnsubscribeFrom=9, BuildSubscriptionQuery=10, Encrypt=11, Decrypt=12, EncryptAnonymously=13,
 * BeginFileEncryption=14, EncryptFileChunk=15, BeginFileDecryption=16, DecryptFileChunk=17,
 * FinishFileEncryption=18, FinishFileDecryption=19, BeginFileEncryptionAnonymously=20,
 * SeekInEncryptedFile=21.
 */
actual class GroupApi actual constructor(connection: Connection) : AutoCloseable {
    /**
     * Creates a new Group whose key distribution is backed by a hidden key tree.
     *
     * @param contextId   ID of the Context to create the Group in
     * @param users       list of [UserWithPubKey] which indicates who will have access to the created Group
     * @param managers    list of [UserWithPubKey] which indicates who will have access (and management rights) to the created Group
     * @param publicMeta  public (unencrypted) metadata
     * @param privateMeta private (encrypted) metadata
     * @param policies    Group's policies
     * @return ID of the created Group
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun createGroup(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy?
    ): String = TODO("Not implemented")

    /**
     * Adds members to a tree-backed Group, without advancing its key epoch.
     *
     * @param groupId    ID of the Group
     * @param newMembers the members to add, each with their public key and the role they take
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun addGroupMembers(groupId: String, newMembers: List<GroupMemberToAdd>): Unit =
        TODO("Not implemented")

    /**
     * Removes several members at once, advancing the key epoch once.
     *
     * @param groupId ID of the Group
     * @param userIds IDs of the members to remove
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun removeGroupMembers(groupId: String, userIds: List<String>): Unit =
        TODO("Not implemented")

    /**
     * Updates an existing Group's metadata.
     *
     * @param groupId     ID of the Group to update
     * @param publicMeta  public (unencrypted) metadata
     * @param privateMeta private (encrypted) metadata
     * @param version     current version of the updated Group
     * @param policies    Group's policies
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun updateGroup(
        groupId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        version: Long,
        policies: ContainerPolicy?
    ): Unit = TODO("Not implemented")

    /**
     * Deletes a Group by given Group ID.
     *
     * @param groupId ID of the Group to delete
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun deleteGroup(groupId: String): Unit = TODO("Not implemented")

    /**
     * Gets a Group by given Group ID.
     *
     * @param groupId ID of the Group to get
     * @return object containing info about the Group
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getGroup(groupId: String): Group = TODO("Not implemented")

    /**
     * Gets a list of Groups in given Context.
     *
     * @param contextId   ID of the Context to get the Groups from
     * @param skip        skip number of elements to skip from result
     * @param limit       limit of elements to return for query
     * @param sortOrder   order of elements in result ("asc" for ascending, "desc" for descending)
     * @param lastId      ID of the element from which query results should start
     * @param queryAsJson stringified JSON object with a custom field to filter result
     * @param sortBy      field by elements are sorted in result
     * @return list of Group summaries
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
    ): PagingList<GroupSummary> = TODO("Not implemented")

    /**
     * Seals content for a Group, as one of its members.
     *
     * @param groupId ID of the Group to seal for
     * @param content data to encrypt
     * @return the envelope
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun encrypt(groupId: String, content: ByteArray): ByteArray = TODO("Not implemented")

    /**
     * Seals content for a Group without revealing, or proving, who sent it.
     *
     * @param groupId     ID of the Group to seal for
     * @param groupPubKey the Group's identity public key (base58-DER encoded)
     * @param content     data to encrypt
     * @return the envelope
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun encryptAnonymously(
        groupId: String,
        groupPubKey: String,
        content: ByteArray
    ): ByteArray = TODO("Not implemented")

    /**
     * Opens an envelope sealed by [encrypt] or by [encryptAnonymously].
     *
     * @param envelope envelope to open
     * @return the content, and what could be established about its author
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun decrypt(envelope: ByteArray): DecryptedEnvelope = TODO("Not implemented")

    /**
     * Begins sealing a file for a Group, as one of its members.
     *
     * @param groupId ID of the Group to seal for
     * @param size    total size of the plaintext file, in bytes
     * @return handle to seal file data with
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun beginFileEncryption(groupId: String, size: Long): Long = TODO("Not implemented")

    /**
     * Begins sealing a file for a Group without revealing, or proving, who sent it.
     *
     * @param groupId     ID of the Group to seal for
     * @param groupPubKey the Group's identity public key (base58-DER encoded)
     * @param size        total size of the plaintext file, in bytes
     * @return handle to seal file data with
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun beginFileEncryptionAnonymously(
        groupId: String,
        groupPubKey: String,
        size: Long
    ): Long = TODO("Not implemented")

    /**
     * Seals the next piece of a file: takes plaintext, returns ciphertext.
     *
     * @param fileHandle handle from [beginFileEncryption] or [beginFileEncryptionAnonymously]
     * @param plainChunk plaintext to append, at most 4 MiB
     * @return ciphertext to store, possibly empty
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun encryptFileChunk(fileHandle: Long, plainChunk: ByteArray): ByteArray =
        TODO("Not implemented")

    /**
     * Finishes sealing a file and releases its handle.
     *
     * @param fileHandle handle from [beginFileEncryption] or [beginFileEncryptionAnonymously]
     * @return the envelope, needed to read the file back
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun finishFileEncryption(fileHandle: Long): ByteArray = TODO("Not implemented")

    /**
     * Begins opening a sealed file.
     *
     * @param envelope envelope returned by [finishFileEncryption]
     * @return handle to open file data with
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun beginFileDecryption(envelope: ByteArray): Long = TODO("Not implemented")

    /**
     * Opens the next piece of a file: takes ciphertext, returns plaintext.
     *
     * @param fileHandle  handle from [beginFileDecryption]
     * @param cipherChunk ciphertext to append, at most 4 MiB
     * @return plaintext, possibly empty
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun decryptFileChunk(fileHandle: Long, cipherChunk: ByteArray): ByteArray =
        TODO("Not implemented")

    /**
     * Moves the read cursor to [position] in the plaintext, and returns where to resume feeding ciphertext.
     *
     * @param fileHandle handle from [beginFileDecryption]
     * @param position   new cursor position in the plaintext, from 0 to the file size
     * @return ciphertext byte offset to resume feeding from
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun seekInEncryptedFile(fileHandle: Long, position: Long): Long = TODO("Not implemented")

    /**
     * Finishes opening a file, reports where it came from, and releases its handle.
     *
     * @param fileHandle handle from [beginFileDecryption]
     * @return which Group the file was sealed for, who — if anyone — is provably its author, and whether all
     * of it arrived
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun finishFileDecryption(fileHandle: Long): DecryptedFileInfo = TODO("Not implemented")

    /**
     * Subscribe for the Group events on the given subscription query.
     *
     * @param subscriptionQueries list of queries
     * @return list of subscriptionIds in matching order to subscriptionQueries
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun subscribeFor(subscriptionQueries: List<String>): List<String> =
        TODO("Not implemented")

    /**
     * Unsubscribe from events with the given subscriptionId.
     *
     * @param subscriptionIds list of subscriptionId
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun unsubscribeFrom(subscriptionIds: List<String>): Unit = TODO("Not implemented")

    /**
     * Generate subscription Query for the Group events.
     *
     * @param eventType    type of event you listen for
     * @param selectorType scope on which you listen for events
     * @param selectorId   ID of the selector
     * @return Query for subscribing event
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun buildSubscriptionQuery(
        eventType: GroupEventType,
        selectorType: GroupEventSelectorType,
        selectorId: String
    ): String = TODO("Not implemented")

    /**
     * Frees memory.
     */
    actual override fun close(): Unit = TODO("Not implemented")
}
