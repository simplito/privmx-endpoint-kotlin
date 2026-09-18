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

import com.simplito.kotlin.privmx_endpoint.LibLoader
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

actual class GroupApi actual constructor(connection: Connection) : AutoCloseable {
    companion object {
        init {
            LibLoader.loadPrivmxLibraries()
        }
    }

    private val api: Long?

    /**
     * Creates an instance of `GroupApi`.
     *
     * @param connection instance of 'Connection'
     * @throws IllegalStateException when given [Connection] is not connected
     */
    init {
        this.api = init(connection)
    }

    @Throws(IllegalStateException::class)
    private external fun init(connection: Connection): Long?

    @Throws(IllegalStateException::class)
    private external fun deinit()

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
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual external fun createGroup(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy?
    ): String

    /**
     * Adds members to a tree-backed Group, without advancing its key epoch.
     *
     * @param groupId    ID of the Group
     * @param newMembers the members to add, each with their public key and the role they take
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun addGroupMembers(groupId: String, newMembers: List<GroupMemberToAdd>)

    /**
     * Removes several members at once, advancing the key epoch **once**.
     *
     * @param groupId ID of the Group
     * @param userIds IDs of the members to remove
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun removeGroupMembers(groupId: String, userIds: List<String>)

    /**
     * Updates an existing Group's metadata.
     *
     * The membership is deliberately not updatable here: it goes through
     * [addGroupMembers]/[removeGroupMembers] instead.
     *
     * @param groupId     ID of the Group to update
     * @param publicMeta  public (unencrypted) metadata
     * @param privateMeta private (encrypted) metadata
     * @param version     current version of the updated Group
     * @param policies    Group's policies
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual external fun updateGroup(
        groupId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        version: Long,
        policies: ContainerPolicy?
    )

    /**
     * Deletes a Group by given Group ID.
     *
     * @param groupId ID of the Group to delete
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun deleteGroup(groupId: String)

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
    actual external fun getGroup(groupId: String): Group

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
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    @JvmOverloads
    actual external fun listGroups(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        queryAsJson: String?,
        sortBy: String?
    ): PagingList<GroupSummary>

    /**
     * Seals content for a Group, as one of its members.
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
    actual external fun encrypt(groupId: String, content: ByteArray): ByteArray

    /**
     * Seals content for a Group without revealing, or proving, who sent it.
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
    actual external fun encryptAnonymously(
        groupId: String,
        groupPubKey: String,
        content: ByteArray
    ): ByteArray

    /**
     * Opens an envelope sealed by [encrypt] or by [encryptAnonymously].
     *
     * @param envelope envelope to open
     * @return the content, and what could be established about its author
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun decrypt(envelope: ByteArray): DecryptedEnvelope

    /**
     * Begins sealing a file for a Group, as one of its members.
     *
     * @param groupId ID of the Group to seal for
     * @param size    total size of the plaintext file, in bytes
     * @return handle to seal file data with
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun beginFileEncryption(groupId: String, size: Long): Long

    /**
     * Begins sealing a file for a Group without revealing, or proving, who sent it.
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
    actual external fun beginFileEncryptionAnonymously(
        groupId: String,
        groupPubKey: String,
        size: Long
    ): Long

    /**
     * Seals the next piece of a file: takes plaintext, returns ciphertext.
     *
     * @param fileHandle handle from [beginFileEncryption] or [beginFileEncryptionAnonymously]
     * @param plainChunk plaintext to append, at most 4 MiB
     * @return ciphertext to store, possibly empty
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun encryptFileChunk(fileHandle: Long, plainChunk: ByteArray): ByteArray

    /**
     * Finishes sealing a file and releases its handle.
     *
     * @param fileHandle handle from [beginFileEncryption] or [beginFileEncryptionAnonymously]
     * @return the envelope, needed to read the file back
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun finishFileEncryption(fileHandle: Long): ByteArray

    /**
     * Begins opening a sealed file.
     *
     * @param envelope envelope returned by [finishFileEncryption]
     * @return handle to open file data with
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun beginFileDecryption(envelope: ByteArray): Long

    /**
     * Opens the next piece of a file: takes ciphertext, returns plaintext.
     *
     * @param fileHandle  handle from [beginFileDecryption]
     * @param cipherChunk ciphertext to append, at most 4 MiB
     * @return plaintext, possibly empty
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun decryptFileChunk(fileHandle: Long, cipherChunk: ByteArray): ByteArray

    /**
     * Moves the read cursor to [position] in the plaintext, and returns where to resume feeding ciphertext.
     *
     * @param fileHandle handle from [beginFileDecryption]
     * @param position   new cursor position in the plaintext, from 0 to the file size
     * @return ciphertext byte offset to resume feeding from
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun seekInEncryptedFile(fileHandle: Long, position: Long): Long

    /**
     * Finishes opening a file, reports where it came from, and releases its handle.
     *
     * @param fileHandle handle from [beginFileDecryption]
     * @return which Group the file was sealed for, who — if anyone — is provably its author, and whether all
     * of it arrived
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun finishFileDecryption(fileHandle: Long): DecryptedFileInfo

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
    actual external fun subscribeFor(subscriptionQueries: List<String>): List<String>

    /**
     * Unsubscribe from events with the given subscriptionId.
     *
     * @param subscriptionIds list of subscriptionId
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun unsubscribeFrom(subscriptionIds: List<String>)

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun buildSubscriptionQuery(
        eventType: GroupEventType,
        selectorType: GroupEventSelectorType,
        selectorId: String
    ): String {
        return buildSubscriptionQuery(
            eventType.ordinal.toLong(),
            selectorType.ordinal.toLong(),
            selectorId
        )
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
    private external fun buildSubscriptionQuery(
        eventType: Long,
        selectorType: Long,
        selectorId: String
    ): String

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed.
     */
    actual override fun close() {
        deinit()
    }
}
