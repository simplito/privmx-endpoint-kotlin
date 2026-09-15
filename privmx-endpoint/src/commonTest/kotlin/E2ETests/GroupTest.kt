package E2ETests

import Utils.Queries.json
import Utils.Queries.json1
import Utils.Queries.json2
import Utils.Queries.json3
import Utils.Queries.json5
import Utils.Queries.json8
import Utils.Queries.json9
import Utils.Queries.query10
import Utils.Queries.query11
import Utils.Queries.query15
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.DecryptedEnvelope
import com.simplito.kotlin.privmx_endpoint.model.DecryptedFileInfo
import com.simplito.kotlin.privmx_endpoint.model.EnvelopeType
import com.simplito.kotlin.privmx_endpoint.model.Group
import com.simplito.kotlin.privmx_endpoint.model.GroupMemberToAdd
import com.simplito.kotlin.privmx_endpoint.model.GroupSummary
import com.simplito.kotlin.privmx_endpoint.model.ItemPolicy
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.GroupEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.GroupEventType
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.group.GroupApi
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class GroupTest : BaseTest() {

    // groupId  - users: [user1],         managers: [user1]
    // group2Id - users: [user1, user2],  managers: [user1, user2]
    // group3Id - users: [user1, user2],  managers: [user1]
    private lateinit var groupId: String
    private lateinit var group2Id: String
    private lateinit var group3Id: String
    private lateinit var groupApi: GroupApi

    private val roleUser = "user"
    private val roleManager = "manager"
    private val user1PubKey = users.first().pubKey
    private val nonexistentId = "f977e533-3524-4579-error-215368c4b2a4"

    @BeforeTest
    fun createConnection() {
        if (connection == null) {
            connection = connectAsUser(ConnectionType.User1, bridgeAddress)
        }
        groupApi = GroupApi(connection!!)
        groupId = newGroup(contextId!!, users.subList(0, 1), users.subList(0, 1))
        group2Id = newGroup(contextId!!, users, users)
        group3Id = newGroup(contextId!!, users, users.subList(0, 1))
    }

    @AfterTest
    @Throws(Exception::class)
    fun closeConnection() {
        if (::groupApi.isInitialized) {
            if (::group3Id.isInitialized) deleteQuietly(group3Id)
            if (::group2Id.isInitialized) deleteQuietly(group2Id)
            if (::groupId.isInitialized) deleteQuietly(groupId)
            try {
                groupApi.close()
            } catch (_: Exception) {
            }
        }
        connection?.close()?.also {
            connection = null
        }
        try {
            connection2?.close()
        } finally {
            connection2 = null
        }
    }

    // region helpers

    private fun newGroup(
        ctxId: String,
        groupUsers: List<UserWithPubKey>,
        groupManagers: List<UserWithPubKey>,
        pubMeta: ByteArray = publicMeta.encodeToByteArray(),
        privMeta: ByteArray = privateMeta.encodeToByteArray(),
        policies: ContainerPolicy? = null
    ): String = groupApi.createGroup(ctxId, groupUsers, groupManagers, pubMeta, privMeta, policies)

    private fun deleteQuietly(id: String, api: GroupApi = groupApi) {
        try {
            api.deleteGroup(id)
        } catch (_: Exception) {
        }
    }

    private fun connectAsUser2(): GroupApi {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        return GroupApi(connection2!!)
    }

    private fun member(user: UserWithPubKey, role: String) = GroupMemberToAdd(user, role)

    private fun concat(parts: List<ByteArray>): ByteArray {
        val result = ByteArray(parts.sumOf { it.size })
        var offset = 0
        parts.forEach {
            it.copyInto(result, offset)
            offset += it.size
        }
        return result
    }

    /**
     * Pushes the whole [content] through an already opened encryption [handle].
     *
     * @return the envelope and the ciphertext, both of which are needed to read the file back
     */
    private fun sealFile(
        api: GroupApi,
        handle: Long,
        content: ByteArray,
        chunkSize: Int = 64 * 1024
    ): Pair<ByteArray, ByteArray> {
        val cipherParts = mutableListOf<ByteArray>()
        var offset = 0
        while (offset < content.size) {
            val end = minOf(offset + chunkSize, content.size)
            cipherParts.add(api.encryptFileChunk(handle, content.copyOfRange(offset, end)))
            offset = end
        }
        return api.finishFileEncryption(handle) to concat(cipherParts)
    }

    private fun sealFile(
        api: GroupApi,
        groupIdToSealFor: String,
        content: ByteArray
    ): Pair<ByteArray, ByteArray> = sealFile(
        api,
        api.beginFileEncryption(groupIdToSealFor, content.size.toLong()),
        content
    )

    /**
     * Feeds the whole [cipher] into a new decryption handle, from the beginning of the file.
     *
     * @return the plaintext and the info reported when the file was closed
     */
    private fun openSealedFile(
        api: GroupApi,
        envelope: ByteArray,
        cipher: ByteArray,
        chunkSize: Int = 64 * 1024
    ): Pair<ByteArray, DecryptedFileInfo> {
        val handle = api.beginFileDecryption(envelope)
        val plainParts = mutableListOf<ByteArray>()
        var offset = 0
        while (offset < cipher.size) {
            val end = minOf(offset + chunkSize, cipher.size)
            plainParts.add(api.decryptFileChunk(handle, cipher.copyOfRange(offset, end)))
            offset = end
        }
        return concat(plainParts) to api.finishFileDecryption(handle)
    }

    private fun fileContent(size: Int): ByteArray = ByteArray(size) { (it % 251).toByte() }

    // endregion

    @Test
    fun createGroupIncorrectInputData() {
        // incorrect contextId
        assertFailsWith(PrivmxException::class) {
            newGroup(groupId, users, users)
        }

        // incorrect users
        assertFailsWith(PrivmxException::class) {
            newGroup(context2Id!!, incorrectUsers, users)
        }

        // incorrect managers
        assertFailsWith(PrivmxException::class) {
            newGroup(context2Id!!, users, incorrectUsers)
        }

        // no managers
        assertFailsWith(PrivmxException::class) {
            newGroup(context2Id!!, users, emptyUsers)
        }

        // creator not in managers
        assertFailsWith(PrivmxException::class) {
            newGroup(context2Id!!, users.subList(0, 1), users.subList(1, 2))
        }
    }

    @Test
    fun createGroupCorrectInputData() {
        val createdIds = mutableListOf<String>()
        lateinit var id: String

        // same users and managers
        assertDoesNotFail {
            id = newGroup(context2Id!!, users, users)
            createdIds.add(id)
        }

        // no users
        assertDoesNotFail {
            createdIds.add(newGroup(context2Id!!, emptyUsers, users))
        }

        // no publicMeta
        assertDoesNotFail {
            createdIds.add(
                newGroup(context2Id!!, emptyUsers, users, pubMeta = ByteArray(0))
            )
        }

        // no privateMeta
        assertDoesNotFail {
            createdIds.add(
                newGroup(context2Id!!, emptyUsers, users, privMeta = ByteArray(0))
            )
        }

        // the same user listed twice - a Group tolerates it and seats them once, unlike a Kvdb,
        // a Thread or a Store, where a duplicate is rejected
        assertDoesNotFail {
            createdIds.add(newGroup(context2Id!!, sameUsers, users))
        }
        assertDoesNotFail {
            createdIds.add(newGroup(context2Id!!, users, sameUsers))
        }

        val group = groupApi.getGroup(id)
        assertEquals(context2Id, group.contextId)
        assertEquals(id, group.groupId)
        assertTrue(group.groupPubKey.isNotEmpty())

        createdIds.forEach { deleteQuietly(it) }
    }

    @Test
    fun createGroupWithPolicy() {
        val itemPolicy = ItemPolicy("owner", "owner", "owner", "owner", "owner", "owner")
        val containerPolicy =
            ContainerPolicy("owner", "owner", "owner", "owner", "no", "no", itemPolicy)
        lateinit var id: String

        assertDoesNotFail {
            id = newGroup(context2Id!!, users, users, policies = containerPolicy)
        }

        val group = groupApi.getGroup(id)
        assertEquals(containerPolicy.get, group.policy.get)
        assertEquals(containerPolicy.update, group.policy.update)
        assertEquals(containerPolicy.delete, group.policy.delete)
        assertEquals(containerPolicy.updatePolicy, group.policy.updatePolicy)
        assertEquals(
            containerPolicy.updaterCanBeRemovedFromManagers,
            group.policy.updaterCanBeRemovedFromManagers
        )
        assertEquals(
            containerPolicy.ownerCanBeRemovedFromManagers,
            group.policy.ownerCanBeRemovedFromManagers
        )

        deleteQuietly(id)
    }

    @Test
    fun getGroup() {
        lateinit var group: Group

        // incorrect Id
        assertFailsWith(PrivmxException::class) {
            groupApi.getGroup(contextId!!)
        }

        // correct Id
        assertDoesNotFail { group = groupApi.getGroup(groupId) }
        assertEquals(groupId, group.groupId)
        assertEquals(contextId!!, group.contextId)
        assertEquals(user1Id!!, group.creator)
        assertEquals(user1Id!!, group.lastModifier)
        assertContentEquals(publicMeta.encodeToByteArray(), group.publicMeta)
        assertContentEquals(privateMeta.encodeToByteArray(), group.privateMeta)
        assertEquals(0, group.statusCode)
        assertEquals(1, group.users.size)
        assertEquals(1, group.managers.size)
        assertEquals(user1Id!!, group.users[0])
        assertEquals(user1Id!!, group.managers[0])
        assertTrue(group.groupPubKey.isNotEmpty())

        // a freshly created Group is on epoch 1, with all three counters at 1
        assertEquals(1, group.keyVersion)
        assertEquals(1, group.publicMetaVersion)
        assertEquals(1, group.privateMetaVersion)
        assertEquals(1, group.rosterVersion)

        // group with 2 users and 1 manager
        assertDoesNotFail { group = groupApi.getGroup(group3Id) }
        assertEquals(2, group.users.size)
        assertEquals(1, group.managers.size)

        // as a member who is not a manager
        val groupApi2 = connectAsUser2()
        assertDoesNotFail { group = groupApi2.getGroup(group3Id) }
        assertEquals(group3Id, group.groupId)

        // as a user who is not a member
        assertFailsWith(PrivmxException::class) { groupApi2.getGroup(groupId) }

        groupApi2.close()
    }

    @Test
    fun addGroupMembers() {
        val groupApi2 = connectAsUser2()
        val id = newGroup(context2Id!!, users.subList(0, 1), users.subList(0, 1))
        val before = groupApi.getGroup(id)

        // incorrect groupId
        assertFailsWith(PrivmxException::class) {
            groupApi.addGroupMembers(context2Id!!, listOf(member(users[1], roleUser)))
        }

        // a public key that does not belong to the added user is NOT rejected here, unlike in
        // createGroup, where the same mismatch throws - the member is seated with the key given
        val idWrongKey = newGroup(context2Id!!, users.subList(0, 1), users.subList(0, 1))
        assertDoesNotFail {
            groupApi.addGroupMembers(idWrongKey, listOf(member(incorrectUsers[1], roleUser)))
        }
        assertEquals(2, groupApi.getGroup(idWrongKey).users.size)
        deleteQuietly(idWrongKey)

        // incorrect role
        assertFailsWith(PrivmxException::class) {
            groupApi.addGroupMembers(id, listOf(member(users[1], "admin")))
        }

        // user who is not a member cannot add members
        assertFailsWith(PrivmxException::class) {
            groupApi2.addGroupMembers(id, listOf(member(users[1], roleUser)))
        }

        // correct - add user2 as a plain user
        assertDoesNotFail {
            groupApi.addGroupMembers(id, listOf(member(users[1], roleUser)))
        }
        var group = groupApi.getGroup(id)
        assertEquals(2, group.users.size)
        assertEquals(1, group.managers.size)
        assertTrue(group.users.contains(user2Id!!))
        assertFalse(group.managers.contains(user2Id!!))

        // roster moved, metadata did not
        assertNotEquals(before.rosterVersion, group.rosterVersion)
        assertEquals(before.publicMetaVersion, group.publicMetaVersion)
        assertEquals(before.privateMetaVersion, group.privateMetaVersion)

        // adding a member does not advance the key epoch
        assertEquals(before.keyVersion, group.keyVersion)

        // the new member can read the Group
        assertDoesNotFail { groupApi2.getGroup(id) }

        // a plain user cannot add members
        assertFailsWith(PrivmxException::class) {
            groupApi2.addGroupMembers(id, listOf(member(users[0], roleUser)))
        }

        // adding somebody who is already a member
        assertFailsWith(PrivmxException::class) {
            groupApi.addGroupMembers(id, listOf(member(users[1], roleUser)))
        }

        // TODO: test adding an empty list of members - unclear whether it is a no-op or an error

        // correct - promote by adding as a manager in another Group
        val id2 = newGroup(context2Id!!, users.subList(0, 1), users.subList(0, 1))
        assertDoesNotFail {
            groupApi.addGroupMembers(id2, listOf(member(users[1], roleManager)))
        }
        group = groupApi.getGroup(id2)
        // a member added with the "manager" role is seated in managers ONLY - the two lists are
        // disjoint here, even though createGroup puts the creator in both
        assertEquals(1, group.users.size)
        assertEquals(2, group.managers.size)
        assertTrue(group.managers.contains(user2Id!!))
        assertFalse(group.users.contains(user2Id!!))

        // the new manager can add and remove members
        assertDoesNotFail { groupApi2.removeGroupMembers(id2, listOf(user1Id!!)) }

        deleteQuietly(id2, groupApi2)
        deleteQuietly(id)
        groupApi2.close()
    }

    @Test
    fun removeGroupMembers() {
        val groupApi2 = connectAsUser2()
        val id = newGroup(context2Id!!, users, users.subList(0, 1))
        val before = groupApi.getGroup(id)

        // incorrect groupId
        assertFailsWith(PrivmxException::class) {
            groupApi.removeGroupMembers(context2Id!!, listOf(user2Id!!))
        }

        // user who is not a member of the Group
        assertFailsWith(PrivmxException::class) {
            groupApi.removeGroupMembers(id, listOf(nonexistentId))
        }

        // a plain user cannot remove members
        assertFailsWith(PrivmxException::class) {
            groupApi2.removeGroupMembers(id, listOf(user1Id!!))
        }

        // correct
        assertDoesNotFail { groupApi.removeGroupMembers(id, listOf(user2Id!!)) }
        val after = groupApi.getGroup(id)
        assertEquals(1, after.users.size)
        assertEquals(1, after.managers.size)
        assertFalse(after.users.contains(user2Id!!))

        // removing a member advances the key epoch exactly once, and re-wraps the metadata, so
        // both metadata counters move with it - unlike an addition, which touches the roster only
        assertEquals(before.keyVersion + 1, after.keyVersion)
        assertEquals(before.rosterVersion + 1, after.rosterVersion)
        assertEquals(before.publicMetaVersion + 1, after.publicMetaVersion)
        assertEquals(before.privateMetaVersion + 1, after.privateMetaVersion)

        // the removed member has no access anymore
        assertFailsWith(PrivmxException::class) { groupApi2.getGroup(id) }

        // removing the same member once again
        assertFailsWith(PrivmxException::class) {
            groupApi.removeGroupMembers(id, listOf(user2Id!!))
        }

        // TODO: test removing an empty list of members, and a manager removing themselves as the
        //  last manager of the Group

        // a batch of removals costs one epoch, however many members leave
        val id2 = newGroup(context2Id!!, users, users)
        val before2 = groupApi.getGroup(id2)
        assertDoesNotFail { groupApi.removeGroupMembers(id2, listOf(user2Id!!)) }
        assertEquals(before2.keyVersion + 1, groupApi.getGroup(id2).keyVersion)

        deleteQuietly(id2)
        deleteQuietly(id)
        groupApi2.close()
    }

    @Test
    fun updateGroupPublicMeta() {
        val groupApi2 = connectAsUser2()
        val id = newGroup(context2Id!!, users, users.subList(0, 1))
        val group = groupApi.getGroup(id)
        val newPublicMeta = "new public meta".encodeToByteArray()

        // incorrect groupId
        assertFailsWith(PrivmxException::class) {
            groupApi.updateGroupPublicMeta(context2Id!!, newPublicMeta, group.publicMetaVersion)
        }

        // incorrect version
        assertFailsWith(PrivmxException::class) {
            groupApi.updateGroupPublicMeta(id, newPublicMeta, group.publicMetaVersion + 5)
        }

        // a plain user cannot update the Group
        assertFailsWith(PrivmxException::class) {
            groupApi2.updateGroupPublicMeta(id, newPublicMeta, group.publicMetaVersion)
        }

        // correct
        assertDoesNotFail {
            groupApi.updateGroupPublicMeta(id, newPublicMeta, group.publicMetaVersion)
        }
        val updated = groupApi.getGroup(id)
        assertContentEquals(newPublicMeta, updated.publicMeta)
        assertEquals(group.publicMetaVersion + 1, updated.publicMetaVersion)

        // this call touches neither the private metadata nor the roster
        assertContentEquals(group.privateMeta, updated.privateMeta)
        assertEquals(group.privateMetaVersion, updated.privateMetaVersion)
        assertEquals(group.rosterVersion, updated.rosterVersion)

        // the version has moved, so the same update cannot land twice
        assertFailsWith(PrivmxException::class) {
            groupApi.updateGroupPublicMeta(id, newPublicMeta, group.publicMetaVersion)
        }

        // empty publicMeta
        assertDoesNotFail {
            groupApi.updateGroupPublicMeta(id, ByteArray(0), updated.publicMetaVersion)
        }
        assertEquals(0, groupApi.getGroup(id).publicMeta.size)

        deleteQuietly(id)
        groupApi2.close()
    }

    @Test
    fun updateGroupPrivateMeta() {
        val groupApi2 = connectAsUser2()
        val id = newGroup(context2Id!!, users, users.subList(0, 1))
        val group = groupApi.getGroup(id)
        val newPrivateMeta = "new private meta".encodeToByteArray()

        // incorrect groupId
        assertFailsWith(PrivmxException::class) {
            groupApi.updateGroupPrivateMeta(context2Id!!, newPrivateMeta, group.privateMetaVersion)
        }

        // incorrect version
        assertFailsWith(PrivmxException::class) {
            groupApi.updateGroupPrivateMeta(id, newPrivateMeta, group.privateMetaVersion + 5)
        }

        // a plain user cannot update the Group
        assertFailsWith(PrivmxException::class) {
            groupApi2.updateGroupPrivateMeta(id, newPrivateMeta, group.privateMetaVersion)
        }

        // correct
        assertDoesNotFail {
            groupApi.updateGroupPrivateMeta(id, newPrivateMeta, group.privateMetaVersion)
        }
        val updated = groupApi.getGroup(id)
        assertContentEquals(newPrivateMeta, updated.privateMeta)
        assertEquals(group.privateMetaVersion + 1, updated.privateMetaVersion)

        // this call touches neither the public metadata nor the roster
        assertContentEquals(group.publicMeta, updated.publicMeta)
        assertEquals(group.publicMetaVersion, updated.publicMetaVersion)
        assertEquals(group.rosterVersion, updated.rosterVersion)

        // the version has moved, so the same update cannot land twice
        assertFailsWith(PrivmxException::class) {
            groupApi.updateGroupPrivateMeta(id, newPrivateMeta, group.privateMetaVersion)
        }

        // the two metadata counters are independent
        assertDoesNotFail {
            groupApi.updateGroupPublicMeta(
                id,
                "another public meta".encodeToByteArray(),
                updated.publicMetaVersion
            )
        }
        assertDoesNotFail {
            groupApi.updateGroupPrivateMeta(
                id,
                "another private meta".encodeToByteArray(),
                updated.privateMetaVersion
            )
        }

        deleteQuietly(id)
        groupApi2.close()
    }

    @Test
    fun updateGroupPolicy() {
        val groupApi2 = connectAsUser2()
        val id = newGroup(context2Id!!, users, users.subList(0, 1))
        val group = groupApi.getGroup(id)
        val itemPolicy = ItemPolicy("owner", "owner", "owner", "owner", "owner", "owner")
        val containerPolicy =
            ContainerPolicy("all", "owner", "owner", "owner", "no", "no", itemPolicy)

        // incorrect groupId
        assertFailsWith(PrivmxException::class) {
            groupApi.updateGroupPolicy(context2Id!!, containerPolicy)
        }

        // a plain user cannot update the policies
        assertFailsWith(PrivmxException::class) {
            groupApi2.updateGroupPolicy(id, containerPolicy)
        }

        // correct
        assertDoesNotFail { groupApi.updateGroupPolicy(id, containerPolicy) }
        val updated = groupApi.getGroup(id)
        assertEquals(containerPolicy.get, updated.policy.get)
        assertEquals(containerPolicy.update, updated.policy.update)
        assertEquals(containerPolicy.delete, updated.policy.delete)
        assertEquals(containerPolicy.updatePolicy, updated.policy.updatePolicy)

        // the policies live outside the encrypted metadata - no version moves
        assertEquals(group.publicMetaVersion, updated.publicMetaVersion)
        assertEquals(group.privateMetaVersion, updated.privateMetaVersion)
        assertEquals(group.rosterVersion, updated.rosterVersion)

        // no version check here, so the same call can be repeated - the last write wins
        assertDoesNotFail { groupApi.updateGroupPolicy(id, containerPolicy) }

        deleteQuietly(id)
        groupApi2.close()
    }

    @Test
    fun deleteGroup() {
        val groupApi2 = connectAsUser2()

        // incorrect groupId
        assertFailsWith(PrivmxException::class) {
            groupApi2.deleteGroup(context2Id!!)
        }

        // user1 creates - user1 deletes (user1 is in managers)
        val id2 = newGroup(context2Id!!, users.subList(0, 1), users.subList(0, 1))
        assertDoesNotFail { groupApi.deleteGroup(id2) }

        // deleting the same Group once again
        assertFailsWith(PrivmxException::class) { groupApi.deleteGroup(id2) }

        // user1 creates - user2 deletes (user2 is in managers list)
        val id3 = newGroup(context2Id!!, users.subList(0, 1), users)
        assertDoesNotFail { groupApi2.deleteGroup(id3) }

        // user1 creates - user2 deletes (user2 is in users list)
        val id4 = newGroup(context2Id!!, users, users.subList(0, 1))
        assertFailsWith(PrivmxException::class) { groupApi2.deleteGroup(id4) }

        // user1 creates - user2 deletes (user2 is not in users list)
        val id5 = newGroup(context2Id!!, users.subList(0, 1), users.subList(0, 1))
        assertFailsWith(PrivmxException::class) { groupApi2.deleteGroup(id5) }

        // a deleted Group cannot be read anymore
        assertFailsWith(PrivmxException::class) { groupApi.getGroup(id3) }

        deleteQuietly(id5)
        deleteQuietly(id4)
        groupApi2.close()
    }

    @Test
    fun listGroupsIncorrectInputData() {
        // incorrect contextId
        assertFailsWith(PrivmxException::class) {
            groupApi.listGroups(groupId, 0, 1, "desc")
        }

        // limit < 0
        assertFailsWith(PrivmxException::class) {
            groupApi.listGroups(contextId!!, 0, -1, "desc")
        }

        // limit == 0
        assertFailsWith(PrivmxException::class) {
            groupApi.listGroups(contextId!!, 0, 0, "desc")
        }

        // incorrect sortOrder
        assertFailsWith(PrivmxException::class) {
            groupApi.listGroups(contextId!!, 0, 1, "wrong")
        }

        // incorrect lastId
        assertFailsWith(PrivmxException::class) {
            groupApi.listGroups(contextId!!, 0, 1, "desc", "wrong")
        }

        // incorrect queryAsJson
        assertFailsWith(PrivmxException::class) {
            groupApi.listGroups(contextId!!, 0, 1, "desc", null, "wrong")
        }

        // incorrect sortBy
        assertFailsWith(PrivmxException::class) {
            groupApi.listGroups(contextId!!, 0, 1, "desc", null, null, "wrong")
        }
    }

    @Test
    fun listGroupsCorrectInputData() {
        val contextId: String = contextId!!
        lateinit var groups: PagingList<GroupSummary>

        // skip greater than the number of Groups
        assertDoesNotFail { groups = groupApi.listGroups(contextId, 4, 1, "desc") }
        assertEquals(3, groups.totalAvailable)
        assertEquals(0, groups.readItems.size)

        // newest first
        assertDoesNotFail { groups = groupApi.listGroups(contextId, 0, 1, "desc") }
        assertEquals(3, groups.totalAvailable)
        assertEquals(1, groups.readItems.size)
        assertEquals(contextId, groups.readItems[0].contextId)
        assertEquals(group3Id, groups.readItems[0].groupId)

        // oldest first, with skip
        assertDoesNotFail { groups = groupApi.listGroups(contextId, 1, 3, "asc") }
        assertEquals(3, groups.totalAvailable)
        assertEquals(2, groups.readItems.size)
        assertEquals(contextId, groups.readItems[0].contextId)
        assertEquals(group2Id, groups.readItems[0].groupId)

        // with lastId parameter
        assertDoesNotFail { groups = groupApi.listGroups(contextId, 0, 10, "asc", groupId) }
        assertEquals(2, groups.totalAvailable)
        assertEquals(2, groups.readItems.size)
        assertEquals(group2Id, groups.readItems[0].groupId)

        // with sortBy parameter - createDate
        assertDoesNotFail {
            groups = groupApi.listGroups(contextId, 0, 10, "desc", null, null, "createDate")
        }
        assertEquals(3, groups.totalAvailable)
        assertEquals(3, groups.readItems.size)

        // a listing carries the identity, the roster and the epoch
        val summary = groups.readItems.first { it.groupId == group3Id }
        assertEquals(contextId, summary.contextId)
        assertEquals(user1Id!!, summary.creator)
        assertEquals(user1Id!!, summary.lastModifier)
        assertEquals(2, summary.users.size)
        assertEquals(1, summary.managers.size)
        assertTrue(summary.groupPubKey.isNotEmpty())

        // as a member who is not a manager
        val groupApi2 = connectAsUser2()
        assertDoesNotFail { groups = groupApi2.listGroups(contextId, 0, 10, "desc") }
        assertEquals(2, groups.readItems.size)
        assertFalse(groups.readItems.any { it.groupId == groupId })

        groupApi2.close()
    }

    @Test
    @Ignore // queryAsJson is validated but matches nothing for Groups: a listing filtered by a
    // publicMeta the Group demonstrably has comes back empty. Un-ignore once the Bridge indexes
    // Group publicMeta the way it does for Kvdbs and Stores.
    fun filteringListGroupsWithQueryAsJson() {
        val created = mutableListOf<String>()

        val idJson = newGroup(context2Id!!, users, users, json.encodeToByteArray())
        val idJson1 = newGroup(context2Id!!, users, users, json1.encodeToByteArray())
        val idJson2 = newGroup(context2Id!!, users, users, json2.encodeToByteArray())
        val idJson3 = newGroup(context2Id!!, users, users, json3.encodeToByteArray())
        val idJson5 = newGroup(context2Id!!, users, users, json5.encodeToByteArray())
        val idJson8 = newGroup(context2Id!!, users, users, json8.encodeToByteArray())
        val idJson9 = newGroup(context2Id!!, users, users, json9.encodeToByteArray())
        created.addAll(listOf(idJson, idJson1, idJson2, idJson3, idJson5, idJson8, idJson9))

        fun matching(query: String): List<String> = groupApi.listGroups(
            context2Id!!, 0, 100, "desc", null, query
        ).readItems.map { it.groupId }.filter { it in created }

        // {"first_field": "abc"} -> json, json1, json8
        assertEquals(
            setOf(idJson, idJson1, idJson8),
            matching(json).toSet()
        )

        // {"first_field": "abc", "second_field": "custom"} -> json1
        assertEquals(setOf(idJson1), matching(json1).toSet())

        // {"first_field": "custom"} -> json2, json9
        assertEquals(setOf(idJson2, idJson9), matching(json2).toSet())

        // {"second_field": "xyz"} -> json3, json9
        assertEquals(setOf(idJson3, idJson9), matching(json3).toSet())

        // $eq -> json, json1, json8
        assertEquals(setOf(idJson, idJson1, idJson8), matching(query10).toSet())

        // third_field $gt 20 -> json8, json9
        assertEquals(setOf(idJson8, idJson9), matching(query11).toSet())

        // first_field $ne "abc" -> json2, json3, json5, json9
        assertEquals(
            setOf(idJson2, idJson3, idJson5, idJson9),
            matching(query15).toSet()
        )

        created.forEach { deleteQuietly(it) }
    }

    @Test
    fun encryptAndDecrypt() {
        val groupApi2 = connectAsUser2()
        val content = "Sealed for the Group".encodeToByteArray()

        // incorrect groupId
        assertFailsWith(PrivmxException::class) {
            groupApi.encrypt(context2Id!!, content)
        }

        // a user who is not a member cannot seal for the Group
        assertFailsWith(PrivmxException::class) {
            groupApi2.encrypt(groupId, content)
        }

        // correct - seal and open as the same member
        lateinit var envelope: ByteArray
        assertDoesNotFail { envelope = groupApi.encrypt(groupId, content) }
        lateinit var decrypted: DecryptedEnvelope
        assertDoesNotFail { decrypted = groupApi.decrypt(envelope) }
        assertContentEquals(content, decrypted.data)
        assertEquals(groupId, decrypted.groupId)
        assertEquals(EnvelopeType.ENVELOPE_FROM_MEMBER, decrypted.type)
        assertEquals(user1PubKey, decrypted.authorPubKey)

        // correct - open as another member of the Group
        val envelope2 = groupApi.encrypt(group2Id, content)
        assertDoesNotFail { decrypted = groupApi2.decrypt(envelope2) }
        assertContentEquals(content, decrypted.data)
        assertEquals(group2Id, decrypted.groupId)
        assertEquals(EnvelopeType.ENVELOPE_FROM_MEMBER, decrypted.type)
        assertEquals(user1PubKey, decrypted.authorPubKey)

        // a user who is not a member cannot open the envelope
        assertFailsWith(PrivmxException::class) { groupApi2.decrypt(envelope) }

        // incorrect envelope
        assertFailsWith(PrivmxException::class) {
            groupApi.decrypt("not an envelope".encodeToByteArray())
        }

        // empty envelope
        assertFailsWith(PrivmxException::class) { groupApi.decrypt(ByteArray(0)) }

        // empty content
        assertDoesNotFail {
            assertEquals(0, groupApi.decrypt(groupApi.encrypt(groupId, ByteArray(0))).data.size)
        }

        groupApi2.close()
    }

    @Test
    fun decryptAfterKeyRotation() {
        val content = "Sealed before the key rotated".encodeToByteArray()
        val id = newGroup(context2Id!!, users, users.subList(0, 1))
        val envelope = groupApi.encrypt(id, content)
        val keyVersion = groupApi.getGroup(id).keyVersion

        // removing a member rotates the Group key
        groupApi.removeGroupMembers(id, listOf(user2Id!!))
        assertEquals(keyVersion + 1, groupApi.getGroup(id).keyVersion)

        // the envelope names the key version it was sealed under, so it still opens
        lateinit var decrypted: DecryptedEnvelope
        assertDoesNotFail { decrypted = groupApi.decrypt(envelope) }
        assertContentEquals(content, decrypted.data)
        assertEquals(id, decrypted.groupId)

        // and so does content sealed under the new epoch
        assertDoesNotFail {
            assertContentEquals(content, groupApi.decrypt(groupApi.encrypt(id, content)).data)
        }

        deleteQuietly(id)
    }

    @Test
    fun encryptAnonymouslyAndDecrypt() {
        val groupApi2 = connectAsUser2()
        val content = "Sealed by a stranger".encodeToByteArray()
        val group = groupApi.getGroup(groupId)
        val otherGroupPubKey = groupApi.getGroup(group2Id).groupPubKey

        // incorrect groupPubKey
        assertFailsWith(PrivmxException::class) {
            groupApi2.encryptAnonymously(groupId, "wrong", content)
        }

        // correct - needs only public information, so a non-member can do it
        lateinit var envelope: ByteArray
        assertDoesNotFail {
            envelope = groupApi2.encryptAnonymously(groupId, group.groupPubKey, content)
        }

        // the Group opens it, and learns nothing about the author
        lateinit var decrypted: DecryptedEnvelope
        assertDoesNotFail { decrypted = groupApi.decrypt(envelope) }
        assertContentEquals(content, decrypted.data)
        assertEquals(groupId, decrypted.groupId)
        assertEquals(EnvelopeType.ENVELOPE_ANONYMOUS, decrypted.type)
        assertTrue(decrypted.authorPubKey.isEmpty())

        // the author cannot read back what they sealed
        assertFailsWith(PrivmxException::class) { groupApi2.decrypt(envelope) }

        // sealed with the public key of another Group - nobody can open it
        val mismatched = groupApi2.encryptAnonymously(groupId, otherGroupPubKey, content)
        assertFailsWith(PrivmxException::class) { groupApi.decrypt(mismatched) }

        // a member may seal anonymously as well
        assertDoesNotFail {
            decrypted = groupApi.decrypt(
                groupApi.encryptAnonymously(groupId, group.groupPubKey, content)
            )
        }
        assertEquals(EnvelopeType.ENVELOPE_ANONYMOUS, decrypted.type)

        groupApi2.close()
    }

    @Test
    fun encryptAndDecryptFile() {
        val groupApi2 = connectAsUser2()
        val content = fileContent(300_000)

        // incorrect groupId
        assertFailsWith(PrivmxException::class) {
            groupApi.beginFileEncryption(context2Id!!, content.size.toLong())
        }

        // a user who is not a member cannot seal a file for the Group
        assertFailsWith(PrivmxException::class) {
            groupApi2.beginFileEncryption(groupId, content.size.toLong())
        }

        // correct
        lateinit var sealed: Pair<ByteArray, ByteArray>
        assertDoesNotFail { sealed = sealFile(groupApi, groupId, content) }
        val (envelope, cipher) = sealed
        assertTrue(envelope.isNotEmpty())

        lateinit var opened: Pair<ByteArray, DecryptedFileInfo>
        assertDoesNotFail { opened = openSealedFile(groupApi, envelope, cipher) }
        assertContentEquals(content, opened.first)
        assertEquals(groupId, opened.second.groupId)
        assertEquals(EnvelopeType.ENVELOPE_FROM_MEMBER, opened.second.type)
        assertEquals(user1PubKey, opened.second.authorPubKey)
        assertTrue(opened.second.complete)

        // another member of the Group opens the same file
        val sealedFor2 = sealFile(groupApi, group2Id, content)
        assertDoesNotFail {
            opened = openSealedFile(groupApi2, sealedFor2.first, sealedFor2.second)
        }
        assertContentEquals(content, opened.first)
        assertEquals(group2Id, opened.second.groupId)

        // a user who is not a member cannot open it
        assertFailsWith(PrivmxException::class) { groupApi2.beginFileDecryption(envelope) }

        // a file envelope is not accepted by decrypt()
        assertFailsWith(PrivmxException::class) { groupApi.decrypt(envelope) }

        // incorrect envelope
        assertFailsWith(PrivmxException::class) {
            groupApi.beginFileDecryption("not an envelope".encodeToByteArray())
        }

        // small file - shorter than a single internal chunk
        val small = "small file".encodeToByteArray()
        val sealedSmall = sealFile(groupApi, groupId, small)
        assertDoesNotFail {
            opened = openSealedFile(groupApi, sealedSmall.first, sealedSmall.second)
        }
        assertContentEquals(small, opened.first)
        assertTrue(opened.second.complete)

        // less plaintext than declared - the file cannot be finished
        val shortHandle = groupApi.beginFileEncryption(groupId, content.size.toLong())
        groupApi.encryptFileChunk(shortHandle, content.copyOfRange(0, 1000))
        assertFailsWith(PrivmxException::class) { groupApi.finishFileEncryption(shortHandle) }

        // the handle is released even though finishing failed
        assertFailsWith(PrivmxException::class) {
            groupApi.encryptFileChunk(shortHandle, content.copyOfRange(1000, 2000))
        }

        // more plaintext than declared
        val longHandle = groupApi.beginFileEncryption(groupId, small.size.toLong())
        assertFailsWith(PrivmxException::class) {
            groupApi.encryptFileChunk(longHandle, content)
        }

        // truncated ciphertext - the file did not arrive whole
        val truncatedHandle = groupApi.beginFileDecryption(envelope)
        groupApi.decryptFileChunk(truncatedHandle, cipher.copyOfRange(0, cipher.size / 2))
        assertFailsWith(PrivmxException::class) {
            groupApi.finishFileDecryption(truncatedHandle)
        }

        groupApi2.close()
    }

    @Test
    fun encryptAndDecryptFileAnonymously() {
        val groupApi2 = connectAsUser2()
        val content = fileContent(300_000)
        val groupPubKey = groupApi.getGroup(groupId).groupPubKey

        // incorrect groupPubKey
        assertFailsWith(PrivmxException::class) {
            groupApi2.beginFileEncryptionAnonymously(groupId, "wrong", content.size.toLong())
        }

        // correct - a non-member seals a file using public information only
        lateinit var sealed: Pair<ByteArray, ByteArray>
        assertDoesNotFail {
            sealed = sealFile(
                groupApi2,
                groupApi2.beginFileEncryptionAnonymously(
                    groupId,
                    groupPubKey,
                    content.size.toLong()
                ),
                content
            )
        }

        // the Group opens it, and learns nothing about the author
        lateinit var opened: Pair<ByteArray, DecryptedFileInfo>
        assertDoesNotFail { opened = openSealedFile(groupApi, sealed.first, sealed.second) }
        assertContentEquals(content, opened.first)
        assertEquals(groupId, opened.second.groupId)
        assertEquals(EnvelopeType.ENVELOPE_ANONYMOUS, opened.second.type)
        assertTrue(opened.second.authorPubKey.isEmpty())
        assertTrue(opened.second.complete)

        // the author cannot read back what they sealed
        assertFailsWith(PrivmxException::class) { groupApi2.beginFileDecryption(sealed.first) }

        groupApi2.close()
    }

    @Test
    fun seekInEncryptedFile() {
        val content = fileContent(300_000)
        val (envelope, cipher) = sealFile(groupApi, groupId, content)
        val handle = groupApi.beginFileDecryption(envelope)

        // position < 0
        assertFailsWith(PrivmxException::class) { groupApi.seekInEncryptedFile(handle, -1) }

        // position > file size
        assertFailsWith(PrivmxException::class) {
            groupApi.seekInEncryptedFile(handle, content.size + 1L)
        }

        // position == file size
        assertDoesNotFail { groupApi.seekInEncryptedFile(handle, content.size.toLong()) }

        // position == 0
        assertDoesNotFail { groupApi.seekInEncryptedFile(handle, 0) }

        // incorrect handle
        assertFailsWith(PrivmxException::class) { groupApi.seekInEncryptedFile(-404, 2) }

        // read a range: the plaintext starts exactly where asked, and may overshoot at the tail
        val from = 100_000
        var cipherOffset = 0L
        assertDoesNotFail { cipherOffset = groupApi.seekInEncryptedFile(handle, from.toLong()) }
        assertTrue(cipherOffset >= 0)
        assertTrue(cipherOffset <= cipher.size)

        val plainParts = mutableListOf<ByteArray>()
        var offset = cipherOffset.toInt()
        while (offset < cipher.size) {
            val end = minOf(offset + 64 * 1024, cipher.size)
            plainParts.add(groupApi.decryptFileChunk(handle, cipher.copyOfRange(offset, end)))
            offset = end
        }
        val plain = concat(plainParts)
        assertTrue(plain.size >= content.size - from)
        assertContentEquals(
            content.copyOfRange(from, content.size),
            plain.copyOfRange(0, content.size - from)
        )

        // seeking gives up the truncation guarantee for this handle
        lateinit var info: DecryptedFileInfo
        assertDoesNotFail { info = groupApi.finishFileDecryption(handle) }
        assertEquals(groupId, info.groupId)
        assertFalse(info.complete)

        // read start to finish on a fresh handle to get the guarantee back
        assertTrue(openSealedFile(groupApi, envelope, cipher).second.complete)
    }

    @Test
    fun fileOperationsIncorrectHandle() {
        val content = fileContent(300_000)
        val (envelope, cipher) = sealFile(groupApi, groupId, content)

        // nonexistent handles
        assertFailsWith(PrivmxException::class) {
            groupApi.encryptFileChunk(-404, content.copyOfRange(0, 10))
        }
        assertFailsWith(PrivmxException::class) {
            groupApi.decryptFileChunk(-404, cipher.copyOfRange(0, 10))
        }
        assertFailsWith(PrivmxException::class) { groupApi.finishFileEncryption(-404) }
        assertFailsWith(PrivmxException::class) { groupApi.finishFileDecryption(-404) }

        val writeHandle = groupApi.beginFileEncryption(groupId, content.size.toLong())
        val readHandle = groupApi.beginFileDecryption(envelope)

        // open with a sealing handle
        assertFailsWith(PrivmxException::class) {
            groupApi.decryptFileChunk(writeHandle, cipher.copyOfRange(0, 10))
        }

        // seal with an opening handle
        assertFailsWith(PrivmxException::class) {
            groupApi.encryptFileChunk(readHandle, content.copyOfRange(0, 10))
        }

        // seek with a sealing handle
        assertFailsWith(PrivmxException::class) { groupApi.seekInEncryptedFile(writeHandle, 2) }

        // finish with a handle of the other kind
        assertFailsWith(PrivmxException::class) { groupApi.finishFileDecryption(writeHandle) }
        assertFailsWith(PrivmxException::class) { groupApi.finishFileEncryption(readHandle) }

        // a handle is released when it is finished
        groupApi.encryptFileChunk(writeHandle, content)
        groupApi.finishFileEncryption(writeHandle)
        assertFailsWith(PrivmxException::class) {
            groupApi.encryptFileChunk(writeHandle, content.copyOfRange(0, 10))
        }

        groupApi.decryptFileChunk(readHandle, cipher)
        groupApi.finishFileDecryption(readHandle)
        assertFailsWith(PrivmxException::class) {
            groupApi.decryptFileChunk(readHandle, cipher.copyOfRange(0, 10))
        }
    }

    @Test
    fun subscribeForGroupEvents() {
        val subscriptionIds = mutableListOf<String>()

        // subscribe for GroupEvents
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                groupApi.buildSubscriptionQuery(
                    GroupEventType.GROUP_CREATE,
                    GroupEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                groupApi.buildSubscriptionQuery(
                    GroupEventType.GROUP_UPDATE,
                    GroupEventSelectorType.GROUP_ID,
                    group3Id
                )
            )
            queries.add(
                groupApi.buildSubscriptionQuery(
                    GroupEventType.GROUP_DELETE,
                    GroupEventSelectorType.GROUP_ID,
                    group3Id
                )
            )
            subscriptionIds.addAll(groupApi.subscribeFor(queries))
        }
        assertEquals(3, subscriptionIds.size)

        // subscribe for events of the whole Context
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                groupApi.buildSubscriptionQuery(
                    GroupEventType.GROUP_UPDATE,
                    GroupEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                groupApi.buildSubscriptionQuery(
                    GroupEventType.GROUP_DELETE,
                    GroupEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            subscriptionIds.addAll(groupApi.subscribeFor(queries))
        }

        // subscribe again
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                groupApi.buildSubscriptionQuery(
                    GroupEventType.GROUP_CREATE,
                    GroupEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            subscriptionIds.addAll(groupApi.subscribeFor(queries))
        }

        // 2 same queries
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                groupApi.buildSubscriptionQuery(
                    GroupEventType.GROUP_UPDATE,
                    GroupEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                groupApi.buildSubscriptionQuery(
                    GroupEventType.GROUP_UPDATE,
                    GroupEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            subscriptionIds.addAll(groupApi.subscribeFor(queries))
        }

        // build query with a selector type the event type does not support
        assertFailsWith(PrivmxException::class) {
            groupApi.buildSubscriptionQuery(
                GroupEventType.GROUP_CREATE,
                GroupEventSelectorType.GROUP_ID,
                group3Id
            )
        }

        // subscribe with nonexisting selectorId
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                groupApi.buildSubscriptionQuery(
                    GroupEventType.GROUP_CREATE,
                    GroupEventSelectorType.CONTEXT_ID,
                    nonexistentId
                )
            )
            subscriptionIds.addAll(groupApi.subscribeFor(queries))
        }

        // subscribe with nonexisting groupId
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                groupApi.buildSubscriptionQuery(
                    GroupEventType.GROUP_UPDATE,
                    GroupEventSelectorType.GROUP_ID,
                    contextId!!
                )
            )
            subscriptionIds.addAll(groupApi.subscribeFor(queries))
        }

        // empty list of queries
        assertDoesNotFail {
            assertEquals(0, groupApi.subscribeFor(emptyList()).size)
        }

        assertDoesNotFail { groupApi.unsubscribeFrom(subscriptionIds) }
    }

    @Test
    fun sendCustomEvent() {
        val groupApi2 = connectAsUser2()
        val channelName = "typing"
        val eventData = "user is typing".encodeToByteArray()

        // incorrect groupId
        assertFailsWith(PrivmxException::class) {
            groupApi.sendCustomEvent(context2Id!!, channelName, eventData)
        }

        // a user who is not a member cannot notify the Group
        assertFailsWith(PrivmxException::class) {
            groupApi2.sendCustomEvent(groupId, channelName, eventData)
        }

        // forbidden characters in the channel name: only the subscription-query builder rejects
        // them - sendCustomEvent takes such a name without complaining, so a notification sent on
        // one can never be subscribed to
        listOf("chan/nel", "chan|nel", "chan,nel", "chan=nel").forEach { wrongChannel ->
            assertFailsWith(PrivmxException::class) {
                groupApi.buildCustomEventSubscriptionQuery(
                    wrongChannel,
                    GroupEventSelectorType.GROUP_ID,
                    groupId
                )
            }
        }

        // payload above the ~11 KB cap
        assertFailsWith(PrivmxException::class) {
            groupApi.sendCustomEvent(groupId, channelName, ByteArray(20 * 1024))
        }

        // correct - every member
        assertDoesNotFail {
            groupApi.sendCustomEvent(group2Id, channelName, eventData)
        }

        // correct - selected members only
        assertDoesNotFail {
            groupApi.sendCustomEvent(group2Id, channelName, eventData, listOf(user2Id!!))
        }

        // correct - a plain member may notify the Group as well
        assertDoesNotFail {
            groupApi2.sendCustomEvent(group3Id, channelName, eventData)
        }

        // subscribing for custom notifications
        val subscriptionIds = mutableListOf<String>()
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                groupApi.buildCustomEventSubscriptionQuery(
                    channelName,
                    GroupEventSelectorType.GROUP_ID,
                    group2Id
                )
            )
            queries.add(
                groupApi.buildCustomEventSubscriptionQuery(
                    channelName,
                    GroupEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            subscriptionIds.addAll(groupApi.subscribeFor(queries))
        }
        assertEquals(2, subscriptionIds.size)

        assertDoesNotFail { groupApi.unsubscribeFrom(subscriptionIds) }

        groupApi2.close()
    }

    @Test
    @Throws(Exception::class)
    fun accessAsPublicUser() {
        val connectionPublic: Connection = connectAsUser(ConnectionType.Public, bridgeAddress)
        val groupApiPublic = GroupApi(connectionPublic)
        val updateMeta = "meta".encodeToByteArray()

        // create group
        assertFailsWith(PrivmxException::class) {
            groupApiPublic.createGroup(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // get group
        assertFailsWith(PrivmxException::class) { groupApiPublic.getGroup(groupId) }

        // list groups
        assertFailsWith(PrivmxException::class) {
            groupApiPublic.listGroups(contextId!!, 0, 10, "desc")
        }

        // add members
        assertFailsWith(PrivmxException::class) {
            groupApiPublic.addGroupMembers(group3Id, listOf(member(users[1], roleUser)))
        }

        // remove members
        assertFailsWith(PrivmxException::class) {
            groupApiPublic.removeGroupMembers(group3Id, listOf(user2Id!!))
        }

        // update publicMeta
        assertFailsWith(PrivmxException::class) {
            groupApiPublic.updateGroupPublicMeta(group3Id, updateMeta, 1)
        }

        // update privateMeta
        assertFailsWith(PrivmxException::class) {
            groupApiPublic.updateGroupPrivateMeta(group3Id, updateMeta, 1)
        }

        // delete group
        assertFailsWith(PrivmxException::class) { groupApiPublic.deleteGroup(group3Id) }

        // encrypt
        assertFailsWith(PrivmxException::class) { groupApiPublic.encrypt(group3Id, updateMeta) }

        // seal a file
        assertFailsWith(PrivmxException::class) {
            groupApiPublic.beginFileEncryption(group3Id, updateMeta.size.toLong())
        }

        // send a custom event
        assertFailsWith(PrivmxException::class) {
            groupApiPublic.sendCustomEvent(group3Id, "typing", updateMeta)
        }

        // sealing anonymously needs public information only, so it works without an account
        val groupPubKey = groupApi.getGroup(group3Id).groupPubKey
        assertDoesNotFail {
            groupApiPublic.encryptAnonymously(group3Id, groupPubKey, updateMeta)
        }

        groupApiPublic.close()
        connectionPublic.close()
    }

    @Test
    fun closeGroupApi() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val groupApi2 = GroupApi(connection2!!)

        // close correct
        assertDoesNotFail { groupApi2.close() }

        // close once again
        assertFailsWith(IllegalStateException::class) { groupApi2.close() }
    }
}