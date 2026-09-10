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
 */
expect class GroupApi(connection: Connection) : AutoCloseable {

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
    fun createGroup(
        contextId: String,
        users: List<UserWithPubKey>,
        managers: List<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy? = null
    ): String

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
    fun addGroupMembers(groupId: String, newMembers: List<GroupMemberToAdd>)

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
    fun removeGroupMembers(groupId: String, userIds: List<String>)

    /**
     * Updates an existing Group's metadata.
     *
     * The membership is deliberately not updatable here: seating a member and re-keying their path is one
     * operation on the Group's key tree, so it goes through [addGroupMembers]/[removeGroupMembers] instead.
     *
     * There is no way to skip the version check — a caller who loses the check has to re-read the Group and
     * build the update again.
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
    fun updateGroup(
        groupId: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        version: Long,
        policies: ContainerPolicy? = null
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
    fun deleteGroup(groupId: String)

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
    fun getGroup(groupId: String): Group

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
    fun listGroups(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String = "desc",
        lastId: String? = null,
        queryAsJson: String? = null,
        sortBy: String? = null
    ): PagingList<GroupSummary>

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
    fun encrypt(groupId: String, content: ByteArray): ByteArray

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
    fun encryptAnonymously(groupId: String, groupPubKey: String, content: ByteArray): ByteArray

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
    fun decrypt(envelope: ByteArray): DecryptedEnvelope

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
    fun beginFileEncryption(groupId: String, size: Long): Long

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
    fun beginFileEncryptionAnonymously(groupId: String, groupPubKey: String, size: Long): Long

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
    fun encryptFileChunk(fileHandle: Long, plainChunk: ByteArray): ByteArray

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
    fun finishFileEncryption(fileHandle: Long): ByteArray

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
    fun beginFileDecryption(envelope: ByteArray): Long

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
    fun decryptFileChunk(fileHandle: Long, cipherChunk: ByteArray): ByteArray

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
    fun seekInEncryptedFile(fileHandle: Long, position: Long): Long

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
    fun finishFileDecryption(fileHandle: Long): DecryptedFileInfo

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
    fun subscribeFor(subscriptionQueries: List<String>): List<String>

    /**
     * Unsubscribe from events with the given subscriptionId.
     *
     * @param subscriptionIds list of subscriptionId
     * @throws IllegalStateException thrown when instance is closed.
     * @throws PrivmxException       thrown when method encounters an exception.
     * @throws NativeException       thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun unsubscribeFrom(subscriptionIds: List<String>)

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
    fun buildSubscriptionQuery(
        eventType: GroupEventType,
        selectorType: GroupEventSelectorType,
        selectorId: String
    ): String

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed.
     */
    override fun close()
}
