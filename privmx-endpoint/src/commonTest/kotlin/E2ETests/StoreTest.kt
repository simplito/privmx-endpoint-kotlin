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
import com.simplito.kotlin.privmx_endpoint.model.File
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.Store
import com.simplito.kotlin.privmx_endpoint.model.VerificationRequest
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.StoreEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.StoreEventType
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.core.UserVerifierInterface
import com.simplito.kotlin.privmx_endpoint.modules.store.StoreApi
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

//TODO: Add tests for close methods

@OptIn(ExperimentalStdlibApi::class, ExperimentalAtomicApi::class)
class StoreTest : BaseTest() {
    private lateinit var storeApi: StoreApi
    private lateinit var storeId: String
    private lateinit var store2Id: String
    private lateinit var store3Id: String
    private lateinit var fileId: String
    private lateinit var file2Id: String
    private val file1Data = "file_data_1"
    private val file2Data = "file_data_2_extra"

    fun createFile(storeId: String, data: String, randomWriteSupport: Boolean): String {
        return createFile(
            storeId,
            data,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            randomWriteSupport
        )
    }

    private fun createFile(
        storeId: String,
        data: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray
    ): String {
        return createFile(storeId, data, publicMeta, privateMeta, false)
    }

    private fun createFile(storeId: String, data: String): String {
        return createFile(
            storeId,
            data,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        );
    }

    private fun createFile(
        storeId: String,
        data: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        randomWriteSupport: Boolean
    ): String {
        val handle = storeApi.createFile(
            storeId,
            publicMeta,
            privateMeta,
            data.encodeToByteArray().size.toLong(),
            randomWriteSupport
        )!!
        storeApi.writeToFile(handle, data.encodeToByteArray())
        return storeApi.closeFile(handle)
    }

    private fun readFile(storeApi: StoreApi, id: String, size: Long): String {
        val handle: Long = storeApi.openFile(id)!!
        val result = storeApi.readFromFile(handle, size).decodeToString()
        storeApi.closeFile(handle)
        return result
    }

    private fun readFile(id: String, size: Long): String {
        return readFile(storeApi, id, size)
    }

    @BeforeTest
    fun createConnection() {
        connection = connectAsUser(ConnectionType.User1, bridgeAddress)
        storeApi = StoreApi(connection!!)
        storeId = storeApi.createStore(
            contextId!!,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        store2Id = storeApi.createStore(
            contextId!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        store3Id = storeApi.createStore(
            contextId!!,
            users,
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        fileId = createFile(storeId, file1Data)
        file2Id = createFile(storeId, file2Data)
    }

    @AfterTest
    @Throws(Exception::class)
    fun closeConnection() {
        if (::storeApi.isInitialized) {
            if (::store3Id.isInitialized) storeApi.deleteStore(store3Id)
            if (::store2Id.isInitialized) storeApi.deleteStore(store2Id)
            if (::storeId.isInitialized) storeApi.deleteStore(storeId)
            storeApi.close()
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
    @Throws(Exception::class)
    fun createStoreIncorrectInputData() {
        // incorrect contextId
        assertFailsWith(PrivmxException::class)
        {
            storeApi.createStore(
                storeId,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // incorrect users
        assertFailsWith(PrivmxException::class)
        {
            storeApi.createStore(
                context2Id!!,
                incorrectUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // incorrect managers
        assertFailsWith(PrivmxException::class)
        {
            storeApi.createStore(
                context2Id!!,
                users,
                incorrectUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // no managers
        assertFailsWith(
            PrivmxException::class
        )
        {
            storeApi.createStore(
                context2Id!!,
                users,
                emptyUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // creator not in managers
        assertFailsWith(
            PrivmxException::class
        )
        {
            storeApi.createStore(
                context2Id!!,
                users.subList(0, 1),
                users.subList(1, 2),
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        //2 same users
        assertFailsWith(PrivmxException::class)
        {
            storeApi.createStore(
                context2Id!!,
                sameUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        //2 same managers
        assertFailsWith(PrivmxException::class)
        {
            storeApi.createStore(
                context2Id!!,
                users,
                sameUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
    }

    @Test
    fun createStoreCorrectInputData() {
        lateinit var id: String

        // same users and managers
        assertDoesNotFail {
            id = storeApi.createStore(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // no users
        assertDoesNotFail {
            storeApi.createStore(
                context2Id!!,
                emptyUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // no publicMeta
        assertDoesNotFail {
            storeApi.createStore(
                context2Id!!,
                emptyUsers,
                users,
                ByteArray(0),
                privateMeta.encodeToByteArray()
            )
        }

        // no privateMeta
        assertDoesNotFail {
            storeApi.createStore(
                context2Id!!,
                emptyUsers,
                users,
                publicMeta.encodeToByteArray(),
                ByteArray(0)
            )
        }
        val store: Store = storeApi.getStore(id)
        assertEquals(store.contextId, context2Id)
        assertEquals(store.storeId, id)
    }

    @Test
    fun createStoredWithPolicy() {
        // TODO: test implementation of creating Store with Policy
    }

    @Test
    fun updateStoreWithPolicy() {
        // TODO: test implementation of updating Store with Policy
    }

    @Test
    @Throws(Exception::class)
    fun updateStoreIncorrectInputData() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val storeApi2 = StoreApi(connection2!!)
        val store: Store = storeApi.getStore(store2Id)

        // incorrect storeId
        assertFailsWith(PrivmxException::class) {
            storeApi.updateStore(
                contextId!!,
                users,
                users.subList(0, 1),
                store.publicMeta,
                store.privateMeta,
                store.version!!,
                false
            )
        }

        // incorrect users
        assertFailsWith(PrivmxException::class) {
            storeApi.updateStore(
                store2Id,
                incorrectUsers,
                users.subList(0, 1),
                store.publicMeta,
                store.privateMeta,
                store.version!!,
                false
            )
        }

        // incorrect managers
        assertFailsWith(PrivmxException::class)
        {
            storeApi.updateStore(
                store2Id,
                users.subList(0, 1),
                incorrectUsers,
                store.publicMeta,
                store.privateMeta,
                store.version!!,
                false
            )
        }

        // creator is not in managers
        assertFailsWith(PrivmxException::class)
        {
            storeApi.updateStore(
                store2Id,
                incorrectUsers,
                users.subList(1, 2),
                store.publicMeta,
                store.privateMeta,
                store.version!!,
                false
            )
        }

        // no managers
        assertFailsWith(PrivmxException::class)
        {
            storeApi.updateStore(
                store2Id,
                users.subList(0, 1),
                emptyUsers,
                store.publicMeta,
                store.privateMeta,
                store.version!!,
                false
            )
        }

        // incorrect version - force false
        assertFailsWith(PrivmxException::class)
        {
            storeApi.updateStore(
                store2Id,
                users.subList(0, 1),
                users.subList(0, 1),
                store.publicMeta,
                store.privateMeta,
                -1,
                false
            )
        }

        //        // updating user is in users, but not in managers
        assertFailsWith(PrivmxException::class)
        {
            storeApi2.updateStore(
                store3Id,
                users,
                users,
                store.publicMeta,
                store.privateMeta,
                2,
                true
            )
        }

        storeApi2.close()
    }

    @Test
    @Throws(Exception::class)
    fun updateStoreCorrectInputData() {
         connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val storeApi2 = StoreApi(connection2!!)
        val publicMetaUpdate = "new meta pub"
        val privateMetaUpdate = "new meta priv"

        val storeId: String? = storeApi.createStore(
            context2Id!!,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        var store = storeApi.getStore(storeId!!)
        val version = store.version!!

        // more users and managers
        assertDoesNotFail {
            storeApi.updateStore(
                storeId,
                users,
                users,
                publicMetaUpdate.encodeToByteArray(),
                privateMetaUpdate.encodeToByteArray(),
                version,
                false
            )
        }
        store = storeApi.getStore(storeId)
        assertEquals(store.contextId, context2Id)
        assertEquals(store.users.size, users.size)
        assertEquals(store.managers.size, users.size)
        assertContentEquals(store.publicMeta, publicMetaUpdate.encodeToByteArray())
        assertContentEquals(store.privateMeta, privateMetaUpdate.encodeToByteArray())
        assertEquals(store.version!!, version + 1)

        // less users
        assertDoesNotFail {
            storeApi.updateStore(
                storeId,
                emptyUsers,
                users.subList(0, 1),
                publicMetaUpdate.encodeToByteArray(),
                privateMetaUpdate.encodeToByteArray(),
                version + 1,
                false
            )
        }
        store = storeApi.getStore(storeId)
        assertEquals(store.contextId, context2Id)
        assertEquals(0, store.users.size)
        assertEquals(1, store.managers.size)
        assertContentEquals(store.publicMeta, publicMetaUpdate.encodeToByteArray())
        assertContentEquals(store.privateMeta, privateMetaUpdate.encodeToByteArray())
        assertEquals(store.version!!, version + 2)

        // incorrect version and force true
        assertDoesNotFail {
            storeApi.updateStore(
                storeId,
                users,
                users,
                publicMetaUpdate.encodeToByteArray(),
                privateMetaUpdate.encodeToByteArray(),
                version + 1000,
                true
            )
        }
        store = storeApi.getStore(storeId)
        assertEquals(store.contextId, context2Id)
        assertEquals(store.users.size, users.size)
        assertEquals(store.managers.size, users.size)
        assertContentEquals(store.publicMeta, publicMetaUpdate.encodeToByteArray())
        assertContentEquals(store.privateMeta, privateMetaUpdate.encodeToByteArray())
        assertEquals(store.version!!, version + 3)

        // user1 creates - user2(manager) updates
        assertDoesNotFail {
            storeApi2.updateStore(
                storeId,
                users,
                users,
                publicMetaUpdate.encodeToByteArray(),
                privateMetaUpdate.encodeToByteArray(),
                3,
                true
            )
        }

        storeApi2.close()
    }

    @Test
    @Throws(Exception::class)
    fun deleteStore() {
         connection2 =
            connectAsUser(ConnectionType.User2, bridgeAddress)
        val storeApi2 = StoreApi(connection2!!)

        // incorrect storeId
        assertFailsWith(PrivmxException::class) { storeApi.deleteStore(context2Id!!) }

        // user1 creates - user1 deletes (user1 is in managers)
        val id2 = storeApi.createStore(
            context2Id!!,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertDoesNotFail { storeApi.deleteStore(id2) }

        // user1 creates - user2 deletes (user2 is in managers list)
        val id3 = storeApi.createStore(
            context2Id!!,
            users.subList(0, 1),
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertDoesNotFail { storeApi2.deleteStore(id3) }

        // user1 creates - user2 deletes (user2 is in users list)
        val id4 = storeApi.createStore(
            context2Id!!,
            users,
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertFailsWith(PrivmxException::class) { storeApi2.deleteStore(id4) }

        // user1 creates - user2 deletes (user2 is not in users list)
        val id5 = storeApi.createStore(
            context2Id!!,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertFailsWith(PrivmxException::class) { storeApi2.deleteStore(id5) }

        storeApi2.close()
    }

    @Test
    fun getFile() {
        // incorrect fileId
        assertFailsWith(PrivmxException::class) { storeApi.getFile(contextId!!) }

        // correct fileId
        assertDoesNotFail { storeApi.getFile(fileId) }

        val file = storeApi.getFile(fileId)

        assertEquals(storeId, file.info.storeId)
        assertEquals(fileId, file.info.fileId)
        assertEquals(user1Id!!, file.info.author)
        assertEquals(users[0].pubKey, file.authorPubKey)
        assertEquals(publicMeta.encodeToByteArray().toHexString(), file.publicMeta.toHexString())
        assertEquals(privateMeta.encodeToByteArray().toHexString(), file.privateMeta.toHexString())
        assertEquals(0, file.statusCode)
        assertEquals(file1Data.encodeToByteArray().size.toString(), file.size.toString())
    }

    @Test
    fun getFileAfterForceKeyGeneration() {
        lateinit var file: File
        val newStoreId = storeApi.createStore(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        val newStore = storeApi.getStore(newStoreId)
        val newFileId = createFile(newStoreId, "data")
        val newFile = storeApi.getFile(newFileId)

        // force key generation
        storeApi.updateStore(
            newStoreId,
            users,
            users,
            newStore.publicMeta,
            newStore.privateMeta,
            newStore.version!!,
            forceGenerateNewKey = true
        )

        assertDoesNotFail { file = storeApi.getFile(newFileId) }
        assertEquals(file.info.storeId, newFile.info.storeId)
        assertEquals(file.info.fileId, newFile.info.fileId)
        assertEquals(file.info.createDate, newFile.info.createDate)
        assertEquals(file.info.author, newFile.info.author)
        assertEquals(file.authorPubKey, newFile.authorPubKey)
        assertContentEquals(file.publicMeta, newFile.publicMeta)
        assertContentEquals(file.privateMeta, newFile.privateMeta)
        assertEquals(file.statusCode, newFile.statusCode)
        assertEquals(file.size, newFile.size)

        // list files
        val files = storeApi.listFiles(newStoreId, 0, 1, "asc")
        assertEquals(1, files.readItems.size)
        assertEquals(1, files.totalAvailable)
        assertEquals(newFileId, files.readItems.first().info.fileId)
    }

    @Test
    fun listFilesIncorrectInputData() {
        // incorrect storeId
        assertFailsWith(PrivmxException::class) {
            storeApi.listFiles(
                contextId!!,
                0,
                1,
                "desc"
            )
        }

        // limit < 0
        assertFailsWith(PrivmxException::class) {
            storeApi.listFiles(
                storeId,
                0,
                -1,
                "desc"
            )
        }

        // limit == 0
        assertFailsWith(PrivmxException::class) {
            storeApi.listFiles(
                storeId,
                0,
                0,
                "desc"
            )
        }

        // incorrect sortOrder
        assertFailsWith(PrivmxException::class) {
            storeApi.listFiles(
                storeId,
                0,
                1,
                "wrong"
            )
        }

        // incorrect lastId
        assertFailsWith(PrivmxException::class) {
            storeApi.listFiles(storeId, 0, 1, "desc", contextId)
        }

        // incorrect queryAsJson
        assertFailsWith(PrivmxException::class) {
            storeApi.listFiles(storeId, 0, 1, "desc", null, "wrong")
        }

        // incorrect sortBy
        assertFailsWith(PrivmxException::class) {
            storeApi.listFiles(storeId, 0, 1, "desc", null, null, "wrong")
        }
    }

    @Test
    @Throws(Exception::class)
    fun listFilesCorrectInputData() {
        lateinit var files: PagingList<File>

        // correct data
        assertDoesNotFail { files = storeApi.listFiles(storeId, 0, 1, "desc") }
        assertEquals(2, files.totalAvailable)
        assertEquals(1, files.readItems.size)
        assertEquals(file2Id, files.readItems.first().info.fileId)

        // correct data
        assertDoesNotFail { files = storeApi.listFiles(storeId, 0, 4, "asc") }
        assertEquals(2, files.totalAvailable)
        assertEquals(2, files.readItems.size)
        assertEquals(fileId, files.readItems.first().info.fileId)
        assertEquals(file2Id, files.readItems.last().info.fileId)

        // skip > files amount
        assertDoesNotFail { files = storeApi.listFiles(storeId, 4, 1, "asc") }
        assertEquals(2, files.totalAvailable)
        assertEquals(0, files.readItems.size)

        // with last fileId
        assertDoesNotFail {
            files = storeApi.listFiles(
                storeId,
                0,
                1,
                "asc",
                fileId
            )
        }
        assertEquals(1, files.totalAvailable)
        assertEquals(1, files.readItems.size)
        assertEquals(file2Id, files.readItems.first().info.fileId)

        // with sortBy - createDate
        assertDoesNotFail {
            files = storeApi.listFiles(storeId, 0, 1, "asc", null, null, "createDate")
        }
        assertEquals(2, files.totalAvailable)
        assertEquals(1, files.readItems.size)
    }

    @Test
    @Throws(PrivmxException::class)
    fun filteringListStoresWithQueryAsJson() {
        lateinit var storesList: PagingList<Store>

        assertDoesNotFail {
            storeApi.createStore(
                context2Id!!,
                users,
                users,
                json.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            storeApi.createStore(
                context2Id!!,
                users,
                users,
                json1.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            storeApi.createStore(
                context2Id!!,
                users,
                users,
                json2.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            storeApi.createStore(
                context2Id!!,
                users,
                users,
                json3.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            storeApi.createStore(
                context2Id!!,
                users,
                users,
                json4.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            storeApi.createStore(
                context2Id!!,
                users,
                users,
                json5.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            storeApi.createStore(
                context2Id!!,
                users,
                users,
                json6.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            storeApi.createStore(
                context2Id!!,
                users,
                users,
                json7.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            storeApi.createStore(
                context2Id!!,
                users,
                users,
                json8.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            storeApi.createStore(
                context2Id!!,
                users,
                users,
                json9.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            storeApi.createStore(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // json -> json & json1 & json8
        storesList = storeApi.listStores(context2Id!!, 0, 100, "desc", null, json)
        assertEquals(3, storesList.readItems.size)

        // json1 -> json1
        storesList = storeApi.listStores(context2Id!!, 0, 100, "desc", null, json1)
        assertEquals(1, storesList.readItems.size)

        // json2 -> json2 & json9
        storesList = storeApi.listStores(context2Id!!, 0, 100, "desc", null, json2)
        assertEquals(2, storesList.readItems.size)

        // json3 -> json3 & json9
        storesList = storeApi.listStores(context2Id!!, 0, 100, "desc", null, json3)
        assertEquals(2, storesList.readItems.size)
        // json4
        assertFailsWith(PrivmxException::class) {
            storesList = storeApi.listStores(
                context2Id!!,
                0,
                100,
                "desc",
                null,
                json4
            )
        }

        // json5 -> json5
        storesList = storeApi.listStores(context2Id!!, 0, 100, "desc", null, json5)
        assertEquals(1, storesList.readItems.size)

        // json6 -> json6
        storesList = storeApi.listStores(context2Id!!, 0, 100, "desc", null, json6)
        assertEquals(1, storesList.readItems.size)

        // json7 -> json7 & json8
        storesList = storeApi.listStores(context2Id!!, 0, 100, "desc", null, json7)
        assertEquals(2, storesList.readItems.size)

        // json8 -> json8
        storesList = storeApi.listStores(context2Id!!, 0, 100, "desc", null, json8)
        assertEquals(1, storesList.readItems.size)

        // json9 -> json9
        storesList = storeApi.listStores(context2Id!!, 0, 100, "desc", null, json9)
        assertEquals(1, storesList.readItems.size)

        // query10 -> json & json1 & json8
        storesList = storeApi.listStores(context2Id!!, 0, 100, "desc", null, query10)
        assertEquals(3, storesList.readItems.size)

        // query11 -> json7 & json8 & json9
        storesList = storeApi.listStores(context2Id!!, 0, 100, "desc", null, query11)
        assertEquals(3, storesList.readItems.size)

        // query12 -> json8
        storesList = storeApi.listStores(context2Id!!, 0, 100, "desc", null, query12)
        assertEquals(1, storesList.readItems.size)

        // query13 -> json8
        storesList = storeApi.listStores(context2Id!!, 0, 100, "desc", null, query13)
        assertEquals(1, storesList.readItems.size)

        // query14 -> json & json1 & json7 & json8 & json9
        storesList = storeApi.listStores(context2Id!!, 0, 100, "desc", null, query14)
        assertEquals(5, storesList.readItems.size)

        // query15 -> json2 & json3 & json4 & json5 & json6 & json7 & json9 & publicMeta
        storesList = storeApi.listStores(context2Id!!, 0, 100, "desc", null, query15)
        assertEquals(8, storesList.readItems.size)

        storesList = storeApi.listStores(context2Id!!, 0, 100, "desc")
        assertTrue(storesList.totalAvailable!! >= 11)

    }

    @Test
    @Throws(PrivmxException::class)
    fun filteringListFilesWithQueryAsJson() {
        lateinit var filesList: PagingList<File>

        val id: String = storeApi.createStore(
            context2Id!!,
            users,
            users,
            json.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        assertDoesNotFail {
            createFile(
                id, "data", json.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            createFile(
                id, "data", json1.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            createFile(
                id, "data", json2.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            createFile(
                id, "data", json3.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            createFile(
                id, "data", json4.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            createFile(
                id, "data", json5.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            createFile(
                id, "data", json6.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            createFile(
                id, "data", json7.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            createFile(
                id, "data", json8.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            createFile(
                id, "data", json9.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            createFile(
                id, "data", publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }

        // json -> json & json1 & json8
        filesList = storeApi.listFiles(id, 0, 100, "desc", null, json)
        assertEquals(3, filesList.readItems.size)

        // json1 -> json1
        filesList = storeApi.listFiles(id, 0, 100, "desc", null, json1)
        assertEquals(1, filesList.readItems.size)

        // json2 -> json2 & json9
        filesList = storeApi.listFiles(id, 0, 100, "desc", null, json2)
        assertEquals(2, filesList.readItems.size)

        // json3 -> json3 & json9
        filesList = storeApi.listFiles(id, 0, 100, "desc", null, json3)
        assertEquals(2, filesList.readItems.size)

        // json4
        assertFailsWith(PrivmxException::class) {
            filesList = storeApi.listFiles(id, 0, 100, "desc", null, json4)
            filesList.readItems.forEach(::println)
        }

        // json5 -> json5
        filesList = storeApi.listFiles(id, 0, 100, "desc", null, json5)
        assertEquals(1, filesList.readItems.size)

        // json6 -> json6
        filesList = storeApi.listFiles(id, 0, 100, "desc", null, json6)
        assertEquals(1, filesList.readItems.size)

        // json7 -> json7 & json8
        filesList = storeApi.listFiles(id, 0, 100, "desc", null, json7)
        assertEquals(2, filesList.readItems.size)

        // json8 -> json8
        filesList = storeApi.listFiles(id, 0, 100, "desc", null, json8)
        assertEquals(1, filesList.readItems.size)

        // json9 -> json9
        filesList = storeApi.listFiles(id, 0, 100, "desc", null, json9)
        assertEquals(1, filesList.readItems.size)

        // query10 -> json & json1 & json8
        filesList = storeApi.listFiles(id, 0, 100, "desc", null, query10)
        assertEquals(3, filesList.readItems.size)

        // query11 -> json7 & json8 & json9
        filesList = storeApi.listFiles(id, 0, 100, "desc", null, query11)
        assertEquals(3, filesList.readItems.size)

        // query12 -> json8
        filesList = storeApi.listFiles(id, 0, 100, "desc", null, query12)
        assertEquals(1, filesList.readItems.size)

        // query13 -> json8
        filesList = storeApi.listFiles(id, 0, 100, "desc", null, query13)
        assertEquals(1, filesList.readItems.size)

        // query14 -> json & json1 & json7 & json8 & json9
        filesList = storeApi.listFiles(id, 0, 100, "desc", null, query14)
        assertEquals(5, filesList.readItems.size)

        // query15 -> json2 & json3 & json4 & json5 & json6 & json7 & json9 & publicMeta
        filesList = storeApi.listFiles(id, 0, 100, "desc", null, query15)
        assertEquals(8, filesList.readItems.size)

        filesList = storeApi.listFiles(id, 0, 100, "desc")
        assertTrue(filesList.readItems.size >= 11)
    }

    @Test
    fun openFile() {
        val fileId = createFile(store2Id, "File content.")
        val fileSize: Long = "File content.".encodeToByteArray().size.toLong()
        var readHandle: Long? = null
        lateinit var bytes: ByteArray

        // open - incorrect fileId
        assertFailsWith(PrivmxException::class) { storeApi.openFile(store2Id) }

        // open - correct fileId
        assertDoesNotFail { readHandle = storeApi.openFile(fileId)!! }

        //after this compiler knows that readHandle is not null
        readHandle!!

        // read
        assertDoesNotFail {
            bytes = storeApi.readFromFile(
                readHandle,
                fileSize
            )
        }
        assertNotNull(bytes)

        storeApi.closeFile(readHandle)
    }

    @Test
    fun createFileIncorrectInputData() {
        // incorrect storeId
        assertFailsWith(PrivmxException::class) {
            storeApi.createFile(
                context2Id!!, publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray(), 24
            )
        }

        // size < 0
        assertFailsWith(PrivmxException::class) {
            storeApi.createFile(
                store2Id, publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray(), -1
            )
        }

        // very big size
        assertFailsWith(PrivmxException::class) {
            storeApi.createFile(
                store2Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                5 * 1024L * 1024L * 1024L
            )
        }
    }

    @Test
    fun createFileCorrectInputData() {
        val fileContent = "File content"
        lateinit var fileId: String

        // correct data: size = 0
        assertDoesNotFail {
            val fileHandle = storeApi.createFile(
                store2Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                0
            )!!
            storeApi.closeFile(fileHandle)
        }

        // correct data
        assertDoesNotFail {
            val fileHandle = storeApi.createFile(
                store2Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                fileContent.encodeToByteArray().size.toLong()
            )!!
            storeApi.writeToFile(fileHandle, fileContent.encodeToByteArray())
            fileId = storeApi.closeFile(fileHandle)
        }

        val file: File = storeApi.getFile(fileId)
        assertEquals(fileContent.encodeToByteArray().size.toLong(), file.size)
        assertEquals(file.info.storeId, store2Id)
    }

    @Test
    fun createFileWithRandomWriteSupport() {
        val fileContent = "File content"
        lateinit var id: String
        var file: File

        // create file with default randomWriteSupport value (false)
         assertDoesNotFail {
             val handle = storeApi.createFile(
                 store3Id,
                 publicMeta.encodeToByteArray(),
                 privateMeta.encodeToByteArray(),
                 fileContent.encodeToByteArray().size.toLong()
             )!!
             storeApi.writeToFile(handle, fileContent.encodeToByteArray())
             id = storeApi.closeFile(handle)
        }
        file = storeApi.getFile(id)
        assertFalse(file.randomWrite)


        // create file with randomWriteSupport = false
        assertDoesNotFail {
            val handle = storeApi.createFile(
                store3Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                fileContent.encodeToByteArray().size.toLong(),
                false
            )!!
            storeApi.writeToFile(handle, fileContent.encodeToByteArray())
            id = storeApi.closeFile(handle)
        }
        file = storeApi.getFile(id)
        assertFalse(file.randomWrite)


        // create file with randomWriteSupport = true
        assertDoesNotFail {
            val handle = storeApi.createFile(
                store3Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                fileContent.encodeToByteArray().size.toLong(),
                true
            )!!
            storeApi.writeToFile(handle, fileContent.encodeToByteArray())
            id = storeApi.closeFile(handle)
        }
        file = storeApi.getFile(id)
        assertTrue(file.randomWrite)
    }
    
    @Test
    fun writeToFileIncorrectInputData() {
        val fileContent = "File content"
        val shortFileContent = "Content"
        val longFileContent = "Very long file content"

        val id = storeApi.createStore(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        // incorrect handle
        assertFailsWith(PrivmxException::class) {
            val fileHandle = storeApi.createFile(
                id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                fileContent.encodeToByteArray().size.toLong()
            )!!
            storeApi.writeToFile(-1, fileContent.encodeToByteArray())
            storeApi.closeFile(fileHandle)
        }

        // content shorter than declared size
        assertFailsWith(PrivmxException::class) {
            val fileHandle = storeApi.createFile(
                id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                fileContent.encodeToByteArray().size.toLong()
            )!!
            storeApi.writeToFile(fileHandle, shortFileContent.encodeToByteArray())
            storeApi.closeFile(fileHandle)
        }

        // content longer than declared size
        assertFailsWith(PrivmxException::class) {
            val fileHandle = storeApi.createFile(
                id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                fileContent.encodeToByteArray().size.toLong()
            )!!
            storeApi.writeToFile(fileHandle, longFileContent.encodeToByteArray())
            storeApi.closeFile(fileHandle)
        }
    }

    @Test
    fun writeToFileCorrectInputData() {
        val fileContent = "File content"
        val id = storeApi.createStore(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        val fileHandle = storeApi.createFile(
            id,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            fileContent.encodeToByteArray().size.toLong()
        )!!
        lateinit var fileId: String

        assertDoesNotFail {
            storeApi.writeToFile(fileHandle, fileContent.encodeToByteArray())
            fileId = storeApi.closeFile(fileHandle)
        }

        val file: File = storeApi.getFile(fileId)
        assertEquals(file.size, fileContent.encodeToByteArray().size.toLong())
        assertEquals(file.info.storeId, id)
    }

    @Test
    fun writeToFileWithRandomWrite() {
        val fileContent = "File content";
        val shortContent = "Short"
        val longContent = "Long file content"
        var file: File
        var content: String

        // shorter content & truncate = true
        val id1 = createFile(store3Id, fileContent, true)
        assertDoesNotFail {
            val read_write_handle: Long = storeApi.openFile(id1)!!
            storeApi.writeToFile(read_write_handle, shortContent.encodeToByteArray(), true)
            storeApi.closeFile(read_write_handle)
        }
        file = storeApi.getFile(id1)
        content = readFile(id1, file.size!!)
        assertEquals(shortContent, content)


        // shorter content & truncate = false
        val id2 = createFile(store3Id, fileContent, true)
        assertDoesNotFail {
            val read_write_handle: Long = storeApi.openFile(id2)!!
            storeApi.writeToFile(read_write_handle, shortContent.encodeToByteArray(), false)
            storeApi.closeFile(read_write_handle)
        }
        file = storeApi.getFile(id2)
        content = readFile(id2, file.size!!)
        val expected: String = shortContent + fileContent.substring(shortContent.length)
        assertEquals(expected, content)


        // longer content & truncate = true
        val id3 = createFile(store3Id, fileContent, true)
        assertDoesNotFail {
            val read_write_handle: Long = storeApi.openFile(id3)!!
            storeApi.writeToFile(read_write_handle, longContent.encodeToByteArray(), true)
            storeApi.closeFile(read_write_handle)
        }
        file = storeApi.getFile(id3)
        content = readFile(id3, file.size!!)
        assertEquals(longContent, content)


        // longer content & truncate = false
        val id4 = createFile(store3Id, fileContent, true)
        assertDoesNotFail {
            val read_write_handle: Long = storeApi.openFile(id4)!!
            storeApi.writeToFile(read_write_handle, longContent.encodeToByteArray())
            storeApi.closeFile(read_write_handle)
        }
        file = storeApi.getFile(id4)
        content = readFile(id4, file.size!!)
        assertEquals(longContent, content)
    }

    @Test
    fun updateFile() {
        val fileContent = "New file content"
        val newPublicMeta = "New public"
        val newPrivateMeta = "New private"
        val shortFileContent = "Content"
        val longFileContent = "Very long file content"

        // incorrect - content shorter than declared size
        assertFailsWith(PrivmxException::class) {
            val fileHandle = storeApi.updateFile(
                file2Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                fileContent.encodeToByteArray().size.toLong()
            )!!
            storeApi.writeToFile(fileHandle, shortFileContent.encodeToByteArray())
            storeApi.closeFile(fileHandle)
        }

        // incorrect - content longer than declared size
        assertFailsWith(PrivmxException::class) {
            val fileHandle = storeApi.updateFile(
                file2Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                fileContent.encodeToByteArray().size.toLong()
            )!!
            storeApi.writeToFile(fileHandle, longFileContent.encodeToByteArray())
            storeApi.closeFile(fileHandle)
        }

        // update - correct data - size = 0
        assertDoesNotFail {
            val handle = storeApi.updateFile(
                file2Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                0
            )!!
            storeApi.closeFile(handle)
        }

        // public meta
        assertDoesNotFail {
            val handle = storeApi.updateFile(
                file2Id,
                newPublicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                0
            )!!
            storeApi.closeFile(handle)
        }

        // private meta
        assertDoesNotFail {
            storeApi.updateFileMeta(
                file2Id,
                publicMeta.encodeToByteArray(),
                newPrivateMeta.encodeToByteArray()
            )
        }

        // content
        var fileHandle: Long? = null
        assertDoesNotFail {
            fileHandle = storeApi.updateFile(
                file2Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                fileContent.encodeToByteArray().size.toLong()
            )!!
        }

        //after this compiler knows that fileHandle is not null
        fileHandle!!

        storeApi.writeToFile(fileHandle, fileContent.encodeToByteArray())
        storeApi.closeFile(fileHandle)
        fileHandle = storeApi.openFile(file2Id)
        val file: File = storeApi.getFile(file2Id)

        assertEquals(file.size, fileContent.encodeToByteArray().size.toLong())
        assertContentEquals(
            storeApi.readFromFile(
                fileHandle!!,
                fileContent.encodeToByteArray().size.toLong()
            ),
            fileContent.encodeToByteArray()
        )

        storeApi.closeFile(fileHandle)
    }

    @Test
    @Throws(Exception::class)
    fun deleteFile() {
         connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val storeApi2 = StoreApi(connection2!!)

        // creating new Store for testing
        val store4Id = storeApi.createStore(
            context2Id!!,
            users,
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        val store = storeApi.getStore(store4Id)

        // incorrect fileId
        assertFailsWith(PrivmxException::class) { storeApi.deleteFile(context2Id!!) }

        // user1 creates - user1(manager) deletes
        val id1 = createFile(store4Id, "data")
        assertDoesNotFail { storeApi.deleteFile(id1) }

        // user1 creates - user2(user & not manager) deletes
        assertFailsWith(PrivmxException::class) { storeApi2.deleteFile(id1) }

        // user1 creates - user2(manager) deletes
        storeApi.updateStore(
            store.storeId,
            users,
            users,
            store.publicMeta,
            store.privateMeta,
            store.version!!,
            false
        )
        val id2 = createFile(store4Id, "data")
        assertDoesNotFail { storeApi2.deleteFile(id2) }

        storeApi2.close()
    }

    @Test
    fun closeFile() {
        // incorrect handle
        assertFailsWith(PrivmxException::class) { storeApi.closeFile(1) }

        // create read handle
        val readHandle = storeApi.openFile(fileId)!!
        assertDoesNotFail { storeApi.closeFile(readHandle) }

        // create write handle
        val writeHandle = storeApi.createFile(
            store2Id,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            0
        )!!
        assertDoesNotFail { storeApi.closeFile(writeHandle) }

        // close file again
        assertFailsWith(PrivmxException::class) { storeApi.closeFile(writeHandle) }
    }

    @Test
    fun seekInFile() {
        val fileId = createFile(store2Id, "file data")
        val file: File = storeApi.getFile(fileId)
        val readHandle = storeApi.openFile(fileId)!!

        // seek - position < 0
        assertFailsWith(PrivmxException::class) { storeApi.seekInFile(readHandle, -1) }

        // seek - position > file size
        assertFailsWith(PrivmxException::class)
        { storeApi.seekInFile(readHandle, file.size!! + 1) }

        // seek - position = 50% file size
        assertDoesNotFail { storeApi.seekInFile(readHandle, file.size!! / 2) }

        // seek - position = 0
        assertDoesNotFail { storeApi.seekInFile(readHandle, 0) }

        // seek - incorrect handle
        assertFailsWith(PrivmxException::class) { storeApi.seekInFile(-404, 2) }

        storeApi.closeFile(readHandle)
    }

    @Test
    fun readFromFile() {
        val content = "Long story about creating files."
        val fileId = createFile(store2Id, content)
        lateinit var fileContent: ByteArray
        val file: File = storeApi.getFile(fileId)
        val readHandle: Long = storeApi.openFile(fileId)!!

        // read - incorrect handle
        assertFailsWith(PrivmxException::class) { storeApi.readFromFile(-404, 2) }

        // read - length > file size
        storeApi.seekInFile(readHandle, 0)
        assertDoesNotFail { storeApi.readFromFile(readHandle, file.size!! + 1) }

        // read - length = 50% file size
        storeApi.seekInFile(readHandle, 0)
        assertDoesNotFail { storeApi.readFromFile(readHandle, file.size!! / 2) }

        // read - length = file size
        storeApi.seekInFile(readHandle, 0)
        assertDoesNotFail {
            fileContent = storeApi.readFromFile(
                readHandle,
                file.size!!
            )
        }

        storeApi.closeFile(readHandle)

        assertContentEquals(fileContent, content.encodeToByteArray())
    }

    @Test
    fun fileOperationsIncorrectHandle() {
        val fileContent = "New file content"

        // create read handle
        val readHandle: Long = storeApi.openFile(fileId)!!

        // write with read handle
        assertFailsWith(PrivmxException::class)
        { storeApi.writeToFile(readHandle, fileContent.encodeToByteArray()) }

        // create write handle
        storeApi.closeFile(readHandle)
        val writeHandle: Long = storeApi.createFile(
            store2Id,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            0
        )!!

        // read with write handle
        assertFailsWith(PrivmxException::class) {
            storeApi.readFromFile(
                writeHandle,
                2
            )
        }

        // seek with write handle
        assertFailsWith(PrivmxException::class) { storeApi.seekInFile(writeHandle, 2) }

        storeApi.closeFile(writeHandle)
    }


    @Test
    fun getStore() {
        lateinit var store: Store

        // incorrect Id
        assertFailsWith(PrivmxException::class) {
            storeApi.getStore(contextId!!)
        }

        // correctId
        assertDoesNotFail { store = storeApi.getStore(storeId) }
        assertEquals(contextId!!, store.contextId)
        assertEquals(storeId, store.storeId)
        assertEquals(user1Id!!, store.creator)
        assertEquals(user1Id!!, store.lastModifier)
        assertEquals("1", store.version.toString())
        assertEquals("2", store.filesCount.toString())
        assertEquals("0", store.statusCode.toString())
        assertEquals(publicMeta.encodeToByteArray().toHexString(), store.publicMeta.toHexString())
        assertEquals(privateMeta.encodeToByteArray().toHexString(), store.privateMeta.toHexString())
        assertEquals(1, store.users.size)
        assertEquals(user1Id!!, store.users.first())
        assertEquals(user1Id!!, store.managers.first())
    }

    @Test
    fun listStoresIncorrectInputData() {
        // incorrect contextId
        assertFailsWith(PrivmxException::class) {
            storeApi.listStores(
                storeId,
                0,
                1,
                "desc"
            )
        }

        // limit < 0
        assertFailsWith(PrivmxException::class) {
            storeApi.listStores(
                contextId!!,
                0,
                -1,
                "desc"
            )
        }

        // limit == 0
        assertFailsWith(PrivmxException::class) {
            storeApi.listStores(
                contextId!!,
                0,
                0,
                "desc"
            )
        }

        // incorrect sortOrder
        assertFailsWith(PrivmxException::class) {
            storeApi.listStores(
                contextId!!,
                0,
                1,
                "wrong"
            )
        }

        // incorrect lastId
        assertFailsWith(PrivmxException::class) {
            storeApi.listStores(contextId.toString(), 0, 1, "desc", "wrong")
        }

        // incorrect queryAsJson
        assertFailsWith(PrivmxException::class) {
            storeApi.listStores(contextId!!, 0, 1, "desc", null, "wrong")
        }

        // incorrect sortBy
        assertFailsWith(PrivmxException::class) {
            storeApi.listStores(contextId!!, 0, 1, "desc", null, null, "wrong")
        }
    }

    @Test
    fun listStoresCorrectInputData() {
        val contextId = contextId!!
        lateinit var stores: PagingList<Store>

        assertDoesNotFail { stores = storeApi.listStores(contextId, 4, 1, "desc") }
        assertEquals(3, stores.totalAvailable)
        assertEquals(0, stores.readItems.size)

        assertDoesNotFail { stores = storeApi.listStores(contextId, 0, 1, "desc") }
        assertEquals(3, stores.totalAvailable)
        assertEquals(1, stores.readItems.size)
        assertEquals(contextId, stores.readItems.first().contextId)
        assertEquals(store3Id, stores.readItems.first().storeId)

        assertDoesNotFail { stores = storeApi.listStores(contextId, 1, 3, "asc") }
        assertEquals(3, stores.totalAvailable)
        assertEquals(2, stores.readItems.size)
        assertEquals(contextId, stores.readItems.first().contextId)
        assertEquals(store2Id, stores.readItems.first().storeId)

        // lastId
        assertDoesNotFail { stores = storeApi.listStores(contextId, 0, 1, "asc", storeId) }
        assertEquals(2, stores.totalAvailable);
        assertEquals(1, stores.readItems.size);

        // sortBy - createDate
        assertDoesNotFail {
            stores = storeApi.listStores(
                contextId,
                0,
                3,
                "asc",
                null,
                null,
                "createDate"
            )
        }
        assertEquals(3, stores.totalAvailable)
        assertEquals(3, stores.readItems.size)


        // sortBy - lastModificationDate
        assertDoesNotFail {
            stores = storeApi.listStores(
                    contextId,
                    0,
                    3,
                    "asc",
                    null,
                    null,
                    "lastModificationDate"
                )
        }
        assertEquals(3, stores.totalAvailable)
        assertEquals(3, stores.readItems.size)


        // sortBy - lastFileDate
        assertDoesNotFail {
            stores = storeApi.listStores(
                contextId,
                0,
                3,
                "asc",
                null,
                null,
                "lastFileDate")
        }

        assertEquals(3, stores.totalAvailable)
        assertEquals(3, stores.readItems.size)
    }

    @Test
    @Throws(Exception::class)
    fun accessAsPublicUser() {
        val connectionPublic =
            connectAsUser(ConnectionType.Public, bridgeAddress)
        val storeApiPublic = StoreApi(connectionPublic)
        val updateMeta = "meta"

        // get store
        assertFailsWith(PrivmxException::class) { storeApiPublic.getStore(storeId) }

        // create store
        assertFailsWith(PrivmxException::class) {
            storeApiPublic.createStore(
                contextId!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // update store
        assertFailsWith(PrivmxException::class) {
            storeApiPublic.updateStore(
                storeId,
                users,
                users,
                updateMeta.encodeToByteArray(),
                updateMeta.encodeToByteArray(),
                2,
                true
            )
        }

        // delete store
        assertFailsWith(PrivmxException::class) { storeApiPublic.deleteStore(storeId) }

        // get file
        assertFailsWith(PrivmxException::class) { storeApiPublic.getFile(fileId) }

        // list files
        assertFailsWith(PrivmxException::class) {
            storeApiPublic.listFiles(storeId, 0, 1, "desc")
        }

        // update file
        assertFailsWith(PrivmxException::class) {
            storeApiPublic.updateFile(
                fileId,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                0
            )
        }

        // create file
        assertFailsWith(PrivmxException::class) {
            storeApiPublic.createFile(
                store2Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                0
            )
        }

        // delete file
        assertFailsWith(PrivmxException::class) { storeApiPublic.deleteFile(fileId) }

        storeApiPublic.close()
        connectionPublic.close()
    }

    @Test
    @Throws(Exception::class)
    fun syncFileWhileReading() {
        val fileContent = "File content"
        val newFileContent = "\nNew data added by user1"

         connection2 = connectAsUserAndCleanEvents(
             ConnectionType.User2,
             bridgeAddress
         )
        val storeApi2 = StoreApi(connection2!!)
        var file: File
        var content: String? = null

        // user1 - create file
        val id = createFile(store3Id, fileContent, true)

        // user2 - open file to read
        file = (storeApi2.getFile(id))
        val user2ReadHandle = storeApi2.openFile(id)

        // user1 - update file
        val user1ReadWriteHandle: Long = storeApi.openFile(id)!!
        storeApi.seekInFile(user1ReadWriteHandle, fileContent.encodeToByteArray().size.toLong())
        storeApi.writeToFile(user1ReadWriteHandle, newFileContent.encodeToByteArray())
        storeApi.closeFile(user1ReadWriteHandle)

        // user2 - read file - without sync
        assertFailsWith(PrivmxException::class) {
            content = storeApi2.readFromFile(user2ReadHandle!!, file.size!!).decodeToString()
        }
        assertNull(content)

        // user2 - read file - with sync
        assertDoesNotFail { storeApi2.syncFile(user2ReadHandle!!) }
        assertDoesNotFail {
            file = storeApi2.getFile(id)
            content = storeApi2.readFromFile(user2ReadHandle!!, file.size!!).decodeToString()
        }
        assertEquals(fileContent + newFileContent, content)

        storeApi2.closeFile(user2ReadHandle!!)
        storeApi2.close()
    }

    @Test
    fun updateFileWhileReading() {
        val fileContent = "New file content"
        val handle: Long = storeApi.openFile(file2Id)!!

        // update file
        assertDoesNotFail {
            val updateHandle = storeApi.updateFile(
                file2Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                0
            )!!
            storeApi.closeFile(updateHandle)
        }

        // try to read a file that has been updated after the file handle was opened.
        assertFailsWith(PrivmxException::class) {
            storeApi.readFromFile(handle, fileContent.encodeToByteArray().size.toLong() / 2)
        }

        // sync file
        storeApi.syncFile(handle)

        // read from file
        assertDoesNotFail {
            storeApi.readFromFile(
                handle,
                (fileContent.encodeToByteArray().size / 2).toLong()
            )
        }

        // try to close file handle
        assertDoesNotFail {
            storeApi.closeFile(
                handle
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun subscribeForStoreEvents() {
        val storesSubscriptionIds = mutableListOf<String>()
        val filesSubscriptionIds = mutableListOf<String>()

        // StoreEvents
        // subscribe for StoreEvents
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.STORE_CREATE,
                    StoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.STORE_UPDATE,
                    StoreEventSelectorType.STORE_ID,
                    store3Id
                )
            )
            queries.add(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.STORE_STATS,
                    StoreEventSelectorType.STORE_ID,
                    store3Id
                )
            )
            queries.add(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.STORE_DELETE,
                    StoreEventSelectorType.STORE_ID,
                    store3Id
                )
            )
            storesSubscriptionIds.addAll(storeApi.subscribeFor(queries))
        }

        // subscribe again
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.STORE_CREATE,
                    StoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            storesSubscriptionIds.addAll(storeApi.subscribeFor(queries))
        }

        // 2 same queries
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.STORE_UPDATE,
                    StoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.STORE_UPDATE,
                    StoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            storesSubscriptionIds.addAll(storeApi.subscribeFor(queries))
        }

        // subscribe with wrong StoreEventSelectorType
        assertFailsWith(PrivmxException::class) {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.STORE_CREATE,
                    StoreEventSelectorType.STORE_ID,
                    store3Id
                )
            )
        }

        // subscribe with nonexisting selectorId
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.STORE_CREATE,
                    StoreEventSelectorType.CONTEXT_ID,
                    "f977e533-3524-4579-error-215368c4b2a4"
                )
            )
        }


        // FileEvents
        // subscribe for FileEvents
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.FILE_UPDATE,
                    StoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.FILE_CREATE,
                    StoreEventSelectorType.STORE_ID,
                    store3Id
                )
            )
            queries.add(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.FILE_DELETE,
                    StoreEventSelectorType.STORE_ID,
                    store3Id
                )
            )
            filesSubscriptionIds.addAll(storeApi.subscribeFor(queries))
        }

        // subscribe again
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.FILE_CREATE,
                    StoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            filesSubscriptionIds.addAll(storeApi.subscribeFor(queries))
        }

        // 2 same queries
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.FILE_UPDATE,
                    StoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.FILE_UPDATE,
                    StoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            filesSubscriptionIds.addAll(storeApi.subscribeFor(queries))
        }

        // subscribe with wrong StoreEventSelectorType
        assertFailsWith(PrivmxException::class) {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.FILE_CREATE,
                    StoreEventSelectorType.FILE_ID,
                    fileId
                )
            )
        }

        // subscribe with nonexisting selectorId
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.FILE_CREATE,
                    StoreEventSelectorType.CONTEXT_ID,
                    store2Id
                )
            )
        }

        // todo - SIGSEGV - will be fixed once the reported issue is fixed on bridge/endpoint
        /*
        // unsubscribe from StoreEvents
         assertDoesNotFail {
            storeApi.unsubscribeFrom(storesSubscriptionIds);
        }

        // unsubscribe from StoreEvents again
         assertDoesNotFail {
            storeApi.unsubscribeFrom(storesSubscriptionIds);
        }

        // unsubscribe from FileEvents
         assertDoesNotFail {
            storeApi.unsubscribeFrom(filesSubscriptionIds);
        }

        // unsubscribe from FileEvents again
        assertDoesNotFail {
            storeApi.unsubscribeFrom(filesSubscriptionIds);
        }
       */

        assertDoesNotFail {
            storeApi.unsubscribeFrom(storesSubscriptionIds + filesSubscriptionIds)
        }
    }

    @Test
    @Throws(Exception::class)
    fun setUserVerifierStore() {
         connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val storeApi2 = StoreApi(connection2!!)
        val store3: Store = storeApi.getStore(store3Id)
        val illegalfile = "Illegal file"

        val userVerifierFalse = object : UserVerifierInterface {
            override fun verify(request: List<VerificationRequest>): List<Boolean> {
                return request.map { req: VerificationRequest? -> false }
            }
        }

        assertDoesNotFail {
            connection!!.setUserVerifier(
                userVerifierFalse
            )
        }

        // create container
        val storeCreatedId: String = storeApi.createStore(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        val storeCreated2Id: String = storeApi2.createStore(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        val storeCreated = storeApi2.getStore(storeCreatedId)
        assertEquals(0, storeCreated.statusCode)

        // get container
        lateinit var storeVerifiedUser: Store
        lateinit var storeUnverifiedUser: Store

        // user verified with positive result
        assertDoesNotFail {
            storeVerifiedUser = storeApi2.getStore(
                storeCreatedId
            )

        }
        assertEquals(0, storeVerifiedUser.statusCode)

        // user verified with negative result
        assertDoesNotFail {
            storeUnverifiedUser = storeApi.getStore(storeCreatedId)

        }
        assertNotEquals(0, storeUnverifiedUser.statusCode)

        // list containers
        lateinit var storesVerifiedUser: PagingList<Store>
        lateinit var storesUnverifiedUser: PagingList<Store>

        // user verified with positive result
        assertDoesNotFail {
            storesVerifiedUser = storeApi2.listStores(
                context2Id!!, 0, 10, "desc"
            )
        }
        assertFalse(storesVerifiedUser.readItems.isEmpty())
        storesVerifiedUser.readItems.forEach {
            assertEquals(0, it.statusCode)
        }

        // user verified with negative result
        assertDoesNotFail {
            storesUnverifiedUser = storeApi.listStores(context2Id!!, 0, 10, "desc")
        }
        assertFalse(storesUnverifiedUser.readItems.isEmpty())
        storesUnverifiedUser.readItems.forEach {
            assertNotEquals(0, it.statusCode)
        }

        // update container
        assertFailsWith(PrivmxException::class) {
            storeApi.updateStore(
                storeCreatedId,
                users,
                users,
                illegalfile.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                store3.version!! + 1,
                false
            )
        }
        val storeUpdated = storeApi2.getStore(storeCreatedId)
        // should be equal - store should not update
        assertContentEquals(storeCreated.publicMeta, storeUpdated.publicMeta)
        assertEquals(storeCreated.version, storeUpdated.version)

        // delete container
        assertDoesNotFail {
            storeApi.deleteStore(storeCreated2Id)
        }

        // store should be deleted
        assertFailsWith(PrivmxException::class) {
            storeApi2.getStore(
                storeCreated2Id
            )
        }

        // create item
        lateinit var fileCreatedId: String
        lateinit var fileCreated2Id: String

        assertFailsWith(PrivmxException::class) {
            val fileHandle = storeApi.createFile(
                storeCreatedId,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray().size.toLong()
            )!!
            storeApi.writeToFile(fileHandle, "data".encodeToByteArray())
            fileCreatedId = storeApi.closeFile(fileHandle)
        }
        assertDoesNotFail {
            val fileHandle = storeApi2.createFile(
                storeCreatedId,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray().size.toLong()
            )!!
            storeApi2.writeToFile(fileHandle, "data".encodeToByteArray())
            fileCreated2Id = storeApi2.closeFile(fileHandle)
        }

        val fileCreated = storeApi2.getFile(fileCreated2Id)
        assertEquals(0, fileCreated.statusCode)

        // get item
        lateinit var fileVerifiedUser: File
        lateinit var fileUnverifiedUser: File

        // user verified with positive result
        assertDoesNotFail {
            fileVerifiedUser = storeApi2.getFile(fileCreated2Id)
        }
        assertEquals(0, fileVerifiedUser.statusCode)

        // user verified with negative result
        assertDoesNotFail {
            fileUnverifiedUser = storeApi.getFile(fileCreated2Id)
        }
        assertNotEquals(0, fileUnverifiedUser.statusCode)

        // list items
        lateinit var filesVerifiedUser: PagingList<File>
        lateinit var filesUnverifiedUser: PagingList<File>

        // user verified with positive result
        assertDoesNotFail {
            filesVerifiedUser = storeApi2.listFiles(
                storeCreatedId, 0, 10, "desc"
            )
        }
        assertFalse(filesVerifiedUser.readItems.isEmpty())
        filesVerifiedUser.readItems.forEach {
            assertEquals(0, it.statusCode)
        }

        // user verified with negative result
        assertDoesNotFail {
            filesUnverifiedUser = storeApi.listFiles(storeCreatedId, 0, 10, "desc")
        }

        assertFalse(filesUnverifiedUser.readItems.isEmpty())
        filesUnverifiedUser.readItems.forEach {
            assertNotEquals(0, it.statusCode)
        }

        // update item
        // user verified with positive result
        assertDoesNotFail {
            storeApi2.updateFile(
                fileCreated2Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                illegalfile.encodeToByteArray().size.toLong()
            )
        }

        // user verified with negative result
        assertDoesNotFail {
            storeApi.updateFile(
                fileCreated2Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                illegalfile.encodeToByteArray().size.toLong()
            )
        }

        // updateFileMeta
        // user verified with positive result
        assertDoesNotFail {
            storeApi2.updateFileMeta(
                fileCreated2Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // user verified with negative result
        assertFailsWith(PrivmxException::class) {
            storeApi.updateFileMeta(
                fileCreated2Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // delete item
        assertDoesNotFail {
            storeApi.deleteFile(
                fileCreated2Id
            )
        }

        // store should be deleted
        assertFailsWith(PrivmxException::class) { storeApi2.getFile(fileCreated2Id) }

        storeApi2.close()
    }
}