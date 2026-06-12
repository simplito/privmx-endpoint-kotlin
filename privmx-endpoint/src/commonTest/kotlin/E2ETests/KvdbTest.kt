package E2ETests

import Utils.IniConfig
import Utils.Queries.json
import Utils.Queries.json1
import Utils.Queries.json2
import Utils.Queries.json3
import Utils.Queries.json4
import Utils.Queries.json5
import Utils.Queries.json6
import Utils.Queries.json7
import Utils.Queries.json8
import Utils.Queries.json9
import Utils.Queries.query10
import Utils.Queries.query11
import Utils.Queries.query12
import Utils.Queries.query13
import Utils.Queries.query14
import Utils.Queries.query15
import com.simplito.kotlin.privmx_endpoint.model.Kvdb
import com.simplito.kotlin.privmx_endpoint.model.KvdbEntry
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.VerificationRequest
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.KvdbEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.KvdbEventType
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.core.UserVerifierInterface
import com.simplito.kotlin.privmx_endpoint.modules.kvdb.KvdbApi
import kotlin.properties.Delegates
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalStdlibApi::class)
class KvdbTest : BaseTest() {

    private lateinit var kvdbId: String
    private lateinit var kvdb2Id: String
    private lateinit var kvdb3Id: String
    private val kvdbEntryKey = "entry_key_1"
    private val kvdbEntry2Key = "entry_key_2"
    private val entry1Data = "entry_data_1"
    private val entry2Data = "entry_data_2"
    private lateinit var kvdbApi: KvdbApi

    @BeforeTest
    fun createConnection() {
        if (connection == null) {
            connection = connectAsUser(ConnectionType.User1, bridgeAddress)
        }
        kvdbApi = KvdbApi(connection!!)
        kvdbId = kvdbApi.createKvdb(contextId!!, users.subList(0, 1), users.subList(0, 1), publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray())
        kvdb2Id = kvdbApi.createKvdb(contextId!!, users, users, publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray())
        kvdb3Id = kvdbApi.createKvdb(contextId!!, users, users.subList(0, 1), publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray())
        kvdbApi.setEntry(kvdbId, kvdbEntryKey, publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray(), entry1Data.encodeToByteArray())
        kvdbApi.setEntry(kvdbId, kvdbEntry2Key, publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray(), entry2Data.encodeToByteArray())
    }

    @AfterTest
    @Throws(Exception::class)
    fun closeConnection() {
        if (::kvdbApi.isInitialized) {
            if (::kvdb3Id.isInitialized) kvdbApi.deleteKvdb(kvdb3Id)
            if (::kvdb2Id.isInitialized) kvdbApi.deleteKvdb(kvdb2Id)
            if (::kvdbId.isInitialized) kvdbApi.deleteKvdb(kvdbId)
            kvdbApi.close()
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

    @Test
    fun createKvdbIncorrectInputData() {
        // incorrect contextId
        assertFailsWith(PrivmxException::class) {
            kvdbApi.createKvdb(
                kvdbId,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // incorrect users
        assertFailsWith(PrivmxException::class) {
            kvdbApi.createKvdb(
                context2Id!!,
                incorrectUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // incorrect managers
        assertFailsWith(PrivmxException::class) {
            kvdbApi.createKvdb(
                context2Id!!,
                users,
                incorrectUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // no managers
        assertFailsWith(PrivmxException::class) {
            kvdbApi.createKvdb(
                context2Id!!,
                users,
                emptyUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // creator not in managers
        assertFailsWith(PrivmxException::class) {
            kvdbApi.createKvdb(
                context2Id!!,
                users.subList(0, 1),
                users.subList(1, 2),
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        //2 same users
        assertFailsWith(PrivmxException::class) {
            kvdbApi.createKvdb(
                context2Id!!,
                sameUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        //2 same managers
        assertFailsWith(PrivmxException::class) {
            kvdbApi.createKvdb(
                context2Id!!,
                users,
                sameUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
    }

    @Test
    fun createKvdbCorrectInputData() {
        lateinit var id: String

        // same users and managers
        assertDoesNotFail {
            id = kvdbApi.createKvdb(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // no users
        assertDoesNotFail {
            kvdbApi.createKvdb(
                context2Id!!,
                emptyUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // no publicMeta
        assertDoesNotFail {
            kvdbApi.createKvdb(
                context2Id!!, emptyUsers, users, ByteArray(0), privateMeta.encodeToByteArray()
            )
        }

        // no privateMeta
        assertDoesNotFail {
            kvdbApi.createKvdb(
                context2Id!!, emptyUsers, users, publicMeta.encodeToByteArray(), ByteArray(0)
            )
        }

        val kvdb = kvdbApi.getKvdb(id)
        assertEquals(context2Id, kvdb.contextId)
        assertEquals(id, kvdb.kvdbId)
    }

    @Test
    fun createKvdbWithPolicy() {
        // TODO: test implementation of creating Kvdb with Policy
    }

    @Test
    @Throws(Exception::class)
    fun updateKvdbIncorrectInputData() {
        connection2 = connectAsUser(
            ConnectionType.User2,
            bridgeAddress
        )
        val kvdbApi2 = KvdbApi(connection2!!)
        val kvdb = kvdbApi.getKvdb(kvdb2Id)

        // incorrect kvdbId
        assertFailsWith(PrivmxException::class) {
            kvdbApi.updateKvdb(
                contextId!!,
                users,
                users.subList(0, 1),
                kvdb.publicMeta,
                kvdb.privateMeta,
                kvdb.version!!,
                false
            )
        }

        // incorrect users
        assertFailsWith(PrivmxException::class) {
            kvdbApi.updateKvdb(
                kvdb2Id,
                incorrectUsers,
                users.subList(0, 1),
                kvdb.publicMeta,
                kvdb.privateMeta,
                kvdb.version!!,
                false
            )
        }

        // incorrect managers
        assertFailsWith(PrivmxException::class) {
            kvdbApi.updateKvdb(
                kvdb2Id,
                users.subList(0, 1),
                incorrectUsers,
                kvdb.publicMeta,
                kvdb.privateMeta,
                kvdb.version!!,
                false
            )
        }

        // creator is not in managers
        assertFailsWith(PrivmxException::class) {
            kvdbApi.updateKvdb(
                kvdb2Id,
                incorrectUsers,
                users.subList(1, 2),
                kvdb.publicMeta,
                kvdb.privateMeta,
                kvdb.version!!,
                false
            )
        }

        // no managers
        assertFailsWith(PrivmxException::class) {
            kvdbApi.updateKvdb(
                kvdb2Id,
                users.subList(0, 1),
                emptyUsers,
                kvdb.publicMeta,
                kvdb.privateMeta,
                kvdb.version!!,
                false
            )
        }

        // incorrect version - force false
        assertFailsWith(PrivmxException::class) {
            kvdbApi.updateKvdb(
                kvdb2Id,
                users.subList(0, 1),
                users.subList(0, 1),
                kvdb.publicMeta,
                kvdb.privateMeta,
                -1,
                false
            )
        }

        // updating users with new user
        assertFailsWith(PrivmxException::class) {
            kvdbApi2.updateKvdb(
                kvdb3Id, users, users, kvdb.publicMeta, kvdb.privateMeta, 2, true
            )
        }

        kvdbApi2.close()
    }

    @Test
    @Throws(Exception::class)
    fun updateKvdbCorrectInputData() {
        connection2 = connectAsUser(
            ConnectionType.User2,
            bridgeAddress
        )
        val kvdbApi2 = KvdbApi(connection2!!)
        val publicMetaUpdate = "new meta pub"
        val privateMetaUpdate = "new meta priv"

        val id: String = kvdbApi.createKvdb(
            context2Id!!,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        var kvdb = kvdbApi.getKvdb(id)
        val version: Long = kvdb.version!!

        // more users and managers
        assertDoesNotFail {
            kvdbApi.updateKvdb(
                id,
                users,
                users,
                publicMetaUpdate.encodeToByteArray(),
                privateMetaUpdate.encodeToByteArray(),
                version,
                false
            )
        }
        kvdb = kvdbApi.getKvdb(id)
        assertEquals(context2Id, kvdb.contextId)
        assertEquals(users.size, kvdb.users.size)
        assertEquals(users.size, kvdb.managers.size)

        assertEquals(publicMetaUpdate, kvdb.publicMeta.decodeToString())
        assertEquals(privateMetaUpdate, kvdb.privateMeta.decodeToString())

        assertEquals(version + 1, kvdb.version)

        // less users
        assertDoesNotFail {
            kvdbApi.updateKvdb(
                id,
                emptyUsers,
                users.subList(0, 1),
                publicMetaUpdate.encodeToByteArray(),
                privateMetaUpdate.encodeToByteArray(),
                version + 1,
                false
            )
        }
        kvdb = kvdbApi.getKvdb(id)
        assertEquals(context2Id, kvdb.contextId)
        assertEquals(0, kvdb.users.size)
        assertEquals(1, kvdb.managers.size)
        assertEquals(publicMetaUpdate, kvdb.publicMeta.decodeToString())
        assertEquals(privateMetaUpdate, kvdb.privateMeta.decodeToString())
        assertEquals(version + 2, kvdb.version)

        // incorrect version and force true
        assertDoesNotFail {
            kvdbApi.updateKvdb(
                id,
                users,
                users,
                publicMetaUpdate.encodeToByteArray(),
                privateMetaUpdate.encodeToByteArray(),
                version + 1000,
                true
            )
        }
        kvdb = kvdbApi.getKvdb(id)
        assertEquals(context2Id, kvdb.contextId)
        assertEquals(users.size, kvdb.users.size)
        assertEquals(users.size, kvdb.managers.size)
        assertEquals(publicMetaUpdate, kvdb.publicMeta.decodeToString())
        assertEquals(privateMetaUpdate, kvdb.privateMeta.decodeToString())
        assertEquals(version + 3, kvdb.version)

        // user1 creates - user2(manager) updates
        assertDoesNotFail {
            kvdbApi2.updateKvdb(
                id,
                users,
                users,
                publicMetaUpdate.encodeToByteArray(),
                privateMetaUpdate.encodeToByteArray(),
                3,
                true
            )
        }

        // forceGenerateNewKey = true
        assertDoesNotFail {
            kvdbApi.updateKvdb(
                id,
                users,
                users,
                publicMetaUpdate.encodeToByteArray(),
                privateMetaUpdate.encodeToByteArray(),
                0,
                true,
                true
            )
        }

        kvdbApi2.close()
    }

    @Test
    fun updateKvdbWithPolicy() {
        // TODO: test implementation of updating Kvdb with Policy
    }

    @Test
    @Throws(Exception::class)
    fun deleteKvdb() {
        connection2 = connectAsUser(
            ConnectionType.User2,
            bridgeAddress
        )
        val kvdbApi2 = KvdbApi(connection2!!)

        // incorrect kvdbId
        assertFailsWith(PrivmxException::class) {
            kvdbApi2.deleteKvdb(context2Id!!)
        }

        // user1 creates - user1 deletes (user1 is in managers)
        val id2: String = kvdbApi.createKvdb(
            context2Id!!,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertDoesNotFail {
            kvdbApi.deleteKvdb(id2)
        }

        // user1 creates - user2 deletes (user2 is in managers list)
        val id3: String = kvdbApi.createKvdb(
            context2Id!!,
            users.subList(0, 1),
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertDoesNotFail {
            kvdbApi2.deleteKvdb(id3)
        }

        // user1 creates - user2 deletes (user2 is in users list)
        val id4: String = kvdbApi.createKvdb(
            context2Id!!,
            users,
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertFailsWith(PrivmxException::class) {
            kvdbApi2.deleteKvdb(id4)
        }

        // user1 creates - user2 deletes (user2 is not in users list)
        val id5: String = kvdbApi.createKvdb(
            context2Id!!,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertFailsWith(PrivmxException::class) {
            kvdbApi2.deleteKvdb(id5)
        }

        kvdbApi2.close()
    }

    @Test
    fun getKvdb() {
        lateinit var kvdb: Kvdb

        // incorrect Id
        assertFailsWith(PrivmxException::class) {
            kvdbApi.getKvdb(contextId!!)
        }

        // correctId
        assertDoesNotFail { kvdb = kvdbApi.getKvdb(kvdbId) }
        assertEquals(kvdbId, kvdb.kvdbId)
        assertEquals(contextId!!, kvdb.contextId)
        assertEquals(user1Id!!, kvdb.creator)
        assertEquals(user1Id!!, kvdb.lastModifier)
        assertEquals(publicMeta.encodeToByteArray().toHexString(), kvdb.publicMeta.toHexString())
        assertEquals(privateMeta.encodeToByteArray().toHexString(), kvdb.privateMeta.toHexString())
        assertEquals("2", kvdb.entries.toString())
        assertEquals("0", kvdb.statusCode.toString())
        assertEquals(1, kvdb.users.size)
        assertEquals(1, kvdb.managers.size)
    }

    @Test
    fun listKvdbsIncorrectInputData() {
        // incorrect contextId
        assertFailsWith(PrivmxException::class) {
            kvdbApi.listKvdbs(
                kvdbId, 0, 1, "desc"
            )
        }
        // limit < 0
        assertFailsWith(PrivmxException::class) {
            kvdbApi.listKvdbs(
                contextId!!, 0, -1, "desc"
            )
        }

        // limit == 0
        assertFailsWith(PrivmxException::class) {
            kvdbApi.listKvdbs(
                contextId!!, 0, 0, "desc"
            )
        }

        // incorrect sortOrder
        assertFailsWith(PrivmxException::class) {
            kvdbApi.listKvdbs(
                contextId!!, 0, 1, "wrong"
            )
        }

        // incorrect lastId
        assertFailsWith(PrivmxException::class) {
            kvdbApi.listKvdbs(
                contextId!!, 0, 1, "desc", "wrong"
            )
        }

        // incorrect queryAsJson
        assertFailsWith(PrivmxException::class) {
            kvdbApi.listKvdbs(
                contextId!!, 0, 1, "desc", null, "wrong"
            )
        }

        // incorrect sortBy
        assertFailsWith(PrivmxException::class) {
            kvdbApi.listKvdbs(
                contextId!!, 0, 1, "desc", null, null, "wrong"
            )
        }
    }

    @Test
    fun listKvdbsCorrectInputData() {
        val contextId: String = contextId!!
        lateinit var kvdbs: PagingList<Kvdb>

        assertDoesNotFail {
            kvdbs = kvdbApi.listKvdbs(
                contextId, 4, 1, "desc"
            )
        }
        assertEquals(3, kvdbs.totalAvailable)
        assertEquals(0, kvdbs.readItems.size)

        assertDoesNotFail {
            kvdbs = kvdbApi.listKvdbs(
                contextId, 0, 1, "desc"
            )
        }
        assertEquals(3, kvdbs.totalAvailable)
        assertEquals(1, kvdbs.readItems.size)
        assertEquals(contextId!!, kvdbs.readItems.get(0).contextId)
        assertEquals(kvdb3Id, kvdbs.readItems.get(0).kvdbId)

        assertDoesNotFail {
            kvdbs = kvdbApi.listKvdbs(
                contextId, 1, 3, "asc"
            )
        }
        assertEquals(3, kvdbs.totalAvailable)
        assertEquals(2, kvdbs.readItems.size)
        assertEquals(contextId!!, kvdbs.readItems.get(0).contextId)
        assertEquals(kvdb2Id, kvdbs.readItems.get(0).kvdbId)

        // with lastId parameter
        assertDoesNotFail {
            kvdbs = kvdbApi.listKvdbs(
                contextId, 0, 10, "asc", kvdbId
            )
        }
        assertEquals(2, kvdbs.totalAvailable)
        assertEquals(2, kvdbs.readItems.size)
        assertEquals(contextId!!, kvdbs.readItems.get(0).contextId)
        assertEquals(kvdb2Id, kvdbs.readItems.get(0).kvdbId)

        // with sortBy parameter - createDate
        assertDoesNotFail {
            kvdbs = kvdbApi.listKvdbs(
                contextId, 0, 10, "desc", null, null, "createDate"
            )
        }
        assertEquals(3, kvdbs.totalAvailable)
        assertEquals(3, kvdbs.readItems.size)
    }

    @Test
    fun getEntry() {
        lateinit var kvdbEntry: KvdbEntry

        // incorrect kvdbId
        assertFailsWith(PrivmxException::class) {
            kvdbApi.getEntry(contextId!!, kvdbEntryKey)
        }

        // incorrect key
        assertFailsWith(PrivmxException::class) {
            kvdbApi.getEntry(kvdbId, "wrong")
        }

        // existing key, but in another kvdb
        assertFailsWith(PrivmxException::class) {
            kvdbApi.getEntry(kvdb2Id, kvdb2Id)
        }

        // correct kvdbId and entryKey
        assertDoesNotFail {
            kvdbEntry = kvdbApi.getEntry(kvdbId, kvdbEntryKey)
        }
        assertEquals(kvdbId, kvdbEntry.info.kvdbId)
        assertEquals(kvdbEntryKey, kvdbEntry.info.key)
        assertEquals(user1Id!!, kvdbEntry.info.author)
        assertEquals(publicMeta.encodeToByteArray().toHexString(), kvdbEntry.publicMeta.toHexString())
        assertEquals(privateMeta.encodeToByteArray().toHexString(), kvdbEntry.privateMeta.toHexString())
        assertEquals(entry1Data.encodeToByteArray().toHexString(), kvdbEntry.data.toHexString())
        assertEquals(users[0].pubKey, kvdbEntry.authorPubKey)
        assertEquals("0", kvdbEntry.statusCode.toString())
    }

    @Test
    @Throws(Exception::class)
    fun hasEntry() {
        connection2 = connectAsUser(
            ConnectionType.User2,
            bridgeAddress
        )
        val kvdbApi2 = KvdbApi(connection2!!)
        var hasEntry by Delegates.notNull<Boolean>()

        // wrong kvdbId
        // todo - should throw exception
        assertDoesNotFail { hasEntry = kvdbApi.hasEntry(contextId!!, kvdbEntryKey) }

        // wrong key
        assertDoesNotFail { hasEntry = kvdbApi.hasEntry(kvdbId, contextId!!) }
        assertFalse(hasEntry)

        // The key belongs to the given kvdb
        assertDoesNotFail { hasEntry = kvdbApi.hasEntry(kvdbId, kvdbEntryKey) }
        assertTrue(hasEntry)

        // The key does not belong to the given kvdb
        assertDoesNotFail { hasEntry = kvdbApi.hasEntry(kvdb2Id, kvdbEntryKey) }
        assertFalse(hasEntry)

        // existing key - created by other user
        val entryKey = "kvdb_entry_created_by_user2"
        kvdbApi2.setEntry(
            kvdb3Id,
            entryKey,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data".encodeToByteArray()
        )
        assertDoesNotFail { hasEntry = kvdbApi.hasEntry(kvdb3Id, entryKey) }
        assertTrue(hasEntry)

        // existing key - in another user's kvdb
        val id: String = kvdbApi2.createKvdb(
            contextId!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        kvdbApi2.setEntry(
            id,
            entryKey,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data".encodeToByteArray()
        )
        assertDoesNotFail { hasEntry = kvdbApi.hasEntry(id, entryKey) }
        assertTrue(hasEntry)

        kvdbApi2.close()
    }

    @Test
    fun listEntriesKeysIncorrectInputData() {
        // wrong kvdbId
        assertFailsWith(PrivmxException::class) {
            kvdbApi.listEntriesKeys(
                contextId!!, 0, 1, "desc"
            )
        }

        // limit < 0
        assertFailsWith(PrivmxException::class) {
            kvdbApi.listEntriesKeys(
                kvdbId, 0, -1, "desc"
            )
        }

        // limit == 0
        assertFailsWith(PrivmxException::class) {
            kvdbApi.listEntriesKeys(
                kvdbId, 0, 0, "desc"
            )
        }

        // incorrect sortOrder
        assertFailsWith(PrivmxException::class) {
            kvdbApi.listEntriesKeys(
                kvdbId, 0, 1, "wrong"
            )
        }

        // incorrect lastId
        assertFailsWith(PrivmxException::class) {
            kvdbApi.listEntriesKeys(
                kvdbId, 0, 1, "desc", "wrong"
            )
        }

        // incorrect queryAsJson
        assertFailsWith(PrivmxException::class) {
            kvdbApi.listEntriesKeys(
                kvdbId, 0, 1, "desc", null, "wrong"
            )
        }

        // incorrect sortBy
        assertFailsWith(PrivmxException::class) {
            kvdbApi.listEntriesKeys(
                kvdbId, 0, 1, "desc", null, null, "wrong"
            )
        }
    }

    @Test
    fun listEntriesKeysCorrectInputData() {
        lateinit var entriesKeys: PagingList<String>

        // correct data
        assertDoesNotFail {
            entriesKeys = kvdbApi.listEntriesKeys(kvdbId, 0, 1, "desc")
        }
        assertEquals(2, entriesKeys.totalAvailable)
        assertEquals(1, entriesKeys.readItems.size)
        assertEquals(kvdbEntry2Key, entriesKeys.readItems.get(0))

        // correct data
        assertDoesNotFail {
            entriesKeys = kvdbApi.listEntriesKeys(
                kvdbId, 0, 4, "asc"
            )
        }
        assertEquals(2, entriesKeys.totalAvailable)
        assertEquals(2, entriesKeys.readItems.size)
        assertEquals(kvdbEntryKey, entriesKeys.readItems.get(0))
        assertEquals(kvdbEntry2Key, entriesKeys.readItems.get(1))

        // skip > files amount
        assertDoesNotFail {
            entriesKeys = kvdbApi.listEntriesKeys(
                kvdbId, 4, 1, "asc"
            )
        }
        assertEquals(2, entriesKeys.totalAvailable)
        assertEquals(0, entriesKeys.readItems.size)

        // with last entryKey
        assertDoesNotFail {
            entriesKeys = kvdbApi.listEntriesKeys(
                kvdbId, 0, 1, "asc", kvdbEntryKey
            )
        }
        assertEquals(1, entriesKeys.totalAvailable)
        assertEquals(1, entriesKeys.readItems.size)
        assertEquals(kvdbEntry2Key, entriesKeys.readItems.get(0))

        // with sortBy parameter - createDate
        assertDoesNotFail {
            entriesKeys = kvdbApi.listEntriesKeys(
                kvdbId, 0, 10, "desc", null, null, "createDate"
            )
        }
        assertEquals(2, entriesKeys.totalAvailable)
        assertEquals(2, entriesKeys.readItems.size)
        assertEquals(kvdbEntry2Key, entriesKeys.readItems.get(0))

        // with sortBy parameter - lastModificationDate
        assertDoesNotFail {
            entriesKeys = kvdbApi.listEntriesKeys(
                kvdbId, 0, 10, "desc", null, null, "lastModificationDate"
            )
        }
        assertEquals(2, entriesKeys.totalAvailable)
        assertEquals(2, entriesKeys.readItems.size)
        assertEquals(kvdbEntry2Key, entriesKeys.readItems.get(0))
    }

    @Test
    @Throws(Exception::class)
    fun setEntry() {
        connection2 = connectAsUser(
            ConnectionType.User2,
            bridgeAddress
        )
        val kvdbApi2 = KvdbApi(connection2!!)

        val key1 = "kvdb_entry_key_1_test"
        val key2 = "kvdb_entry_key_2_test"
        val key3 = "kvdb_entry_key_3_test"

        // version < 0
        assertFailsWith(PrivmxException::class) {
            kvdbApi.setEntry(
                kvdb3Id,
                key1,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray(),
                -1
            )
        }

        // version > 0 - first setting
        assertFailsWith(PrivmxException::class) {
            kvdbApi.setEntry(
                kvdb3Id,
                key1,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray(),
                3
            )
        }

        // correct
        assertDoesNotFail {
            kvdbApi.setEntry(
                kvdb3Id,
                key2,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }

        // user does not belong to kvdb users
        val id: String = kvdbApi.createKvdb(
            context2Id!!,
            users.subList(0, 1),
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertDoesNotFail {
            kvdbApi2.setEntry(
                id,
                key3,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }

        // user2 does not belong to kvdb managers
        val id2: String = kvdbApi.createKvdb(
            context2Id!!,
            users,
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertDoesNotFail {
            kvdbApi2.setEntry(
                id2,
                key2,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }

        assertDoesNotFail {
            kvdbApi.setEntry(
                id2,
                key3,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }

        // update public meta
        assertDoesNotFail {
            kvdbApi.setEntry(
                kvdb3Id,
                key2,
                "new public meta".encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray(),
                1
            )
        }

        // update private meta
        assertDoesNotFail {
            kvdbApi.setEntry(
                kvdb3Id,
                key2,
                publicMeta.encodeToByteArray(),
                "new private meta".encodeToByteArray(),
                "data".encodeToByteArray(),
                2
            )
        }

        // update data
        assertDoesNotFail {
            kvdbApi.setEntry(
                kvdb3Id,
                key2,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "new data".encodeToByteArray(),
                3
            )
        }

        // new version only
        val kvdbEntry: KvdbEntry = kvdbApi.getEntry(kvdb3Id, key2)
        assertDoesNotFail {
            kvdbApi.setEntry(
                kvdb3Id, key2, kvdbEntry.publicMeta, kvdbEntry.privateMeta, kvdbEntry.data, 4
            )
        }

        // user update entry created by user2 & user is a manager
        assertDoesNotFail {
            kvdbApi.setEntry(
                id2,
                key2,
                "new public meta".encodeToByteArray(),
                "new private meta".encodeToByteArray(),
                "new data".encodeToByteArray(),
                1
            )
        }

        // user2 update entry created by user & user2 is not a manager
        assertFailsWith(PrivmxException::class) {
            kvdbApi2.setEntry(
                id2,
                key3,
                "new public meta".encodeToByteArray(),
                "new private meta".encodeToByteArray(),
                "new data".encodeToByteArray(),
                1
            )
        }

        kvdbApi2.close()
    }

    @Test
    @Throws(Exception::class)
    fun deleteEntry() {
        val key = "kvdb_entry_key_to_delete"
        val key2 = "kvdb_entry_key_to_delete_2"
        connection2 = connectAsUser(
            ConnectionType.User2,
            bridgeAddress
        )
        val kvdbApi2 = KvdbApi(connection2!!)

        // wrong kvdbId
        assertFailsWith(PrivmxException::class) {
            kvdbApi.deleteEntry(contextId!!, key)
        }

        // wrong key
        assertFailsWith(PrivmxException::class) {
            kvdbApi.deleteEntry(kvdb3Id, "wrong_key")
        }

        // user does not belong to kvdb users
        val id: String = kvdbApi.createKvdb(
            context2Id!!,
            users.subList(0, 1),
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        kvdbApi.setEntry(
            id,
            key,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data".encodeToByteArray()
        )
        assertDoesNotFail {
            kvdbApi2.deleteEntry(id, key)
        }

        // user does not belong to kvdb managers
        val id2: String = kvdbApi.createKvdb(
            context2Id!!,
            users,
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        // user1 is a creator of an entry
        kvdbApi.setEntry(
            id2,
            key,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data".encodeToByteArray()
        )
        assertFailsWith(PrivmxException::class) {
            kvdbApi2.deleteEntry(id2, key)
        }

        // user2 is a creator of an entry
        kvdbApi2.setEntry(
            id2,
            key2,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data".encodeToByteArray()
        )
        assertDoesNotFail {
            kvdbApi2.deleteEntry(id2, key2)
        }

        // user is a creator of an entry and a kvdb
        val id3: String = kvdbApi.createKvdb(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        kvdbApi.setEntry(
            id3,
            key,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data".encodeToByteArray()
        )
        assertDoesNotFail {
            kvdbApi.deleteEntry(id3, key)
        }

        kvdbApi2.close()
    }

    @Test
    @Throws(Exception::class)
    fun deleteEntries() {
        lateinit var map: Map<String, Boolean>
        lateinit var resultMap: Map<String, Boolean>
        val keysSet: Set<String> = setOf(
            "entry_key_1_delete",
            "entry_key_2_delete",
            "entry_key_3_delete"
        )
        val keys: List<String> = keysSet.toList()

        connection2 = connectAsUser(
            ConnectionType.User2,
            bridgeAddress
        )
        val kvdbApi2 = KvdbApi(connection2!!)

        // wrong kvdbId
        assertFailsWith(PrivmxException::class) {
            kvdbApi.deleteEntries(contextId!!, keysSet)
        }

        // wrong keys
        assertDoesNotFail {
            resultMap = kvdbApi.deleteEntries(kvdb3Id, setOf("wrong"))
        }

        assertFalse(resultMap.get("wrong")!!)

        // not existing key inside list of keys
        val id: String = kvdbApi.createKvdb(
            context2Id!!,
            users.subList(0, 1),
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        kvdbApi.setEntry(
            id,
            keys.get(0),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data".encodeToByteArray()
        )
        kvdbApi.setEntry(
            id,
            keys.get(1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data".encodeToByteArray()
        )
        assertDoesNotFail {
            map = kvdbApi2.deleteEntries(id, keysSet)
        }

        assertTrue(map.get(keys.get(0))!!)
        assertTrue(map.get(keys.get(1))!!)
        assertFalse(map.get(keys.get(2))!!)

        // user does not belong to kvdb users
        val id2: String = kvdbApi.createKvdb(
            context2Id!!,
            users.subList(0, 1),
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        kvdbApi.setEntry(
            id2,
            keys.get(0),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data".encodeToByteArray()
        )
        kvdbApi.setEntry(
            id2,
            keys.get(1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data".encodeToByteArray()
        )
        kvdbApi.setEntry(
            id2,
            keys.get(2),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data".encodeToByteArray()
        )
        assertDoesNotFail {
            map = kvdbApi2.deleteEntries(id2, keysSet)
        }

        assertTrue(map.get(keys.get(0))!!)
        assertTrue(map.get(keys.get(1))!!)
        assertTrue(map.get(keys.get(2))!!)

        // user does not belong to kvdb managers
        val id3: String = kvdbApi.createKvdb(
            context2Id!!,
            users,
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        kvdbApi.setEntry(
            id3,
            keys.get(0),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data".encodeToByteArray()
        )
        kvdbApi.setEntry(
            id3,
            keys.get(1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data".encodeToByteArray()
        )
        kvdbApi.setEntry(
            id3,
            keys.get(2),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data".encodeToByteArray()
        )
        assertDoesNotFail {
            map = kvdbApi2.deleteEntries(id3, keysSet)
        }

        assertFalse(map.get(keys.get(0))!!)
        assertFalse(map.get(keys.get(1))!!)
        assertFalse(map.get(keys.get(2))!!)

        // user is a creator
        val id4: String = kvdbApi.createKvdb(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        kvdbApi.setEntry(
            id4,
            keys.get(0),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data".encodeToByteArray()
        )
        assertDoesNotFail {
            map = kvdbApi.deleteEntries(id4, keysSet)
        }

        assertTrue(map.get(keys.get(0))!!)
        assertFalse(map.get(keys.get(1))!!)
        assertFalse(map.get(keys.get(2))!!)

        // empty list
        val id5: String = kvdbApi.createKvdb(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        kvdbApi.setEntry(
            id5,
            keys.get(0),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data".encodeToByteArray()
        )
        assertDoesNotFail {
            map = kvdbApi.deleteEntries(id5, setOf())
        }
        assertTrue(map.isEmpty())

        kvdbApi2.close()
    }

    @Test
    @Ignore
    @Throws(PrivmxException::class)
    fun filteringListKvdbsWithQueryAsJson() {
        lateinit var kvdbsList: PagingList<Kvdb>

        assertDoesNotFail {
            kvdbApi.createKvdb(
                context2Id!!,
                users,
                users,
                json.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            kvdbApi.createKvdb(
                context2Id!!,
                users,
                users,
                json1.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            kvdbApi.createKvdb(
                context2Id!!,
                users,
                users,
                json2.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            kvdbApi.createKvdb(
                context2Id!!,
                users,
                users,
                json3.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        assertDoesNotFail {
            kvdbApi.createKvdb(
                context2Id!!,
                users,
                users,
                json4.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            kvdbApi.createKvdb(
                context2Id!!,
                users,
                users,
                json5.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            kvdbApi.createKvdb(
                context2Id!!,
                users,
                users,
                json6.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            kvdbApi.createKvdb(
                context2Id!!,
                users,
                users,
                json7.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            kvdbApi.createKvdb(
                context2Id!!,
                users,
                users,
                json8.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            kvdbApi.createKvdb(
                context2Id!!,
                users,
                users,
                json9.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            kvdbApi.createKvdb(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // json -> json & json1 & json8
        assertDoesNotFail {
            kvdbsList = kvdbApi.listKvdbs(
                context2Id!!,
                0,
                100,
                "desc",
                null,
                json
            )
        }
        assertEquals(
            3,
            kvdbsList.readItems.size
        )

        // json1 -> json1
        assertDoesNotFail {
            kvdbsList = kvdbApi.listKvdbs(
                context2Id!!,
                0,
                100,
                "desc",
                null,
                json1
            )
        }
        assertEquals(
            1,
            kvdbsList.readItems.size
        )

        // json2 -> json2 & json9
        assertDoesNotFail {
            kvdbsList = kvdbApi.listKvdbs(
                context2Id!!,
                0,
                100,
                "desc",
                null,
                json2
            )
        }
        assertEquals(
            2,
            kvdbsList.readItems.size
        )

        // json3 -> json3 & json9
        assertDoesNotFail {
            kvdbsList = kvdbApi.listKvdbs(
                context2Id!!,
                0,
                100,
                "desc",
                null,
                json3
            )
        }
        assertEquals(
            2,
            kvdbsList.readItems.size
        )

        // json4
        assertFailsWith(PrivmxException::class) {
            kvdbsList = kvdbApi.listKvdbs(
                context2Id!!,
                0,
                100,
                "desc",
                null,
                json4
            )
        }

        // json5 -> json5
        assertDoesNotFail {
            kvdbsList = kvdbApi.listKvdbs(
                context2Id!!,
                0,
                100,
                "desc",
                null,
                json5
            )
        }
        assertEquals(
            1,
            kvdbsList.readItems.size
        )

        // json6 -> json6
        assertDoesNotFail {
            kvdbsList = kvdbApi.listKvdbs(
                context2Id!!,
                0,
                100,
                "desc",
                null,
                json6
            )
        }
        assertEquals(
            1,
            kvdbsList.readItems.size
        )

        // json7 -> json7 & json8
        assertDoesNotFail {
            kvdbsList = kvdbApi.listKvdbs(
                context2Id!!,
                0,
                100,
                "desc",
                null,
                json7
            )
        }
        assertEquals(
            2,
            kvdbsList.readItems.size
        )

        // json8 -> json8
        assertDoesNotFail {
            kvdbsList = kvdbApi.listKvdbs(
                context2Id!!,
                0,
                100,
                "desc",
                null,
                json8
            )
        }
        assertEquals(
            1,
            kvdbsList.readItems.size
        )

        // json9 -> json9
        assertDoesNotFail {
            kvdbsList = kvdbApi.listKvdbs(
                context2Id!!,
                0,
                100,
                "desc",
                null,
                json9
            )
        }
        assertEquals(
            1,
            kvdbsList.readItems.size
        )

        // query10 -> json & json1 & json8
        assertDoesNotFail {
            kvdbsList = kvdbApi.listKvdbs(
                context2Id!!,
                0,
                100,
                "desc",
                null,
                query10
            )
        }
        assertEquals(
            3,
            kvdbsList.readItems.size
        )

        // query11 -> json7 & json8 & json9
        assertDoesNotFail {
            kvdbsList = kvdbApi.listKvdbs(
                context2Id!!,
                0,
                100,
                "desc",
                null,
                query11
            )
        }
        assertEquals(
            3,
            kvdbsList.readItems.size
        )

        // query12 -> json8
        assertDoesNotFail {
            kvdbsList = kvdbApi.listKvdbs(
                context2Id!!,
                0,
                100,
                "desc",
                null,
                query12
            )
        }
        assertEquals(
            1,
            kvdbsList.readItems.size
        )

        // query13 -> json8
        assertDoesNotFail {
            kvdbsList = kvdbApi.listKvdbs(
                context2Id!!,
                0,
                100,
                "desc",
                null,
                query13
            )
        }
        assertEquals(
            1,
            kvdbsList.readItems.size
        )

        // query14 -> json & json1 & json7 & json8 & json9
        assertDoesNotFail {
            kvdbsList = kvdbApi.listKvdbs(
                context2Id!!,
                0,
                100,
                "desc",
                null,
                query14
            )
        }
        assertEquals(
            5,
            kvdbsList.readItems.size
        )

        // query15 -> json2 & json3 & json4 & json5 & json6 & json7 & json9 & publicMeta
        assertDoesNotFail {
            kvdbsList = kvdbApi.listKvdbs(
                context2Id!!,
                0,
                100,
                "desc",
                null,
                query15
            )
        }
        assertEquals(8, kvdbsList.readItems.size)

        assertDoesNotFail {
            kvdbsList = kvdbApi.listKvdbs(
                context2Id!!,
                0,
                100,
                "desc"
            )
        }
        assertTrue(kvdbsList.readItems.size >= 11)
    }

    @Test
    @Ignore
    @Throws(PrivmxException::class)
    fun filteringListKvdbEntriesWithQueryAsJson() {
        lateinit var entriesList: PagingList<KvdbEntry>
        val id: String = kvdbApi.createKvdb(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        assertDoesNotFail {
            kvdbApi.setEntry(
                id,
                "key",
                json.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }

        assertDoesNotFail {
            kvdbApi.setEntry(
                id,
                "key1",
                json1.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }

        assertDoesNotFail {
            kvdbApi.setEntry(
                id,
                "key2",
                json2.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            kvdbApi.setEntry(
                id,
                "key3",
                json3.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }

        assertDoesNotFail {
            kvdbApi.setEntry(
                id,
                "key4",
                json4.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }

        assertDoesNotFail {
            kvdbApi.setEntry(
                id,
                "key5",
                json5.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            kvdbApi.setEntry(
                id,
                "key6",
                json6.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            kvdbApi.setEntry(
                id,
                "key7",
                json7.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            kvdbApi.setEntry(
                id,
                "key8",
                json8.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            kvdbApi.setEntry(
                id,
                "key9",
                json9.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            kvdbApi.setEntry(
                id,
                "key10",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }

        // json -> json & json1 & json8
        assertDoesNotFail {
            entriesList = kvdbApi.listEntries(
                id,
                0,
                100,
                "desc",
                null,
                json
            )
        }
        // todo - caused by an error in the library - will be fixed after lib fix - should be another list size
        assertNotEquals(
            3,
            entriesList.readItems.size
        )

        // json1 -> json1
        assertDoesNotFail {
            entriesList = kvdbApi.listEntries(
                id,
                0,
                100,
                "desc",
                null,
                json1
            )
        }
        // todo - caused by an error in the library - will be fixed after lib fix - should be another list size
        assertNotEquals(
            1,
            entriesList.readItems.size
        )

        // json2 -> json2 & json9
        assertDoesNotFail {
            entriesList = kvdbApi.listEntries(
                id,
                0,
                100,
                "desc",
                null,
                json2
            )
        }
        // todo - caused by an error in the library - will be fixed after lib fix - should be another list size
        assertNotEquals(
            2,
            entriesList.readItems.size
        )

        // json3 -> json3 & json9
        assertDoesNotFail {
            entriesList = kvdbApi.listEntries(
                id,
                0,
                100,
                "desc",
                null,
                json3
            )
        }
        // todo - caused by an error in the library - will be fixed after lib fix - should be another list size
        assertNotEquals(
            2,
            entriesList.readItems.size
        )

        // json4
        assertFailsWith(PrivmxException::class) {
            entriesList = kvdbApi.listEntries(
                id,
                0,
                100,
                "desc",
                null,
                json4
            )
        }

        // json5 -> json5
        assertDoesNotFail {
            entriesList = kvdbApi.listEntries(
                id,
                0,
                100,
                "desc",
                null,
                json5
            )
        }
        // todo - caused by an error in the library - will be fixed after lib fix - should be another list size
        assertNotEquals(
            1,
            entriesList.readItems.size
        )

        // json6 -> json6
        assertDoesNotFail {
            entriesList = kvdbApi.listEntries(
                id,
                0,
                100,
                "desc",
                null,
                json6
            )
        }
        // todo - caused by an error in the library - will be fixed after lib fix - should be another list size
        assertNotEquals(
            1,
            entriesList.readItems.size
        )

        // json7 -> json7 & json8
        assertDoesNotFail {
            entriesList = kvdbApi.listEntries(
                id,
                0,
                100,
                "desc",
                null,
                json7
            )
        }
        // todo - caused by an error in the library - will be fixed after lib fix - should be another list size
        assertNotEquals(
            2,
            entriesList.readItems.size
        )

        // json8 -> json8
        assertDoesNotFail {
            entriesList = kvdbApi.listEntries(
                id,
                0,
                100,
                "desc",
                null,
                json8
            )
        }
        // todo - caused by an error in the library - will be fixed after lib fix - should be another list size
        assertNotEquals(
            1,
            entriesList.readItems.size
        )

        // json9 -> json9
        assertDoesNotFail {
            entriesList = kvdbApi.listEntries(
                id,
                0,
                100,
                "desc",
                null,
                json9
            )
        }
        // todo - caused by an error in the library - will be fixed after lib fix - should be another list size
        assertNotEquals(
            1,
            entriesList.readItems.size
        )

        // query10 -> json & json1 & json8
        assertDoesNotFail {
            entriesList = kvdbApi.listEntries(
                id,
                0,
                100,
                "desc",
                null,
                query10
            )
        }
        // todo - caused by an error in the library - will be fixed after lib fix - should be another list size
        assertNotEquals(
            3,
            entriesList.readItems.size
        )

        // query11 -> json7 & json8 & json9
        assertDoesNotFail {
            entriesList = kvdbApi.listEntries(
                id,
                0,
                100,
                "desc",
                null,
                query11
            )
        }
        // todo - caused by an error in the library - will be fixed after lib fix - should be another list size
        assertNotEquals(
            3,
            entriesList.readItems.size
        )

        // query12 -> json8
        assertDoesNotFail {
            entriesList = kvdbApi.listEntries(
                id,
                0,
                100,
                "desc",
                null,
                query12
            )
        }
        // todo - caused by an error in the library - will be fixed after lib fix - should be another list size
        assertNotEquals(
            1,
            entriesList.readItems.size
        )

        // query13 -> json8
        assertDoesNotFail {
            entriesList = kvdbApi.listEntries(
                id,
                0,
                100,
                "desc",
                null,
                query13
            )
        }
        // todo - caused by an error in the library - will be fixed after lib fix - should be another list size
        assertNotEquals(
            1,
            entriesList.readItems.size
        )

        // query14 -> json & json1 & json7 & json8 & json9
        assertDoesNotFail {
            entriesList = kvdbApi.listEntries(
                id,
                0,
                100,
                "desc",
                null,
                query14
            )
        }
        // todo - caused by an error in the library - will be fixed after lib fix - should be another list size
        assertNotEquals(
            5,
            entriesList.readItems.size
        )

        // query15 -> json2 & json3 & json4 & json5 & json6 & json7 & json9 & publicMeta
        assertDoesNotFail {
            entriesList = kvdbApi.listEntries(
                id,
                0,
                100,
                "desc",
                null,
                query15
            )
        }
        // todo - caused by an error in the library - will be fixed after lib fix - should be another list size
        assertNotEquals(
            8,
            entriesList.readItems.size
        )

        assertDoesNotFail {
            entriesList = kvdbApi.listEntries(
                id,
                0,
                100,
                "desc"
            )
        }
        assertTrue(entriesList.readItems.size >= 11)
    }

    @Test
    @Throws(Exception::class)
    fun subscribeForKvdbEvents() {
        val kvdbsSubscriptionIds = mutableListOf<String>()
        val entriesSubscriptionIds = mutableListOf<String>()

        // KvdbEvents
        // subscribe for KvdbEvents
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.KVDB_CREATE,
                    KvdbEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.KVDB_UPDATE,
                    KvdbEventSelectorType.KVDB_ID,
                    kvdb3Id
                )
            )
            queries.add(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.KVDB_STATS,
                    KvdbEventSelectorType.KVDB_ID,
                    kvdb3Id
                )
            )
            queries.add(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.KVDB_DELETE,
                    KvdbEventSelectorType.KVDB_ID,
                    kvdb3Id
                )
            )
            kvdbsSubscriptionIds.addAll(kvdbApi.subscribeFor(queries))
        }

        // subscribe again
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.KVDB_CREATE,
                    KvdbEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            kvdbsSubscriptionIds.addAll(kvdbApi.subscribeFor(queries))
        }

        // 2 same queries
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.KVDB_UPDATE,
                    KvdbEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.KVDB_UPDATE,
                    KvdbEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            kvdbsSubscriptionIds.addAll(kvdbApi.subscribeFor(queries))
        }

        // subscribe with wrong KvdbEventSelectorType
        assertFailsWith(PrivmxException::class) {
            val queries: MutableList<String> = mutableListOf()
                queries.add(
                    kvdbApi.buildSubscriptionQuery(
                        KvdbEventType.KVDB_CREATE,
                        KvdbEventSelectorType.KVDB_ID,
                        kvdb3Id
                    )
                )
            }

        // subscribe with nonexisting selectorId
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.KVDB_CREATE,
                    KvdbEventSelectorType.CONTEXT_ID,
                    "f977e533-3524-4579-error-215368c4b2a4"
                )
            )
            kvdbsSubscriptionIds.addAll(kvdbApi.subscribeFor(queries))
        }

        // EntryEvents
        // subscribe for EntryEvents
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.ENTRY_UPDATE,
                    KvdbEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.ENTRY_CREATE,
                    KvdbEventSelectorType.KVDB_ID,
                    kvdb3Id
                )
            )
            queries.add(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.ENTRY_DELETE,
                    KvdbEventSelectorType.KVDB_ID,
                    kvdb3Id
                )
            )
            queries.add(
                kvdbApi.buildSubscriptionQueryForSelectedEntry(
                    KvdbEventType.ENTRY_UPDATE,
                    kvdbId,
                    kvdbEntry2Key
                )
            )
            entriesSubscriptionIds.addAll(kvdbApi.subscribeFor(queries))
        }

        // subscribe again
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.ENTRY_CREATE,
                    KvdbEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            entriesSubscriptionIds.addAll(kvdbApi.subscribeFor(queries))
        }

        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                kvdbApi.buildSubscriptionQueryForSelectedEntry(
                    KvdbEventType.ENTRY_UPDATE,
                    kvdbId,
                    kvdbEntry2Key
                )
            )
            entriesSubscriptionIds.addAll(kvdbApi.subscribeFor(queries))
        }

        // 2 same queries
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.ENTRY_UPDATE,
                    KvdbEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.ENTRY_UPDATE,
                    KvdbEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            entriesSubscriptionIds.addAll(kvdbApi.subscribeFor(queries))
        }

        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                kvdbApi.buildSubscriptionQueryForSelectedEntry(
                    KvdbEventType.ENTRY_UPDATE,
                    kvdbId,
                    kvdbEntry2Key
                )
            )
            queries.add(
                kvdbApi.buildSubscriptionQueryForSelectedEntry(
                    KvdbEventType.ENTRY_UPDATE,
                    kvdbId,
                    kvdbEntry2Key
                )
            )
            entriesSubscriptionIds.addAll(kvdbApi.subscribeFor(queries))
        }

        // build query with wrong KvdbEventType
        assertFailsWith(PrivmxException::class) {
            val queries: MutableList<String?> = mutableListOf()
            queries.add(
                kvdbApi.buildSubscriptionQueryForSelectedEntry(
                    KvdbEventType.KVDB_UPDATE,
                    kvdbId,
                    kvdbEntry2Key
                )
            )
        }

        // build query with ENTRY_CREATE event type for specific entry that already exist
        assertFailsWith(PrivmxException::class) {
            val queries: MutableList<String?> = mutableListOf()
            queries.add(
                kvdbApi.buildSubscriptionQueryForSelectedEntry(
                    KvdbEventType.ENTRY_CREATE,
                    kvdbId,
                    kvdbEntry2Key
                )
            )
        }

        // subscribe with nonexisting selectorId
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.ENTRY_CREATE,
                    KvdbEventSelectorType.CONTEXT_ID,
                    kvdb2Id
                )
            )
            entriesSubscriptionIds.addAll(kvdbApi.subscribeFor(queries))
        }

        // subscribe with nonexisting KVDB ID
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                kvdbApi.buildSubscriptionQueryForSelectedEntry(
                    KvdbEventType.ENTRY_UPDATE,
                    contextId!!,
                    kvdbEntryKey
                )
            )
            entriesSubscriptionIds.addAll(kvdbApi.subscribeFor(queries))
        }

        // subscribe with nonexisting KVDB ENTRY KEY
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                kvdbApi.buildSubscriptionQueryForSelectedEntry(
                    KvdbEventType.ENTRY_UPDATE,
                    kvdbId,
                    contextId!!
                )
            )
            entriesSubscriptionIds.addAll(kvdbApi.subscribeFor(queries))
        }

        // todo - SIGSEGV - will be fixed once the reported issue is fixed on bridge/endpoint
        /*
        // unsubscribe from KvdbEvents
         assertDoesNotFail {
            kvdbApi.unsubscribeFrom(kvdbsSubscriptionIds);
        }

        // unsubscribe from KvdbEvents again
         assertDoesNotFail {
            kvdbApi.unsubscribeFrom(kvdbsSubscriptionIds);
        }

        // unsubscribe from EntryEvents
         assertDoesNotFail {
            kvdbApi.unsubscribeFrom(entriesSubscriptionIds);
        }

        // unsubscribe from EntryEvents again
         assertDoesNotFail {
            kvdbApi.unsubscribeFrom(entriesSubscriptionIds);
        }
        */

        assertDoesNotFail {
            kvdbApi.unsubscribeFrom(kvdbsSubscriptionIds + entriesSubscriptionIds)
        }
    }

    @Test
    @Throws(Exception::class)
    fun accessAsPublicUser() {
        val connectionPublic: Connection = connectAsUser(
            ConnectionType.Public,
            bridgeAddress
        )
        val kvdbApiPublic = KvdbApi(connectionPublic)
        val updateMeta = "meta"

        // get kvdb
        assertFailsWith(PrivmxException::class) {
            kvdbApiPublic.getKvdb(kvdbId)
        }
        // create kvdb
        assertFailsWith(PrivmxException::class) {
            kvdbApiPublic.createKvdb(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // update kvdb
        assertFailsWith(PrivmxException::class) {
            kvdbApiPublic.updateKvdb(
                kvdb3Id, users, users, updateMeta.encodeToByteArray(), updateMeta.encodeToByteArray(), 2, true
            )
        }

        // list kvdbs
        assertFailsWith(PrivmxException::class) {
            kvdbApiPublic.listKvdbs(
                contextId!!, 0, 10, "desc"
            )
        }

        // delete kvdb
        assertFailsWith(PrivmxException::class) {
            kvdbApiPublic.deleteKvdb(kvdb3Id)
        }

        // get entry
        assertFailsWith(PrivmxException::class) {
            kvdbApiPublic.getEntry(kvdbId, kvdbEntryKey)
        }

        // list entries
        assertFailsWith(PrivmxException::class) {
            kvdbApiPublic.listEntriesKeys(
                kvdbId, 0, 1, "desc"
            )
        }

        // set entry
        assertFailsWith(PrivmxException::class) {
            kvdbApiPublic.setEntry(
                kvdb3Id,
                "public_entry_key",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }

        // update entry
        assertFailsWith(
            PrivmxException::class
        ) {
            kvdbApiPublic.setEntry(
                kvdb3Id,
                "public_entry_key",
                "new public meta".encodeToByteArray(),
                "new private meta".encodeToByteArray(),
                "data".encodeToByteArray(),
                1
            )
        }

        // delete entry
        assertFailsWith(PrivmxException::class) {
            kvdbApiPublic.deleteEntry(kvdbId, kvdbEntry2Key)
        }

        kvdbApiPublic.close()
        connectionPublic.close()
    }

    @Test
    @Ignore
    @Throws(Exception::class)
    fun setUserVerifierKvdb() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val kvdbApi2 = KvdbApi(connection2!!)
        val kvdb3 = kvdbApi.getKvdb(kvdb3Id)
        val illegalData = "Illegal data"

        val userVerifierFalse: UserVerifierInterface = object : UserVerifierInterface {
            override fun verify(request: List<VerificationRequest>): List<Boolean> {
                return request.map { req -> false }
            }
        }

        assertDoesNotFail {
            connection!!.setUserVerifier(
                userVerifierFalse
            )
        }

        // create container
        lateinit var kvdbCreatedId: String
        lateinit var kvdbCreated2Id: String

        assertDoesNotFail {
            kvdbCreatedId = kvdbApi.createKvdb(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            kvdbCreated2Id = kvdbApi2.createKvdb(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        val kvdbCreated = kvdbApi2.getKvdb(kvdbCreated2Id)
        assertEquals(0, kvdbCreated.statusCode)

        // get container
        lateinit var kvdbVerifiedUser: Kvdb
        lateinit var kvdbUnverifiedUser: Kvdb

        // user verified with positive result
        assertDoesNotFail {
            kvdbVerifiedUser = kvdbApi2.getKvdb(kvdbCreatedId)

        }
        assertEquals(0, kvdbVerifiedUser.statusCode)

        // user verified with negative result
        assertDoesNotFail {
            kvdbUnverifiedUser = kvdbApi.getKvdb(kvdbCreatedId)
        }
        assertNotEquals(0, kvdbUnverifiedUser.statusCode)

        // list containers
        lateinit var kvdbsVerifiedUser: PagingList<Kvdb>
        lateinit var kvdbsUnverifiedUser: PagingList<Kvdb>

        // user verified with positive result
        assertDoesNotFail {
            kvdbsVerifiedUser = kvdbApi2.listKvdbs(context2Id!!, 0, 10, "desc")
        }
        assertFalse(kvdbsVerifiedUser.readItems.isEmpty())
        kvdbsVerifiedUser.readItems.forEach({ i ->
            assertEquals(0, i.statusCode)
        })

        // user verified with negative result
        assertDoesNotFail {
            kvdbsUnverifiedUser = kvdbApi.listKvdbs(context2Id!!, 0, 10, "desc")
        }
        assertFalse(kvdbsUnverifiedUser.readItems.isEmpty())
        kvdbsUnverifiedUser.readItems.forEach({ i ->
            assertNotEquals(0, i.statusCode)
        })

        // update container
        assertFailsWith(PrivmxException::class) {
            kvdbApi.updateKvdb(
                kvdbCreatedId,
                users,
                users,
                illegalData.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                kvdb3.version!! + 1,
                false
            )
        }
        val kvdbUpdated = kvdbApi2.getKvdb(kvdbCreatedId)
        // should be equal - kvdb should not update
        assertEquals(kvdbCreated.publicMeta.decodeToString(), kvdbUpdated.publicMeta.decodeToString())
        assertEquals(kvdbCreated.version, kvdbUpdated.version)

        // delete container
        assertDoesNotFail {
            kvdbApi.deleteKvdb(
                kvdbCreated2Id
            )
        }

        // kvdb should be deleted
        assertFailsWith(PrivmxException::class) {
            kvdbApi2.getKvdb(kvdbCreated2Id)
        }

        // create item
        val kvdbEntryKey = "entry_key_1"
        val kvdbEntryKey2 = "entry_key_2"

        assertFailsWith(PrivmxException::class) {
            kvdbApi.setEntry(
                kvdbCreatedId,
                kvdbEntryKey,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            kvdbApi2.setEntry(
                kvdbCreatedId,
                kvdbEntryKey2,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        val entryCreated = kvdbApi2.getEntry(kvdbCreatedId, kvdbEntryKey2)
        assertEquals(0, entryCreated.statusCode)

        // get item
        lateinit var entryVerifiedUser: KvdbEntry
        lateinit var entryUnverifiedUser: KvdbEntry

        // user verified with positive result
        assertDoesNotFail {
            entryVerifiedUser = kvdbApi2.getEntry(kvdbCreatedId, kvdbEntryKey2)
        }
        assertEquals(0, entryVerifiedUser.statusCode)

        // user verified with negative result
        assertDoesNotFail {
            entryUnverifiedUser = kvdbApi.getEntry(kvdbCreatedId, kvdbEntryKey2)
        }
        assertNotEquals(0, entryUnverifiedUser.statusCode)

        // list items
        lateinit var entriessVerifiedUser: PagingList<KvdbEntry>
        lateinit var entriesUnverifiedUser: PagingList<KvdbEntry>

        // user verified with positive result
        assertDoesNotFail {
            entriessVerifiedUser = kvdbApi2.listEntries(kvdbCreatedId, 0, 10, "desc")
        }
        assertFalse(entriessVerifiedUser.readItems.isEmpty())
        entriessVerifiedUser.readItems.forEach({ i ->
            assertEquals(0, i.statusCode)
        })

        // user verified with negative result
        assertDoesNotFail {
            entriesUnverifiedUser = kvdbApi.listEntries(kvdbCreatedId, 0, 10, "desc")

        }
        assertFalse(entriesUnverifiedUser.readItems.isEmpty())
        entriesUnverifiedUser.readItems.forEach({ i -> assertNotEquals(0, i.statusCode) })

        // user1 updates
        assertFailsWith (PrivmxException::class) {
            kvdbApi.setEntry(
                kvdbCreatedId,
                kvdbEntryKey2,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                illegalData.encodeToByteArray(),
                1
            )
        }

        // delete item
        assertDoesNotFail {
            kvdbApi.deleteEntry(
                kvdbCreatedId, kvdbEntryKey2
            )
        }

        kvdbApi2.close()
    }
}