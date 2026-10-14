package E2ETests

import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicyWithoutItem
import com.simplito.kotlin.privmx_endpoint.model.GroupGrant
import com.simplito.kotlin.privmx_endpoint.model.GroupGrantWithKey
import com.simplito.kotlin.privmx_endpoint.model.GroupMemberToAdd
import com.simplito.kotlin.privmx_endpoint.model.GroupRole
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.group.GroupApi
import com.simplito.kotlin.privmx_endpoint.modules.inbox.InboxApi
import com.simplito.kotlin.privmx_endpoint.modules.kvdb.KvdbApi
import com.simplito.kotlin.privmx_endpoint.modules.store.StoreApi
import com.simplito.kotlin.privmx_endpoint.modules.stream.StreamApiLow
import com.simplito.kotlin.privmx_endpoint.modules.thread.ThreadApi
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue


class ContainerGroupsTest : BaseTest() {

    private companion object {
        val CONTENT = "content".encodeToByteArray()
    }

    // What a container looks like through the group-grant lens, whichever one it is
    private class View(
        val version: Long,
        val groups: List<GroupGrant>,
        val staleGroups: List<String>,
        val policy: ContainerPolicyWithoutItem?
    )

    // One container type, reduced to the operations that take or report group grants.
    private class Container(
        val name: String,
        val create: (
            users: List<UserWithPubKey>,
            managers: List<UserWithPubKey>,
            groups: List<GroupGrantWithKey>,
            policies: ContainerPolicy?
        ) -> String,
        val get: (id: String) -> View,
        val update: (
            id: String,
            users: List<UserWithPubKey>,
            managers: List<UserWithPubKey>,
            version: Long,
            groups: List<GroupGrantWithKey>
        ) -> Unit,
        val rotate: (
            id: String,
            users: List<UserWithPubKey>,
            managers: List<UserWithPubKey>,
            version: Long,
            force: Boolean,
            groups: List<GroupGrantWithKey>
        ) -> Unit,
        val delete: (id: String) -> Unit,
        // Adds new content: a message, a file, an entry
        val write: (id: String) -> Unit
    )


    // The four container APIs on one connection. Inner, so the lambdas can read [publicMeta] and [privateMeta] off [BaseTest].
    private inner class Apis(connection: Connection, val groupApi: GroupApi?) {
        val threadApi = ThreadApi(connection, groupApi)
        val storeApi = StoreApi(connection, groupApi)
        val kvdbApi = KvdbApi(connection, groupApi)
        val inboxApi = InboxApi(connection, threadApi, storeApi, groupApi)
        val streamApi = StreamApiLow(connection, groupApi)

        val containers: List<Container> = listOf(
            Container(
                name = "Thread",
                create = { u, m, groups, policies ->
                    threadApi.createThread(
                        contextId!!,
                        u,
                        m,
                        publicMeta.encodeToByteArray(),
                        privateMeta.encodeToByteArray(),
                        policies = policies,
                        groups = groups
                    )
                },
                get = { id ->
                    threadApi.getThread(id)
                        .let { View(it.version!!, it.groups, it.staleGroups, it.policy) }
                },
                update = { id, u, m, version, groups ->
                    threadApi.updateThread(
                        id,
                        u,
                        m,
                        publicMeta.encodeToByteArray(),
                        privateMeta.encodeToByteArray(),
                        version,
                        groups = groups
                    )
                },
                rotate = { id, u, m, version, force, groups ->
                    threadApi.rotateThreadKeys(id, u, m, version, force, groups)
                },
                delete = { id -> threadApi.deleteThread(id) },
                write = { id ->
                    threadApi.sendMessage(
                        id,
                        publicMeta.encodeToByteArray(),
                        privateMeta.encodeToByteArray(),
                        CONTENT
                    )
                }
            ),
            Container(
                name = "Store",
                create = { u, m, groups, policies ->
                    storeApi.createStore(
                        contextId!!,
                        u,
                        m,
                        publicMeta.encodeToByteArray(),
                        privateMeta.encodeToByteArray(),
                        policies = policies,
                        groups = groups
                    )
                },
                get = { id ->
                    storeApi.getStore(id)
                        .let { View(it.version!!, it.groups, it.staleGroups, it.policy) }
                },
                update = { id, u, m, version, groups ->
                    storeApi.updateStore(
                        id,
                        u,
                        m,
                        publicMeta.encodeToByteArray(),
                        privateMeta.encodeToByteArray(),
                        version,
                        groups = groups
                    )
                },
                rotate = { id, u, m, version, force, groups ->
                    storeApi.rotateStoreKeys(id, u, m, version, force, groups)
                },
                delete = { id -> storeApi.deleteStore(id) },
                write = { id ->
                    val handle = storeApi.createFile(
                        id,
                        publicMeta.encodeToByteArray(),
                        privateMeta.encodeToByteArray(),
                        CONTENT.size.toLong()
                    )!!
                    storeApi.writeToFile(handle, CONTENT)
                    storeApi.closeFile(handle)
                }
            ),
            Container(
                name = "Kvdb",
                create = { u, m, groups, policies ->
                    kvdbApi.createKvdb(
                        contextId!!,
                        u,
                        m,
                        publicMeta.encodeToByteArray(),
                        privateMeta.encodeToByteArray(),
                        policies = policies,
                        groups = groups
                    )
                },
                get = { id ->
                    kvdbApi.getKvdb(id)
                        .let { View(it.version!!, it.groups, it.staleGroups, it.policy) }
                },
                update = { id, u, m, version, groups ->
                    kvdbApi.updateKvdb(
                        id,
                        u,
                        m,
                        publicMeta.encodeToByteArray(),
                        privateMeta.encodeToByteArray(),
                        version,
                        force = false,
                        groups = groups
                    )
                },
                rotate = { id, u, m, version, force, groups ->
                    kvdbApi.rotateKvdbKeys(id, u, m, version, force, groups)
                },
                delete = { id -> kvdbApi.deleteKvdb(id) },
                write = { id ->
                    kvdbApi.setEntry(
                        id,
                        "key-${Random.nextLong()}",
                        publicMeta.encodeToByteArray(),
                        privateMeta.encodeToByteArray(),
                        CONTENT
                    )
                }
            ),
            Container(
                name = "Inbox",
                create = { u, m, groups, policies ->
                    inboxApi.createInbox(
                        contextId!!,
                        u,
                        m,
                        publicMeta.encodeToByteArray(),
                        privateMeta.encodeToByteArray(),
                        policies = policies,
                        groups = groups
                    )
                },
                get = { id ->
                    inboxApi.getInbox(id)
                        .let { View(it.version!!, it.groups, it.staleGroups, it.policy) }
                },
                update = { id, u, m, version, groups ->
                    inboxApi.updateInbox(
                        id,
                        u,
                        m,
                        publicMeta.encodeToByteArray(),
                        privateMeta.encodeToByteArray(),
                        version = version,
                        groups = groups
                    )
                },
                rotate = { id, u, m, version, force, groups ->
                    inboxApi.rotateInboxKeys(id, u, m, version, force, groups)
                },
                delete = { id -> inboxApi.deleteInbox(id) },
                write = { id -> inboxApi.sendEntry(inboxApi.prepareEntry(id, CONTENT)!!) }
            )
        )

        fun container(name: String): Container = containers.first { it.name == name }

        fun close() {
            listOf(streamApi, inboxApi, kvdbApi, storeApi, threadApi).forEach {
                try {
                    it.close()
                } catch (_: Exception) {
                }
            }
        }
    }

    // Container APIs without Group support.
    private val plainApis: Apis by lazy {
        Apis(connection!!, null).also { apisToClose.add(it::close) }
    }

    private val groupApi: GroupApi by lazy {
        GroupApi(connection!!).also { apisToClose.add(it::close) }
    }

    // Container APIs with Group support.
    private val apis: Apis by lazy {
        Apis(connection!!, groupApi).also { apisToClose.add(it::close) }
    }

    // Everything created by a test, together with the Container that can delete it.
    private val created = mutableListOf<Pair<Container, String>>()

    // APIs created during a test, closed in reverse order after it.
    private val apisToClose = mutableListOf<() -> Unit>()

    private val createdGroups = mutableListOf<String>()

    // Stream Rooms have no Container entry (no content to write), so they are handled separately.
    private val streamApi: StreamApiLow by lazy {
        StreamApiLow(connection!!, groupApi).also { apisToClose.add(it::close) }
    }

    private val createdStreamRooms = mutableListOf<String>()


    @BeforeTest
    fun beforeTest() {
        connection = connectAsUser(ConnectionType.User1, bridgeAddress)
    }

    @AfterTest
    fun afterTest() {
        created.asReversed().forEach { (container, id) ->
            try {
                container.delete(id)
            } catch (_: Exception) {
            }
        }
        created.clear()

        createdStreamRooms.asReversed().forEach {
            try {
                streamApi.deleteStreamRoom(it)
            } catch (_: Exception) {
            }
        }
        createdStreamRooms.clear()

        if (createdGroups.isNotEmpty()) {
            val notDeleted = createdGroups.filterNot {
                try {
                    groupApi.deleteGroup(it)
                    true
                } catch (_: Exception) {
                    false
                }
            }
            // Groups user1 was removed from can only be deleted by user2, who stayed as a manager
            if (notDeleted.isNotEmpty()) {
                try {
                    connectAsUser(ConnectionType.User2, bridgeAddress).use { user2Connection ->
                        GroupApi(user2Connection).use { user2GroupApi ->
                            notDeleted.forEach {
                                try {
                                    user2GroupApi.deleteGroup(it)
                                } catch (_: Exception) {
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                }
            }
            createdGroups.clear()
        }

        apisToClose.asReversed().forEach {
            try {
                it()
            } catch (_: Exception) {
            }
        }
        apisToClose.clear()

        connection?.close()?.also { connection = null }
        try {
            connection2?.close()
        } finally {
            connection2 = null
        }
    }

    // Creates a container addressed to user1 alone, so any second-user access comes from a grant.
    private fun Container.new(
        groups: List<GroupGrantWithKey> = emptyList(),
        containerUsers: List<UserWithPubKey> = users.subList(0, 1),
        containerManagers: List<UserWithPubKey> = users.subList(0, 1),
        policies: ContainerPolicy? = null
    ): String =
        create(containerUsers, containerManagers, groups, policies).also { created.add(this to it) }


    // Runs [body] with a second connection as user2 and its own set of APIs.
    private fun <T> executeAsUser2(withGroupApi: Boolean, body: (Apis) -> T): T {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val user2GroupApi = if (withGroupApi) GroupApi(connection2!!) else null
        val user2Apis = Apis(connection2!!, user2GroupApi)
        try {
            return body(user2Apis)
        } finally {
            user2Apis.close()
            try {
                user2GroupApi?.close()
            } catch (_: Exception) {
            }
        }
    }

    private fun newGroup(
        groupUsers: List<UserWithPubKey> = users.subList(0, 1),
        groupManagers: List<UserWithPubKey> = users.subList(0, 1)
    ): String = groupApi.createGroup(
        contextId!!,
        groupUsers,
        groupManagers,
        publicMeta.encodeToByteArray(),
        privateMeta.encodeToByteArray()
    ).also { createdGroups.add(it) }

    // A Group owned by user1 with user2 seated in it.
    private fun newGroupWithUser2(role: String = GroupRole.USER): String = newGroup().also {
        groupApi.addGroupMembers(it, listOf(GroupMemberToAdd(users[1], role)))
    }

    // Reads the Group's current epoch key, which is what a grant has to carry.
    private fun grantFor(
        groupId: String,
        role: String = GroupRole.USER,
        api: GroupApi = groupApi
    ): GroupGrantWithKey =
        api.getGroup(groupId).let {
            GroupGrantWithKey(
                groupId = it.groupId,
                role = role,
                groupPubKey = it.groupPubKey,
                groupEpoch = it.keyVersion
            )
        }

    @Test
    fun containersReportNoGroupsWhenNoneGranted() {
        plainApis.containers.forEach { container ->
            val view = container.get(container.new())

            assertTrue(
                view.groups.isEmpty(),
                "${container.name}: expected no grants, got ${view.groups}"
            )
            assertTrue(
                view.staleGroups.isEmpty(),
                "${container.name}: expected no stale groups, got ${view.staleGroups}"
            )
        }
    }

    @Test
    fun rotateKeysWithoutGroups() {
        plainApis.containers.forEach { container ->
            val id = container.new()
            val before = container.get(id)

            assertDoesNotFail("${container.name}: re-key without groups") {
                container.rotate(
                    id,
                    users.subList(0, 1),
                    users.subList(0, 1),
                    before.version,
                    false,
                    emptyList()
                )
            }

            // the container survives the re-key and is still readable
            assertTrue(
                container.get(id).groups.isEmpty(),
                "${container.name}: a re-key adds no grants"
            )
        }
    }

    // A re-key is version-guarded like an update, unless forced.
    @Test
    fun rotateKeysRejectsStaleVersion() {
        plainApis.containers.forEach { container ->
            val id = container.new()
            val version = container.get(id).version


            assertFailsWith(
                PrivmxException::class,
                "${container.name}: a stale version must be rejected"
            ) {
                container.rotate(
                    id,
                    users.subList(0, 1),
                    users.subList(0, 1),
                    version - 1,
                    false,
                    emptyList()
                )
            }

            assertDoesNotFail("${container.name}: a forced re-key ignores the version") {
                container.rotate(
                    id,
                    users.subList(0, 1),
                    users.subList(0, 1),
                    version - 1,
                    true,
                    emptyList()
                )
            }
        }
    }


    // A grant naming a Group that does not exist
    @Test
    fun createRejectsUnknownGroupId() {
        val grants = listOf(
            GroupGrantWithKey(
                groupId = "00000000-0000-0000-0000-000000000000",
                role = GroupRole.USER,
                groupPubKey = users[1].pubKey,
                groupEpoch = 1
            )
        )

        plainApis.containers.forEach { container ->
            assertFailsWith(
                PrivmxException::class,
                "${container.name}: a grant for an unknown group must be rejected"
            ) {
                container.new(grants)
            }
        }
    }

    @Test
    fun grantGroupOnCreate() {
        val groupId = newGroupWithUser2()
        val grant = grantFor(groupId)

        apis.containers.forEach { container ->
            val groups = container.get(container.new(listOf(grant))).groups

            assertEquals(
                1,
                groups.size,
                "${container.name}: expected exactly one grant, got $groups"
            )
            assertEquals(groupId, groups[0].groupId, "${container.name}: wrong groupId in grant")
            assertEquals(GroupRole.USER, groups[0].role, "${container.name}: wrong role in grant")
        }
    }

    // An update can add a grant to a container that had none.
    @Test
    fun grantGroupOnUpdate() {
        val groupId = newGroupWithUser2()
        val grant = grantFor(groupId)

        apis.containers.forEach { container ->
            val id = container.new()
            assertTrue(
                container.get(id).groups.isEmpty(),
                "${container.name}: a container created without grants should have none before the update, " +
                        "got ${container.get(id).groups}"
            )

            container.update(
                id,
                users.subList(0, 1),
                users.subList(0, 1),
                container.get(id).version,
                listOf(grant)
            )

            val groups = container.get(id).groups
            assertEquals(
                1,
                groups.size,
                "${container.name}: update should add one grant, got $groups"
            )
            assertEquals(
                groupId,
                groups[0].groupId,
                "${container.name}: wrong groupId after update"
            )
        }
    }

    @Test
    fun updateWithEmptyGroupsRevokesEveryGrant() {
        val groupId = newGroupWithUser2()
        val grant = grantFor(groupId)

        apis.containers.forEach { container ->
            val id = container.new(listOf(grant))
            assertEquals(
                1,
                container.get(id).groups.size,
                "${container.name}: a container created with one grant should have it before the update, " +
                        "got ${container.get(id).groups}"
            )

            container.update(
                id,
                users.subList(0, 1),
                users.subList(0, 1),
                container.get(id).version,
                emptyList()
            )

            assertTrue(
                container.get(id).groups.isEmpty(),
                "${container.name}: an empty groups list must revoke every grant"
            )
        }
    }


    // A Group can't be deleted while any container still grants it, revoking the grant unblocks it.
    @Test
    fun groupCannotBeDeletedWhileGrantedToContainer() {
        apis.containers.forEach { container ->
            val groupId = newGroup()
            val id = container.new(listOf(grantFor(groupId)))

            assertFailsWith(
                PrivmxException::class,
                "${container.name}: a Group granted to a container must not be deletable"
            ) {
                groupApi.deleteGroup(groupId)
            }

            container.update(
                id,
                users.subList(0, 1),
                users.subList(0, 1),
                container.get(id).version,
                emptyList()
            )

            groupApi.deleteGroup(groupId)
            createdGroups.remove(groupId)
            assertFailsWith(
                PrivmxException::class,
                "${container.name}: a deleted Group must not be readable"
            ) {
                groupApi.getGroup(groupId)
            }
        }
    }

    @Test
    fun groupCannotBeDeletedWhileGrantedToStreamRoom() {
        val groupId = newGroup()
        val id = newStreamRoom(groups = listOf(grantFor(groupId)))

        assertFailsWith(
            PrivmxException::class,
            "StreamRoom: a Group granted to a Stream Room must not be deletable"
        ) {
            groupApi.deleteGroup(groupId)
        }

        streamApi.updateStreamRoom(
            id,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            streamApi.getStreamRoom(id).version,
            force = false,
            forceGenerateNewKey = false,
            groups = emptyList()
        )

        assertDoesNotFail {
            groupApi.deleteGroup(groupId)
            createdGroups.remove(groupId)
        }
        assertFailsWith(
            PrivmxException::class,
            "StreamRoom: a deleted Group must not be readable"
        ) {
            groupApi.getGroup(groupId)
        }
    }

    // A member of a granted Group reads the container without ever
    // appearing in its `users` or `managers` - every container here is addressed to user1 alone.

    @Test
    fun groupMemberCanReadGrantedContainer() {
        val groupId = newGroupWithUser2()
        val grant = grantFor(groupId)
        val ids = apis.containers.associate { it.name to it.new(listOf(grant)) }

        executeAsUser2(withGroupApi = true) { user2 ->
            user2.containers.forEach { container ->
                val id = ids.getValue(container.name)
                assertDoesNotFail("${container.name}: user2 reads through the group") {
                    container.get(
                        id
                    )
                }
                assertEquals(
                    groupId,
                    container.get(id).groups.single().groupId,
                    "${container.name}: user2 should see the grant it reads through"
                )
            }
        }
    }

    // A user in no granted Group still cannot read the container.
    @Test
    fun nonMemberStillCannotReadContainer() {
        val grant = grantFor(newGroup()) // user2 not added
        val ids = apis.containers.associate { it.name to it.new(listOf(grant)) }

        executeAsUser2(withGroupApi = true) { user2 ->
            user2.containers.forEach { container ->
                assertFailsWith(
                    PrivmxException::class,
                    "${container.name}: user2 is in no granted group and must not read it"
                ) {
                    container.get(ids.getValue(container.name))
                }
            }
        }
    }

    // Removing a member rotates the Group's key, which leaves every container still wrapped to the old epoch.
    // Those containers must report the Group in `staleGroups`, and `rotateXxxKeys` must clear it without touching the grant itself.

    @Test
    fun groupRekeyMarksContainerStaleAndRotateClearsIt() {
        val groupId = newGroupWithUser2()
        val ids = apis.containers.associate { it.name to it.new(listOf(grantFor(groupId))) }

        val epochBefore = groupApi.getGroup(groupId).keyVersion
        groupApi.removeGroupMembers(groupId, listOf(user2Id!!))
        val epochAfter = groupApi.getGroup(groupId).keyVersion
        assertTrue(
            epochAfter > epochBefore,
            "precondition: removing a member should advance the group epoch ($epochBefore -> $epochAfter)"
        )

        apis.containers.forEach { container ->
            val id = ids.getValue(container.name)
            assertTrue(
                container.get(id).staleGroups.contains(groupId),
                "${container.name}: expected $groupId in staleGroups after the group re-keyed, got " + "${
                    container.get(
                        id
                    ).staleGroups
                }"
            )

            assertDoesNotFail("${container.name}: re-key of a stale container") {
                container.rotate(
                    id,
                    users.subList(0, 1),
                    users.subList(0, 1),
                    container.get(id).version,
                    false,
                    emptyList()
                )
            }

            val after = container.get(id)
            assertTrue(
                after.staleGroups.isEmpty(),
                "${container.name}: a re-key must clear staleGroups, got ${after.staleGroups}"
            )
            assertEquals(
                groupId,
                after.groups.single().groupId,
                "${container.name}: a re-key changes no grants"
            )
        }
    }

    // An update that passes the current grants (built from freshly fetched Groups) re-keys a stale
    // container on its own, so no `rotate...Keys` is needed afterwards
    @Test
    fun updateWithFreshGrantsClearsStaleGroups() {
        val groupId = newGroupWithUser2()
        val ids = apis.containers.associate { it.name to it.new(listOf(grantFor(groupId))) }

        groupApi.removeGroupMembers(groupId, listOf(user2Id!!))
        val freshGrant = grantFor(groupId)

        apis.containers.forEach { container ->
            val id = ids.getValue(container.name)
            assertTrue(
                container.get(id).staleGroups.contains(groupId),
                "${container.name}: precondition - expected $groupId in staleGroups"
            )

            container.update(
                id,
                users.subList(0, 1),
                users.subList(0, 1),
                container.get(id).version,
                listOf(freshGrant)
            )

            val after = container.get(id)
            assertTrue(
                after.staleGroups.isEmpty(),
                "${container.name}: an update with fresh grants must clear staleGroups, got ${after.staleGroups}"
            )
            assertEquals(
                groupId,
                after.groups.single().groupId,
                "${container.name}: the grant must stay in place"
            )
        }
    }


    // `rotate...Keys` is callable by any member, not only a manager - that is what separates it from
    // `update...`, whose `update` policy is manager-only. user2 is a plain user on every container here
    @Test
    fun rotateKeysIsAllowedForPlainMember() {
        val ids = plainApis.containers.associate { container ->
            container.name to container.new(
                containerUsers = users,
                containerManagers = users.subList(0, 1)
            )
        }

        executeAsUser2(withGroupApi = false) { user2 ->
            user2.containers.forEach { container ->
                val id = ids.getValue(container.name)
                val version = container.get(id).version

                assertDoesNotFail("${container.name}: a plain member must be able to re-key") {
                    container.rotate(id, users, users.subList(0, 1), version, false, emptyList())
                }
            }
        }
    }

    // A caller outside all the container's grantee Groups, who passes no Group keys, cannot re-key it and gets a PrivmxException
    @Test
    fun rotateKeysFailsWhenGranteeCannotBeResolved() {
        // a Group user2 is NOT in, granted on containers user2 IS a manager of
        val grant = grantFor(newGroup())
        val ids = apis.containers.associate { container ->
            container.name to container.new(
                groups = listOf(grant),
                containerUsers = users,
                containerManagers = users
            )
        }

        executeAsUser2(withGroupApi = true) { user2 ->
            user2.containers.forEach { container ->
                val id = ids.getValue(container.name)
                val version = container.get(id).version

                assertFailsWith(
                    PrivmxException::class,
                    "${container.name}: a caller outside every grantee group must not be able to re-key"
                ) {
                    container.rotate(id, users, users, version, false, emptyList())
                }
            }
        }
    }

    // A caller outside the grantee Group can still re-key the container if it passes the Group's key itself.
    @Test
    fun rotateKeysSucceedsWhenGranteeKeyIsSupplied() {
        val grant = grantFor(newGroup())
        val ids = apis.containers.associate { container ->
            container.name to container.new(
                groups = listOf(grant),
                containerUsers = users,
                containerManagers = users
            )
        }

        executeAsUser2(withGroupApi = true) { user2 ->
            user2.containers.forEach { container ->
                val id = ids.getValue(container.name)
                val version = container.get(id).version

                assertDoesNotFail("${container.name}: re-key with a supplied grantee key") {
                    container.rotate(id, users, users, version, false, listOf(grant))
                }
            }
        }
    }


    // Granting a Group access to an Inbox also gives it access to the Inbox's entries and files,
    // which are kept in an inner Thread and Store
    @Test
    fun inboxGrantsPropagateToInnerContainers() {
        val groupId = newGroupWithUser2()
        val inboxId = apis.container("Inbox").new(listOf(grantFor(groupId)))

        // an entry with a file: the entry lives in the inner Thread, the file in the inner Store
        val entryData = "entry".encodeToByteArray()
        val fileContent = ByteArray(10_000) { (it % 251).toByte() }
        val inboxApi = apis.inboxApi
        val fileHandle = inboxApi.createFileHandle(
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            fileContent.size.toLong()
        )!!
        val entryHandle = inboxApi.prepareEntry(inboxId, entryData, listOf(fileHandle))!!
        inboxApi.writeToFile(entryHandle, fileHandle, fileContent)
        inboxApi.sendEntry(entryHandle)

        executeAsUser2(withGroupApi = true) { user2 ->
            assertDoesNotFail("user2 reads the Inbox through the group") {
                user2.inboxApi.getInbox(
                    inboxId
                )
            }

            val entry = user2.inboxApi.listEntries(inboxId, 0L, 10L).readItems.first()
            assertEquals(
                0L,
                entry.statusCode,
                "user2 should decrypt the entry kept in the inner Thread"
            )
            assertContentEquals(entryData, entry.data, "user2 should read the entry data")

            val fileId = entry.files.single().info.fileId
            val handle = user2.inboxApi.openFile(fileId)!!
            val read = user2.inboxApi.readFromFile(handle, fileContent.size.toLong())
            user2.inboxApi.closeFile(handle)
            assertContentEquals(
                fileContent,
                read,
                "user2 should read the file kept in the inner Store"
            )
        }
    }

    // A container created without policies leaves both new ones unset.
    @Test
    fun rotateKeysAndForwardSecrecyAreUnsetByDefault() {
        plainApis.containers.forEach { container ->
            val policy = container.get(container.new()).policy

            assertEquals(
                null,
                policy?.rotateKeys,
                "${container.name}: rotateKeys should be unset by default"
            )
            assertEquals(
                null,
                policy?.forwardSecrecy,
                "${container.name}: forwardSecrecy should be unset by default"
            )
        }
    }

    // Both policies passed to create come back unchanged when the container is read.
    @Test
    fun rotateKeysAndForwardSecrecyRoundTrip() {
        listOf("manager" to "yes", "user" to "no").forEach { (rotateKeys, forwardSecrecy) ->
            plainApis.containers.forEach { container ->
                val policy = container.get(
                    container.new(
                        policies = ContainerPolicy(
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            rotateKeys,
                            forwardSecrecy,
                            null
                        )
                    )
                ).policy

                assertEquals(rotateKeys, policy?.rotateKeys, "${container.name}: wrong rotateKeys")
                assertEquals(
                    forwardSecrecy,
                    policy?.forwardSecrecy,
                    "${container.name}: wrong forwardSecrecy"
                )
            }
        }
    }

    // With `rotateKeys = "manager"`, only managers can re-key the container, plain members can't.
    @Test
    fun rotateKeysManagerPolicyRefusesPlainMember() {
        val ids = plainApis.containers.associate { container ->
            container.name to container.new(
                containerUsers = users,
                containerManagers = users.subList(0, 1),
                policies = ContainerPolicy(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    rotateKeys = "manager",
                    forwardSecrecy = null,
                    null
                )
            )
        }

        executeAsUser2(withGroupApi = false) { user2 ->
            user2.containers.forEach { container ->
                val id = ids.getValue(container.name)
                assertFailsWith(
                    PrivmxException::class,
                    "${container.name}: a plain member must not re-key under rotateKeys = \"manager\""
                ) {
                    container.rotate(
                        id,
                        users,
                        users.subList(0, 1),
                        container.get(id).version,
                        false,
                        emptyList()
                    )
                }
            }
        }

        plainApis.containers.forEach { container ->
            val id = ids.getValue(container.name)
            assertDoesNotFail("${container.name}: a manager can still re-key") {
                container.rotate(
                    id,
                    users,
                    users.subList(0, 1),
                    container.get(id).version,
                    false,
                    emptyList()
                )
            }
        }
    }

    private fun newStreamRoom(
        roomUsers: List<UserWithPubKey> = users.subList(0, 1),
        roomManagers: List<UserWithPubKey> = users.subList(0, 1),
        policies: ContainerPolicyWithoutItem? = null,
        groups: List<GroupGrantWithKey> = emptyList()
    ): String = streamApi.createStreamRoom(
        contextId!!,
        roomUsers,
        roomManagers,
        publicMeta.encodeToByteArray(),
        privateMeta.encodeToByteArray(),
        policies,
        groups = groups
    ).also { createdStreamRooms.add(it) }

    // A Stream Room created without policies leaves both new ones unset.
    @Test
    fun streamRoomRotateKeysAndForwardSecrecyAreUnsetByDefault() {
        val policy = streamApi.getStreamRoom(newStreamRoom()).policy

        assertEquals(null, policy.rotateKeys, "StreamRoom: rotateKeys should be unset by default")
        assertEquals(
            null,
            policy.forwardSecrecy,
            "StreamRoom: forwardSecrecy should be unset by default"
        )
    }

    // Both policies passed to createStreamRoom come back unchanged.
    @Test
    fun streamRoomRotateKeysAndForwardSecrecyRoundTrip() {
        listOf("manager" to "yes", "user" to "no").forEach { (rotateKeys, forwardSecrecy) ->
            val policy = streamApi.getStreamRoom(
                newStreamRoom(
                    policies = ContainerPolicy(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        rotateKeys,
                        forwardSecrecy,
                        null
                    )
                )
            ).policy

            assertEquals(rotateKeys, policy.rotateKeys, "StreamRoom: wrong rotateKeys")
            assertEquals(forwardSecrecy, policy.forwardSecrecy, "StreamRoom: wrong forwardSecrecy")
        }
    }

    // With `rotateKeys = "manager"`, only managers can re-key the Stream Room, plain members can't.
    @Test
    fun streamRoomRotateKeysManagerPolicyRefusesPlainMember() {
        val id = newStreamRoom(
            roomUsers = users,
            roomManagers = users.subList(0, 1),
            policies = ContainerPolicy(
                null,
                null,
                null,
                null,
                null,
                null,
                rotateKeys = "manager",
                forwardSecrecy = null,
                null
            )
        )

        executeAsUser2(withGroupApi = false) {
            StreamApiLow(connection2!!).use { user2StreamApi ->
                assertFailsWith(
                    PrivmxException::class,
                    "StreamRoom: a plain member must not re-key under rotateKeys = \"manager\""
                ) {
                    user2StreamApi.rotateStreamRoomKeys(
                        id,
                        users,
                        users.subList(0, 1),
                        user2StreamApi.getStreamRoom(id).version
                    )
                }
            }
        }

        assertDoesNotFail("StreamRoom: a manager can still re-key") {
            streamApi.rotateStreamRoomKeys(
                id,
                users,
                users.subList(0, 1),
                streamApi.getStreamRoom(id).version
            )
        }
    }


    // Prepares stale containers for the forward-secrecy tests and runs [body] as user2.
    //
    // user1 manages each container. user2 is a plain member who can't re-key it, and also has access
    // through a Group. user2 removes user1 from the Group, so the Group gets a new key and every
    // container becomes stale.
    private fun executeAsUser2WithStaleContainers(
        forwardSecrecy: String,
        body: (user2: Apis, ids: Map<String, String>) -> Unit
    ) {
        // user1 (user & manager), user2 (manager)
        val groupId = newGroupWithUser2(role = GroupRole.MANAGER)
        val grant = grantFor(groupId)
        val ids = apis.containers.associate { container ->
            container.name to container.new(
                groups = listOf(grant),
                containerUsers = users,
                containerManagers = users.subList(0, 1),
                policies = ContainerPolicy(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    rotateKeys = "manager",
                    forwardSecrecy = forwardSecrecy,
                    null
                )
            )
        }

        executeAsUser2(withGroupApi = true) { user2 ->
            GroupApi(connection2!!).use { it.removeGroupMembers(groupId, listOf(user1Id!!)) }

            apis.containers.forEach { container ->
                assertEquals(
                    listOf(groupId),
                    container.get(ids.getValue(container.name)).staleGroups,
                    "${container.name}: the container should be stale after the group re-keyed"
                )
            }
            body(user2, ids)
        }
    }

    // With `forwardSecrecy = "yes"`, a stale container blocks writes, and a blocked write changes nothing
    @Test
    fun forwardSecrecyBlocksWritesWhileStale() {
        executeAsUser2WithStaleContainers(forwardSecrecy = "yes") { user2, ids ->
            user2.containers.forEach { container ->
                val id = ids.getValue(container.name)
                val before = container.get(id)

                assertFailsWith(
                    PrivmxException::class,
                    "${container.name}: content must not be accepted under a superseded group epoch"
                ) {
                    container.write(id)
                }

                val after = container.get(id)
                assertEquals(
                    before.version,
                    after.version,
                    "${container.name}: a refused write must not re-key"
                )
                assertEquals(
                    before.staleGroups,
                    after.staleGroups,
                    "${container.name}: still stale"
                )
            }
        }
    }

    // With `forwardSecrecy = "no"`, a stale Inbox still accepts new entries
    @Test
    fun forwardSecrecyOffAllowsInboxEntriesWhileStale() {
        executeAsUser2WithStaleContainers(forwardSecrecy = "no") { user2, ids ->
            val inbox = user2.container("Inbox")
            assertDoesNotFail("Inbox: sending an entry should be allowed without forward secrecy") {
                inbox.write(ids.getValue(inbox.name))
            }
        }
    }

    // With `forwardSecrecy = "no"`, a stale container should still accept writes from a member who
    // can't re-key it.
    @Test
//    @Ignore // todo - Thread, Store and Kvdb refuse to write under forwardSecrecy = "no" as well - see above.
    fun forwardSecrecyOffAllowsWritesWhileStale() {
        executeAsUser2WithStaleContainers(forwardSecrecy = "no") { user2, ids ->
            user2.containers.filter { it.name != "Inbox" }.forEach { container ->
                assertDoesNotFail("${container.name}: writing should be allowed without forward secrecy") {
                    container.write(ids.getValue(container.name))
                }
            }
        }
    }

    // Sends an entry with one file as user1 and returns its ID
    private fun sendEntryWithFile(inboxId: String, data: ByteArray, file: ByteArray): String {
        val inboxApi = apis.inboxApi
        val fileHandle = inboxApi.createFileHandle(
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            file.size.toLong()
        )!!
        val handle = inboxApi.prepareEntry(inboxId, data, listOf(fileHandle))!!
        inboxApi.writeToFile(handle, fileHandle, file)
        inboxApi.sendEntry(handle)
        return inboxApi.listEntries(inboxId, 0, 1, "desc").readItems.first().entryId
    }

    private fun readEntryFile(api: InboxApi, entryId: String, size: Int): ByteArray {
        val fileId = api.readEntry(entryId).files.first().info.fileId
        val handle = api.openFile(fileId)!!
        val content = api.readFromFile(handle, size.toLong())
        api.closeFile(handle)
        return content
    }

    // A member removed from the Group loses access to the Thread granted to the Group
    @Test
    fun removedMemberLosesAccessToGrantedThread() {
        val groupId = newGroup(groupUsers = users)
        val threadId = apis.container("Thread").new(listOf(grantFor(groupId)))
        val messageId = apis.threadApi.sendMessage(
            threadId,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "before".encodeToByteArray()
        )

        executeAsUser2(withGroupApi = true) { user2 ->
            val statusCode = user2.threadApi.getMessage(messageId).statusCode
            assertEquals(0L, statusCode, "user2 reads through the Group")

            groupApi.removeGroupMembers(groupId, listOf(user2Id!!))

            assertEquals(listOf(groupId), apis.threadApi.getThread(threadId).staleGroups)
            assertFailsWith(PrivmxException::class) { user2.threadApi.getThread(threadId) }
            assertFailsWith(PrivmxException::class, "removed before any re-key") {
                user2.threadApi.getMessage(messageId)
            }

            val thread = apis.threadApi.getThread(threadId)
            apis.threadApi.rotateThreadKeys(
                threadId,
                users.subList(0, 1),
                users.subList(0, 1),
                thread.version!!
            )

            assertTrue(apis.threadApi.getThread(threadId).staleGroups.isEmpty())
            assertFailsWith(PrivmxException::class, "removed member must not read it") {
                user2.threadApi.getMessage(messageId)
            }
        }
    }

    // After the Group's key changes, the Thread refuses new messages until it is re-keyed.
    // A user outside the Group can re-key only with the Group's key provided by a member.
    @Test
    fun staleThreadRefusesWritesUntilRekey() {
        val groupId = newGroup(groupUsers = users, groupManagers = users)
        val threadId = apis.container("Thread").new(listOf(grantFor(groupId)))
        val content = "after".encodeToByteArray()

        executeAsUser2(withGroupApi = true) { user2 ->
            // user1 leaves the Group, user2 stays
            user2.groupApi!!.removeGroupMembers(groupId, listOf(user1Id!!))
            assertEquals(listOf(groupId), apis.threadApi.getThread(threadId).staleGroups)

            // user1 cannot get the Group's key, so it can neither write nor re-key
            assertFailsWith(PrivmxException::class, "stale - writing must be refused") {
                apis.threadApi.sendMessage(
                    threadId,
                    publicMeta.encodeToByteArray(),
                    privateMeta.encodeToByteArray(),
                    content
                )
            }

            val version = apis.threadApi.getThread(threadId).version!!
            assertFailsWith(
                PrivmxException::class,
                "re-key without the Group's key must be refused"
            ) {
                apis.threadApi.rotateThreadKeys(
                    threadId,
                    users.subList(0, 1),
                    users.subList(0, 1),
                    version
                )
            }

            // with the key from user2 the re-key succeeds
            apis.threadApi.rotateThreadKeys(
                threadId,
                users.subList(0, 1),
                users.subList(0, 1),
                version,
                groups = listOf(grantFor(groupId, api = user2.groupApi!!))
            )
            assertTrue(apis.threadApi.getThread(threadId).staleGroups.isEmpty())

            val messageId = apis.threadApi.sendMessage(
                threadId,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                content
            )
            val statusCode = user2.threadApi.getMessage(messageId).statusCode
            assertEquals(0L, statusCode, "readable through the Group after the re-key")
            assertContentEquals(content, user2.threadApi.getMessage(messageId).data)
        }
    }

    // After the Group's key changes, the Inbox refuses new entries until it is re-keyed.
    // After the re-key a Group member reads both the entry (inner Thread) and its file (inner Store).
    @Test
    fun inboxRekeyCoversInnerThreadAndStore() {
        val groupId = newGroup(groupUsers = users, groupManagers = users)
        val inboxId = apis.container("Inbox").new(listOf(grantFor(groupId)))
        val data = "entry".encodeToByteArray()
        val file = ByteArray(10_000) { (it % 251).toByte() }

        executeAsUser2(withGroupApi = true) { user2 ->
            user2.groupApi!!.removeGroupMembers(groupId, listOf(user1Id!!))
            assertEquals(listOf(groupId), apis.inboxApi.getInbox(inboxId).staleGroups)

            assertFailsWith(PrivmxException::class, "stale - sending an entry must be refused") {
                sendEntryWithFile(inboxId, data, file)
            }

            val inbox = apis.inboxApi.getInbox(inboxId)
            apis.inboxApi.rotateInboxKeys(
                inboxId,
                users.subList(0, 1),
                users.subList(0, 1),
                inbox.version!!,
                false,
                listOf(grantFor(groupId, api = user2.groupApi!!))
            )
            assertTrue(apis.inboxApi.getInbox(inboxId).staleGroups.isEmpty())

            val entryId = sendEntryWithFile(inboxId, data, file)

            // entry - inner Thread
            val statusCode = user2.inboxApi.readEntry(entryId).statusCode
            assertEquals(0L, statusCode, "entry readable after the re-key")
            assertContentEquals(data, user2.inboxApi.readEntry(entryId).data)

            // file - inner Store
            assertContentEquals(file, readEntryFile(user2.inboxApi, entryId, file.size))
        }
    }

    // A member removed from the Group loses access to the KVDB granted to the Group
    @Test
    fun removedMemberLosesAccessToGrantedKvdb() {
        val groupId = newGroup(groupUsers = users)
        val kvdbId = apis.container("Kvdb").new(listOf(grantFor(groupId)))
        apis.kvdbApi.setEntry(
            kvdbId,
            "before",
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "before".encodeToByteArray()
        )

        executeAsUser2(withGroupApi = true) { user2 ->
            val statusCode = user2.kvdbApi.getEntry(kvdbId, "before").statusCode
            assertEquals(0L, statusCode, "user2 reads through the Group")

            groupApi.removeGroupMembers(groupId, listOf(user2Id!!))

            assertEquals(listOf(groupId), apis.kvdbApi.getKvdb(kvdbId).staleGroups)
            assertFailsWith(PrivmxException::class) { user2.kvdbApi.getKvdb(kvdbId) }
            assertFailsWith(PrivmxException::class, "removed before any re-key") {
                user2.kvdbApi.getEntry(kvdbId, "before")
            }

            val kvdb = apis.kvdbApi.getKvdb(kvdbId)
            apis.kvdbApi.rotateKvdbKeys(
                kvdbId,
                users.subList(0, 1),
                users.subList(0, 1),
                kvdb.version!!
            )

            assertTrue(apis.kvdbApi.getKvdb(kvdbId).staleGroups.isEmpty())
            assertFailsWith(PrivmxException::class, "removed member must not read it") {
                user2.kvdbApi.getEntry(kvdbId, "before")
            }
        }
    }

    // After the Group's key changes, the KVDB refuses new entries until it is re-keyed.
    // A user outside the Group can re-key only with the Group's key provided by a member.
    @Test
    fun staleKvdbRefusesWritesUntilRekey() {
        val groupId = newGroup(groupUsers = users, groupManagers = users)
        val kvdbId = apis.container("Kvdb").new(listOf(grantFor(groupId)))
        val content = "after".encodeToByteArray()

        executeAsUser2(withGroupApi = true) { user2 ->
            // user1 leaves the Group, user2 stays
            user2.groupApi!!.removeGroupMembers(groupId, listOf(user1Id!!))
            assertEquals(listOf(groupId), apis.kvdbApi.getKvdb(kvdbId).staleGroups)

            // user1 cannot get the Group's key, so it can neither write nor re-key
            assertFailsWith(PrivmxException::class, "stale - writing must be refused") {
                apis.kvdbApi.setEntry(
                    kvdbId,
                    "after",
                    publicMeta.encodeToByteArray(),
                    privateMeta.encodeToByteArray(),
                    content
                )
            }

            val version = apis.kvdbApi.getKvdb(kvdbId).version!!
            assertFailsWith(
                PrivmxException::class,
                "re-key without the Group's key must be refused"
            ) {
                apis.kvdbApi.rotateKvdbKeys(
                    kvdbId,
                    users.subList(0, 1),
                    users.subList(0, 1),
                    version
                )
            }

            // with the key from user2 the re-key succeeds
            apis.kvdbApi.rotateKvdbKeys(
                kvdbId,
                users.subList(0, 1),
                users.subList(0, 1),
                version,
                groups = listOf(grantFor(groupId, api = user2.groupApi!!))
            )
            assertTrue(apis.kvdbApi.getKvdb(kvdbId).staleGroups.isEmpty())

            apis.kvdbApi.setEntry(
                kvdbId,
                "after",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                content
            )
            val statusCode = user2.kvdbApi.getEntry(kvdbId, "after").statusCode
            assertEquals(0L, statusCode, "readable through the Group after the re-key")
            assertContentEquals(content, user2.kvdbApi.getEntry(kvdbId, "after").data)
        }
    }

    // A member removed from the Group loses access to the Stream Room granted to the Group
    @Test
    fun removedMemberLosesAccessToGrantedStreamRoom() {
        val groupId = newGroup(groupUsers = users)
        val roomId = newStreamRoom(groups = listOf(grantFor(groupId)))
        assertEquals(listOf(groupId), streamApi.getStreamRoom(roomId).groups.map { it.groupId })

        executeAsUser2(withGroupApi = true) { user2 ->
            val statusCode = user2.streamApi.getStreamRoom(roomId).statusCode
            assertEquals(0L, statusCode, "user2 reads through the Group")
            assertContentEquals(
                privateMeta.encodeToByteArray(),
                user2.streamApi.getStreamRoom(roomId).privateMeta
            )

            groupApi.removeGroupMembers(groupId, listOf(user2Id!!))

            assertEquals(listOf(groupId), streamApi.getStreamRoom(roomId).staleGroups)
            assertFailsWith(PrivmxException::class, "removed before any re-key") {
                user2.streamApi.getStreamRoom(roomId)
            }

            val room = streamApi.getStreamRoom(roomId)
            streamApi.rotateStreamRoomKeys(
                roomId,
                users.subList(0, 1),
                users.subList(0, 1),
                room.version
            )

            assertTrue(streamApi.getStreamRoom(roomId).staleGroups.isEmpty())
            assertFailsWith(PrivmxException::class, "removed member must not read it") {
                user2.streamApi.getStreamRoom(roomId)
            }
        }
    }

    // After the Group's key changes, a user outside the Group can re-key the Stream Room
    // only with the Group's key provided by a member.
    @Test
    fun streamRoomRekeyNeedsGroupKey() {
        val groupId = newGroup(groupUsers = users, groupManagers = users)
        val roomId = newStreamRoom(groups = listOf(grantFor(groupId)))

        executeAsUser2(withGroupApi = true) { user2 ->
            // user1 leaves the Group, user2 stays
            user2.groupApi!!.removeGroupMembers(groupId, listOf(user1Id!!))
            assertEquals(listOf(groupId), streamApi.getStreamRoom(roomId).staleGroups)

            val version = streamApi.getStreamRoom(roomId).version
            assertFailsWith(
                PrivmxException::class,
                "re-key without the Group's key must be refused"
            ) {
                streamApi.rotateStreamRoomKeys(
                    roomId,
                    users.subList(0, 1),
                    users.subList(0, 1),
                    version
                )
            }

            // with the key from user2 the re-key succeeds, and user2 still reads the room through the Group
            streamApi.rotateStreamRoomKeys(
                roomId,
                users.subList(0, 1),
                users.subList(0, 1),
                version,
                groups = listOf(grantFor(groupId, api = user2.groupApi))
            )
            assertTrue(streamApi.getStreamRoom(roomId).staleGroups.isEmpty())
            val statusCode = user2.streamApi.getStreamRoom(roomId).statusCode
            assertEquals(0L, statusCode, "readable through the Group after the re-key")
        }
    }
}
