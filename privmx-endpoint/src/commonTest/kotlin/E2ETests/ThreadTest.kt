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
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.ItemPolicy
import com.simplito.kotlin.privmx_endpoint.model.Message
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.Thread
import com.simplito.kotlin.privmx_endpoint.model.VerificationRequest
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.ThreadEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.ThreadEventType
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.core.UserVerifierInterface
import com.simplito.kotlin.privmx_endpoint.modules.thread.ThreadApi
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

//TODO: Add tests for close methods

@OptIn(ExperimentalStdlibApi::class, ExperimentalAtomicApi::class)
@Ignore
class ThreadTest : BaseTest() {
    private lateinit var threadApi: ThreadApi

    private lateinit var threadId: String
    private lateinit var thread2Id: String
    private lateinit var thread3Id: String
    private lateinit var messageId: String
    private lateinit var message2Id: String

    @BeforeTest
    @Throws(PrivmxException::class, NativeException::class)
    fun createConnection() {
        connection = connectAsUser(ConnectionType.User1, bridgeAddress)
        threadApi = ThreadApi(connection!!)
        threadId = threadApi.createThread(
            contextId!!,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        thread2Id = threadApi.createThread(
            contextId!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        thread3Id = threadApi.createThread(
            contextId!!,
            users,
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        messageId = threadApi.sendMessage(
            threadId,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "message_from_sendMessage".encodeToByteArray()
        )
        message2Id = threadApi.sendMessage(
            threadId,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "message_from_sendMessage".encodeToByteArray()
        )
    }

    @AfterTest
    @Throws(Exception::class)
    fun closeConnection() {
        if (::threadApi.isInitialized) {
            if (::thread3Id.isInitialized) threadApi.deleteThread(thread3Id)
            if (::thread2Id.isInitialized) threadApi.deleteThread(thread2Id)
            if (::threadId.isInitialized) threadApi.deleteThread(threadId)
            threadApi.close()
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
    fun createThreadIncorrectInputData() {
        // incorrect contextId
        assertFailsWith(PrivmxException::class) {
            threadApi.createThread(
                threadId,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // incorrect contextId
        assertFailsWith(PrivmxException::class) {
            threadApi.createThread(
                "",
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // incorrect users
        assertFailsWith(PrivmxException::class) {
            threadApi.createThread(
                context2Id!!,
                incorrectUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // incorrect managers
        assertFailsWith(PrivmxException::class) {
            threadApi.createThread(
                context2Id!!,
                users,
                incorrectUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // no managers
        assertFailsWith(PrivmxException::class) {
            threadApi.createThread(
                context2Id!!,
                users,
                emptyUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // different users and managers
        assertFailsWith(PrivmxException::class) {
            threadApi.createThread(
                context2Id!!,
                users.subList(0, 1),
                users.subList(1, 2),
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        //2 same users
        assertFailsWith(PrivmxException::class) {
            threadApi.createThread(
                context2Id!!,
                sameUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        //2 same managers
        assertFailsWith(PrivmxException::class) {
            threadApi.createThread(
                context2Id!!,
                users,
                sameUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
    }

    @Test
    fun createThreadCorrectInputData() {
        lateinit var id: String

        // same users and managers
        assertDoesNotFail {
            id = threadApi.createThread(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )

        }

        // no users
        assertDoesNotFail {
            threadApi.createThread(
                context2Id!!,
                emptyUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // empty publicMeta
        assertDoesNotFail {
            threadApi.createThread(
                context2Id!!,
                emptyUsers,
                users,
                ByteArray(0),
                privateMeta.encodeToByteArray()
            )
        }

        // empty privateMeta
        assertDoesNotFail {
            threadApi.createThread(
                context2Id!!,
                emptyUsers,
                users,
                publicMeta.encodeToByteArray(),
                ByteArray(0)
            )
        }

        val thread: Thread = threadApi.getThread(id)
        assertEquals(thread.contextId, context2Id)
        assertEquals(thread.threadId, id)
    }

    @Test
    fun createThreadWithPolicy() {
        val itemPolicy = ItemPolicy("owner", "owner", "owner", "owner", "owner", "owner")
        val containerPolicy =
            ContainerPolicy("owner", "owner", "owner", "owner", "no", "no", itemPolicy)
        lateinit var id: String

        assertDoesNotFail {
            id = threadApi.createThread(
                context2Id.toString(),
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                containerPolicy
            )

        }
        val thread: Thread = threadApi.getThread(id)
        assertEquals(thread.policy.item?.get, containerPolicy.item?.get)
        assertEquals(thread.policy.item?.listMy, containerPolicy.item?.listMy)
        assertEquals(thread.policy.item?.listAll, containerPolicy.item?.listAll)
        assertEquals(thread.policy.item?.create, containerPolicy.item?.create)
        assertEquals(thread.policy.item?.update, containerPolicy.item?.update)
        assertEquals(thread.policy.delete, containerPolicy.delete)
        assertEquals(thread.policy.get, containerPolicy.get)
        assertEquals(thread.policy.update, containerPolicy.update)
        assertEquals(thread.policy.delete, containerPolicy.delete)
        assertEquals(thread.policy.updatePolicy, containerPolicy.updatePolicy)
        assertEquals(
            thread.policy.updaterCanBeRemovedFromManagers,
            containerPolicy.updaterCanBeRemovedFromManagers
        )
        assertEquals(
            thread.policy.ownerCanBeRemovedFromManagers,
            containerPolicy.ownerCanBeRemovedFromManagers
        )
    }

    @Test
    fun updateThreadWithPolicy() {
        // TODO implement tests of updating Threads with policy value
    }

    @Test
    @Throws(Exception::class)
    fun updateThreadIncorrectInputData() {
         connection2 =
            connectAsUser(ConnectionType.User2, bridgeAddress)
        val threadApi2 = ThreadApi(connection2!!)
        val id: String = threadApi.createThread(
            context2Id!!,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        val thread: Thread = threadApi.getThread(id)

        // incorrect threadId
        assertFailsWith(PrivmxException::class) {
            threadApi.updateThread(
                contextId!!,
                users,
                users.subList(0, 1),
                thread.publicMeta,
                thread.privateMeta,
                thread.version!!,
                false
            )
        }

        // incorrect users
        assertFailsWith(PrivmxException::class) {
            threadApi.updateThread(
                id,
                incorrectUsers,
                users.subList(0, 1),
                thread.publicMeta,
                thread.privateMeta,
                thread.version!!,
                false
            )
        }

        // incorrect managers
        assertFailsWith(PrivmxException::class) {
            threadApi.updateThread(
                id,
                users.subList(0, 1),
                incorrectUsers,
                thread.publicMeta,
                thread.privateMeta,
                thread.version!!,
                false
            )
        }

        // creator is not in managers
        assertFailsWith(PrivmxException::class) {
            threadApi.updateThread(
                id,
                incorrectUsers,
                users.subList(1, 2),
                thread.publicMeta,
                thread.privateMeta,
                thread.version!!,
                false
            )
        }

        // no managers
        assertFailsWith(PrivmxException::class) {
            threadApi.updateThread(
                id,
                users.subList(0, 1),
                emptyUsers,
                thread.publicMeta,
                thread.privateMeta,
                thread.version!!,
                false
            )
        }

        // incorrect version
        assertFailsWith(PrivmxException::class) {
            threadApi.updateThread(
                id,
                users.subList(0, 1),
                users.subList(0, 1),
                thread.publicMeta,
                thread.privateMeta,
                -1,
                false
            )
        }

        // user1 creates - user2(user & not manager) updates
        val id2: String = threadApi.createThread(
            context2Id!!,
            users,
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        val thread2: Thread = threadApi.getThread(id2)
        assertFailsWith(PrivmxException::class) {
            threadApi2.updateThread(
                id2,
                users.subList(0, 1),
                users.subList(0, 1),
                thread.publicMeta,
                thread.privateMeta,
                thread2.version!!,
                false
            )
        }

        // user1 creates - user2(not user & not manager) updates
        assertFailsWith(PrivmxException::class) {
            threadApi2.updateThread(
                id,
                users.subList(0, 1),
                users.subList(0, 1),
                thread.publicMeta,
                thread.privateMeta,
                thread.version!!,
                false
            )
        }

        threadApi2.close()
    }

    @Test
    @Throws(Exception::class)
    fun updateThreadCorrectInputData() {
         connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val threadApi2 = ThreadApi(connection2!!)
        val publicMetaUpdate = "new meta"
        val privateMetaUpdate = "new meta"
        var thread: Thread = threadApi.getThread(thread2Id)
        val version: Long? = thread.version

        // more users and managers
        assertDoesNotFail {
            threadApi.updateThread(
                thread2Id,
                users,
                users,
                publicMetaUpdate.encodeToByteArray(),
                privateMetaUpdate.encodeToByteArray(),
                version!!,
                false
            )
        }
        thread = threadApi.getThread(thread2Id)
        assertEquals(thread.contextId, contextId)
        assertEquals(thread.users.size, users.size)
        assertEquals(thread.managers.size, users.size)
        assertContentEquals(thread.publicMeta, publicMetaUpdate.encodeToByteArray())
        assertContentEquals(thread.privateMeta, privateMetaUpdate.encodeToByteArray())
        assertEquals(thread.version, version!! + 1)

        // less users
        assertDoesNotFail {
            threadApi.updateThread(
                thread2Id,
                emptyUsers,
                users.subList(0, 1),
                publicMetaUpdate.encodeToByteArray(),
                privateMetaUpdate.encodeToByteArray(),
                version + 1,
                false
            )
        }
        thread = threadApi.getThread(thread2Id)
        assertEquals(thread.contextId, contextId)
        assertEquals(0, thread.users.size)
        assertEquals(1, thread.managers.size)
        assertContentEquals(thread.publicMeta, publicMetaUpdate.encodeToByteArray())
        assertContentEquals(thread.privateMeta, privateMetaUpdate.encodeToByteArray())
        assertEquals(thread.version, version + 2)

        // incorrect version and force true
        assertDoesNotFail {
            threadApi.updateThread(
                thread2Id,
                users,
                users,
                publicMetaUpdate.encodeToByteArray(),
                privateMetaUpdate.encodeToByteArray(),
                version + 1000,
                true
            )
        }
        thread = threadApi.getThread(thread2Id)
        assertEquals(thread.contextId, contextId)
        assertEquals(thread.users.size, users.size)
        assertEquals(thread.managers.size, users.size)
        assertContentEquals(thread.publicMeta, publicMetaUpdate.encodeToByteArray())
        assertContentEquals(thread.privateMeta, privateMetaUpdate.encodeToByteArray())
        assertEquals(thread.version, version + 3)

        // user1 creates - user2(manager) updates
        val id2: String = threadApi.createThread(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        val thread2: Thread = threadApi.getThread(id2)
        assertDoesNotFail {
            threadApi2.updateThread(
                id2,
                users.subList(0, 1),
                users,
                thread2.publicMeta,
                thread2.privateMeta,
                thread2.version!!,
                false
            )
        }

        threadApi2.close()
    }

    @Test
    @Throws(Exception::class)
    fun deleteThread() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val threadApi2 = ThreadApi(connection2!!)

        // incorrect threadId
        threadApi.createThread(
            context2Id.toString(),
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertFailsWith(PrivmxException::class) {
            threadApi.deleteThread(context2Id.toString())
        }

        // incorrect threadId
        assertFailsWith(PrivmxException::class) {
            threadApi.deleteThread("")
        }

        // user1 creates - user1(manager) deletes
        val id2: String = threadApi.createThread(
            context2Id!!,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertDoesNotFail {
            threadApi.deleteThread(id2)
        }

        // user1 creates - user2(manager) deletes
        val id3: String = threadApi.createThread(
            context2Id!!,
            users.subList(0, 1),
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertDoesNotFail {
            threadApi2.deleteThread(id3)
        }

        // user1 creates - user2(user & not manager) deletes
        val id4: String = threadApi.createThread(
            context2Id!!,
            users,
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertFailsWith(PrivmxException::class) {
            threadApi2.deleteThread(id4)
        }

        // user1 creates - user2(not user & not manager) deletes
        val id5: String = threadApi.createThread(
            context2Id!!,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        assertFailsWith(PrivmxException::class) {
            threadApi2.deleteThread(id5)
        }

        threadApi2.close()
    }

//    @Test
//    fun getMessage() {
//        lateinit var message: Message
//
//        // incorrect messageId
//        assertFailsWith(PrivmxException::class) {
//            threadApi.getMessage(threadId)
//        }
//
//        // correct messageId
//        assertDoesNotFail {
//            message = threadApi.getMessage(IniConfig["Message_1", "info_messageId"])
//        }
//        assertEquals(IniConfig["Message_1", "info_threadId"], message.info.threadId)
//        assertEquals(IniConfig["Message_1", "info_messageId"], message.info.messageId)
//        assertEquals(
//            message.info.createDate.toString(),
//            IniConfig["Message_1", "info_createDate"]
//        )
//        assertEquals(message.info.author, IniConfig["Message_1", "info_author"])
//        assertEquals(
//            message.publicMeta.toHexString(),
//            IniConfig["Message_1", "publicMeta_inHex"]
//        )
//        assertEquals(
//            IniConfig["Message_1", "privateMeta_inHex"],
//            message.privateMeta.toHexString()
//        )
//        assertEquals(
//            IniConfig["Message_1", "data_inHex"],
//            message.data.toHexString()
//        )
//        assertEquals(
//            IniConfig["Message_1", "statusCode"],
//            message.statusCode.toString()
//        )
//        assertEquals(
//            IniConfig["Message_1", "uploaded_publicMeta_inHex"],
//            message.publicMeta.toHexString()
//        )
//        assertEquals(
//            IniConfig["Message_1", "uploaded_privateMeta_inHex"],
//            message.privateMeta.toHexString()
//        )
//        assertEquals(
//            IniConfig["Message_1", "uploaded_data_inHex"],
//            message.data.toHexString()
//        )
//    }

    @Test
    fun listMessageIncorrectInputData() {
        // incorrect threadId
        assertFailsWith(PrivmxException::class) {
            threadApi.listMessages(messageId, 0, 1, "desc")
        }

        // limit < 0
        assertFailsWith(PrivmxException::class) {
            threadApi.listMessages(threadId, 0, -1, "desc")
        }

        // limit == 0
        assertFailsWith(PrivmxException::class) {
            threadApi.listMessages(threadId, 0, 0, "desc")
        }

        // incorrect sortOrder
        assertFailsWith(PrivmxException::class) {
            threadApi.listMessages(threadId, 0, 0, "wrong")
        }

        // incorrect lastId
        assertFailsWith(PrivmxException::class) {
            threadApi.listMessages(threadId, 0, 0, "desc", threadId)
        }

        // incorrect queryAsJson
        assertFailsWith(PrivmxException::class) {
            threadApi.listMessages(threadId, 0, 0, "desc", null, "wrong")
        }

        // incorrect sortBy
        assertFailsWith(PrivmxException::class) {
            threadApi.listMessages(threadId, 0, 0, "desc", null, null, "wrong")
        }
    }


    @Test
    fun listMessageCorrectInputData() {
        lateinit var messages: PagingList<Message>

        assertDoesNotFail {
            messages = threadApi.listMessages(threadId, 0, 1, "desc")
        }
        assertEquals(2, messages.totalAvailable)
        assertEquals(1, messages.readItems.size)
        assertEquals(message2Id, messages.readItems.first().info.messageId)

        assertDoesNotFail {
            messages = threadApi.listMessages(threadId, 0, 4, "asc")
        }
        assertEquals(2, messages.totalAvailable)
        assertEquals(2, messages.readItems.size)
        assertEquals(messageId, messages.readItems.first().info.messageId)
        assertEquals(message2Id, messages.readItems.last().info.messageId)

        assertDoesNotFail {
            messages = threadApi.listMessages(threadId, 4, 1, "asc")
        }
        assertEquals(2, messages.totalAvailable)
        assertEquals(0, messages.readItems.size)

        // with last messageId
        assertDoesNotFail {
            messages = threadApi.listMessages(threadId, 0, 1, "asc", messageId)
        }
        assertEquals(1, messages.totalAvailable)
        assertEquals(1, messages.readItems.size)

        // with sortBy - createDate
        assertDoesNotFail {
            messages = threadApi.listMessages(threadId, 0, 1, "asc", null, null, "createDate")
        }
        assertEquals(2, messages.totalAvailable)
        assertEquals(1, messages.readItems.size)
    }

    @Test
    fun filteringThreadListWithQueryAsJson() {
        lateinit var threadsList: PagingList<Thread>

        assertDoesNotFail {
            threadApi.createThread(
                context2Id!!,
                users,
                users,
                json.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.createThread(
                context2Id!!,
                users,
                users,
                json1.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.createThread(
                context2Id!!,
                users,
                users,
                json2.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.createThread(
                context2Id!!,
                users,
                users,
                json3.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.createThread(
                context2Id!!,
                users,
                users,
                json4.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.createThread(
                context2Id!!,
                users,
                users,
                json5.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.createThread(
                context2Id!!,
                users,
                users,
                json6.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.createThread(
                context2Id!!,
                users,
                users,
                json7.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.createThread(
                context2Id!!,
                users,
                users,
                json8.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.createThread(
                context2Id!!,
                users,
                users,
                json9.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.createThread(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // json -> json & json1 & json8
        threadsList = threadApi.listThreads(context2Id!!, 0, 100, "desc", null, json)
        assertEquals(3, threadsList.readItems.size)

        // json1 -> json1
        threadsList = threadApi.listThreads(context2Id!!, 0, 100, "desc", null, json1)
        assertEquals(1, threadsList.readItems.size)

        // json2 -> json2 & json9
        threadsList = threadApi.listThreads(context2Id!!, 0, 100, "desc", null, json2)
        assertEquals(2, threadsList.readItems.size)

        // json3 -> json3 & json9
        threadsList = threadApi.listThreads(context2Id!!, 0, 100, "desc", null, json3)
        assertEquals(2, threadsList.readItems.size)
        // json4
        assertFailsWith(PrivmxException::class) {
            threadsList = threadApi.listThreads(context2Id!!, 0, 100, "desc", null, json4)
            threadsList.readItems.forEach(::println)
            println(threadsList.totalAvailable)
        }

        // json5 -> json5
        threadsList = threadApi.listThreads(context2Id!!, 0, 100, "desc", null, json5)
        assertEquals(1, threadsList.readItems.size)

        // json6 -> json6
        threadsList = threadApi.listThreads(context2Id!!, 0, 100, "desc", null, json6)
        assertEquals(1, threadsList.readItems.size)

        // json7 -> json7 & json8
        threadsList = threadApi.listThreads(context2Id!!, 0, 100, "desc", null, json7)
        assertEquals(2, threadsList.readItems.size)

        // json8 -> json8
        threadsList = threadApi.listThreads(context2Id!!, 0, 100, "desc", null, json8)
        assertEquals(1, threadsList.readItems.size)

        // json9 -> json9
        threadsList = threadApi.listThreads(context2Id!!, 0, 100, "desc", null, json9)
        assertEquals(1, threadsList.readItems.size)

        // query10 -> json & json1 & json8
        threadsList = threadApi.listThreads(context2Id!!, 0, 100, "desc", null, query10)
        assertEquals(3, threadsList.readItems.size)

        // query11 -> json7 & json8 & json9
        threadsList = threadApi.listThreads(context2Id!!, 0, 100, "desc", null, query11)
        assertEquals(3, threadsList.readItems.size)

        // query12 -> json8
        threadsList = threadApi.listThreads(context2Id!!, 0, 100, "desc", null, query12)
        assertEquals(1, threadsList.readItems.size)

        // query13 -> json8
        threadsList = threadApi.listThreads(context2Id!!, 0, 100, "desc", null, query13)
        assertEquals(1, threadsList.readItems.size)

        // query14 -> json & json1 & json7 & json8 & json9
        threadsList = threadApi.listThreads(context2Id!!, 0, 100, "desc", null, query14)
        assertEquals(5, threadsList.readItems.size)

        // query15 -> json2 & json3 & json4 & json5 & json6 & json7 & json9 & publicMeta
        threadsList = threadApi.listThreads(context2Id!!, 0, 100, "desc", null, query15)
        assertEquals(8, threadsList.readItems.size)

        threadsList = threadApi.listThreads(context2Id!!, 0, 100, "desc")
        assertTrue(threadsList.readItems.size >= 11)
    }

    @Test
    fun filteringMessageListWithQueryAsJson() {
        lateinit var messagesList: PagingList<Message>
        val id = threadApi.createThread(
            context2Id!!,
            users,
            users,
            json1.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        assertDoesNotFail {
            threadApi.sendMessage(
                id,
                json.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.sendMessage(
                id,
                json1.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.sendMessage(
                id,
                json2.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.sendMessage(
                id,
                json3.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.sendMessage(
                id,
                json4.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.sendMessage(
                id,
                json5.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.sendMessage(
                id,
                json6.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.sendMessage(
                id,
                json7.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.sendMessage(
                id,
                json8.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.sendMessage(
                id,
                json9.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadApi.sendMessage(
                id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }

        // json -> json & json1 & json8
        messagesList = threadApi.listMessages(id, 0, 100, "desc", null, json)
        assertEquals(3, messagesList.readItems.size)

        // json1 -> json1
        messagesList = threadApi.listMessages(id, 0, 100, "desc", null, json1)
        assertEquals(1, messagesList.readItems.size)

        // json2 -> json2 & json9
        messagesList = threadApi.listMessages(id, 0, 100, "desc", null, json2)
        assertEquals(2, messagesList.readItems.size)

        // json3 -> json3 & json9
        messagesList = threadApi.listMessages(id, 0, 100, "desc", null, json3)
        assertEquals(2, messagesList.readItems.size)

        // json4
        assertFailsWith(PrivmxException::class) {
            messagesList = threadApi.listMessages(id, 0, 100, "desc", null, json4)
            messagesList.readItems.forEach(::println)
            println(messagesList.totalAvailable)
        }

        // json5 -> json5
        messagesList = threadApi.listMessages(id, 0, 100, "desc", null, json5)
        assertEquals(1, messagesList.readItems.size)

        // json6 -> json6
        messagesList = threadApi.listMessages(id, 0, 100, "desc", null, json6)
        assertEquals(1, messagesList.readItems.size)

        // json7 -> json7 & json8
        messagesList = threadApi.listMessages(id, 0, 100, "desc", null, json7)
        assertEquals(2, messagesList.readItems.size)

        // json8 -> json8
        messagesList = threadApi.listMessages(id, 0, 100, "desc", null, json8)
        assertEquals(1, messagesList.readItems.size)

        // json9 -> json9
        messagesList = threadApi.listMessages(id, 0, 100, "desc", null, json9)
        assertEquals(1, messagesList.readItems.size)

        // query10 -> json & json1 & json8
        messagesList = threadApi.listMessages(id, 0, 100, "desc", null, query10)
        assertEquals(3, messagesList.readItems.size)

        // query11 -> json7 & json8 & json9
        messagesList = threadApi.listMessages(id, 0, 100, "desc", null, query11)
        assertEquals(3, messagesList.readItems.size)

        // query12 -> json8
        messagesList = threadApi.listMessages(id, 0, 100, "desc", null, query12)
        assertEquals(1, messagesList.readItems.size)

        // query13 -> json8
        messagesList = threadApi.listMessages(id, 0, 100, "desc", null, query13)
        assertEquals(1, messagesList.readItems.size)

        // query14 -> json & json1 & json7 & json8 & json9
        messagesList = threadApi.listMessages(id, 0, 100, "desc", null, query14)
        assertEquals(5, messagesList.readItems.size)

        // query15 -> json2 & json3 & json4 & json5 & json6 & json7 & json9 & publicMeta
        messagesList = threadApi.listMessages(id, 0, 100, "desc", null, query15)
        assertEquals(8, messagesList.readItems.size)

        messagesList = threadApi.listMessages(id, 0, 100, "desc")
        assertTrue(messagesList.readItems.size >= 11)
    }

    @Test
    fun sendMessageIncorrectInputData() {
        val data = "message"

        // incorrect threadId
        assertFailsWith(PrivmxException::class) {
            threadApi.sendMessage(
                context2Id!!,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                data.encodeToByteArray()
            )
        }

        // msg total data bigger then limit length
        assertFailsWith(PrivmxException::class) {
            threadApi.sendMessage(
                thread2Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                ByteArray(1024 * 1024)
            )
        }
    }

    @Test
    fun sendMessageCorrectInputData() {
        lateinit var messageId: String
        val data = "message"

        // correct data
        assertDoesNotFail {
            messageId = threadApi.sendMessage(
                thread2Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                data.encodeToByteArray()
            )

        }

        val message: Message = threadApi.getMessage(messageId)
        assertEquals(message.data.size, data.length)
        assertEquals(message.info.threadId, thread2Id)
        assertContentEquals(message.data, data.encodeToByteArray())
    }

    @Test
    @Throws(Exception::class)
    fun updateMessageIncorrectInputData() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val threadApi2 = ThreadApi(connection2!!)
        val data = "message"
        val updatedData = "new message"

        // users [user1, user2] , managers [user1]
        val id: String = threadApi.createThread(
            context2Id!!,
            users,
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        val messageId: String = threadApi.sendMessage(
            id,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            data.encodeToByteArray()
        )

        // incorrect messageId
        assertFailsWith(PrivmxException::class) {
            threadApi.updateMessage(
                context2Id!!,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                data.encodeToByteArray()
            )
        }

        // msg total data bigger then limit length
        assertFailsWith(PrivmxException::class) {
            threadApi.updateMessage(
                messageId,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                ByteArray(1024 * 1024)
            )
        }

        // user1 creates - user2(only user) updates
        assertFailsWith(PrivmxException::class) {
            threadApi2.updateMessage(
                messageId,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                updatedData.encodeToByteArray()
            )
        }

        threadApi2.close()
    }

    @Test
    @Throws(Exception::class)
    fun updateMessageCorrectInputData() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val threadApi2 = ThreadApi(connection2!!)
        val data = "message"
        val updatedData = "new message"

        // users [user1] , managers [user1]
        val threadId: String = threadApi.createThread(
            context2Id!!,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        // users [user1] , managers [user1, user2]
        val threadId2: String = threadApi.createThread(
            context2Id!!,
            users.subList(0, 1),
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        val messageId: String = threadApi.sendMessage(
            threadId,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            data.encodeToByteArray()
        )
        val messageId2: String = threadApi.sendMessage(
            threadId2,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            data.encodeToByteArray()
        )

        // user1 creates - user1(manager) updates
        assertDoesNotFail {
            threadApi.updateMessage(
                messageId,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                updatedData.encodeToByteArray()
            )
        }
        var message: Message = threadApi.getMessage(messageId)
        assertEquals(message.data.size, updatedData.length)
        assertContentEquals(message.data, updatedData.encodeToByteArray())

        // user1 creates - user2(manager) updates
        assertDoesNotFail {
            threadApi2.updateMessage(
                messageId2,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                updatedData.encodeToByteArray()
            )
        }
        message = threadApi.getMessage(messageId)
        assertEquals(message.data.size, updatedData.length)
        assertContentEquals(message.data, updatedData.encodeToByteArray())

        threadApi2.close()
    }

    @Test
    @Throws(Exception::class)
    fun deleteMessageIncorrectInputData() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val threadApi2 = ThreadApi(connection2!!)
        val data = "message"

        // users [user1, user2] , managers [user1]
        val threadId: String = threadApi.createThread(
            context2Id!!,
            users,
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        val messageId: String = threadApi.sendMessage(
            threadId,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            data.encodeToByteArray()
        )

        // incorrect messageId
        assertFailsWith(PrivmxException::class) {
            threadApi.deleteMessage(context2Id!!)
        }

        // user1 creates - user2(user) deletes
        assertFailsWith(PrivmxException::class) {
            threadApi2.deleteMessage(messageId)
        }

        threadApi2.close()
    }

    @Test
    @Throws(Exception::class)
    fun deleteMessageCorrectInputData() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val threadApi2 = ThreadApi(connection2!!)
        val data = "message"

        // users [user1] , managers [user1]
        val threadId: String = threadApi.createThread(
            context2Id!!,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        // users [user1, user2] , managers [user1, user2]
        val threadId2: String = threadApi.createThread(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        val messageId: String = threadApi.sendMessage(
            threadId,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            data.encodeToByteArray()
        )
        val messageId2: String = threadApi.sendMessage(
            threadId2,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            data.encodeToByteArray()
        )

        // user1 creates - user1(manager) deletes
        assertDoesNotFail {
            threadApi.deleteMessage(messageId)
        }

        // user1 creates - user2(manager) deletes
        assertDoesNotFail {
            threadApi2.deleteMessage(messageId2)
        }

        threadApi2.close()
    }

//    @Test
//    fun getThread() {
//        lateinit var thread: Thread
//
//        // incorrect Id
//        assertFailsWith(PrivmxException::class) {
//            threadApi.getThread(IniConfig["Login", "contextId"])
//        }
//
//        // correctId
//        assertDoesNotFail {
//            thread = threadApi.getThread(IniConfig["Thread_1", "threadId"])
//        }
//        assertEquals(IniConfig["Thread_1", "contextId"], thread.contextId)
//        assertEquals(IniConfig["Thread_1", "threadId"], thread.threadId)
//        assertEquals(IniConfig["Thread_1", "createDate"], thread.createDate.toString())
//        assertEquals(IniConfig["Thread_1", "creator"], thread.creator)
//        assertEquals(
//            IniConfig["Thread_1", "lastModificationDate"],
//            thread.lastModificationDate.toString()
//        )
//        assertEquals(IniConfig["Thread_1", "lastModifier"], thread.lastModifier)
//        assertEquals(IniConfig["Thread_1", "version"], thread.version.toString())
//        assertEquals(IniConfig["Thread_1", "lastMsgDate"], thread.lastMsgDate.toString())
//        assertEquals(
//            IniConfig["Thread_1", "messagesCount"],
//            thread.messagesCount.toString()
//        )
//        assertEquals(IniConfig["Thread_1", "statusCode"], thread.statusCode.toString())
//        assertEquals(
//            IniConfig["Thread_1", "messagesCount"],
//            thread.messagesCount.toString()
//        )
//        assertEquals(
//            IniConfig["Thread_1", "messagesCount"],
//            thread.messagesCount.toString()
//        )
//        assertEquals(IniConfig["Thread_1", "version"], thread.version.toString())
//
//        assertEquals(
//            IniConfig["Thread_1", "publicMeta_inHex"],
//            thread.publicMeta.toHexString()
//        )
//        assertEquals(
//            IniConfig["Thread_1", "uploaded_publicMeta_inHex"],
//            thread.publicMeta.toHexString()
//        )
//        assertEquals(
//            IniConfig["Thread_1", "privateMeta_inHex"],
//            thread.privateMeta.toHexString()
//        )
//        assertEquals(
//            IniConfig["Thread_1", "uploaded_privateMeta_inHex"],
//            thread.privateMeta.toHexString()
//        )
//
//        assertEquals(IniConfig["Thread_1", "creator"], thread.creator)
//        assertEquals(IniConfig["Thread_1", "version"], thread.version.toString())
//        assertEquals(1, thread.users.size)
//        assertEquals(IniConfig["Thread_1", "creator"], thread.users.first())
//        assertEquals(IniConfig["Thread_1", "creator"], thread.managers.first())
//    }

    @Test
    fun listThreadsIncorrectInputData() {
        val contextId: String = contextId!!

        // incorrect contextId
        assertFailsWith(PrivmxException::class) {
            threadApi.listThreads(threadId, 0, 1, "desc")
        }

        // limit < 0
        assertFailsWith(PrivmxException::class) {
            threadApi.listThreads(contextId, 0, -1, "desc")
        }

        // limit == 0
        assertFailsWith(PrivmxException::class) {
            threadApi.listThreads(contextId, 0, 0, "desc")
        }

        // incorrect sortOrder
        assertFailsWith(PrivmxException::class) {
            threadApi.listThreads(contextId, 0, 1, "wrong")
        }

        // incorrect lastId
        assertFailsWith(PrivmxException::class) {
            threadApi.listThreads(contextId, 0, 1, "desc", contextId)
        }

        // incorrect queryAsJson
        assertFailsWith(PrivmxException::class) {
            threadApi.listThreads(contextId, 0, 1, "desc", null, "wrong")
        }

        // incorrect sortBy
        assertFailsWith(PrivmxException::class) {
            threadApi.listThreads(contextId, 0, 1, "desc", null, null, "wrong")
        }
    }

    @Test
    fun listThreadsCorrectInputData() {
        val contextId = contextId!!
        val thread3ContextId = contextId
        val thread3ThreadId = thread3Id
        val thread2ContextId = contextId
        val thread2ThreadId = thread2Id
        lateinit var threads: PagingList<Thread>

        assertDoesNotFail {
            threads = threadApi.listThreads(contextId, 4, 1, "desc")
        }

        assertEquals(3, threads.totalAvailable)
        assertEquals(0, threads.readItems.size)

        assertDoesNotFail {
            threads = threadApi.listThreads(contextId, 0, 1, "desc")
        }
        assertEquals(3, threads.totalAvailable)
        assertEquals(1, threads.readItems.size)
        assertEquals(thread3ContextId, threads.readItems.first().contextId)
        assertEquals(thread3ThreadId, threads.readItems.first().threadId)

        assertDoesNotFail {
            threads = threadApi.listThreads(contextId, 1, 3, "asc")
        }
        assertEquals(3, threads.totalAvailable)
        assertEquals(2, threads.readItems.size)
        assertEquals(thread2ContextId, threads.readItems.first().contextId)
        assertEquals(thread2ThreadId, threads.readItems.first().threadId)

        // lastId
        assertDoesNotFail {
            threads = threadApi.listThreads(contextId, 0, 1, "asc", thread2Id)
        }
        // todo - should be equal - will be fixed once the reported issue is fixed on bridge/endpoint
        assertNotEquals(2, threads.totalAvailable)
        assertEquals(1, threads.readItems.size)

        // sortBy - createDate
        assertDoesNotFail {
            threads = threadApi.listThreads(
                contextId,
                0,
                3,
                "asc",
                null,
                null,
                "createDate"
            )
        }
        assertEquals(3, threads.totalAvailable)
        assertEquals(3, threads.readItems.size)

        // sortBy - lastModificationDate
        assertDoesNotFail {
            threads = threadApi.listThreads(
                contextId,
                0,
                3,
                "asc",
                null,
                null,
                "lastModificationDate"
            )
        }
        assertEquals(3, threads.totalAvailable)
        assertEquals(3, threads.readItems.size)

        // sortBy - lastMsgDate
        assertDoesNotFail {
            threads = threadApi.listThreads(
                contextId,
                0,
                3,
                "asc",
                null,
                null,
                "lastMsgDate"
            )
        }
        assertEquals(3, threads.totalAvailable)
        assertEquals(3, threads.readItems.size)
    }

    @Test
    @Throws(Exception::class)
    fun accessAsPublicUser() {
        val connectionPublicUser =
            connectAsUser(ConnectionType.Public, bridgeAddress)
        val threadApiPublicUser = ThreadApi(connectionPublicUser)
        val updateMeta = "meta"
        val data = "message"

        // get thread
        assertFailsWith(PrivmxException::class) {
            threadApiPublicUser.getThread(thread3Id)
        }

        // create thread
        assertFailsWith(PrivmxException::class) {
            threadApiPublicUser.createThread(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        // update thread
        assertFailsWith(PrivmxException::class) {
            threadApiPublicUser.updateThread(
                thread3Id,
                users,
                users,
                updateMeta.encodeToByteArray(),
                updateMeta.encodeToByteArray(),
                2,
                true
            )
        }

        // update thread
        assertFailsWith(PrivmxException::class) {
            threadApiPublicUser.deleteThread(thread3Id)
        }

        // get message
        assertFailsWith(PrivmxException::class) {
            threadApiPublicUser.getMessage(messageId)
        }

        // list messages
        assertFailsWith(PrivmxException::class) {
            threadApiPublicUser.listMessages(thread3Id, 0, 1, "desc")
        }

        // update message
        assertFailsWith(PrivmxException::class) {
            threadApiPublicUser.updateMessage(
                messageId,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                data.encodeToByteArray()
            )
        }

        // delete message
        assertFailsWith(PrivmxException::class) {
            threadApiPublicUser.deleteMessage(messageId)
        }

        threadApiPublicUser.close()
        connectionPublicUser.close()
    }

    @Test
    @Throws(Exception::class)
    fun subscribeForThreadEvents() {
        val threadsSubscriptionIds = mutableListOf<String>()
        val messagesSubscriptionIds = mutableListOf<String>()

        // ThreadEvents
        // subscribe for ThreadEvents
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.THREAD_CREATE,
                    ThreadEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.THREAD_UPDATE,
                    ThreadEventSelectorType.THREAD_ID,
                    thread3Id
                )
            )
            queries.add(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.THREAD_STATS,
                    ThreadEventSelectorType.THREAD_ID,
                    thread3Id
                )
            )
            queries.add(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.THREAD_DELETE,
                    ThreadEventSelectorType.THREAD_ID,
                    thread3Id
                )
            )
            threadsSubscriptionIds.addAll(threadApi.subscribeFor(queries))
        }

        // subscribe again
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.THREAD_CREATE,
                    ThreadEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            threadsSubscriptionIds.addAll(threadApi.subscribeFor(queries))
        }

        // 2 same queries
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.THREAD_UPDATE,
                    ThreadEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.THREAD_UPDATE,
                    ThreadEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            threadsSubscriptionIds.addAll(threadApi.subscribeFor(queries))
        }

        // subscribe with wrong ThreadEventSelectorType
        assertFailsWith(PrivmxException::class) {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.THREAD_CREATE,
                    ThreadEventSelectorType.THREAD_ID,
                    thread3Id
                )
            )
        }

        // subscribe with nonexisting selectorId
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.THREAD_CREATE,
                    ThreadEventSelectorType.CONTEXT_ID,
                    "f977e533-3524-4579-error-215368c4b2a4"
                )
            )
            threadsSubscriptionIds.addAll(threadApi.subscribeFor(queries))
        }


        // MessageEvents
        // subscribe for MessageEvents
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.MESSAGE_UPDATE,
                    ThreadEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.MESSAGE_CREATE,
                    ThreadEventSelectorType.THREAD_ID,
                    thread3Id
                )
            )
            queries.add(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.MESSAGE_DELETE,
                    ThreadEventSelectorType.THREAD_ID,
                    thread3Id
                )
            )
            messagesSubscriptionIds.addAll(threadApi.subscribeFor(queries))
        }

        // subscribe again
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.MESSAGE_CREATE,
                    ThreadEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            messagesSubscriptionIds.addAll(threadApi.subscribeFor(queries))
        }

        // 2 same queries
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.MESSAGE_UPDATE,
                    ThreadEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.MESSAGE_UPDATE,
                    ThreadEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            messagesSubscriptionIds.addAll(threadApi.subscribeFor(queries))
        }

        // subscribe with wrong ThreadEventSelectorType
        assertFailsWith(PrivmxException::class) {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.MESSAGE_CREATE,
                    ThreadEventSelectorType.MESSAGE_ID,
                    messageId
                )
            )
        }

        // subscribe with nonexisting selectorId
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()
            queries.add(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.MESSAGE_CREATE,
                    ThreadEventSelectorType.CONTEXT_ID,
                    thread2Id
                )
            )
            messagesSubscriptionIds.addAll(threadApi.subscribeFor(queries))
        }

        // todo - SIGSEGV - will be fixed once the reported issue is fixed on bridge/endpoint
        /*
        // unsubscribe from ThreadEvents
         assertDoesNotFail {
            threadApi.unsubscribeFrom(threadsSubscriptionIds);
        }

        // unsubscribe from ThreadEvents again
         assertDoesNotFail {
            threadApi.unsubscribeFrom(threadsSubscriptionIds);
        }

        // unsubscribe from MessageEvents
        assertDoesNotFail {
            threadApi.unsubscribeFrom(messagesSubscriptionIds);
        }

        // unsubscribe from MessageEvents again
         assertDoesNotFail {
            threadApi.unsubscribeFrom(messagesSubscriptionIds);
        }
        */

        assertDoesNotFail {
            threadApi.unsubscribeFrom(threadsSubscriptionIds + messagesSubscriptionIds)
        }
    }

    @Test
    @Ignore
    @Throws(Exception::class)
    fun setUserVerifierThread() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val threadApi2 = ThreadApi(connection2!!)
        val thread3 = threadApi.getThread(thread3Id)
        val illegalMessage = "Illegal message"

        val userVerifierFalse: UserVerifierInterface = object : UserVerifierInterface {
            override fun verify(request: List<VerificationRequest>): List<Boolean> {
                return request.map { req: VerificationRequest -> false }
            }
        }

        assertDoesNotFail {
            connection!!.setUserVerifier(
                userVerifierFalse
            )
        }

        // create container
        lateinit var threadCreatedId: String
        lateinit var threadCreated2Id: String

        assertDoesNotFail {
            threadCreatedId = threadApi.createThread(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }
        assertDoesNotFail {
            threadCreated2Id = threadApi2.createThread(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
        }

        val threadCreated = threadApi2.getThread(threadCreatedId)
        assertEquals(0, threadCreated.statusCode)

        // get container
        lateinit var threadVerifiedUser: Thread
        lateinit var threadUnverifiedUser: Thread

        // user verified with positive result
        assertDoesNotFail {
            threadVerifiedUser = threadApi2.getThread(threadCreatedId)
        }
        assertEquals(
            0,
            threadVerifiedUser.statusCode
        )

        // user verified with negative result
        assertDoesNotFail {
            threadUnverifiedUser = threadApi.getThread(threadCreatedId)
        }
        assertNotEquals(0, threadUnverifiedUser.statusCode)

        // list containers
        lateinit var threadsVerifiedUser: PagingList<Thread>
        lateinit var threadsUnverifiedUser: PagingList<Thread>

        // user verified with positive result
        assertDoesNotFail {
            threadsVerifiedUser = threadApi2.listThreads(context2Id!!, 0, 10, "desc")
        }
        assertFalse(threadsVerifiedUser.readItems.isEmpty())
        threadsVerifiedUser.readItems.forEach {
            assertEquals(0, it.statusCode)
        }

        // user verified with negative result
        assertDoesNotFail {
            threadsUnverifiedUser = threadApi.listThreads(context2Id!!, 0, 10, "desc")
        }
        assertFalse(threadsUnverifiedUser.readItems.isEmpty())
        threadsUnverifiedUser.readItems.forEach {
            assertNotEquals(0, it.statusCode)
        }
        // update container
        assertFailsWith(PrivmxException::class) {
            threadApi.updateThread(
                threadCreatedId,
                users,
                users,
                illegalMessage.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                thread3.version!! + 1,
                false
            )
        }

        val threadUpdated = threadApi2.getThread(threadCreatedId)
        // should be equal - thread should not update
        assertContentEquals(threadCreated.publicMeta, threadUpdated.publicMeta)
        assertEquals(threadCreated.version, threadUpdated.version)

        // delete container
        assertDoesNotFail {
            threadApi.deleteThread(
                threadCreated2Id
            )
        }

        // thread should be deleted
        assertFailsWith(PrivmxException::class) {
            threadApi2.getThread(threadCreated2Id)
        }

        // create item
        lateinit var messageCreatedId: String
        lateinit var messageCreated2Id: String

        assertFailsWith(PrivmxException::class) {
            messageCreatedId = threadApi.sendMessage(
                threadCreatedId,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        assertDoesNotFail {
            messageCreated2Id = threadApi2.sendMessage(
                threadCreatedId,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
        }
        val messageCreated = threadApi2.getMessage(messageCreated2Id)
        assertEquals(0, messageCreated.statusCode)

        // get item
        // user verified with positive result
        lateinit var messageVerifiedUser: Message
        lateinit var messageUnverifiedUser: Message

        // user verified with positive result
        assertDoesNotFail {
            messageVerifiedUser = threadApi2.getMessage(messageCreated2Id)
        }
        assertEquals(0, messageVerifiedUser.statusCode)

        // user verified with negative result
        assertDoesNotFail {
            messageUnverifiedUser = threadApi.getMessage(messageCreated2Id)
        }
        assertNotEquals(0, messageUnverifiedUser.statusCode)

        // list items
        lateinit var messagesVerifiedUser: PagingList<Message>
        lateinit var messagesUnverifiedUser: PagingList<Message>

        // user verified with positive result
        assertDoesNotFail {
            messagesVerifiedUser = threadApi2.listMessages(threadCreatedId, 0, 10, "desc")
        }
        assertFalse(messagesVerifiedUser.readItems.isEmpty())
        messagesVerifiedUser.readItems.forEach {
            assertEquals(0, it.statusCode)
        }

        // user verified with negative result
        assertDoesNotFail {
            messagesUnverifiedUser = threadApi.listMessages(threadCreatedId, 0, 10, "desc")
        }
        assertFalse(messagesUnverifiedUser.readItems.isEmpty())
        messagesUnverifiedUser.readItems.forEach {
            assertNotEquals(0, it.statusCode)
        }


        // user1 updates
        assertFailsWith(PrivmxException::class) {
            threadApi.updateMessage(
                messageCreated2Id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                illegalMessage.encodeToByteArray()
            )
        }

        // delete item
        assertDoesNotFail {
            threadApi.deleteMessage(
                messageCreated2Id
            )
        }

        // thread should be deleted
        assertFailsWith(
            PrivmxException::class
        ) {
            threadApi2.getMessage(messageCreated2Id)
        }

        threadApi2.close()
    }
}