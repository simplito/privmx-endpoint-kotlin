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
import com.simplito.kotlin.privmx_endpoint.model.FilesConfig
import com.simplito.kotlin.privmx_endpoint.model.Inbox
import com.simplito.kotlin.privmx_endpoint.model.InboxEntry
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.VerificationRequest
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.InboxEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.InboxEventType
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.UserVerifierInterface
import com.simplito.kotlin.privmx_endpoint.modules.inbox.InboxApi
import kotlin.concurrent.atomics.ExperimentalAtomicApi
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

@OptIn(ExperimentalStdlibApi::class, ExperimentalAtomicApi::class)
class InboxTest : BaseTest() {
    private lateinit var inboxApi: InboxApi
    private lateinit var inboxId: String
    private lateinit var inbox2Id: String
    private lateinit var inbox3Id: String
    private lateinit var entryId: String
    private lateinit var entry2Id: String
    private lateinit var entry1File0Id: String
    private val entry1Data = "message_from_inboxSendCommit_1"
    private val entry2Data = "message_from_inboxSendCommit_2"
    private val entry1File0Data = "test_entry_1_FileData_0"
    private val entry1File1Data = "test_entry_1_FileData_1"

    @BeforeTest
    @Throws(PrivmxException::class, NativeException::class)
    fun createConnection() {
        if (connection == null) {
            connection = connectAsUser(ConnectionType.User1, bridgeAddress)
        }
        inboxApi = InboxApi(connection!!)
        inboxId = inboxApi.createInbox(
            contextId!!, users.subList(0, 1), users.subList(0, 1),
            publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray(),
            FilesConfig(0L, 10L, 104857600L, 1048576000L)
        )
        inbox2Id = inboxApi.createInbox(
            contextId!!, users, users,
            publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray(),
            FilesConfig(0L, 2L, 134217728L, 267386880L)
        )
        inbox3Id = inboxApi.createInbox(
            contextId!!, users, users.subList(0, 1),
            publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray()
        )
        val file0Handle = inboxApi.createFileHandle(
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            entry1File0Data.encodeToByteArray().size.toLong()
        )!!
        val file1Handle = inboxApi.createFileHandle(
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            entry1File1Data.encodeToByteArray().size.toLong()
        )!!
        val entry1Handle = inboxApi.prepareEntry(
            inboxId, entry1Data.encodeToByteArray(), listOf(file0Handle, file1Handle)
        )!!
        inboxApi.writeToFile(entry1Handle, file0Handle, entry1File0Data.encodeToByteArray())
        inboxApi.writeToFile(entry1Handle, file1Handle, entry1File1Data.encodeToByteArray())
        inboxApi.sendEntry(entry1Handle)
        val entry1Item = inboxApi.listEntries(inboxId, 0, 1, "desc").readItems.first()
        entryId = entry1Item.entryId
        entry1File0Id = entry1Item.files.first().info.fileId

        val entry2Handle = inboxApi.prepareEntry(
            inboxId, entry2Data.encodeToByteArray()
        )!!
        inboxApi.sendEntry(entry2Handle)
        entry2Id = inboxApi.listEntries(inboxId, 0, 1, "desc").readItems.first().entryId
    }

    @AfterTest
    @Throws(Exception::class)
    fun closeConnection() {
        if (::inboxApi.isInitialized) {
            if (::inbox3Id.isInitialized) inboxApi.deleteInbox(inbox3Id)
            if (::inbox2Id.isInitialized) inboxApi.deleteInbox(inbox2Id)
            if (::inboxId.isInitialized) inboxApi.deleteInbox(inboxId)
            inboxApi.close()
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

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun sendFileToNewEntry(inboxApi: InboxApi, data: String, dataFile: String, inboxId: String): File {
        val handle = inboxApi.createFileHandle(
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            dataFile.encodeToByteArray().size.toLong()
        )!!
        val entryHandle = inboxApi.prepareEntry(inboxId, data.encodeToByteArray(), listOf(handle))!!
        inboxApi.writeToFile(entryHandle, handle, dataFile.encodeToByteArray())
        inboxApi.sendEntry(entryHandle)

        return inboxApi.listEntries(inboxId, 0, 1, "desc").readItems.first().files.first()
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun sendFileToNewEntry(data: String, dataFile: String, inboxId: String): File {
        return sendFileToNewEntry(inboxApi, data, dataFile, inboxId)
    }

    @Test
    fun getInbox() {
        lateinit var inbox: Inbox

        // incorrect inboxId
        assertFailsWith(PrivmxException::class) {
            inboxApi.getInbox(contextId!!)
        }

        // correct InboxId
        assertDoesNotFail {
            inbox = inboxApi.getInbox(inboxId)
        }
        assertEquals(inboxId, inbox.inboxId)
        assertEquals(contextId!!, inbox.contextId)
        assertEquals(user1Id!!, inbox.creator)
        assertEquals(user1Id!!, inbox.lastModifier)
        assertEquals("1", inbox.version.toString())
        assertEquals(publicMeta.encodeToByteArray().toHexString(), inbox.publicMeta.toHexString())
        assertEquals(privateMeta.encodeToByteArray().toHexString(), inbox.privateMeta.toHexString())
        assertEquals("0", inbox.statusCode.toString())
        assertEquals(1, inbox.users.size)
        assertEquals(1, inbox.managers.size)
    }

    @Test
    fun listInboxesIncorrectInputData() {
        // incorrect contextId
        assertFailsWith(PrivmxException::class) {
            inboxApi.listInboxes(inboxId, 0, 1, "desc")
        }

        // limit < 0
        assertFailsWith(PrivmxException::class) {
            inboxApi.listInboxes(contextId!!, 0, -1, "desc")
        }

        // limit == 0
        assertFailsWith(PrivmxException::class) {
            inboxApi.listInboxes(contextId!!, 0, 0, "desc")
        }

        // incorrect sortOrder
        assertFailsWith(PrivmxException::class) {
            inboxApi.listInboxes(contextId!!, 0, 1, "wrong")
        }

        // incorrect lastId
        assertFailsWith(PrivmxException::class) {
            inboxApi.listInboxes(contextId!!, 0, 1, "desc", contextId)
        }

        // incorrect queryAsJson
        assertFailsWith(PrivmxException::class) {
            inboxApi.listInboxes(contextId!!, 0, 1, "desc", null, "wrong")
        }

        // incorrect sortBy
        assertFailsWith(PrivmxException::class) {
            inboxApi.listInboxes(contextId!!, 0, 1, "desc", null, null, "wrong")
        }
    }

    @Test
    fun listInboxesCorrectInputData() {
        lateinit var inboxes: PagingList<Inbox>

        assertDoesNotFail {
            inboxes = inboxApi.listInboxes(contextId!!, 0, 1, "desc")
        }
        assertEquals(3, inboxes.totalAvailable)
        assertEquals(1, inboxes.readItems.size)
        assertEquals(inbox3Id, inboxes.readItems.first().inboxId)

        assertDoesNotFail {
            inboxes = inboxApi.listInboxes(contextId!!, 0, 4, "asc")
        }
        assertEquals(3, inboxes.totalAvailable)
        assertEquals(3, inboxes.readItems.size)
        assertEquals(inboxId, inboxes.readItems.first().inboxId)
        assertEquals(inbox3Id, inboxes.readItems.last().inboxId)

        assertDoesNotFail {
            inboxes = inboxApi.listInboxes(contextId!!, 4, 1, "asc")
        }
        assertEquals(3, inboxes.totalAvailable)
        assertEquals(0, inboxes.readItems.size)

        assertDoesNotFail {
            inboxes = inboxApi.listInboxes(contextId!!, 0, 1, "asc", inboxId)
        }
        assertEquals(2, inboxes.totalAvailable)
        assertEquals(1, inboxes.readItems.size)
        assertEquals(inbox2Id, inboxes.readItems.first().inboxId)

        // sortBy - createDate
        assertDoesNotFail {
            inboxes = inboxApi.listInboxes(contextId!!, 0, 1, "asc", null, null, "createDate")
        }
        assertEquals(3, inboxes.totalAvailable);
        assertEquals(1, inboxes.readItems.size);

        // sortBy - lastModificationDate
        assertDoesNotFail {
            inboxes = inboxApi.listInboxes(contextId!!, 0, 1, "asc", null, null, "lastModificationDate")
        }
        assertEquals(3, inboxes.totalAvailable);
        assertEquals(1, inboxes.readItems.size);
    }

    @Test
    @Throws(PrivmxException::class)
    fun filteringListInboxesWithQueryAsJson() {
        lateinit var inboxesList: PagingList<Inbox>

        assertDoesNotFail {  
            inboxApi.createInbox(
                context2Id!!, users, users, json.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            inboxApi.createInbox(
                context2Id!!, users, users, json.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            inboxApi.createInbox(
                context2Id!!, users, users, json.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            inboxApi.createInbox(
                context2Id!!, users, users, json.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            inboxApi.createInbox(
                context2Id!!, users, users, json.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            inboxApi.createInbox(
                context2Id!!, users, users, json.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            inboxApi.createInbox(
                context2Id!!, users, users, json.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            inboxApi.createInbox(
                context2Id!!, users, users, json.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            inboxApi.createInbox(
                context2Id!!, users, users, json.encodeToByteArray(), privateMeta.encodeToByteArray()
            )
        }

        // json -> json & json1 & json8
        inboxesList = inboxApi.listInboxes(context2Id!!, 0, 10, "desc", null, json)
        assertEquals(3, inboxesList.readItems.size)

        // json1 -> json1
        inboxesList = inboxApi.listInboxes(context2Id!!, 0, 10, "desc", null, json1)
        assertEquals(1, inboxesList.readItems.size)

        // json2 -> json2 & json9
        inboxesList = inboxApi.listInboxes(context2Id!!, 0, 10, "desc", null, json2)
        assertEquals(2, inboxesList.readItems.size)

        // json3 -> json3 & json9
        inboxesList = inboxApi.listInboxes(context2Id!!, 0, 10, "desc", null, json3)
        assertEquals(2, inboxesList.readItems.size)

        // json4
        assertFailsWith(PrivmxException::class) {
            inboxesList = inboxApi.listInboxes(context2Id!!, 0, 10, "desc", null, json4)
        }

        // json5 -> json5
        inboxesList = inboxApi.listInboxes(context2Id!!, 0, 10, "desc", null, json5)
        assertEquals(1, inboxesList.readItems.size)

        // json6 -> json6
        inboxesList = inboxApi.listInboxes(context2Id!!, 0, 10, "desc", null, json6)
        assertEquals(1, inboxesList.readItems.size)

        // json7 -> json7 & json8
        inboxesList = inboxApi.listInboxes(context2Id!!, 0, 10, "desc", null, json7)
        assertEquals(2, inboxesList.readItems.size)

        // json8 -> json8
        inboxesList = inboxApi.listInboxes(context2Id!!, 0, 10, "desc", null, json8)
        assertEquals(1, inboxesList.readItems.size)

        // json9 -> json9
        inboxesList = inboxApi.listInboxes(context2Id!!, 0, 10, "desc", null, json9)
        assertEquals(1, inboxesList.readItems.size)

        // query10 -> json & json1 & json8
        inboxesList = inboxApi.listInboxes(context2Id!!, 0, 10, "desc", null, query10)
        assertEquals(3, inboxesList.readItems.size)

        // query11 -> json7 & json8 & json9
        inboxesList = inboxApi.listInboxes(context2Id!!, 0, 10, "desc", null, query11)
        assertEquals(3, inboxesList.readItems.size)

        // query12 -> json8
        inboxesList = inboxApi.listInboxes(context2Id!!, 0, 10, "desc", null, query12)
        assertEquals(1, inboxesList.readItems.size)

        // query13 -> json8
        inboxesList = inboxApi.listInboxes(context2Id!!, 0, 10, "desc", null, query13)
        assertEquals(1, inboxesList.readItems.size)

        // query14 -> json & json1 & json7 & json8 & json9
        inboxesList = inboxApi.listInboxes(context2Id!!, 0, 10, "desc", null, query14)
        assertEquals(5, inboxesList.readItems.size)

        // query15 -> json2 & json3 & json4 & json5 & json6 & json7 & json9 & publicMeta
        inboxesList = inboxApi.listInboxes(context2Id!!, 0, 10, "desc", null, query15)
        assertEquals(8, inboxesList.readItems.size)

        inboxesList = inboxApi.listInboxes(context2Id!!, 0, 10, "desc")
        assertTrue(inboxesList.readItems.size >= 11)
    }

    @Test
    fun createInboxIncorrectInputData() {
        // incorrect contextId
        assertFailsWith(PrivmxException::class) {
            inboxApi.createInbox(
                inboxId,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // incorrect users
        assertFailsWith(PrivmxException::class) {
            inboxApi.createInbox(
                context2Id!!,
                incorrectUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // incorrect managers
        assertFailsWith(PrivmxException::class) {
            inboxApi.createInbox(
                context2Id!!,
                users,
                incorrectUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // empty managers list
        assertFailsWith(PrivmxException::class) {
            inboxApi.createInbox(
                context2Id!!,
                users,
                emptyUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // creator not in managers
        assertFailsWith(PrivmxException::class) {
            inboxApi.createInbox(
                context2Id!!,
                users,
                users.subList(1, 2),
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // 2 same users
        assertFailsWith(PrivmxException::class) {
            inboxApi.createInbox(
                context2Id!!,
                sameUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // 2 same managers
        assertFailsWith(PrivmxException::class) {
            inboxApi.createInbox(
                context2Id!!,
                users,
                sameUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // empty managers list
        assertFailsWith(PrivmxException::class) {
            inboxApi.createInbox(
                context2Id!!,
                users,
                emptyUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
    }

    @Test
    fun filesConfig() {
        lateinit var inbox: String

        // correct
        assertDoesNotFail {
            inbox = inboxApi.createInbox(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                FilesConfig(0L, 5L, 10L * 1024L * 1024L, 20L * 1024L * 1024L)
            )
            sendFileToNewEntry("data", "content", inbox)
        }

        val inboxWithFilesConfig: Inbox = inboxApi.getInbox(inbox)
        assertEquals(0L, inboxWithFilesConfig.filesConfig?.minCount)
        assertEquals(5L, inboxWithFilesConfig.filesConfig?.maxCount)
        assertEquals(10L * 1024L * 1024L, inboxWithFilesConfig.filesConfig?.maxFileSize)
        assertEquals(20L * 1024L * 1024L, inboxWithFilesConfig.filesConfig?.maxWholeUploadSize)

        // min count < 0
        assertFailsWith(PrivmxException::class) {
            inbox = inboxApi.createInbox(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                FilesConfig(-1L, 5L, 10L * 1024L * 1024L, 10L * 1024L * 1024L)
            )

        }

        // max count < 0
        assertFailsWith(PrivmxException::class) {
            inbox = inboxApi.createInbox(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                FilesConfig(0L, -1L, 10L * 1024L * 1024L, 10L * 1024L * 1024L)
            )
        }

        // max count == 0
        assertFailsWith(PrivmxException::class) {
            inbox = inboxApi.createInbox(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                FilesConfig(0L, 0L, 10L * 1024L * 1024L, 10L * 1024L * 1024L)
            )
            sendFileToNewEntry("data", "content", inbox)
        }

        // max file size < 0
        assertFailsWith(PrivmxException::class) {
            inbox = inboxApi.createInbox(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                FilesConfig(0L, 5L, -1L, 10L * 1024L * 1024L)
            )
        }

        // max whole upload size < 0
        assertFailsWith(PrivmxException::class) {
            inbox = inboxApi.createInbox(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                FilesConfig(0L, 5L, 10L * 1024L * 1024L, -1L)
            )
        }

        // max whole upload size == 0
        assertFailsWith(PrivmxException::class) {
            inbox = inboxApi.createInbox(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                FilesConfig(0L, 5L, 10L * 1024L * 1024L, 0L)
            )
            sendFileToNewEntry("data", "content", inbox)
        }
    }

    @Test
    fun createInboxCorrectInputData() {
        lateinit var id: String

        // same users and managers
        assertDoesNotFail {
            inboxApi.createInbox(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // empty users list
        assertDoesNotFail {
            inboxApi.createInbox(
                context2Id!!,
                emptyUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // empty publicMeta
        assertDoesNotFail {
            inboxApi.createInbox(
                context2Id!!,
                users,
                users,
                ByteArray(0),
                privateMeta.encodeToByteArray()
            )
        }

        // empty privateMeta
        assertDoesNotFail {
            inboxApi.createInbox(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                ByteArray(0)
            )
        }

        // user2 in users & user1 in managers
        assertDoesNotFail {
            id = inboxApi.createInbox(
                context2Id!!,
                users.subList(1, 2),
                users.subList(0, 1),
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        val inbox: Inbox = inboxApi.getInbox(id)
        assertEquals(id, inbox.inboxId)
        assertEquals(context2Id, inbox.contextId)
        assertEquals(user1Id, inbox.creator)
        assertEquals(1, inbox.users.size)
        assertEquals(1, inbox.managers.size)
    }

    @Test
    fun createInboxWithPolicy() {
        // TODO: test implementation of creating Inbox with Policy
    }

    @Test
    @Throws(Exception::class)
    fun updateInboxIncorrectInputData() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val inboxApi2 = InboxApi(connection2!!)
        val inbox = inboxApi.getInbox(inbox2Id)

        // incorrect inboxId
        assertFailsWith(PrivmxException::class) {
            inboxApi.updateInbox(
                contextId!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                null,
                inbox.version!!,
                false
            )
        }

        // incorrect users
        assertFailsWith(PrivmxException::class) {
            inboxApi.updateInbox(
                inbox2Id,
                incorrectUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                null,
                inbox.version!!,
                false
            )
        }

        // incorrect managers
        assertFailsWith(PrivmxException::class) {
            inboxApi.updateInbox(
                inbox2Id,
                users,
                incorrectUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                null,
                inbox.version!!,
                false
            )
        }

        // creator is not in managers
        assertFailsWith(PrivmxException::class) {
            inboxApi.updateInbox(
                inbox2Id,
                users,
                users.subList(1, 2),
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                null,
                inbox.version!!,
                false
            )
        }

        // empty managers list
        assertFailsWith(PrivmxException::class) {
            inboxApi.updateInbox(
                inbox2Id,
                users,
                emptyUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                null,
                inbox.version!!,
                false
            )
        }

        // incorrect version & force false
        assertFailsWith(PrivmxException::class) {
            inboxApi.updateInbox(
                inbox2Id,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                null,
                10,
                false
            )
        }

        // user1 creates - user2(user & not manager) updates
        val id = inboxApi.createInbox(
            context2Id!!,
            users,
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertFailsWith(PrivmxException::class) {
            inboxApi2.updateInbox(
                id,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                null,
                10,
                false
            )
        }

        // user1 creates - user2(not user & not manager) updates
        inboxApi.updateInbox(
            id,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            null,
            10,
            true
        )
        assertFailsWith(PrivmxException::class) {
            inboxApi2.updateInbox(
                id,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                null,
                10,
                false
            )
        }

        inboxApi2.close()
    }

    @Test
    @Throws(Exception::class)
    fun updateInboxCorrectInputData() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val inboxApi2 = InboxApi(connection2!!)
        var inbox: Inbox = inboxApi.getInbox(inbox3Id)
        val version: Long = inbox.version!!

        // less users
        val finalInbox: Inbox = inbox
        assertDoesNotFail {
            inboxApi.updateInbox(
                inbox3Id,
                emptyUsers,
                users.subList(0, 1),
                finalInbox.publicMeta,
                finalInbox.privateMeta,
                null,
                version,
                false
            )
        }
        inbox = inboxApi.getInbox(inbox3Id)
        assertEquals(0, inbox.users.size)
        assertEquals(1, inbox.managers.size)
        assertEquals(version + 1, inbox.version)

        // more users and managers
        val finalInbox1: Inbox = inbox
        assertDoesNotFail {
            inboxApi.updateInbox(
                inbox3Id,
                users,
                users,
                finalInbox1.publicMeta,
                finalInbox1.privateMeta,
                null,
                version + 1,
                false
            )
        }
        inbox = inboxApi.getInbox(inbox3Id)
        assertEquals(2, inbox.users.size)
        assertEquals(2, inbox.managers.size)
        assertEquals(version + 2, inbox.version)

        // with filesConfig
        assertDoesNotFail {
            inboxApi.updateInbox(
                inbox3Id,
                users,
                users,
                finalInbox1.publicMeta,
                finalInbox1.privateMeta,
                FilesConfig(0L, 3L, 4L * 1024L, 25L * 1024L),
                version + 2,
                false
            )
        }

        // incorrect version and force true
        val finalInbox2: Inbox = inbox
        assertDoesNotFail {
            inboxApi.updateInbox(
                inbox3Id,
                users,
                users,
                finalInbox2.publicMeta,
                finalInbox2.privateMeta,
                null,
                version + 10,
                true
            )
        }
        inbox = inboxApi.getInbox(inbox3Id)
        assertEquals(2, inbox.users.size)
        assertEquals(2, inbox.managers.size)
        assertEquals(version + 4, inbox.version)

        //user1 creates - user2(manager) updates
        val finalInbox3: Inbox = inbox
        assertDoesNotFail {
            inboxApi2.updateInbox(
                inbox3Id,
                users,
                users,
                finalInbox3.publicMeta,
                finalInbox3.privateMeta,
                null,
                version + 4,
                false
            )
        }
        inbox = inboxApi.getInbox(inbox3Id)
        assertEquals(2, inbox.users.size)
        assertEquals(2, inbox.managers.size)
        assertEquals(version + 5, inbox.version)

        inboxApi2.close()
    }

    @Test
    fun updateInboxWithPolicy() {
        // TODO: test implementation of updating Inbox with Policy
    }

    @Test
    fun closeInbox() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val inboxApi2 = InboxApi(connection2!!)

        // close correct
        assertDoesNotFail { inboxApi2.close() }

        // close once again
        assertFailsWith(IllegalStateException::class) { inboxApi2.close() }
    }

    @Test
    @Throws(Exception::class)
    fun deleteInboxIncorrectInputData() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val inboxApi2 = InboxApi(connection2!!)

        // incorrect inboxId
        assertFailsWith(PrivmxException::class) {
            inboxApi.deleteInbox(contextId!!)
        }

        //user1 creates - user2(not user & not manager) deletes
        val id = inboxApi.createInbox(
            context2Id!!,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertFailsWith(PrivmxException::class) {
            inboxApi2.deleteInbox(id)
        }

        //user1 creates - user2(user & not manager) deletes
        val id2 = inboxApi.createInbox(
            context2Id!!,
            users,
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertFailsWith(PrivmxException::class) {
            inboxApi2.deleteInbox(id2)
        }

        inboxApi2.close()
    }

    @Test
    @Throws(Exception::class)
    fun deleteInboxCorrectInputData() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val inboxApi2 = InboxApi(connection2!!)

        // user1 creates - user1 deletes
        val id = inboxApi.createInbox(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertDoesNotFail {
            inboxApi.deleteInbox(id)
        }

        // user1 creates - user2(manager) deletes
        val id2 = inboxApi.createInbox(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertDoesNotFail {
            inboxApi2.deleteInbox(id2)
        }

        inboxApi2.close()
    }

    @Test
    fun prepareEntry() {
        val data: ByteArray = "message".encodeToByteArray()

        // incorrect inboxId
        assertFailsWith(PrivmxException::class) {
            inboxApi.prepareEntry(contextId!!, data)
        }

        // correct
        assertDoesNotFail {
            inboxApi.prepareEntry(inbox2Id, data)
        }

        // incorrect with list of writeHandles
        assertFailsWith(PrivmxException::class) {
            inboxApi.prepareEntry(inbox2Id, data, listOf(-404L))
        }

        // correct with list of writeHandles
        val writeHandle: Long =
            inboxApi.createFileHandle(
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                data.size.toLong()
            )!!
        assertDoesNotFail {
            inboxApi.prepareEntry(inbox2Id, data, listOf(writeHandle))
        }

        // incorrect userPrivKey
        assertFailsWith(PrivmxException::class) {
            inboxApi.prepareEntry(
                inbox2Id,
                data,
                listOf(writeHandle),
                IniConfig["Login", "userPubKey"]
            )
        }

        // correct userPrivKey
        assertDoesNotFail {
            inboxApi.prepareEntry(
                inbox2Id,
                data,
                listOf(writeHandle),
                IniConfig["Login", "userPrivKey"]
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun readEntry() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val inboxApi2 = InboxApi(connection2!!)
        val inbox: Inbox = inboxApi.getInbox(inbox2Id)
        val version: Long = inbox.version!!

        sendFileToNewEntry("data", "dataFile", inbox2Id)
        val inboxEntry2 = inboxApi2.listEntries(inbox2Id, 0, 1, "desc").readItems.first()
        val entry3Id = inboxEntry2.entryId
        lateinit var inboxEntry: InboxEntry

        //incorrect entryId
        assertFailsWith(PrivmxException::class) {
            inboxApi.readEntry(contextId!!)
        }

        // user1 creates - user2(not user & not manager) reads
        inboxApi.updateInbox(
            inbox.inboxId,
            users.subList(0, 1),
            users.subList(0, 1),
            inbox.publicMeta,
            inbox.privateMeta,
            null,
            inbox.version!!,
            false
        )
        assertFailsWith(PrivmxException::class) {
            inboxApi2.readEntry(entry3Id)
        }

        // user1 creates - user2(user & not manager) reads
        inboxApi.updateInbox(
            inbox.inboxId,
            users,
            users.subList(0, 1),
            inbox.publicMeta,
            inbox.privateMeta,
            null,
            version + 1,
            false
        )
        assertDoesNotFail {
            inboxApi2.readEntry(entry3Id)
        }

        // user1 creates - user2(manager) reads
        inboxApi.updateInbox(
            inbox.inboxId,
            users.subList(0, 1),
            users,
            inbox.publicMeta,
            inbox.privateMeta,
            null,
            version + 2,
            false
        )
        assertDoesNotFail {
            inboxApi2.readEntry(entry3Id)
        }

        // user1 creates - user1(manager) reads
        assertDoesNotFail {
            inboxEntry = inboxApi.readEntry(entryId)
        }
        assertEquals(entryId, inboxEntry.entryId)
        assertEquals(inboxId, inboxEntry.inboxId)
        assertEquals(entry1Data.encodeToByteArray().toHexString(), inboxEntry.data.toHexString())
        assertEquals("0", inboxEntry.statusCode.toString())

        assertEquals(2, inboxEntry.files.size)
        if (!inboxEntry.files.isEmpty()) {
            assertEquals("<anonymous>", inboxEntry.files.first().info.author)
            assertEquals("0", inboxEntry.files.first().statusCode.toString())
            assertEquals(
                publicMeta.encodeToByteArray().toHexString(),
                inboxEntry.files.first().publicMeta.toHexString()
            )
            assertEquals(
                privateMeta.encodeToByteArray().toHexString(),
                inboxEntry.files.first().privateMeta.toHexString()
            )
            assertEquals(entry1File0Data.encodeToByteArray().size.toLong(), inboxEntry.files.first().size)

            assertEquals("<anonymous>", inboxEntry.files[1].info.author)
            assertEquals("0", inboxEntry.files[1].statusCode.toString())
            assertEquals(
                publicMeta.encodeToByteArray().toHexString(),
                inboxEntry.files[1].publicMeta.toHexString()
            )
            assertEquals(
                privateMeta.encodeToByteArray().toHexString(),
                inboxEntry.files[1].privateMeta.toHexString()
            )
            assertEquals(entry1File1Data.encodeToByteArray().size.toLong(), inboxEntry.files[1].size)
        }

        // for entry without files
        assertDoesNotFail {
            inboxEntry = inboxApi.readEntry(entry2Id)
        }
        assertEquals(entry2Id, inboxEntry.entryId)
        assertEquals(inboxId, inboxEntry.inboxId)
        assertEquals(entry2Data.encodeToByteArray().toHexString(), inboxEntry.data.toHexString())
        assertEquals("0", inboxEntry.statusCode.toString())
        assertEquals(0, inboxEntry.files.size)

        inboxApi2.close()
    }

    @Test
    @Throws(Exception::class)
    fun listEntriesIncorrectInputData() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val inboxApi2 = InboxApi(connection2!!)

        // incorrect inboxId
        assertFailsWith(PrivmxException::class) {
            inboxApi.listEntries(contextId!!, 0, 1, "desc")
        }

        // limit < 0
        assertFailsWith(PrivmxException::class) {
            inboxApi.listEntries(inboxId, 0, -1, "desc")
        }

        // limit == 0
        assertFailsWith(PrivmxException::class) {
            inboxApi.listEntries(inboxId, 0, 0, "desc")
        }

        // incorrect sortOrder
        assertFailsWith(PrivmxException::class) {
            inboxApi.listEntries(inboxId, 0, 1, "wrong")
        }

        // incorrect lastId
        assertFailsWith(PrivmxException::class) {
            inboxApi.listEntries(inboxId, 0, 1, "desc", contextId)
        }

        // user1 creates - user2(not user & not manager) lists
        assertFailsWith(PrivmxException::class) {
            inboxApi2.listEntries(inboxId, 0, 1, "desc", entryId)
        }

        // incorrect queryAsJson
        assertFailsWith(PrivmxException::class) {
            inboxApi.listEntries(inboxId, 0, 1, "desc", null, "wrong");
        }

        // incorrect sortBy
        assertFailsWith(PrivmxException::class) {
            inboxApi.listEntries(inboxId, 0, 1, "desc", null, null, "wrong");
        }

        inboxApi2.close()
    }

    @Test
    @Throws(Exception::class)
    fun listEntriesCorrectInputData() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val inboxApi2 = InboxApi(connection2!!)
        lateinit var entries: PagingList<InboxEntry>

        assertDoesNotFail {
            entries = inboxApi.listEntries(inboxId, 0, 1, "asc")
        }
        assertEquals(2, entries.totalAvailable)
        assertEquals(1, entries.readItems.size)
        assertEquals(inboxId, entries.readItems.first().inboxId)
        assertEquals(entryId, entries.readItems.first().entryId)

        assertDoesNotFail {
            entries = inboxApi.listEntries(inboxId, 4, 1, "desc")
        }
        assertEquals(2, entries.totalAvailable)
        assertEquals(0, entries.readItems.size)

        assertDoesNotFail {
            entries = inboxApi.listEntries(inboxId, 1, 1, "asc")
        }
        assertEquals(2, entries.totalAvailable)
        assertEquals(1, entries.readItems.size)
        assertEquals(inboxId, entries.readItems.first().inboxId)
        assertEquals(entry2Id, entries.readItems.first().entryId)

        assertDoesNotFail {
            entries = inboxApi.listEntries(inboxId, 0, 2, "desc")
        }
        assertEquals(2, entries.totalAvailable)
        assertEquals(2, entries.readItems.size)
        assertEquals(inboxId, entries.readItems.first().inboxId)
        assertEquals(entry2Id, entries.readItems.first().entryId)
        assertEquals(inboxId, entries.readItems.last().inboxId)
        assertEquals(entryId, entries.readItems.last().entryId)

        assertDoesNotFail {
            entries = inboxApi.listEntries(inboxId, 0, 1, "asc", entryId)
        }
        assertEquals(1, entries.totalAvailable)
        assertEquals(1, entries.readItems.size)
        assertEquals(inboxId, entries.readItems.first().inboxId)
        assertEquals(entry2Id, entries.readItems.first().entryId)

        // sortBy - createDate
        assertDoesNotFail {
            entries = inboxApi.listEntries(inboxId, 0, 1, "asc", null, null, "createDate")
        }
        assertEquals(2, entries.totalAvailable);
        assertEquals(1, entries.readItems.size);

        // user1 creates - user2(user & not manager) lists
        assertDoesNotFail {
            inboxApi2.listEntries(inbox3Id, 0, 1, "desc", entryId)
        }

        // user1 creates - user2(manager) lists
        assertDoesNotFail {
            inboxApi2.listEntries(inbox2Id, 0, 1, "desc", entryId)
        }

        inboxApi2.close()
    }


    @Test
    @Throws(Exception::class)
    fun deleteEntry() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val inboxApi2 = InboxApi(connection2!!)
        var handle: Long
        val data = "message"

        //incorrect entryId
        assertFailsWith(PrivmxException::class) {
            inboxApi.deleteEntry(contextId!!)
        }

        // user1 creates - user2(user & not manager) deletes
        inboxApi.updateInbox(
            inbox3Id,
            users,
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            null,
            1,
            true
        )
        handle = inboxApi.prepareEntry(inbox3Id, data.encodeToByteArray())!!
        inboxApi.sendEntry(handle)
        val inboxEntry = inboxApi.listEntries(inbox3Id, 0, 1, "desc").readItems.first()
        assertFailsWith(PrivmxException::class) {
            inboxApi2.deleteEntry(inboxEntry.entryId)
        }

        // user1 creates - user2(manager) deletes
        inboxApi.updateInbox(
            inbox3Id,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            null,
            1,
            true
        )
        handle = inboxApi.prepareEntry(inbox3Id, data.encodeToByteArray())!!
        inboxApi.sendEntry(handle)
        val inboxEntry2 = inboxApi.listEntries(inbox3Id, 0, 1, "desc").readItems.first()
        assertDoesNotFail {
            inboxApi2.deleteEntry(inboxEntry2.entryId)
        }

        // user1 creates - user1(manager) deletes
        handle = inboxApi.prepareEntry(inbox3Id, data.encodeToByteArray())!!
        inboxApi.sendEntry(handle)
        val inboxEntry3 = inboxApi.listEntries(inbox3Id, 0, 1, "desc").readItems.first()
        assertDoesNotFail {
            inboxApi.deleteEntry(inboxEntry3.entryId)
        }

        inboxApi2.close()
    }

    @Test
    fun sendEntry() {
        // incorrect inboxHandle
        assertFailsWith(PrivmxException::class) {
            inboxApi.sendEntry(-404L)
        }

        // correct inboxHandle
        val handle = inboxApi.prepareEntry(inbox3Id, "".encodeToByteArray())!!
        assertDoesNotFail {
            inboxApi.sendEntry(handle)
        }
    }

    @Test
    fun openFile() {
        // incorrect fileId
        assertFailsWith(PrivmxException::class) {
            inboxApi.openFile(contextId!!)
        }

        // correct fileId
        assertDoesNotFail {
            inboxApi.openFile(entry1File0Id)
        }
    }

    @Test
    fun readFromFile() {
        lateinit var fileContent: String
        val data = "Message"
        val dataFile = "File content"

        // incorrect fileHandle
        assertFailsWith(PrivmxException::class) {
            fileContent = inboxApi.readFromFile(
                -404L,
                entry1File0Data.encodeToByteArray().size.toLong()
            ).toHexString()
        }

        // correct
        val newFile = sendFileToNewEntry(data, dataFile, inbox2Id)
        val handle = inboxApi.openFile(newFile.info.fileId)!!
        assertDoesNotFail {
            fileContent = inboxApi.readFromFile(handle, newFile.size!!).toHexString()
        }

        assertEquals(fileContent, dataFile.encodeToByteArray().toHexString())
    }

    @Test
    fun seekInFile() {
        val data = "Message"
        val dataFile = "File content"
        val newFile: File = sendFileToNewEntry(data, dataFile, inbox2Id)
        val handle: Long = inboxApi.openFile(newFile.info.fileId)!!

        // incorrect fileHandle
        assertFailsWith(PrivmxException::class) {
            inboxApi.seekInFile(-404L, 0)
        }

        // position < 0
        assertFailsWith(PrivmxException::class) {
            inboxApi.seekInFile(handle, -1)
        }

        // position > file size
        assertFailsWith(PrivmxException::class) {
            inboxApi.seekInFile(handle, newFile.size!! + 1)
        }

        // position == 0
        assertDoesNotFail {
            inboxApi.seekInFile(handle, 0)
        }

        //position == 50% file size
        assertDoesNotFail {
            inboxApi.seekInFile(handle, newFile.size!! / 2)
        }
    }

    @Test
    fun closeFile() {
        val data = "Message"
        val dataFile = "File content"
        val newFile: File = sendFileToNewEntry(data, dataFile, inbox2Id)

        // incorrect fileHandle
        assertFailsWith(PrivmxException::class) {
            inboxApi.closeFile(-404L)
        }

        // fileHandle to write
        val writeHandle = inboxApi.createFileHandle(
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            dataFile.encodeToByteArray().size.toLong()
        )!!
        assertFailsWith(PrivmxException::class) {
            inboxApi.closeFile(writeHandle)
        }

        // fileHandle to read
        val readHandle: Long = inboxApi.openFile(newFile.info.fileId)!!
        assertDoesNotFail {
            inboxApi.closeFile(readHandle)
        }

        // try to close once again
        assertFailsWith(PrivmxException::class) {
            inboxApi.closeFile(readHandle)
        }
    }

    @Test
    fun writeToFile() {
        val data = "Message"
        val fileContent: ByteArray = "File content".encodeToByteArray()
        val shortFileContent: ByteArray = "Content".encodeToByteArray()
        val longFileContent: ByteArray = "Very long file content".encodeToByteArray()

        // incorrect entryHandle
        assertFailsWith(PrivmxException::class) {
            val handle: Long = inboxApi.createFileHandle(
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                fileContent.size.toLong()
            )!!
            inboxApi.writeToFile(-404L, handle, fileContent)
        }

        // incorrect inboxFileHandle
        assertFailsWith(PrivmxException::class) {
            val handle: Long = inboxApi.createFileHandle(
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                fileContent.size.toLong()
            )!!
            val entryHandle: Long = inboxApi.prepareEntry(
                inbox2Id, data.encodeToByteArray(), listOf(handle)
            )!!
            inboxApi.writeToFile(entryHandle, -404L, fileContent)
        }

        // content shorter than declared size
        assertFailsWith(PrivmxException::class) {
            val handle: Long = inboxApi.createFileHandle(
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                fileContent.size.toLong()
            )!!
            val entryHandle: Long = inboxApi.prepareEntry(
                inbox2Id, data.encodeToByteArray(), listOf(handle)
            )!!

            inboxApi.writeToFile(entryHandle, handle, shortFileContent)
            inboxApi.sendEntry(entryHandle)
        }

        // content longer than declared size
        assertFailsWith(PrivmxException::class) {
            val handle: Long = inboxApi.createFileHandle(
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                fileContent.size.toLong()
            )!!
            val entryHandle: Long = inboxApi.prepareEntry(
                inbox2Id, data.encodeToByteArray(), listOf(handle)
            )!!

            inboxApi.writeToFile(entryHandle, handle, longFileContent)
            inboxApi.sendEntry(entryHandle)
        }

        // correct
        assertDoesNotFail {
            val handle: Long = inboxApi.createFileHandle(
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                fileContent.size.toLong()
            )!!
            val entryHandle: Long = inboxApi.prepareEntry(
                inbox2Id, data.encodeToByteArray(), listOf(handle)
            )!!

            inboxApi.writeToFile(entryHandle, handle, fileContent)
            inboxApi.sendEntry(entryHandle)
        }
    }

    @Test
    fun createFileHandle() {
        val fileContent: ByteArray = "File content".encodeToByteArray()

        // file size < 0
        assertFailsWith(PrivmxException::class) {
            inboxApi.createFileHandle(publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray(), -1)
        }

        // file size == 0
        assertDoesNotFail {
            inboxApi.createFileHandle(publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray(), 0)
        }

        // correct
        assertDoesNotFail {
            inboxApi.createFileHandle(
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                fileContent.size.toLong()
            )
        }
    }

    @Test
    fun operationsAfterForceKeyGeneration() {
        val fileData = "file"
        val file: File = sendFileToNewEntry("data", fileData, inbox3Id)
        lateinit var inbox: Inbox
        lateinit var fileContent: ByteArray
        lateinit var entriesAfterKeyGeneration: PagingList<InboxEntry>
        val entries: PagingList<InboxEntry> = inboxApi.listEntries(inbox3Id, 0, 2, "asc")

        // force new key generation
        inboxApi.updateInbox(
            inbox3Id,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            null,
            1,
            force = true,
            forceGenerateNewKey = true
        )

        // get Inbox
        assertDoesNotFail {
            inboxApi.getInbox(inbox3Id)
        }

        assertDoesNotFail {
            inbox = inboxApi.getInbox(inbox3Id)
        }
        assertEquals(inbox3Id, inbox.inboxId)
        assertEquals(users.size, inbox.users.size)
        assertEquals(users.size, inbox.managers.size)
        assertContentEquals(publicMeta.encodeToByteArray(), inbox.publicMeta)
        assertContentEquals(privateMeta.encodeToByteArray(), inbox.privateMeta)

        // list entries
        assertDoesNotFail {
            entriesAfterKeyGeneration = inboxApi.listEntries(inbox3Id, 0, 2, "asc")
        }
        assertEquals(entries.totalAvailable, entriesAfterKeyGeneration.totalAvailable)
        assertEquals(
            entries.readItems.first().entryId,
            entriesAfterKeyGeneration.readItems.first().entryId
        )
        assertEquals(
            entries.readItems.first().authorPubKey,
            entriesAfterKeyGeneration.readItems.first().authorPubKey
        )

        assertDoesNotFail {
            val handle = inboxApi.openFile(file.info.fileId)!!
            fileContent = inboxApi.readFromFile(handle, file.size!!)
            inboxApi.closeFile(handle)
        }
        assertContentEquals(fileData.encodeToByteArray(), fileContent)
    }

    @Test
    @Throws(Exception::class)
    fun accessAsPublicUser() {
        val connectionPublic =
            connectAsUser(ConnectionType.Public, bridgeAddress)
        val inboxApiPublic = InboxApi(connectionPublic)
        var fileHandle: Long?
        var entryHandle: Long?
        val data = "message"
        val file: File = sendFileToNewEntry("data", "file", inbox3Id)
        var inboxesSubscriptionIds = emptyList<String>()
        var entriesSubscriptionIds = emptyList<String>()

        // get inbox
        assertFailsWith(PrivmxException::class) {
            inboxApiPublic.getInbox(inbox3Id)
        }

        //get inbox public view
        assertDoesNotFail {
            inboxApiPublic.getInboxPublicView(inboxId)
        }

        // create inbox
        assertFailsWith(PrivmxException::class) {
            inboxApiPublic.createInbox(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // update inbox
        assertFailsWith(PrivmxException::class) {
            inboxApiPublic.updateInbox(
                inbox3Id,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                null,
                1,
                false
            )
        }

        // delete inbox
        assertFailsWith(PrivmxException::class) {
            inboxApiPublic.deleteInbox(inbox3Id)
        }

        // list inboxes
        assertFailsWith(PrivmxException::class) {
            inboxApiPublic.listInboxes(context2Id!!, 0, 1, "asc")
        }

        // create file handle
        assertDoesNotFail {
            fileHandle = inboxApiPublic.createFileHandle(
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                data.encodeToByteArray().size.toLong()
            )
        }

        // prepare and send entry
        assertDoesNotFail {
            entryHandle = inboxApiPublic.prepareEntry(
                inbox3Id,
                "public user".encodeToByteArray()
            )
            inboxApiPublic.sendEntry(entryHandle!!)
        }

        // read entry
        assertFailsWith(PrivmxException::class) {
            inboxApiPublic.readEntry(entry2Id)
        }

        // open file
        // this case also prevents reading the file or seeking within it
        assertFailsWith(PrivmxException::class) {
            inboxApiPublic.openFile(file.info.fileId)
        }

        // write to file
        assertDoesNotFail {
            fileHandle = inboxApiPublic.createFileHandle(
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                data.encodeToByteArray().size.toLong()
            )
            entryHandle = inboxApiPublic.prepareEntry(
                inbox3Id,
                data.encodeToByteArray(),
                listOf(fileHandle!!)
            )
            inboxApiPublic.writeToFile(
                entryHandle!!,
                fileHandle!!,
                data.encodeToByteArray()
            )
            inboxApiPublic.sendEntry(entryHandle!!)
        }

        // close file
        assertFailsWith(PrivmxException::class) {
            val writeHandle: Long = inboxApi.createFileHandle(
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                data.encodeToByteArray().size.toLong()
            )!!
            inboxApiPublic.closeFile(writeHandle)
        }

        // subscribeForInboxEvents
        assertFailsWith(PrivmxException::class) {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                inboxApiPublic.buildSubscriptionQuery(
                    InboxEventType.INBOX_CREATE,
                    InboxEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                inboxApiPublic.buildSubscriptionQuery(
                    InboxEventType.INBOX_UPDATE,
                    InboxEventSelectorType.INBOX_ID,
                    inbox3Id
                )
            )
            queries.add(
                inboxApiPublic.buildSubscriptionQuery(
                    InboxEventType.INBOX_DELETE,
                    InboxEventSelectorType.INBOX_ID,
                    inbox3Id
                )
            )
            inboxesSubscriptionIds = inboxApiPublic.subscribeFor(queries)
        }

        // subscribeForEntryEvents
        assertFailsWith(PrivmxException::class) {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                inboxApiPublic.buildSubscriptionQuery(
                    InboxEventType.ENTRY_CREATE,
                    InboxEventSelectorType.INBOX_ID,
                    inbox3Id
                )
            )
            queries.add(
                inboxApiPublic.buildSubscriptionQuery(
                    InboxEventType.ENTRY_DELETE,
                    InboxEventSelectorType.INBOX_ID,
                    inbox3Id
                )
            )
            entriesSubscriptionIds = inboxApiPublic.subscribeFor(queries)
        }

        // unsubscribeFromInboxEvents
        assertFailsWith(PrivmxException::class) {
            inboxApiPublic.unsubscribeFrom(listOf())
        }

        // unsubscribeFromEntryEvents
        assertFailsWith(PrivmxException::class) {
            inboxApiPublic.unsubscribeFrom(listOf())
        }

        inboxApiPublic.close()
        connectionPublic.close()
    }

    @Test
    @Throws(Exception::class)
    fun subscribeForInboxEvents() {
        val inboxesSubscriptionIds: MutableList<String> = mutableListOf()
        val entriesSubscriptionIds: MutableList<String> = mutableListOf()

        // InboxEvents
        // subscribe for InboxEvents
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.INBOX_CREATE,
                    InboxEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.INBOX_UPDATE,
                    InboxEventSelectorType.INBOX_ID,
                    inbox3Id
                )
            )
            queries.add(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.INBOX_DELETE,
                    InboxEventSelectorType.INBOX_ID,
                    inbox3Id
                )
            )
            inboxesSubscriptionIds.addAll(inboxApi.subscribeFor(queries))
        }

        // subscribe again
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()

            queries.add(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.INBOX_CREATE,
                    InboxEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            inboxesSubscriptionIds.addAll(inboxApi.subscribeFor(queries))
        }

        // 2 same queries
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()

            queries.add(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.INBOX_UPDATE,
                    InboxEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.INBOX_UPDATE,
                    InboxEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            inboxesSubscriptionIds.addAll(inboxApi.subscribeFor(queries))
        }

        // subscribe with wrong InboxEventSelectorType
        assertFailsWith(PrivmxException::class) {
            val queries: MutableList<String> = mutableListOf()

            queries.add(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.INBOX_CREATE,
                    InboxEventSelectorType.INBOX_ID,
                    inbox3Id
                )
            )
        }

        // subscribe with nonexisting selectorId
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()

            queries.add(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.INBOX_CREATE,
                    InboxEventSelectorType.CONTEXT_ID,
                    "f977e533-3524-4579-error-215368c4b2a4"
                )
            )
            inboxesSubscriptionIds.addAll(inboxApi.subscribeFor(queries))
        }

        // EntryEvents
        // subscribe for EntryEvents
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()

            queries.add(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.ENTRY_CREATE,
                    InboxEventSelectorType.INBOX_ID,
                    inbox3Id
                )
            )
            queries.add(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.ENTRY_DELETE,
                    InboxEventSelectorType.INBOX_ID,
                    inbox3Id
                )
            )
            entriesSubscriptionIds.addAll(inboxApi.subscribeFor(queries))
        }

        // subscribe again
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()

            queries.add(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.ENTRY_CREATE,
                    InboxEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            entriesSubscriptionIds.addAll(inboxApi.subscribeFor(queries))
        }

        // 2 same queries
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.ENTRY_DELETE,
                    InboxEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.ENTRY_DELETE,
                    InboxEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            entriesSubscriptionIds.addAll(inboxApi.subscribeFor(queries))
        }

        // subscribe with wrong InboxEventSelectorType
        assertFailsWith(PrivmxException::class) {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.ENTRY_CREATE,
                    InboxEventSelectorType.ENTRY_ID,
                    entryId
                )
            )
        }

        // subscribe with nonexisting selectorId
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.ENTRY_CREATE,
                    InboxEventSelectorType.CONTEXT_ID,
                    inbox2Id
                )
            )
            entriesSubscriptionIds.addAll(inboxApi.subscribeFor(queries))
        }

        // todo - SIGSEGV - will be fixed once the reported issue is fixed on bridge/endpoint
        /*
        // unsubscribe from InboxEvents
        assertDoesNotFail {
            inboxApi.unsubscribeFrom(inboxesSubscriptionIds);
        }

        // unsubscribe from InboxEvents again
        assertDoesNotFail {
            inboxApi.unsubscribeFrom(inboxesSubscriptionIds);
        }

        // unsubscribe from EntryEvents
        assertDoesNotFail {
            inboxApi.unsubscribeFrom(entriesSubscriptionIds);
        }

        // unsubscribe from EntryEvents again
        assertDoesNotFail {
            inboxApi.unsubscribeFrom(entriesSubscriptionIds);
        }
        */

        assertDoesNotFail {
            inboxApi.unsubscribeFrom(inboxesSubscriptionIds + entriesSubscriptionIds)
        }
    }

    @Test
    @Ignore
    @Throws(Exception::class)
    fun setUserVerifierInbox() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val inboxApi2 = InboxApi(connection2!!)
        val inbox3 = inboxApi.getInbox(inbox3Id)
        val illegalfile = "Illegal file"

        val userVerifierFalse = object : UserVerifierInterface {
            override fun verify(request: List<VerificationRequest>): List<Boolean> {
                return request.map { req: VerificationRequest? -> false }
            }
        }
        assertDoesNotFail {
            connection!!.setUserVerifier(userVerifierFalse)
        }

        // create container
        val inboxCreatedId: String = inboxApi.createInbox(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        val inboxCreated2Id: String = inboxApi2.createInbox(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        val inboxCreated = inboxApi2.getInbox(inboxCreatedId)
        assertEquals(0, inboxCreated.statusCode)

        // get container
        lateinit var inboxVerifiedUser: Inbox
        lateinit var inboxUnverifiedUser: Inbox

        // user verified with positive result
        assertDoesNotFail {
            inboxVerifiedUser = inboxApi2.getInbox(
                inboxCreatedId
            )
        }
        assertEquals(0, inboxVerifiedUser.statusCode)

        // user verified with negative result
        assertDoesNotFail {
            inboxUnverifiedUser = inboxApi.getInbox(inboxCreatedId)
        }
        assertNotEquals(0, inboxUnverifiedUser.statusCode)

        // list containers
        lateinit var inboxesVerifiedUser: PagingList<Inbox>
        lateinit var inboxesUnverifiedUser: PagingList<Inbox>

        // user verified with positive result
        assertDoesNotFail {
            inboxesVerifiedUser = inboxApi2.listInboxes(
                context2Id!!, 0, 10, "desc"
            )
        }
        assertFalse(inboxesVerifiedUser.readItems.isEmpty())
        inboxesVerifiedUser.readItems.forEach {
            assertEquals(0, it.statusCode)
        }

        // user verified with negative result
        assertDoesNotFail {
            inboxesUnverifiedUser = inboxApi.listInboxes(
                context2Id!!, 0, 10, "desc"
            )
        }
        assertFalse(inboxesUnverifiedUser.readItems.isEmpty())
        inboxesUnverifiedUser.readItems.forEach {
            assertNotEquals(0, it.statusCode)
        }

        // update container
        assertFailsWith(PrivmxException::class) {
            inboxApi.updateInbox(
                inboxCreatedId,
                users,
                users,
                illegalfile.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                FilesConfig(0L, 20L, 102400L, 1024000L),
                inbox3.version!! + 1,
                false
            )
        }

        val inboxUpdated = inboxApi2.getInbox(inboxCreatedId)
        // should be equal - inbox should not update
        assertContentEquals(inboxCreated.publicMeta, inboxUpdated.publicMeta)
        assertEquals(inboxCreated.version, inboxUpdated.version)
        assertNotEquals(0, inboxUpdated.version)

        // delete container
        assertDoesNotFail {
            inboxApi.deleteInbox(inboxCreated2Id)
        }

        // inbox should be deleted
        assertFailsWith(PrivmxException::class) {
            inboxApi2.getInbox(inboxCreated2Id)
        }

        // create item
        assertDoesNotFail {
            val entryHandle: Long =
                inboxApi.prepareEntry(
                    inboxCreatedId,
                    "data".encodeToByteArray(),
                    emptyList()
                )!!
            inboxApi.sendEntry(entryHandle)
        }

        assertDoesNotFail {
            val entryHandle: Long = inboxApi2.prepareEntry(
                inboxCreatedId,
                "data".encodeToByteArray(),
                emptyList()
            )!!
            inboxApi2.sendEntry(entryHandle)
        }

        // list items
        lateinit var inboxEntriesVerifiedUser: PagingList<InboxEntry>
        lateinit var inboxEntriesUnverifiedUser: PagingList<InboxEntry>

        // user verified with positive result
        assertDoesNotFail {
            inboxEntriesVerifiedUser = inboxApi2.listEntries(inboxCreatedId, 0, 10, "desc")
        }
        assertFalse(inboxEntriesVerifiedUser.readItems.isEmpty())
        inboxEntriesVerifiedUser.readItems.forEach {
            assertEquals(0, it.statusCode)
        }

        // user verified with negative result
        assertDoesNotFail {
            inboxEntriesUnverifiedUser = inboxApi.listEntries(
                inboxCreatedId, 0, 10, "desc"
            )
        }
        assertFalse(inboxEntriesUnverifiedUser.readItems.isEmpty())
        inboxEntriesUnverifiedUser.readItems.forEach {
            assertNotEquals(0, it.statusCode)
        }


        // delete item
        assertDoesNotFail {
            inboxApi.deleteEntry(
                inboxEntriesVerifiedUser.readItems.first().entryId
            )
        }

        // writeToFile
        assertDoesNotFail {
            val handle: Long = inboxApi.createFileHandle(
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "File content".encodeToByteArray().size.toLong()
            )!!
            val entryHandle: Long = inboxApi.prepareEntry(
                inboxCreatedId,
                "data".encodeToByteArray(),
                listOf(handle)
            )!!

            inboxApi.writeToFile(entryHandle, handle, "File content".encodeToByteArray())
            inboxApi.sendEntry(entryHandle)
        }

        // readFromFile
        lateinit var fileContent: String
        val newFile = sendFileToNewEntry(inboxApi2, "data", "File data", inboxCreatedId)
        assertDoesNotFail {
            val handle: Long = inboxApi.openFile(newFile.info.fileId)!!
            fileContent = inboxApi.readFromFile(handle, newFile.size!!).toHexString()
        }

        inboxApi2.close()
    }
}