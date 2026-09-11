package E2ETests

import com.simplito.kotlin.privmx_endpoint.model.CollectionItemChange
import com.simplito.kotlin.privmx_endpoint.model.FilesConfig
import com.simplito.kotlin.privmx_endpoint.model.Event
import com.simplito.kotlin.privmx_endpoint.model.FileChange
import com.simplito.kotlin.privmx_endpoint.model.InboxEntry
import com.simplito.kotlin.privmx_endpoint.model.KvdbEntry
import com.simplito.kotlin.privmx_endpoint.model.VerificationRequest
import com.simplito.kotlin.privmx_endpoint.model.events.CollectionChangedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ContextCustomEventData
import com.simplito.kotlin.privmx_endpoint.model.events.InboxDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.KvdbDeletedEntryEventData
import com.simplito.kotlin.privmx_endpoint.model.events.KvdbStatsEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreFileDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreFileUpdatedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreStatsChangedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadDeletedMessageEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadStatsEventData
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.CustomEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.InboxEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.KvdbEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.StoreEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.ThreadEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.InboxEventType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.KvdbEventType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.StoreEventType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.ThreadEventType
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.core.EventQueue
import com.simplito.kotlin.privmx_endpoint.modules.core.UserVerifierInterface
import com.simplito.kotlin.privmx_endpoint.modules.event.EventApi
import com.simplito.kotlin.privmx_endpoint.modules.inbox.InboxApi
import com.simplito.kotlin.privmx_endpoint.modules.kvdb.KvdbApi
import com.simplito.kotlin.privmx_endpoint.modules.store.StoreApi
import com.simplito.kotlin.privmx_endpoint.modules.thread.ThreadApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class EventsTest : BaseTest() {
    lateinit var eventsConnection: Connection
    private lateinit var thread3Id: String
    private lateinit var thread1Id: String
    private lateinit var messageId: String
    private lateinit var store3Id: String
    private lateinit var store1Id: String
    private lateinit var file2Id: String
    private lateinit var inbox3Id: String
    private lateinit var kvdb3Id: String
    private lateinit var kvdb1Id: String
    private val kvdbEntry2Key = "entry_key_2"

    /**
     * Waits for event and return it.
     * After passed duration emits break event and returns null.
     */
    fun waitForEventWithTimeout(duration: Duration = 2.seconds): Event<*> = runBlocking {
        val waitResult = async(Dispatchers.IO) {
            EventQueue.waitEvent()
        }
        val delayJob = launch(Dispatchers.IO) {
            delay(duration)
            EventQueue.emitBreakEvent()
        }
        waitResult.await().also {
            delayJob.cancel()
        }
    }

    /**
     * Gets an event and verifies that its type matches the one provided as an argument.
     */
    fun expectAndVerifyEvent(eventType: String): Event<*>? {
        return expectAndVerifyEvent(
            eventsConnection.getConnectionId()!!,
            eventType
        )
    }

    /**
     * Gets an event and verifies that its type matches the one provided as an argument.
     */
    fun expectAndVerifyEvent(
        connectionId: Long,
        eventType: String,
    ): Event<*>? {
        var event: Event<*>? = null

        assertDoesNotFail {
            event = waitForEventWithTimeout()
        }
        assertEquals(connectionId, event!!.connectionId)
        assertEquals(eventType, event.type)

        return event
    }

    /**
     * Function checks that no new events have been added to the queue.
     */
    fun expectNoEventOccurs() {
        runBlocking {
            delay(1500)
        }
        assertNull(EventQueue.getEvent())
    }

    fun createFile(
        storeApi: StoreApi,
        storeId: String,
        data: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray
    ): String {
        val handle: Long =
            storeApi.createFile(
                storeId,
                publicMeta,
                privateMeta,
                data.encodeToByteArray().size.toLong()
            )!!
        storeApi.writeToFile(handle, data.encodeToByteArray())
        return storeApi.closeFile(handle)
    }

    fun updateFile(
        storeApi: StoreApi,
        fileId: String,
        data: String,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        size: Long
    ) {
        val handle: Long = storeApi.updateFile(fileId, publicMeta, privateMeta, size)!!
        storeApi.writeToFile(handle, data.encodeToByteArray())
        storeApi.closeFile(handle)
    }

    fun writeToFileOnPosition(
        api: StoreApi,
        fileId: String,
        pos: Long,
        data: ByteArray,
        truncate: Boolean
    ) {
        val writeReadHandle: Long = api.openFile(fileId)!!
        api.seekInFile(writeReadHandle, pos)
        api.writeToFile(writeReadHandle, data, truncate)
        api.closeFile(writeReadHandle)
    }

    fun readFromFile(storeApi: StoreApi, fileId: String, pos: Long?, length: Long): ByteArray? {
        val handle: Long = storeApi.openFile(fileId)!!
        storeApi.seekInFile(handle, pos!!)
        val content: ByteArray? = storeApi.readFromFile(handle, length)
        storeApi.closeFile(handle)
        return content
    }

    @BeforeTest
    fun beforeTest() {
        eventsConnection = connectAsUser(ConnectionType.User1, bridgeAddress)

        val threadApi = ThreadApi(eventsConnection)
        thread3Id = threadApi.createThread(contextId!!, users, users.subList(0, 1), publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray())
        thread1Id = threadApi.createThread(contextId!!, users.subList(0, 1), users.subList(0, 1), publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray())
        messageId = threadApi.sendMessage(thread1Id, publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray(), "message".encodeToByteArray())
        threadApi.close()

        val storeApi = StoreApi(eventsConnection)
        store3Id = storeApi.createStore(contextId!!, users, users.subList(0, 1), publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray())
        store1Id = storeApi.createStore(contextId!!, users.subList(0, 1), users.subList(0, 1), publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray())
        file2Id = createFile(storeApi, store1Id, "file_data_2_extra", publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray())
        storeApi.close()

        val inboxApi = InboxApi(eventsConnection)
        inbox3Id = inboxApi.createInbox(contextId!!, users, users.subList(0, 1), publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray(), FilesConfig(0L, 10L, 100*1024*1024, 100*1024*1024))
        inboxApi.close()

        val kvdbApi = KvdbApi(eventsConnection)
        kvdb3Id = kvdbApi.createKvdb(contextId!!, users, users.subList(0, 1), publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray())
        kvdb1Id = kvdbApi.createKvdb(contextId!!, users.subList(0, 1), users.subList(0, 1), publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray())
        kvdbApi.setEntry(kvdb1Id, kvdbEntry2Key, publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray(), "entry_data_2".encodeToByteArray())
        kvdbApi.close()

        // get libConnected event
        EventQueue.getEvent()
    }

    @AfterTest
    fun afterAll() {
        try {
            val threadApiCleanup = ThreadApi(eventsConnection)
            if (::thread1Id.isInitialized) threadApiCleanup.deleteThread(thread1Id)
            if (::thread3Id.isInitialized) threadApiCleanup.deleteThread(thread3Id)
            threadApiCleanup.close()
        } catch (ignore: Exception) {}

        try {
            val storeApiCleanup = StoreApi(eventsConnection)
            if (::store1Id.isInitialized) storeApiCleanup.deleteStore(store1Id)
            if (::store3Id.isInitialized) storeApiCleanup.deleteStore(store3Id)
            storeApiCleanup.close()
        } catch (ignore: Exception) {}

        try {
            val inboxApiCleanup = InboxApi(eventsConnection)
            if (::inbox3Id.isInitialized) inboxApiCleanup.deleteInbox(inbox3Id)
            inboxApiCleanup.close()
        } catch (ignore: Exception) {}

        try {
            val kvdbApiCleanup = KvdbApi(eventsConnection)
            if (::kvdb1Id.isInitialized) kvdbApiCleanup.deleteKvdb(kvdb1Id)
            if (::kvdb3Id.isInitialized) kvdbApiCleanup.deleteKvdb(kvdb3Id)
            kvdbApiCleanup.close()
        } catch (ignore: Exception) {}

        eventsConnection.close()
        try {
            closeConnectionAndCleanEvents(connection2!!)
        } catch (ignore: Exception) {
        } finally {
            connection2 = null
        }

        // get libDisconnected event
        EventQueue.getEvent()
        EventQueue.getEvent()
    }

    @Test
    fun getEventTest() {
        val eventData = "Content of event data".encodeToByteArray()
        val eventApi = EventApi(eventsConnection)
        var event: Event<*>? = null

        // subscribe for event
        eventApi.subscribeFor(
            listOf(
                eventApi.buildSubscriptionQuery(
                    "Test",
                    CustomEventSelectorType.CONTEXT_ID,
                    context2Id!!
                )
            )
        )

        // emit event
        eventApi.emitEvent(
            context2Id!!,
            users,
            "Test",
            eventData
        )

        assertDoesNotFail {
            runBlocking {
                delay(1500)
            }
            event = EventQueue.getEvent()
        }
        assertNotNull(event)
        assertEquals(eventsConnection.getConnectionId(), event?.connectionId)
        assertNotNull(event?.timestamp)

        // wait for event - but no event emitted
        assertDoesNotFail {
            runBlocking {
                delay(1500)
            }
            event = EventQueue.getEvent()
        }
        assertNull(event)
    }

    @Test
    fun waitEventTest() {
        val eventData = "Content of event data".encodeToByteArray()
        val eventApi = EventApi(eventsConnection)
        var event: Event<*>? = null

        // subscribe for event
        eventApi.subscribeFor(
            listOf(
                eventApi.buildSubscriptionQuery(
                    "Test",
                    CustomEventSelectorType.CONTEXT_ID,
                    context2Id!!
                )
            )
        )

        // emit event
        eventApi.emitEvent(
            context2Id!!,
            users,
            "Test",
            eventData
        )

        assertDoesNotFail {
            event = waitForEventWithTimeout()
        }
        assertNotNull(event)
        assertEquals(eventsConnection.getConnectionId(), event.connectionId)
        assertNotNull(event.timestamp)

        // wait for event - but no event emitted
        assertDoesNotFail {
            event = waitForEventWithTimeout()
        }
        assertNotNull(event)
        assertEquals("libBreak", event.type)
        assertNotNull(event.timestamp)
    }

    @Test
    fun getEventMultipleConnections() {
        val connection2 = connectAsUser(
            ConnectionType.User2,
            bridgeAddress
        )
        var event: Event<*>? = null

        assertDoesNotFail {
            event = EventQueue.getEvent()
        }
        assertEquals(connection2.getConnectionId(), event?.connectionId)
        assertEquals("libConnected", event?.type)

        connection2.close()
        EventQueue.getEvent() //libDisconnected
        EventQueue.getEvent() //libPlatformDisconnected
    }

    @Test
    fun getEventDisconnect() {
        val connection2 = connectAsUser(
            ConnectionType.User2,
            bridgeAddress
        )

        var event: Event<*>? = null

        EventQueue.getEvent()

        connection2.disconnect()
        assertDoesNotFail {
            event = EventQueue.getEvent()
        }
        assertEquals(connection2.getConnectionId(), event?.connectionId)
        assertEquals("libDisconnected", event?.type)

        connection2.close()
        EventQueue.getEvent() //libPlatformDisconnected
    }

    @Test
    fun getEventPlatformDisconnect() {
        val connection2 = connectAsUser(
            ConnectionType.User2,
            bridgeAddress
        )
        var event: Event<*>? = null

        EventQueue.getEvent()

        // get libDisconnect
        connection2.disconnect()
        EventQueue.getEvent()

        // get libPlatformDisconnected
        assertDoesNotFail {
            event = EventQueue.getEvent()
        }
        assertEquals(connection2.getConnectionId(), event?.connectionId)
        assertEquals("libPlatformDisconnected", event?.type)
        connection2.close()
    }

    @Test
    fun waitEventMultipleConnections() {
        val connection2 = connectAsUser(
            ConnectionType.User2,
            bridgeAddress
        )
        lateinit var event: Event<*>

        assertDoesNotFail {
            event = waitForEventWithTimeout()
        }
        assertEquals(connection2.getConnectionId(), event.connectionId)
        assertEquals("libConnected", event.type)

        connection2.close()
        EventQueue.getEvent() //libDisconnected
        EventQueue.getEvent() //libPlatformDisconnected
    }

    @Test
    fun waitEventDisconnect() {
        val connection2 = connectAsUser(
            ConnectionType.User2,
            bridgeAddress
        )
        lateinit var event: Event<*>

        EventQueue.getEvent()

        connection2.disconnect()
        assertDoesNotFail {
            event = waitForEventWithTimeout()
        }
        assertEquals(connection2.getConnectionId(), event.connectionId)
        assertEquals("libDisconnected", event.type)

        connection2.close()
        EventQueue.getEvent() //libPlatformDisconnected
    }

    @Test
    fun waitEventPlatformDisconnect() {
        val connection2 = connectAsUser(
            ConnectionType.User2,
            bridgeAddress
        )
        lateinit var event: Event<*>

        // get second libConnection
        waitForEventWithTimeout()

        // get libDisconnect
        connection2.disconnect()
        waitForEventWithTimeout()

        // get libPlatformDisconnected
        assertDoesNotFail {
            event = waitForEventWithTimeout()
        }
        assertEquals(connection2.getConnectionId(), event.connectionId)
        assertEquals("libPlatformDisconnected", event.type)
        connection2.close()
    }

    @Test
    fun getEvent_ThreadCreated_ContextId() {
        val threadApi = ThreadApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String?>

        // subscribe and create thread
        subscriptionIds = threadApi.subscribeFor(
            listOf(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.THREAD_CREATE,
                    ThreadEventSelectorType.CONTEXT_ID,
                    context2Id!!
                )
            )
        )

        assertDoesNotFail {
            threadApi.createThread(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
            event = expectAndVerifyEvent("threadCreated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        threadApi.unsubscribeFrom(subscriptionIds)

        threadApi.close()
    }

    @Test
    fun getEvent_ThreadDeleted_ContextId() {
        val threadApi = ThreadApi(eventsConnection)
        var threadDeletedEventData: ThreadDeletedEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String? = threadApi.createThread(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        // subscribe
        subscriptionIds = threadApi.subscribeFor(
            listOf(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.THREAD_DELETE,
                    ThreadEventSelectorType.CONTEXT_ID,
                    context2Id!!
                )
            )
        )

        // delete thread
        assertDoesNotFail {
            threadApi.deleteThread(id!!)
            event = expectAndVerifyEvent("threadDeleted")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            threadDeletedEventData =
                event.data as ThreadDeletedEventData?
        }
        assertEquals(id, threadDeletedEventData!!.threadId)

        // unsubscribe
        threadApi.unsubscribeFrom(subscriptionIds)

        threadApi.close()
    }

    @Test
    fun getEvent_ThreadDeleted_ThreadId() {
        val threadApi = ThreadApi(eventsConnection)
        var threadDeletedEventData: ThreadDeletedEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String?>

        val id: String? = threadApi.createThread(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        // subscribe
        subscriptionIds = threadApi.subscribeFor(
            listOf(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.THREAD_DELETE,
                    ThreadEventSelectorType.THREAD_ID,
                    id!!
                )
            )
        )

        // delete thread
        assertDoesNotFail {
            threadApi.deleteThread(id)
            event = expectAndVerifyEvent("threadDeleted")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            threadDeletedEventData =
                event.data as ThreadDeletedEventData?
        }
        assertEquals(id, threadDeletedEventData!!.threadId)


        // unsubscribe
        threadApi.unsubscribeFrom(subscriptionIds)

        threadApi.close()
    }

    @Test
    fun getEvent_ThreadUpdated_ContextId() {
        val threadApi = ThreadApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = thread3Id

        // subscribe
        subscriptionIds = threadApi.subscribeFor(
            listOf(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.THREAD_UPDATE,
                    ThreadEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        // update thread
        assertDoesNotFail {
            threadApi.updateThread(
                id,
                users,
                users,
                publicMeta.encodeToByteArray(),
                "new privateMeta".encodeToByteArray(),
                1,
                true
            )
            event = expectAndVerifyEvent("threadUpdated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        threadApi.unsubscribeFrom(subscriptionIds)

        threadApi.close()
    }

    @Test
    fun getEvent_ThreadUpdated_ThreadId() {
        val threadApi = ThreadApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = thread3Id

        // subscribe
        subscriptionIds = threadApi.subscribeFor(
            listOf(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.THREAD_UPDATE,
                    ThreadEventSelectorType.THREAD_ID,
                    id
                )
            )
        )

        // update thread
        assertDoesNotFail {
            threadApi.updateThread(
                id,
                users,
                users,
                publicMeta.encodeToByteArray(),
                "new privateMeta".encodeToByteArray(),
                1,
                true
            )
            event = expectAndVerifyEvent("threadUpdated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        threadApi.unsubscribeFrom(subscriptionIds)

        threadApi.close()
    }

    @Test
    fun getEvent_ThreadStatsChanged_ContextId() {
        val threadApi = ThreadApi(eventsConnection)
        lateinit var event: Event<*>
        var threadStatsEventData: ThreadStatsEventData? = null
        val subscriptionIds: List<String>

        val id: String = thread3Id

        val id_message: String? = threadApi.sendMessage(
            id,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "message".encodeToByteArray()
        )
        val amount: Long = threadApi.getThread(id).messagesCount!!

        // subscribe
        subscriptionIds = threadApi.subscribeFor(
            listOf(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.THREAD_STATS,
                    ThreadEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        // send message
        assertDoesNotFail {
            threadApi.sendMessage(
                id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "message".encodeToByteArray()
            )
            event = expectAndVerifyEvent("threadStatsChanged")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            threadStatsEventData =
                event.data as ThreadStatsEventData?
        }
        assertEquals(id, threadStatsEventData!!.threadId)
        assertEquals(amount + 1, threadStatsEventData!!.messagesCount)

        // update message ( check if other operation do not send event)
        assertDoesNotFail {
            threadApi.updateMessage(
                id_message!!,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "message".encodeToByteArray()
            )
            expectNoEventOccurs()
        }

        // unsubscribe
        threadApi.unsubscribeFrom(subscriptionIds)

        threadApi.close()
    }

    @Test
    fun getEvent_ThreadStatsChanged_ThreadId() {
        val threadApi = ThreadApi(eventsConnection)
        lateinit var event: Event<*>
        var threadStatsEventData: ThreadStatsEventData? = null
        val subscriptionIds: List<String>

        val id: String = thread3Id
        val id_message: String? = threadApi.sendMessage(
            id,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "message".encodeToByteArray()
        )

        val amount: Long = threadApi.getThread(id).messagesCount!!

        // subscribe
        subscriptionIds = threadApi.subscribeFor(
            listOf(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.THREAD_STATS,
                    ThreadEventSelectorType.THREAD_ID,
                    id
                )
            )
        )

        // send message
        assertDoesNotFail {
            threadApi.sendMessage(
                id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "message".encodeToByteArray()
            )
            event = expectAndVerifyEvent("threadStatsChanged")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            threadStatsEventData = event.data as ThreadStatsEventData?
        }
        assertEquals(id, threadStatsEventData!!.threadId)
        assertEquals(amount + 1, threadStatsEventData!!.messagesCount)

        // update message  ( check if other operation do not send event)
        assertDoesNotFail {
            threadApi.updateMessage(
                id_message!!,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "message".encodeToByteArray()
            )
            expectNoEventOccurs()
        }

        // unsubscribe
        threadApi.unsubscribeFrom(subscriptionIds)

        threadApi.close()
    }

    @Test
    fun getEvent_ThreadNewMessage_ThreadId() {
        val threadApi = ThreadApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = thread3Id

        // subscribe
        subscriptionIds = threadApi.subscribeFor(
            listOf(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.MESSAGE_CREATE,
                    ThreadEventSelectorType.THREAD_ID,
                    id
                )
            )
        )

        // send message
        assertDoesNotFail {
            threadApi.sendMessage(
                id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "message".encodeToByteArray()
            )
            event = expectAndVerifyEvent("threadNewMessage")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        threadApi.unsubscribeFrom(subscriptionIds)

        threadApi.close()
    }

    @Test
    fun getEvent_ThreadUpdatedMessage_ContextId() {
        val threadApi = ThreadApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = messageId

        // subscribe
        subscriptionIds = threadApi.subscribeFor(
            listOf(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.MESSAGE_UPDATE,
                    ThreadEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        // update message
        assertDoesNotFail {
            threadApi.updateMessage(
                id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "new message".encodeToByteArray()
            )
            event = expectAndVerifyEvent("threadUpdatedMessage")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        threadApi.unsubscribeFrom(subscriptionIds)

        threadApi.close()
    }

    @Test
    fun getEvent_ThreadUpdatedMessage_ThreadId() {
        val threadApi = ThreadApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val threadId: String = thread1Id
        val id: String = messageId

        // subscribe
        subscriptionIds = threadApi.subscribeFor(
            listOf(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.MESSAGE_UPDATE,
                    ThreadEventSelectorType.THREAD_ID,
                    threadId
                )
            )
        )

        // update message
        assertDoesNotFail {
            threadApi.updateMessage(
                id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "new message".encodeToByteArray()
            )
            event = expectAndVerifyEvent("threadUpdatedMessage")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        threadApi.unsubscribeFrom(subscriptionIds)

        threadApi.close()
    }

    @Test
    fun getEvent_ThreadUpdatedMessage_MessageId() {
        val threadApi = ThreadApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = messageId

        // subscribe
        subscriptionIds = threadApi.subscribeFor(
            listOf(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.MESSAGE_UPDATE,
                    ThreadEventSelectorType.MESSAGE_ID,
                    id!!
                )
            )
        )

        // update message
        assertDoesNotFail {
            threadApi.updateMessage(
                id,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "new message".encodeToByteArray()
            )
            event = expectAndVerifyEvent("threadUpdatedMessage")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        threadApi.unsubscribeFrom(subscriptionIds)

        threadApi.close()
    }

    @Test
    fun getEvent_ThreadMessageDeleted_ContextId() {
        val threadApi = ThreadApi(eventsConnection)
        var threadDeletedMessageEventData: ThreadDeletedMessageEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val threadId: String = thread3Id
        val id: String = threadApi.sendMessage(
            threadId!!,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "message".encodeToByteArray()
        )

        // subscribe
        subscriptionIds = threadApi.subscribeFor(
            listOf(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.MESSAGE_DELETE,
                    ThreadEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        // delete message
        assertDoesNotFail {
            threadApi.deleteMessage(id)
            event = expectAndVerifyEvent("threadMessageDeleted")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            threadDeletedMessageEventData =
                event.data as ThreadDeletedMessageEventData?
        }
        assertEquals(threadId, threadDeletedMessageEventData!!.threadId)
        assertEquals(id, threadDeletedMessageEventData!!.messageId)

        // unsubscribe
        threadApi.unsubscribeFrom(subscriptionIds)

        threadApi.close()
    }

    @Test
    fun getEvent_ThreadMessageDeleted_ThreadId() {
        val threadApi = ThreadApi(eventsConnection)
        var threadDeletedMessageEventData: ThreadDeletedMessageEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val threadId: String = thread3Id
        val id: String = threadApi.sendMessage(
            threadId,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "message".encodeToByteArray()
        )

        // subscribe
        subscriptionIds = threadApi.subscribeFor(
            listOf(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.MESSAGE_DELETE,
                    ThreadEventSelectorType.THREAD_ID,
                    threadId
                )
            )
        )

        // delete message
        assertDoesNotFail {
            threadApi.deleteMessage(id)
            event = expectAndVerifyEvent("threadMessageDeleted")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            threadDeletedMessageEventData =
                event.data as ThreadDeletedMessageEventData?
        }
        assertEquals(threadId, threadDeletedMessageEventData!!.threadId)
        assertEquals(id, threadDeletedMessageEventData!!.messageId)

        // unsubscribe
        threadApi.unsubscribeFrom(subscriptionIds)

        threadApi.close()
    }

    @Test
    fun getEvent_ThreadMessageDeleted_MessageId() {
        val threadApi = ThreadApi(eventsConnection)
        var threadDeletedMessageEventData: ThreadDeletedMessageEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val threadId: String = thread3Id
        val id: String? = threadApi.sendMessage(
            threadId!!,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "message".encodeToByteArray()
        )

        // subscribe
        subscriptionIds = threadApi.subscribeFor(
            listOf(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.MESSAGE_DELETE,
                    ThreadEventSelectorType.MESSAGE_ID,
                    id!!
                )
            )
        )

        // delete message
        assertDoesNotFail {
            threadApi.deleteMessage(id)
            event = expectAndVerifyEvent("threadMessageDeleted")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            threadDeletedMessageEventData =
                event.data as ThreadDeletedMessageEventData?
        }
        assertEquals(threadId, threadDeletedMessageEventData!!.threadId)
        assertEquals(id, threadDeletedMessageEventData!!.messageId)

        // unsubscribe
        threadApi.unsubscribeFrom(subscriptionIds)

        threadApi.close()
    }

    //TODO: Not working
//    @Test
//    @Throws(Exception::class)
//    fun getEvent_ThreadCollectionChanged_ContextId() {
//        val threadApi = ThreadApi(eventsConnection)
//        var collectionChangedEventData: CollectionChangedEventData? = null
//        var collectionItemsChange: List<CollectionItemChange> = emptyList()
//        var event: Event<*>? = null
//        val subscriptionIds: List<String>
//
//        var message: String? = null
//        val threadId: String = thread3Id
//
//        // subscribe
//        subscriptionIds = threadApi.subscribeFor(
//            listOf(
//                threadApi.buildSubscriptionQuery(
//                    ThreadEventType.COLLECTION_CHANGE,
//                    ThreadEventSelectorType.CONTEXT_ID,
//                    contextId!!
//                )
//            )
//        )
//
//
//        // send message
//        assertDoesNotFail {
//            message = threadApi.sendMessage(
//                threadId!!,
//                publicMeta.encodeToByteArray(),
//                privateMeta.encodeToByteArray(),
//                "message 1".encodeToByteArray()
//            )
//            event = expectAndVerifyEvent("collectionChanged")!!
//        }
//
//        // CollectionChangedEventData
//        assertDoesNotFail {
//            collectionChangedEventData =
//                event?.data as CollectionChangedEventData?
//        }
//        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
//        assertEquals(threadId, collectionChangedEventData?.moduleId)
//        assertEquals("thread", collectionChangedEventData?.moduleType)
//
//        // CollectionItemChange
//        assertDoesNotFail {
//            collectionItemsChange =
//                collectionChangedEventData?.items!!
//        }
//        assertEquals(message, collectionItemsChange[0].itemId)
//        assertEquals("create", collectionItemsChange[0].action)
//
//
//        // update message
//        assertDoesNotFail {
//            threadApi.updateMessage(
//                message!!,
//                "new meta".encodeToByteArray(),
//                privateMeta.encodeToByteArray(),
//                "edit".encodeToByteArray()
//            )
//            event = expectAndVerifyEvent("collectionChanged")!!
//        }
//
//        // CollectionChangedEventData
//        assertDoesNotFail {
//            collectionChangedEventData =
//                event?.data as CollectionChangedEventData?
//        }
//        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
//        assertEquals(threadId, collectionChangedEventData?.moduleId)
//        assertEquals("thread", collectionChangedEventData?.moduleType)
//
//        // CollectionItemChange
//        assertDoesNotFail {
//            collectionItemsChange =
//                collectionChangedEventData?.items!!
//        }
//        assertEquals(message, collectionItemsChange[0].itemId)
//        assertEquals("update", collectionItemsChange[0].action)
//
//
//        // delete message
//        assertDoesNotFail {
//            threadApi.deleteMessage(message!!)
//            event = expectAndVerifyEvent("collectionChanged")!!
//        }
//
//        // CollectionChangedEventData
//        assertDoesNotFail {
//            collectionChangedEventData =
//                event?.data as CollectionChangedEventData?
//        }
//        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
//        assertEquals(threadId, collectionChangedEventData?.moduleId)
//        assertEquals("thread", collectionChangedEventData?.moduleType)
//
//        // CollectionItemChange
//        assertDoesNotFail {
//            collectionItemsChange =
//                collectionChangedEventData?.items!!
//        }
//        assertEquals(message, collectionItemsChange[0].itemId)
//        assertEquals("delete", collectionItemsChange[0].action)
//
//
//        // unsubscribe
//        threadApi.unsubscribeFrom(subscriptionIds)
//
//        threadApi.close()
//    }

    @Test
    @Throws(Exception::class)
    fun getEvent_ThreadCollectionChanged_ThreadId() {
        val threadApi = ThreadApi(eventsConnection)
        var collectionChangedEventData: CollectionChangedEventData? = null
        var collectionItemsChange: List<CollectionItemChange> = emptyList()
        var event: Event<*>? = null
        val subscriptionIds: List<String>

        var message: String? = null
        val threadId: String = thread3Id

        // subscribe
        subscriptionIds = threadApi.subscribeFor(
            listOf(
                threadApi.buildSubscriptionQuery(
                    ThreadEventType.COLLECTION_CHANGE,
                    ThreadEventSelectorType.THREAD_ID,
                    threadId!!
                )
            )
        )


        // send message
        assertDoesNotFail {
            message = threadApi.sendMessage(
                threadId,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "message 1".encodeToByteArray()
            )
            event = expectAndVerifyEvent("collectionChanged")!!
        }


        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event?.data as CollectionChangedEventData?
        }
        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
        assertEquals(threadId, collectionChangedEventData?.moduleId)
        assertEquals("thread", collectionChangedEventData?.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange =
                collectionChangedEventData?.items!!
        }
        assertEquals(1, collectionItemsChange.size)
        assertEquals(message, collectionItemsChange[0].itemId)
        assertEquals("create", collectionItemsChange[0].action)


        // update message
        assertDoesNotFail {
            threadApi.updateMessage(
                message!!,
                "new meta".encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "edit".encodeToByteArray()
            )
            event = expectAndVerifyEvent("collectionChanged")!!
        }

        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event?.data as CollectionChangedEventData?
        }
        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
        assertEquals(threadId, collectionChangedEventData?.moduleId)
        assertEquals("thread", collectionChangedEventData?.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange =
                collectionChangedEventData?.items!!
        }
        assertEquals(1, collectionItemsChange.size)
        assertEquals(message, collectionItemsChange[0].itemId)
        assertEquals("update", collectionItemsChange[0].action)


        // delete message
        assertDoesNotFail {
            threadApi.deleteMessage(message!!)
            event = expectAndVerifyEvent("collectionChanged")!!
        }

        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event?.data as CollectionChangedEventData?
        }
        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
        assertEquals(threadId, collectionChangedEventData?.moduleId)
        assertEquals("thread", collectionChangedEventData?.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange =
                collectionChangedEventData?.items!!
        }
        assertEquals(1, collectionItemsChange.size)
        assertEquals(message, collectionItemsChange[0].itemId)
        assertEquals("delete", collectionItemsChange[0].action)


        // unsubscribe
        threadApi.unsubscribeFrom(subscriptionIds)

        threadApi.close()
    }

    @Test
    fun getEvent_StoreCreated_ContextId() {
        val storeApi = StoreApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        // subscribe and create thread
        subscriptionIds = storeApi.subscribeFor(
            listOf(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.STORE_CREATE,
                    StoreEventSelectorType.CONTEXT_ID,
                    context2Id!!
                )
            )
        )

        assertDoesNotFail {
            storeApi.createStore(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
            event = expectAndVerifyEvent("storeCreated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        storeApi.unsubscribeFrom(subscriptionIds)

        storeApi.close()
    }

    @Test
    fun getEvent_StoreUpdated_ContextId() {
        val storeApi = StoreApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = store3Id

        // subscribe and create thread
        subscriptionIds = storeApi.subscribeFor(
            listOf(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.STORE_UPDATE,
                    StoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        assertDoesNotFail {
            storeApi.updateStore(
                id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                10,
                true
            )
            event = expectAndVerifyEvent("storeUpdated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        storeApi.unsubscribeFrom(subscriptionIds)

        storeApi.close()
    }

    @Test
    fun getEvent_StoreUpdated_StoreId() {
        val storeApi = StoreApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = store3Id

        // subscribe and create thread
        subscriptionIds = storeApi.subscribeFor(
            listOf(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.STORE_UPDATE,
                    StoreEventSelectorType.STORE_ID,
                    id!!
                )
            )
        )

        assertDoesNotFail {
            storeApi.updateStore(
                id,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                10,
                true
            )
            event = expectAndVerifyEvent("storeUpdated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        storeApi.unsubscribeFrom(subscriptionIds)

        storeApi.close()
    }

    @Test
    fun getEvent_StoreStatsChanged_ContextId() {
        val storeApi = StoreApi(eventsConnection)
        var storeStatsChangedEventData: StoreStatsChangedEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = store3Id
        val id_file = createFile(
            storeApi,
            id!!,
            "file content",
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        val amount: Long = storeApi.getStore(id).filesCount!!

        // subscribe
        subscriptionIds = storeApi.subscribeFor(
            listOf(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.STORE_STATS,
                    StoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        assertDoesNotFail {
            createFile(
                storeApi,
                id,
                "file content",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
            event = expectAndVerifyEvent("storeStatsChanged")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            storeStatsChangedEventData = event.data as StoreStatsChangedEventData?

        }
        assertEquals(id, storeStatsChangedEventData!!.storeId)
        assertEquals(amount + 1, storeStatsChangedEventData!!.filesCount)

        // update message
        assertDoesNotFail {
            updateFile(
                storeApi,
                id_file,
                "new data",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "new data".encodeToByteArray().size.toLong()
            )
            expectNoEventOccurs()
        }

        // unsubscribe
        storeApi.unsubscribeFrom(subscriptionIds)

        storeApi.close()
    }

    @Test
    fun getEvent_StoreStatsChanged_StoreId() {
        val storeApi = StoreApi(eventsConnection)
        var storeStatsChangedEventData: StoreStatsChangedEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = store3Id
        val id_file = createFile(
            storeApi,
            id!!,
            "file content",
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        val amount: Long = storeApi.getStore(id).filesCount!!

        // subscribe
        subscriptionIds = storeApi.subscribeFor(
            listOf(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.STORE_STATS,
                    StoreEventSelectorType.STORE_ID,
                    id
                )
            )
        )

        assertDoesNotFail {
            createFile(
                storeApi,
                id,
                "file content",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
            event = expectAndVerifyEvent("storeStatsChanged")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            storeStatsChangedEventData = event.data as StoreStatsChangedEventData?
        }
        assertEquals(id, storeStatsChangedEventData!!.storeId)
        assertEquals(amount + 1, storeStatsChangedEventData!!.filesCount)

        // update message
        assertDoesNotFail {
            updateFile(
                storeApi,
                id_file,
                "new data",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "new data".encodeToByteArray().size.toLong()
            )

            expectNoEventOccurs()
        }
        // unsubscribe
        storeApi.unsubscribeFrom(subscriptionIds)

        storeApi.close()
    }

    @Test
    fun getEvent_StoreDeleted_ContextId() {
        val storeApi = StoreApi(eventsConnection)
        var storeDeletedEventData: StoreDeletedEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String? = storeApi.createStore(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        // subscribe
        subscriptionIds = storeApi.subscribeFor(
            listOf(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.STORE_DELETE,
                    StoreEventSelectorType.CONTEXT_ID,
                    context2Id!!
                )
            )
        )

        // delete thread
        assertDoesNotFail {
            storeApi.deleteStore(id!!)
            event = expectAndVerifyEvent("storeDeleted")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            storeDeletedEventData =
                event.data as StoreDeletedEventData?

        }
        assertEquals(id, storeDeletedEventData!!.storeId)

        // unsubscribe
        storeApi.unsubscribeFrom(subscriptionIds)

        storeApi.close()
    }

    @Test
    fun getEvent_StoreDeleted_StoreId() {
        val storeApi = StoreApi(eventsConnection)
        var storeDeletedEventData: StoreDeletedEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String? = storeApi.createStore(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        // subscribe
        subscriptionIds = storeApi.subscribeFor(
            listOf(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.STORE_DELETE,
                    StoreEventSelectorType.STORE_ID,
                    id!!
                )
            )
        )

        // delete thread
        assertDoesNotFail {
            storeApi.deleteStore(id)
            event = expectAndVerifyEvent("storeDeleted")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            storeDeletedEventData =
                event.data as StoreDeletedEventData?

        }
        assertEquals(id, storeDeletedEventData!!.storeId)

        // unsubscribe
        storeApi.unsubscribeFrom(subscriptionIds)

        storeApi.close()
    }

    @Test
    fun getEvent_StoreFileCreated_ContextId() {
        val storeApi = StoreApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = store3Id

        // subscribe
        subscriptionIds = storeApi.subscribeFor(
            listOf(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.FILE_CREATE,
                    StoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        // create file
        assertDoesNotFail {
            createFile(
                storeApi,
                id!!,
                "file created",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
            event = expectAndVerifyEvent("storeFileCreated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        storeApi.unsubscribeFrom(subscriptionIds)

        storeApi.close()
    }

    @Test
    fun getEvent_StoreFileCreated_StoreId() {
        val storeApi = StoreApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = store3Id

        // subscribe
        subscriptionIds = storeApi.subscribeFor(
            listOf(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.FILE_CREATE,
                    StoreEventSelectorType.STORE_ID,
                    id!!
                )
            )
        )

        // create file
        assertDoesNotFail {
            createFile(
                storeApi,
                id,
                "file created",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
            event = expectAndVerifyEvent("storeFileCreated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        storeApi.unsubscribeFrom(subscriptionIds)

        storeApi.close()
    }

    @Test
    fun getEvent_StoreFileUpdated_ContextId() {
        val storeApi = StoreApi(eventsConnection)
        var storeFileUpdatedEventData: StoreFileUpdatedEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = file2Id

        // subscribe
        subscriptionIds = storeApi.subscribeFor(
            listOf(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.FILE_UPDATE,
                    StoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        // update file
        assertDoesNotFail {
            updateFile(
                storeApi,
                id!!,
                "new file content",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "new file content".encodeToByteArray().size.toLong()
            )
            event = expectAndVerifyEvent("storeFileUpdated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            storeFileUpdatedEventData =
                event.data as StoreFileUpdatedEventData

        }
        assertEquals(id, storeFileUpdatedEventData!!.file.info.fileId)

        // update file meta
        assertDoesNotFail {
            storeApi.updateFileMeta(
                id!!,
                "new public meta".encodeToByteArray(),
                "new private meta".encodeToByteArray()
            )
            event = expectAndVerifyEvent("storeFileUpdated")!!
        }

        // unsubscribe
        storeApi.unsubscribeFrom(subscriptionIds)

        storeApi.close()
    }

    @Test
    fun getEvent_StoreFileUpdated_StoreId() {
        val storeApi = StoreApi(eventsConnection)
        var storeFileUpdatedEventData: StoreFileUpdatedEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id_store: String = store1Id
        val id: String = file2Id

        // subscribe
        subscriptionIds = storeApi.subscribeFor(
            listOf(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.FILE_UPDATE,
                    StoreEventSelectorType.STORE_ID,
                    id_store!!
                )
            )
        )

        // create file
        assertDoesNotFail {
            updateFile(
                storeApi,
                id!!,
                "new content",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "new content".encodeToByteArray().size.toLong()
            )
            event = expectAndVerifyEvent("storeFileUpdated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            storeFileUpdatedEventData =
                event.data as StoreFileUpdatedEventData?
        }
        assertEquals(id, storeFileUpdatedEventData!!.file.info.fileId)

        // unsubscribe
        storeApi.unsubscribeFrom(subscriptionIds)

        storeApi.close()
    }

    @Test
    fun getEvent_StoreFileUpdated_FileId() {
        val storeApi = StoreApi(eventsConnection)
        var storeFileUpdatedEventData: StoreFileUpdatedEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = file2Id

        // subscribe
        subscriptionIds = storeApi.subscribeFor(
            listOf(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.FILE_UPDATE,
                    StoreEventSelectorType.FILE_ID,
                    id!!
                )
            )
        )

        // create file
        assertDoesNotFail {
            updateFile(
                storeApi,
                id,
                "new file content",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "new file content".encodeToByteArray().size.toLong()
            )
            event = expectAndVerifyEvent("storeFileUpdated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            storeFileUpdatedEventData =
                event.data as StoreFileUpdatedEventData?
        }
        assertEquals(id, storeFileUpdatedEventData!!.file.info.fileId)

        // unsubscribe
        storeApi.unsubscribeFrom(subscriptionIds)

        storeApi.close()
    }

    @Test
    fun getEvent_StoreFileDeleted_ContextId() {
        val storeApi = StoreApi(eventsConnection)
        var storeFileDeletedEventData: StoreFileDeletedEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id_store: String = store3Id
        val id = createFile(
            storeApi,
            id_store!!,
            "file content",
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        // subscribe
        subscriptionIds = storeApi.subscribeFor(
            listOf(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.FILE_DELETE,
                    StoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        // create file
        assertDoesNotFail {
            storeApi.deleteFile(id)
            event = expectAndVerifyEvent("storeFileDeleted")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            storeFileDeletedEventData =
                event.data as StoreFileDeletedEventData?
        }
        assertEquals(contextId, storeFileDeletedEventData!!.contextId)
        assertEquals(id_store, storeFileDeletedEventData!!.storeId)
        assertEquals(id, storeFileDeletedEventData!!.fileId)

        // unsubscribe
        storeApi.unsubscribeFrom(subscriptionIds)

        storeApi.close()
    }

    @Test
    fun getEvent_StoreFileDeleted_StoreId() {
        val storeApi = StoreApi(eventsConnection)
        var storeFileDeletedEventData: StoreFileDeletedEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id_store: String = store3Id
        val id = createFile(
            storeApi,
            id_store!!,
            "file content",
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        // subscribe
        subscriptionIds = storeApi.subscribeFor(
            listOf(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.FILE_DELETE,
                    StoreEventSelectorType.STORE_ID,
                    id_store
                )
            )
        )

        // create file
        assertDoesNotFail {
            storeApi.deleteFile(id)
            event = expectAndVerifyEvent("storeFileDeleted")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            storeFileDeletedEventData =
                event.data as StoreFileDeletedEventData?
        }
        assertEquals(contextId, storeFileDeletedEventData!!.contextId)
        assertEquals(id_store, storeFileDeletedEventData!!.storeId)
        assertEquals(id, storeFileDeletedEventData!!.fileId)

        // unsubscribe
        storeApi.unsubscribeFrom(subscriptionIds)

        storeApi.close()
    }

    @Test
    fun getEvent_StoreFileDeleted_FileId() {
        val storeApi = StoreApi(eventsConnection)
        var storeFileDeletedEventData: StoreFileDeletedEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id_store: String = store3Id
        val id = createFile(
            storeApi,
            id_store!!,
            "file content",
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        // subscribe
        subscriptionIds = storeApi.subscribeFor(
            listOf(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.FILE_DELETE,
                    StoreEventSelectorType.FILE_ID,
                    id
                )
            )
        )

        // create file
        assertDoesNotFail {
            storeApi.deleteFile(id)
            event = expectAndVerifyEvent("storeFileDeleted")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            storeFileDeletedEventData =
                event.data as StoreFileDeletedEventData?
        }
        assertEquals(contextId, storeFileDeletedEventData!!.contextId)
        assertEquals(id_store, storeFileDeletedEventData!!.storeId)
        assertEquals(id, storeFileDeletedEventData!!.fileId)

        // unsubscribe
        storeApi.unsubscribeFrom(subscriptionIds)

        storeApi.close()
    }

    fun catchStoreFileUpdatedEvent(
        storeApi: StoreApi,
        fileId: String,
        fileContent: ByteArray,
        pos: Long?,
        truncate: Boolean
    ): List<FileChange> {
        var pos = pos
        val file = storeApi.getFile(fileId)
        var event: Event<*>? = null
        lateinit var storeFileUpdatedEventData: StoreFileUpdatedEventData

        // if pos == null -> gets the end of a file
        if (pos == null) pos = file.size

        assertDoesNotFail {
            writeToFileOnPosition(
                storeApi,
                fileId,
                pos!!,
                fileContent,
                truncate
            )
            event = expectAndVerifyEvent("storeFileUpdated")!!
        }

        assertDoesNotFail {
            storeFileUpdatedEventData = event!!.data as StoreFileUpdatedEventData
        }

        val list: List<FileChange> = storeFileUpdatedEventData.changes
        return list
    }

    @Test
    @Throws(Exception::class)
    fun storeFileChanges() {
        val storeApi = StoreApi(eventsConnection)
        val id_store: String = store3Id
        val subscriptionIds: List<String>
        var fileChange: FileChange
        var fileChanges: List<FileChange?>

        val baseFileContent = ByteArray(0)

        // one full block = 128 * 1024
        val fullBlock = ByteArray(128 * 1024) { 1.toByte() }

        // half of a block
        val halfBlock = ByteArray(128 * 512) { 2.toByte() }

        // one and a half of block
        val content1 = ByteArray(128 * 1536) { 5.toByte() }

        var result: ByteArray?

        // create file
        val handle: Long = storeApi.createFile(
            id_store!!,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            baseFileContent.size.toLong(),
            true
        )!!
        storeApi.writeToFile(handle, baseFileContent)
        val id_file: String? = storeApi.closeFile(handle)

        // subscribe
        subscriptionIds = storeApi.subscribeFor(
            listOf(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.FILE_UPDATE,
                    StoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )


        // 1 full block + 0,5 block

        // size = 128 * 1024 (full block #1)
        fileChanges = catchStoreFileUpdatedEvent(
            storeApi,
            id_file!!,
            fullBlock,
            null,
            false
        )
        assertEquals(1, fileChanges.size)
        fileChange = fileChanges[0]
        assertEquals(0, fileChange.pos)
        assertEquals(fullBlock.size.toLong(), fileChange.length)
        assertFalse(fileChange.truncate)
        // compare
        assertContentEquals(
            fullBlock,
            readFromFile(storeApi, id_file, fileChange.pos, fileChange.length!!)
        )

        // size = 128 * 512 (start new block #2)
        fileChanges = catchStoreFileUpdatedEvent(
            storeApi,
            id_file,
            halfBlock,
            null,
            false
        )
        assertEquals(1, fileChanges.size)
        fileChange = fileChanges[0]
        assertEquals(fullBlock.size.toLong(), fileChange.pos)
        assertEquals(halfBlock.size.toLong(), fileChange.length)
        assertFalse(fileChange.truncate)
        // compare
        assertContentEquals(
            halfBlock,
            readFromFile(storeApi, id_file, fileChange.pos, fileChange.length!!)
        )


        // 1,5 block (old content) + 1 full block

        // on the border of the blocks (fill block #2 to the end and start block #3)
        fileChanges = catchStoreFileUpdatedEvent(
            storeApi,
            id_file,
            fullBlock,
            null,
            false
        )
        assertEquals(1, fileChanges.size.toLong())
        fileChange = fileChanges[0]
        assertEquals(fullBlock.size.toLong(), fileChange.pos)   // the beginning of #2
        assertEquals(
            halfBlock.size.toLong() + fullBlock.size.toLong(),    // from the beginning of #2: old content(0,5 block) + new content(1 block)
            fileChange.length
        )
        assertFalse(fileChange.truncate)
        // compare
        result = halfBlock + fullBlock
        assertContentEquals(
            result,
            readFromFile(storeApi, id_file, fileChange.pos, fileChange.length!!)
        )


        // 1,5 block + 0,5 block

        // create new block #1 and start #2
        fileChanges = catchStoreFileUpdatedEvent(
            storeApi,
            id_file,
            content1,
            0L,
            true
        )
        assertEquals(1, fileChanges.size)
        fileChange = fileChanges[0]
        assertEquals(0, fileChange.pos)
        assertEquals(fullBlock.size.toLong() + halfBlock.size.toLong(), fileChange.length)
        assertTrue(fileChange.truncate)
        // compare
        assertContentEquals(
            content1,
            readFromFile(storeApi, id_file, fileChange.pos, fileChange.length!!)
        )

        // fill block #2
        fileChanges = catchStoreFileUpdatedEvent(
            storeApi,
            id_file,
            halfBlock,
            null,
            false
        )
        assertEquals(1, fileChanges.size)
        fileChange = fileChanges[0]
        assertEquals(fullBlock.size.toLong(), fileChange.pos) // the beginning of #2
        assertEquals(
            halfBlock.size.toLong() + halfBlock.size.toLong(),  // from the beginning of #2: old content(0,5 block) + new content(0,5 block)
            fileChange.length
        )
        assertFalse(fileChange.truncate)
        // compare
        assertContentEquals(
            ByteArray(halfBlock.size) { content1[0] } + ByteArray(halfBlock.size) { halfBlock[0] },
            readFromFile(storeApi, id_file, fileChange.pos, fileChange.length!!)
        )


        // 0,5 block + 0,5 block
        fileChanges = catchStoreFileUpdatedEvent(
            storeApi,
            id_file,
            halfBlock,
            halfBlock.size.toLong(),
            true
        )
        assertEquals(1, fileChanges.size)
        fileChange = fileChanges[0]
        assertEquals(0, fileChange.pos)
        assertEquals(fullBlock.size.toLong(), fileChange.length)
        assertTrue(fileChange.truncate)
        // compare
        assertContentEquals(
            ByteArray(halfBlock.size) { content1[0] } + ByteArray(halfBlock.size) { halfBlock[0] },
            readFromFile(storeApi, id_file, fileChange.pos, fileChange.length!!)
        )


        // unsubscribe
        storeApi.unsubscribeFrom(subscriptionIds)

        storeApi.close()
    }

    //TODO: Not working
//    @Test
//    @Throws(Exception::class)
//    fun getEvent_StoreCollectionChanged_ContextId() {
//        val storeApi = StoreApi(eventsConnection)
//        var collectionChangedEventData: CollectionChangedEventData? = null
//        var collectionItemsChange: List<CollectionItemChange> = emptyList()
//        var event: Event<*>? = null
//        val subscriptionIds: List<String>
//
//        var file: String? = null
//        val storeId: String = store3Id
//
//        // subscribe
//        subscriptionIds = storeApi.subscribeFor(
//            listOf(
//                storeApi.buildSubscriptionQuery(
//                    StoreEventType.COLLECTION_CHANGE,
//                    StoreEventSelectorType.CONTEXT_ID,
//                    contextId!!
//                )
//            )
//        )
//
//
//        // create file
//        assertDoesNotFail {
//            file = createFile(
//                storeApi,
//                storeId!!,
//                "file content",
//                publicMeta.encodeToByteArray(),
//                privateMeta.encodeToByteArray()
//            )
//            event = expectAndVerifyEvent("collectionChanged")!!
//        }
//
//
//        // CollectionChangedEventData
//        assertDoesNotFail {
//            collectionChangedEventData =
//                event?.data as CollectionChangedEventData?
//        }
//        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
//        assertEquals(storeId, collectionChangedEventData?.moduleId)
//        assertEquals("store", collectionChangedEventData?.moduleType)
//
//        // CollectionItemChange
//        assertDoesNotFail {
//            collectionItemsChange =
//                collectionChangedEventData?.items!!
//        }
//
//        assertEquals(1, collectionItemsChange.size)
//        assertEquals(file, collectionItemsChange[0].itemId)
//        assertEquals("create", collectionItemsChange[0].action)
//
//
//        // update file
//        assertDoesNotFail {
//            updateFile(
//                storeApi,
//                file!!,
//                "edit",
//                "new meta".encodeToByteArray(),
//                privateMeta.encodeToByteArray(),
//                "edit".encodeToByteArray().size.toLong()
//            )
//            event = expectAndVerifyEvent("collectionChanged")!!
//        }
//
//        // CollectionChangedEventData
//        assertDoesNotFail {
//            collectionChangedEventData =
//                event?.data as CollectionChangedEventData?
//        }
//        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
//        assertEquals(storeId, collectionChangedEventData?.moduleId)
//        assertEquals("store", collectionChangedEventData?.moduleType)
//
//        // CollectionItemChange
//        assertDoesNotFail {
//            collectionItemsChange =
//                collectionChangedEventData?.items!!
//        }
//        assertEquals(1, collectionItemsChange.size)
//        assertEquals(file, collectionItemsChange[0].itemId)
//        assertEquals("update", collectionItemsChange[0].action)
//
//
//        // delete file
//        assertDoesNotFail {
//            storeApi.deleteFile(file!!)
//            event = expectAndVerifyEvent("collectionChanged")!!
//        }
//
//        // CollectionChangedEventData
//        assertDoesNotFail {
//            collectionChangedEventData =
//                event?.data as CollectionChangedEventData?
//        }
//
//        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
//        assertEquals(storeId, collectionChangedEventData?.moduleId)
//        assertEquals("store", collectionChangedEventData?.moduleType)
//
//        // CollectionItemChange
//        assertDoesNotFail {
//            collectionItemsChange = collectionChangedEventData?.items!!
//        }
//        assertEquals(1, collectionItemsChange.size)
//        assertEquals(file, collectionItemsChange[0].itemId)
//        assertEquals("delete", collectionItemsChange[0].action)
//
//
//        // unsubscribe
//        storeApi.unsubscribeFrom(subscriptionIds)
//
//        storeApi.close()
//    }

    @Test
    @Throws(Exception::class)
    fun getEvent_StoreCollectionChanged_StoreId() {
        val storeApi = StoreApi(eventsConnection)
        var collectionChangedEventData: CollectionChangedEventData? = null
        var collectionItemsChange: List<CollectionItemChange> = emptyList()
        var event: (Event<*>?) = null
        val subscriptionIds: List<String?>?

        var file: String? = null
        val storeId: String = store3Id

        // subscribe
        subscriptionIds = storeApi.subscribeFor(
            listOf(
                storeApi.buildSubscriptionQuery(
                    StoreEventType.COLLECTION_CHANGE,
                    StoreEventSelectorType.STORE_ID,
                    storeId!!
                )
            )
        )


        // create file
        assertDoesNotFail {
            file = createFile(
                storeApi,
                storeId,
                "file content",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
            event = expectAndVerifyEvent("collectionChanged")!!
        }

        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event?.data as CollectionChangedEventData?
        }

        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
        assertEquals(storeId, collectionChangedEventData?.moduleId)
        assertEquals("store", collectionChangedEventData?.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange =
                collectionChangedEventData?.items!!
        }
        assertEquals(1, collectionItemsChange.size)
        assertEquals(file, collectionItemsChange[0].itemId)
        assertEquals("create", collectionItemsChange[0].action)


        // update file
        assertDoesNotFail {
            updateFile(
                storeApi,
                file!!,
                "edit",
                "new meta".encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "edit".encodeToByteArray().size.toLong()
            )
            event = expectAndVerifyEvent("collectionChanged")
        }

        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event?.data as CollectionChangedEventData?
        }
        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
        assertEquals(storeId, collectionChangedEventData?.moduleId)
        assertEquals("store", collectionChangedEventData?.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange =
                collectionChangedEventData?.items!!
        }
        assertEquals(1, collectionItemsChange.size)
        assertEquals(file, collectionItemsChange[0].itemId)
        assertEquals("update", collectionItemsChange[0].action)


        // delete file
        assertDoesNotFail {
            storeApi.deleteFile(file!!)
            event = expectAndVerifyEvent("collectionChanged")
        }

        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event?.data as CollectionChangedEventData?
        }
        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
        assertEquals(storeId, collectionChangedEventData?.moduleId)
        assertEquals("store", collectionChangedEventData?.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange = collectionChangedEventData?.items!!
        }
        assertEquals(1, collectionItemsChange.size)
        assertEquals(file, collectionItemsChange[0].itemId)
        assertEquals("delete", collectionItemsChange[0].action)

        // unsubscribe
        storeApi.unsubscribeFrom(subscriptionIds)

        storeApi.close()
    }

    @Test
    fun getEvent_InboxCreated_ContextId() {
        val inboxApi: InboxApi = InboxApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        // subscribe
        subscriptionIds = inboxApi.subscribeFor(
            listOf(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.INBOX_CREATE,
                    InboxEventSelectorType.CONTEXT_ID,
                    context2Id!!
                )
            )
        )

        // create file
        assertDoesNotFail {
            inboxApi.createInbox(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
            event = expectAndVerifyEvent("inboxCreated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0]);
        assertNotNull(event.timestamp)

        // unsubscribe
        inboxApi.unsubscribeFrom(subscriptionIds)

        inboxApi.close()
    }

    @Test
    fun getEvent_InboxUpdated_ContextId() {
        val inboxApi: InboxApi = InboxApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = inbox3Id

        // subscribe
        subscriptionIds = inboxApi.subscribeFor(
            listOf(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.INBOX_UPDATE,
                    InboxEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        // create file
        assertDoesNotFail {
            inboxApi.updateInbox(
                id,
                users,
                users,
                publicMeta.encodeToByteArray(),
                "new privateMeta".encodeToByteArray(),
                null,
                2,
                true
            )
            event = expectAndVerifyEvent("inboxUpdated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0]);
        assertNotNull(event.timestamp)

        // unsubscribe
        inboxApi.unsubscribeFrom(subscriptionIds)

        inboxApi.close()
    }

    @Test
    fun getEvent_InboxUpdated_InboxId() {
        val inboxApi: InboxApi = InboxApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = inbox3Id

        // subscribe
        subscriptionIds = inboxApi.subscribeFor(
            listOf(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.INBOX_UPDATE,
                    InboxEventSelectorType.INBOX_ID,
                    id
                )
            )
        )

        // create file
        assertDoesNotFail {
            inboxApi.updateInbox(
                id,
                users,
                users,
                publicMeta.encodeToByteArray(),
                "new privateMeta".encodeToByteArray(),
                null,
                2,
                true
            )
            event = expectAndVerifyEvent("inboxUpdated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0]);
        assertNotNull(event.timestamp)

        // unsubscribe
        inboxApi.unsubscribeFrom(subscriptionIds)

        inboxApi.close()
    }

    @Test
    fun getEvent_InboxDeleted_ContextId() {
        val inboxApi: InboxApi = InboxApi(eventsConnection)
        var inboxDeletedEventData: InboxDeletedEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = inboxApi.createInbox(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        // subscribe
        subscriptionIds = inboxApi.subscribeFor(
            listOf(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.INBOX_DELETE,
                    InboxEventSelectorType.CONTEXT_ID,
                    context2Id!!
                )
            )
        )

        assertDoesNotFail {
            inboxApi.deleteInbox(id)
            event = expectAndVerifyEvent("inboxDeleted")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            inboxDeletedEventData = event.data as InboxDeletedEventData?
        }
        assertEquals(id, inboxDeletedEventData!!.inboxId)

        // unsubscribe
        inboxApi.unsubscribeFrom(subscriptionIds)

        inboxApi.close()
    }

    @Test
    fun getEvent_InboxDeleted_InboxId() {
        val inboxApi: InboxApi = InboxApi(eventsConnection)
        var inboxDeletedEventData: InboxDeletedEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = inboxApi.createInbox(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )

        // subscribe
        subscriptionIds = inboxApi.subscribeFor(
            listOf(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.INBOX_DELETE,
                    InboxEventSelectorType.INBOX_ID,
                    id
                )
            )
        )

        assertDoesNotFail {
            inboxApi.deleteInbox(id)
            event = expectAndVerifyEvent("inboxDeleted")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            inboxDeletedEventData = event.data as InboxDeletedEventData?
        }
        assertEquals(id, inboxDeletedEventData!!.inboxId)

        // unsubscribe
        inboxApi.unsubscribeFrom(subscriptionIds)

        inboxApi.close()
    }

    fun createNewEntryWithFile(
        inboxApi: InboxApi,
        inboxId: String?,
        data: String,
        dataFile: String
    ): InboxEntry {
        val handle: Long = inboxApi.createFileHandle(
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            dataFile.encodeToByteArray().size.toLong()
        )!!
        val entryHandle: Long =
            inboxApi.prepareEntry(inboxId!!, data.encodeToByteArray(), listOf(handle))!!
        inboxApi.writeToFile(entryHandle, handle, dataFile.encodeToByteArray())
        inboxApi.sendEntry(entryHandle)

        return inboxApi.listEntries(inboxId, 0, 1, "desc").readItems[0]
    }

    @Test
    fun getEvent_InboxEntryCreated_ContextId() {
        val inboxApi = InboxApi(eventsConnection)
        lateinit var event: Event<*>
        var subscriptionIds: List<String>

        val id: String = inbox3Id

        // subscribe
        subscriptionIds = inboxApi.subscribeFor(
            listOf(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.ENTRY_CREATE,
                    InboxEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        assertDoesNotFail {
            createNewEntryWithFile(
                inboxApi,
                id,
                "inbox data",
                "file data"
            )
            event = expectAndVerifyEvent("inboxEntryCreated")!!
        }
        assertEquals(1, event.subscriptions.size);
        assertEquals(subscriptionIds[0], event.subscriptions[0]);
        assertNotNull(event.timestamp)

        // unsubscribe
        inboxApi.unsubscribeFrom(subscriptionIds)

        inboxApi.close()
    }

    @Test
    fun getEvent_InboxEntryCreated_InboxId() {
        val inboxApi = InboxApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = inbox3Id

        // subscribe
        subscriptionIds = inboxApi.subscribeFor(
            listOf(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.ENTRY_CREATE,
                    InboxEventSelectorType.INBOX_ID,
                    id!!
                )
            )
        )

        assertDoesNotFail {
            createNewEntryWithFile(
                inboxApi,
                id,
                "inbox data",
                "file data"
            )
            event = expectAndVerifyEvent("inboxEntryCreated")!!
        }
        assertEquals(1, event.subscriptions.size);
        assertEquals(subscriptionIds[0], event.subscriptions[0]);
        assertNotNull(event.timestamp)

        // unsubscribe
        inboxApi.unsubscribeFrom(subscriptionIds)

        inboxApi.close()
    }

    @Test
    fun getEvent_InboxEntryDeleted_ContextId() {
        val inboxApi = InboxApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id_inbox: String = inbox3Id
        val entry: InboxEntry =
            createNewEntryWithFile(inboxApi, id_inbox, "data inbox", "file data")
        val id: String? = entry.entryId

        // subscribe
        subscriptionIds = inboxApi.subscribeFor(
            listOf(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.ENTRY_DELETE,
                    InboxEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        assertDoesNotFail {
            inboxApi.deleteEntry(id!!)
            event = expectAndVerifyEvent("inboxEntryDeleted")!!
        }
        assertEquals(1, event.subscriptions.size);
        assertEquals(subscriptionIds[0], event.subscriptions[0]);
        assertNotNull(event.timestamp)

        // unsubscribe
        inboxApi.unsubscribeFrom(subscriptionIds)

        inboxApi.close()
    }

    @Test
    fun getEvent_InboxEntryDeleted_InboxId() {
        val inboxApi = InboxApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id_inbox: String = inbox3Id
        val entry: InboxEntry =
            createNewEntryWithFile(inboxApi, id_inbox, "data inbox", "file data")
        val id: String? = entry.entryId


        // subscribe
        subscriptionIds = inboxApi.subscribeFor(
            listOf(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.ENTRY_DELETE,
                    InboxEventSelectorType.INBOX_ID,
                    id_inbox!!
                )
            )
        )

        assertDoesNotFail {
            inboxApi.deleteEntry(id!!)
            event = expectAndVerifyEvent("inboxEntryDeleted")!!
        }
        assertEquals(1, event.subscriptions.size);
        assertEquals(subscriptionIds[0], event.subscriptions[0]);
        assertNotNull(event.timestamp)

        // unsubscribe
        inboxApi.unsubscribeFrom(subscriptionIds)

        inboxApi.close()
    }

    @Test
    fun getEvent_InboxEntryDeleted_EntryId() {
        val inboxApi = InboxApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id_inbox: String = inbox3Id
        val entry: InboxEntry =
            createNewEntryWithFile(inboxApi, id_inbox, "data inbox", "file data")
        val id: String? = entry.entryId

        // subscribe
        subscriptionIds = inboxApi.subscribeFor(
            listOf(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.ENTRY_DELETE,
                    InboxEventSelectorType.ENTRY_ID,
                    id!!
                )
            )
        )

        assertDoesNotFail {
            inboxApi.deleteEntry(id)
            event = expectAndVerifyEvent("inboxEntryDeleted")!!
        }
        assertEquals(1, event.subscriptions.size);
        assertEquals(subscriptionIds[0], event.subscriptions[0]);
        assertNotNull(event.timestamp)

        // unsubscribe
        inboxApi.unsubscribeFrom(subscriptionIds)

        inboxApi.close()
    }

    @Test
    @Throws(Exception::class)
    fun getEvent_InboxCollectionChanged_ContextId() {
        val inboxApi = InboxApi(eventsConnection)
        var collectionChangedEventData: CollectionChangedEventData? = null
        var collectionItemsChange: List<CollectionItemChange> = emptyList()
        var event: Event<*>? = null
        val subscriptionIds: List<String>

        var entry: String? = null
        val inboxId: String = inbox3Id

        // subscribe
        subscriptionIds = inboxApi.subscribeFor(
            listOf(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.COLLECTION_CHANGE,
                    InboxEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )


        // send entry
        assertDoesNotFail {
            val handle: Long = inboxApi.prepareEntry(inboxId!!, "data 1".encodeToByteArray())!!
            inboxApi.sendEntry(handle)
            event = expectAndVerifyEvent("collectionChanged")!!
        }

        entry = inboxApi.listEntries(inboxId!!, 0, 1, "desc").readItems[0].entryId


        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event!!.data as CollectionChangedEventData?
        }
        assertEquals(1, collectionChangedEventData!!.affectedItemsCount)
        // todo - will be fixed after lib fix (reported)
//        assertEquals(inboxId, collectionChangedEventData!!.moduleId)
        assertEquals("inbox", collectionChangedEventData!!.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange =
                collectionChangedEventData!!.items
        }
        assertEquals(1, collectionItemsChange.size)
        assertEquals(entry, collectionItemsChange[0].itemId)
        assertEquals("create", collectionItemsChange[0].action)


        // delete entry
        assertDoesNotFail {
            inboxApi.deleteEntry(entry)
            event = expectAndVerifyEvent("collectionChanged")!!
        }

        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event?.data as CollectionChangedEventData?
        }
        assertEquals(1, collectionChangedEventData!!.affectedItemsCount)
        // todo - will be fixed after lib fix (reported)
//        assertEquals(inboxId, collectionChangedEventData!!.moduleId)
        assertEquals("inbox", collectionChangedEventData!!.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange =
                collectionChangedEventData!!.items
        }
        assertEquals(1, collectionItemsChange.size)
        assertEquals(entry, collectionItemsChange[0].itemId)
        assertEquals("delete", collectionItemsChange[0].action)

        // unsubscribe
        inboxApi.unsubscribeFrom(subscriptionIds)

        inboxApi.close()
    }

    // todo - cannot catch event - will be uncommented after lib fix
    /*
    @Test
    @Throws(Exception::class)
    fun getEvent_InboxCollectionChanged_InboxId() {
        val inboxApi = InboxApi(eventsConnection)
        var collectionChangedEventData: CollectionChangedEventData? = null
        var collectionItemsChange: List<CollectionItemChange> = emptyList()
        var event: Event<*>? = null
        val subscriptionIds: List<String>

        var entry: String? = null
        val inboxId: String = inbox3Id

        // subscribe
        subscriptionIds = inboxApi.subscribeFor(
            listOf(
                inboxApi.buildSubscriptionQuery(
                    InboxEventType.COLLECTION_CHANGE,
                    InboxEventSelectorType.INBOX_ID,
                    inboxId!!
                )
            )
        )


        // send entry
        assertDoesNotFail {
            val handle: Long = inboxApi.prepareEntry(inboxId!!, "data 1".encodeToByteArray())!!
            inboxApi.sendEntry(handle)
            event = expectAndVerifyEvent("collectionChanged")!!
        }

        entry = inboxApi.listEntries(inboxId, 0, 1, "desc").readItems[0].entryId


        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event!!.data as CollectionChangedEventData?
        }
        assertEquals(1, collectionChangedEventData!!.affectedItemsCount)
        // todo - will be fixed after lib fix (reported)
//        assertEquals(inboxId, collectionChangedEventData!!.moduleId)
        assertEquals("inbox", collectionChangedEventData!!.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange =
                collectionChangedEventData!!.items
        }
        assertEquals(1, collectionItemsChange.size)
        assertEquals(entry, collectionItemsChange[0].itemId)
        assertEquals("create", collectionItemsChange[0].action)


        // delete entry
        assertDoesNotFail {
            inboxApi.deleteEntry(entry)!!
            event = expectAndVerifyEvent("collectionChanged")!!
        }

        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event?.data as CollectionChangedEventData?
        }
        assertEquals(1, collectionChangedEventData!!.affectedItemsCount)
        // todo - will be fixed after lib fix (reported)
//        assertEquals(inboxId, collectionChangedEventData!!.moduleId)
        assertEquals("inbox", collectionChangedEventData!!.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange =
                collectionChangedEventData!!.items
        }
        assertEquals(1, collectionItemsChange.size)
        assertEquals(entry, collectionItemsChange[0].itemId)
        assertEquals("delete", collectionItemsChange[0].action)

        // unsubscribe
        inboxApi.unsubscribeFrom(subscriptionIds)

        inboxApi.close()
    }
    */

    @Test
    fun getEvent_KvdbCreated_ContextId() {
        val kvdbApi: KvdbApi = KvdbApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        // subscribe
        subscriptionIds = kvdbApi.subscribeFor(
            listOf(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.KVDB_CREATE,
                    KvdbEventSelectorType.CONTEXT_ID,
                    context2Id!!
                )
            )
        )

        assertDoesNotFail {
            kvdbApi.createKvdb(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray()
            )
            event = expectAndVerifyEvent("kvdbCreated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        kvdbApi.unsubscribeFrom(subscriptionIds)

        kvdbApi.close()
    }

    @Test
    fun getEvent_KvdbUpdated_ContextId() {
        val kvdbApi: KvdbApi = KvdbApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = kvdb3Id

        // subscribe
        subscriptionIds = kvdbApi.subscribeFor(
            listOf(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.KVDB_UPDATE,
                    KvdbEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        assertDoesNotFail {
            kvdbApi.updateKvdb(
                id,
                users,
                users,
                publicMeta.encodeToByteArray(),
                "new privateMeta".encodeToByteArray(),
                3,
                true
            )
            event = expectAndVerifyEvent("kvdbUpdated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        kvdbApi.unsubscribeFrom(subscriptionIds)

        kvdbApi.close()
    }

    @Test
    fun getEvent_KvdbUpdated_KvdbId() {

        val kvdbApi: KvdbApi = KvdbApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id: String = kvdb3Id

        // subscribe
        subscriptionIds = kvdbApi.subscribeFor(
            listOf(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.KVDB_UPDATE,
                    KvdbEventSelectorType.KVDB_ID,
                    id
                )
            )
        )

        assertDoesNotFail {
            kvdbApi.updateKvdb(
                id,
                users,
                users,
                publicMeta.encodeToByteArray(),
                "new privateMeta".encodeToByteArray(),
                3,
                true
            )
            event = expectAndVerifyEvent("kvdbUpdated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        kvdbApi.unsubscribeFrom(subscriptionIds)

        kvdbApi.close()
    }

    @Test
    fun getEvent_KvdbStatsChanged_ContextId() {
        val kvdbApi: KvdbApi = KvdbApi(eventsConnection)
        var kvdbStatsEventData: KvdbStatsEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id_kvdb: String = kvdb3Id
        val amount: Long = kvdbApi.getKvdb(id_kvdb).entries!!

        // subscribe
        subscriptionIds = kvdbApi.subscribeFor(
            listOf(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.KVDB_STATS,
                    KvdbEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        assertDoesNotFail {
            kvdbApi.setEntry(
                id_kvdb,
                "kvdb_key_context",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
            event = expectAndVerifyEvent("kvdbStatsChanged")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            kvdbStatsEventData = event.data as KvdbStatsEventData?
        }
        assertEquals(id_kvdb, kvdbStatsEventData?.kvdbId);
        assertEquals(amount + 1, kvdbStatsEventData?.entries);

        // unsubscribe
        kvdbApi.unsubscribeFrom(subscriptionIds)

        kvdbApi.close()
    }

    @Test
    fun getEvent_KvdbStatsChanged_KvdbId() {
        val kvdbApi: KvdbApi = KvdbApi(eventsConnection)
        var kvdbStatsEventData: KvdbStatsEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id_kvdb: String = kvdb3Id
        val amount: Long = kvdbApi.getKvdb(id_kvdb).entries!!

        // subscribe
        subscriptionIds = kvdbApi.subscribeFor(
            listOf(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.KVDB_STATS,
                    KvdbEventSelectorType.KVDB_ID,
                    id_kvdb
                )
            )
        )

        assertDoesNotFail {
            kvdbApi.setEntry(
                id_kvdb,
                "kvdb_key_kvdbId",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data".encodeToByteArray()
            )
            event = expectAndVerifyEvent("kvdbStatsChanged")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            kvdbStatsEventData = event.data as KvdbStatsEventData?
        }
        assertEquals(id_kvdb, kvdbStatsEventData?.kvdbId)
        assertEquals(amount + 1, kvdbStatsEventData?.entries)

        // unsubscribe
        kvdbApi.unsubscribeFrom(subscriptionIds)

        kvdbApi.close()
    }

    @Test
    fun getEvent_KvdbNewEntry_ContextId() {
        val kvdbApi = KvdbApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id_kvdb: String = kvdb3Id

        // subscribe
        subscriptionIds = kvdbApi.subscribeFor(
            listOf(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.ENTRY_CREATE,
                    KvdbEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        assertDoesNotFail {
            kvdbApi.setEntry(
                id_kvdb!!,
                "kvdb_new_key_context",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "new key".encodeToByteArray()
            )
            event = expectAndVerifyEvent("kvdbNewEntry")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        kvdbApi.unsubscribeFrom(subscriptionIds)

        kvdbApi.close()
    }

    @Test
    fun getEvent_KvdbNewEntry_KvdbId() {
        val kvdbApi = KvdbApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id_kvdb: String = kvdb3Id

        // subscribe
        subscriptionIds = kvdbApi.subscribeFor(
            listOf(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.ENTRY_CREATE,
                    KvdbEventSelectorType.KVDB_ID,
                    id_kvdb!!
                )
            )
        )

        assertDoesNotFail {
            kvdbApi.setEntry(
                id_kvdb,
                "kvdb_new_key_kvdbId",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "new key".encodeToByteArray()
            )
            event = expectAndVerifyEvent("kvdbNewEntry")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        kvdbApi.unsubscribeFrom(subscriptionIds)
        kvdbApi.close()
    }

    @Test
    fun getEvent_KvdbEntryUpdated_ContextId() {
        val kvdbApi = KvdbApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id_kvdb: String = kvdb1Id
        val key_entry: String = kvdbEntry2Key
        val kvdbEntry: KvdbEntry = kvdbApi.getEntry(id_kvdb!!, key_entry!!)

        // subscribe
        subscriptionIds = kvdbApi.subscribeFor(
            listOf(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.ENTRY_UPDATE,
                    KvdbEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        assertDoesNotFail {
            kvdbApi.setEntry(
                id_kvdb,
                key_entry,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "updated key value".encodeToByteArray(),
                kvdbEntry.version!!
            )
            event = expectAndVerifyEvent("kvdbEntryUpdated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        kvdbApi.unsubscribeFrom(subscriptionIds)

        kvdbApi.close()
    }

    @Test
    fun getEvent_KvdbEntryUpdated_KvdbId() {
        val kvdbApi = KvdbApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id_kvdb: String = kvdb1Id
        val key_entry: String = kvdbEntry2Key
        val kvdbEntry: KvdbEntry = kvdbApi.getEntry(id_kvdb!!, key_entry!!)

        // subscribe
        subscriptionIds = kvdbApi.subscribeFor(
            listOf(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.ENTRY_UPDATE,
                    KvdbEventSelectorType.KVDB_ID,
                    id_kvdb
                )
            )
        )

        assertDoesNotFail {
            kvdbApi.setEntry(
                id_kvdb,
                key_entry,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "new key value".encodeToByteArray(),
                kvdbEntry.version!!
            )
            event = expectAndVerifyEvent("kvdbEntryUpdated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        kvdbApi.unsubscribeFrom(subscriptionIds)

        kvdbApi.close()
    }

    @Test
    fun getEvent_KvdbEntryUpdated_EntryId() {
        val kvdbApi = KvdbApi(eventsConnection)
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val id_kvdb: String = kvdb1Id
        val key_entry: String = kvdbEntry2Key
        val kvdbEntry: KvdbEntry = kvdbApi.getEntry(id_kvdb!!, key_entry!!)

        // subscribe
        subscriptionIds = kvdbApi.subscribeFor(
            listOf(
                kvdbApi.buildSubscriptionQueryForSelectedEntry(
                    KvdbEventType.ENTRY_UPDATE,
                    id_kvdb,
                    key_entry
                )
            )
        )

        assertDoesNotFail {
            kvdbApi.setEntry(
                id_kvdb,
                key_entry,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "updated key value".encodeToByteArray(),
                kvdbEntry.version!!
            )
            event = expectAndVerifyEvent("kvdbEntryUpdated")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        // unsubscribe
        kvdbApi.unsubscribeFrom(subscriptionIds)

        kvdbApi.close()
    }

    @Test
    fun getEvent_kvdbEntryDeleted_ContextId() {
        val kvdbApi = KvdbApi(eventsConnection)
        var kvdbDeletedEntryEventData: KvdbDeletedEntryEventData? = null
        lateinit var event: Event<*>
        lateinit var event2: Event<*>
        lateinit var event3: Event<*>
        val subscriptionIds: List<String>

        val id_kvdb: String? = kvdbApi.createKvdb(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        val key_entry = "kvdb_entry_deleted_key_1"
        val key_entry2 = "kvdb_entry_deleted_key_2"
        val key_entry3 = "kvdb_entry_deleted_key_3"

        kvdbApi.setEntry(
            id_kvdb!!,
            key_entry,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "key value 1".encodeToByteArray()
        )
        kvdbApi.setEntry(
            id_kvdb,
            key_entry2,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "key value 2".encodeToByteArray()
        )
        kvdbApi.setEntry(
            id_kvdb,
            key_entry3,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "key value 3".encodeToByteArray()
        )

        // subscribe
        subscriptionIds = kvdbApi.subscribeFor(
            listOf(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.ENTRY_DELETE,
                    KvdbEventSelectorType.CONTEXT_ID,
                    context2Id!!
                )
            )
        )

        // delete entry
        assertDoesNotFail {
            kvdbApi.deleteEntry(id_kvdb, key_entry)
            event = expectAndVerifyEvent("kvdbEntryDeleted")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            kvdbDeletedEntryEventData = event.data as KvdbDeletedEntryEventData?
        }
        assertEquals(id_kvdb, kvdbDeletedEntryEventData?.kvdbId);
        assertEquals(key_entry, kvdbDeletedEntryEventData?.kvdbEntryKey);

        // delete many entries
        kvdbApi.deleteEntries(id_kvdb, setOf(key_entry, key_entry2, key_entry3))
        assertDoesNotFail {
            runBlocking {
                delay(1500)
            }
            event2 = EventQueue.getEvent()!!
            event3 = EventQueue.getEvent()!!
        }
        assertEquals("kvdbEntryDeleted", event2.type)
        assertEquals("kvdbEntryDeleted", event3.type)
        assertEquals(eventsConnection.getConnectionId(), event2.connectionId)
        assertEquals(eventsConnection.getConnectionId(), event3.connectionId)

        assertDoesNotFail {
            kvdbDeletedEntryEventData = event2.data as KvdbDeletedEntryEventData?
        }
        assertEquals(id_kvdb, kvdbDeletedEntryEventData?.kvdbId);
        assertEquals(key_entry2, kvdbDeletedEntryEventData?.kvdbEntryKey);

        assertDoesNotFail {
            kvdbDeletedEntryEventData = event3.data as KvdbDeletedEntryEventData?
        }
        assertEquals(id_kvdb, kvdbDeletedEntryEventData?.kvdbId);
        assertEquals(key_entry3, kvdbDeletedEntryEventData?.kvdbEntryKey);

        // unsubscribe
        kvdbApi.unsubscribeFrom(subscriptionIds)

        kvdbApi.close()
    }

    @Test
    fun getEvent_kvdbEntryDeleted_KvdbId() {
        val kvdbApi = KvdbApi(eventsConnection)
        var kvdbDeletedEntryEventData: KvdbDeletedEntryEventData? = null
        lateinit var event: Event<*>
        lateinit var event2: Event<*>
        lateinit var event3: Event<*>
        val subscriptionIds: List<String>

        val id_kvdb: String? = kvdbApi.createKvdb(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        val key_entry = "kvdb_entry_deleted_key_1"
        val key_entry2 = "kvdb_entry_deleted_key_2"
        val key_entry3 = "kvdb_entry_deleted_key_3"

        kvdbApi.setEntry(
            id_kvdb!!,
            key_entry,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "key value 1".encodeToByteArray()
        )
        kvdbApi.setEntry(
            id_kvdb,
            key_entry2,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "key value 2".encodeToByteArray()
        )
        kvdbApi.setEntry(
            id_kvdb,
            key_entry3,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "key value 3".encodeToByteArray()
        )

        // subscribe
        subscriptionIds = kvdbApi.subscribeFor(
            listOf(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.ENTRY_DELETE,
                    KvdbEventSelectorType.KVDB_ID,
                    id_kvdb
                )
            )
        )

        // delete entry
        assertDoesNotFail {
            kvdbApi.deleteEntry(id_kvdb, key_entry)
            event = expectAndVerifyEvent("kvdbEntryDeleted")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            kvdbDeletedEntryEventData = event.data as KvdbDeletedEntryEventData?
        }
        assertEquals(id_kvdb, kvdbDeletedEntryEventData?.kvdbId);
        assertEquals(key_entry, kvdbDeletedEntryEventData?.kvdbEntryKey);

        // delete many entries
        kvdbApi.deleteEntries(id_kvdb, setOf(key_entry, key_entry2, key_entry3))
        assertDoesNotFail {
            runBlocking {
                delay(1500)
            }
            event2 = EventQueue.getEvent()!!
            event3 = EventQueue.getEvent()!!
        }
        assertEquals("kvdbEntryDeleted", event2.type)
        assertEquals("kvdbEntryDeleted", event3.type)
        assertEquals(eventsConnection.getConnectionId(), event2.connectionId)
        assertEquals(eventsConnection.getConnectionId(), event3.connectionId)

        assertDoesNotFail {
            kvdbDeletedEntryEventData = event2.data as KvdbDeletedEntryEventData?
        }
        assertEquals(id_kvdb, kvdbDeletedEntryEventData?.kvdbId);
        assertEquals(key_entry2, kvdbDeletedEntryEventData?.kvdbEntryKey);

        assertDoesNotFail {
            kvdbDeletedEntryEventData =
                event3.data as KvdbDeletedEntryEventData?
        }
        assertEquals(id_kvdb, kvdbDeletedEntryEventData?.kvdbId);
        assertEquals(key_entry3, kvdbDeletedEntryEventData?.kvdbEntryKey);

        // unsubscribe
        kvdbApi.unsubscribeFrom(subscriptionIds)

        kvdbApi.close()
    }

    @Test
    fun getEvent_kvdbEntryDeleted_EntryId() {
        val kvdbApi = KvdbApi(eventsConnection)
        var kvdbDeletedEntryEventData: KvdbDeletedEntryEventData? = null
        lateinit var event: Event<*>
        lateinit var event2: Event<*>
        lateinit var event3: Event<*>
        val subscriptionIds: List<String>

        val id_kvdb: String? = kvdbApi.createKvdb(
            context2Id!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        val key_entry = "kvdb_entry_deleted_key_1"
        val key_entry2 = "kvdb_entry_deleted_key_2"
        val key_entry3 = "kvdb_entry_deleted_key_3"

        kvdbApi.setEntry(
            id_kvdb!!,
            key_entry,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "key value 1".encodeToByteArray()
        )
        kvdbApi.setEntry(
            id_kvdb,
            key_entry2,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "key value 2".encodeToByteArray()
        )
        kvdbApi.setEntry(
            id_kvdb,
            key_entry3,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "key value 3".encodeToByteArray()
        )

        // subscribe
        subscriptionIds = kvdbApi.subscribeFor(
            listOf(
                kvdbApi.buildSubscriptionQueryForSelectedEntry(
                    KvdbEventType.ENTRY_DELETE,
                    id_kvdb,
                    key_entry
                ),
                kvdbApi.buildSubscriptionQueryForSelectedEntry(
                    KvdbEventType.ENTRY_DELETE,
                    id_kvdb,
                    key_entry
                ),
                kvdbApi.buildSubscriptionQueryForSelectedEntry(
                    KvdbEventType.ENTRY_DELETE,
                    id_kvdb,
                    key_entry2
                ),
                kvdbApi.buildSubscriptionQueryForSelectedEntry(
                    KvdbEventType.ENTRY_DELETE,
                    id_kvdb,
                    key_entry3
                )
            )
        )

        // delete entry
        assertDoesNotFail {
            kvdbApi.deleteEntry(id_kvdb, key_entry)
            event = expectAndVerifyEvent("kvdbEntryDeleted")!!
        }
        assertEquals(2, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        assertDoesNotFail {
            kvdbDeletedEntryEventData = event.data as KvdbDeletedEntryEventData?
        }
        assertEquals(id_kvdb, kvdbDeletedEntryEventData?.kvdbId);
        assertEquals(key_entry, kvdbDeletedEntryEventData?.kvdbEntryKey);

        // delete many entries
        kvdbApi.deleteEntries(id_kvdb, setOf(key_entry, key_entry2, key_entry3))
        assertDoesNotFail {
            runBlocking {
                delay(1500)
            }
            event2 = EventQueue.getEvent()!!
            event3 = EventQueue.getEvent()!!
        }
        assertEquals("kvdbEntryDeleted", event2.type)
        assertEquals("kvdbEntryDeleted", event3.type)
        assertEquals(eventsConnection.getConnectionId(), event2.connectionId)
        assertEquals(eventsConnection.getConnectionId(), event3.connectionId)
        assertEquals(1, event2.subscriptions.size)
        assertEquals(1, event3.subscriptions.size)

        assertDoesNotFail {
            kvdbDeletedEntryEventData = event2.data as KvdbDeletedEntryEventData?
        }
        assertEquals(id_kvdb, kvdbDeletedEntryEventData?.kvdbId);
        assertTrue(
            listOf(key_entry2, key_entry3).contains(kvdbDeletedEntryEventData?.kvdbEntryKey)
        )
        assertDoesNotFail {
            kvdbDeletedEntryEventData = event3.data as KvdbDeletedEntryEventData?
        }
        assertEquals(id_kvdb, kvdbDeletedEntryEventData?.kvdbId);
        assertTrue(
            listOf(key_entry2, key_entry3).contains(kvdbDeletedEntryEventData?.kvdbEntryKey)
        )

        // unsubscribe
        kvdbApi.unsubscribeFrom(subscriptionIds)

        kvdbApi.close()
    }

    @Test
    @Throws(Exception::class)
    fun getEvent_KvdbCollectionChanged_ContextId() {
        val kvdbApi = KvdbApi(eventsConnection)
        var collectionChangedEventData: CollectionChangedEventData? = null
        var collectionItemsChange: List<CollectionItemChange> = mutableListOf()
        var event: Event<*>? = null
        val subscriptionIds: List<String>
        val kvdbId: String = kvdb3Id

        // helper entry
        kvdbApi.setEntry(
            kvdbId!!,
            "key_1",
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data 1".encodeToByteArray()
        )

        // Delay to ensure the operation above (setEntry) finishes before subscription.
        // This avoid capturing creating first entry as CollectionChanged event
        assertDoesNotFail {
            runBlocking {
                delay(1000)
            }
        }

        // subscribe
        subscriptionIds = kvdbApi.subscribeFor(
            listOf(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.COLLECTION_CHANGE,
                    KvdbEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        // set entry
        assertDoesNotFail {
            kvdbApi.setEntry(
                kvdbId,
                "key_2",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data 2".encodeToByteArray()
            )
            event = expectAndVerifyEvent("collectionChanged")!!
        }

        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event?.data as CollectionChangedEventData?
        }
        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
        assertEquals(kvdbId, collectionChangedEventData?.moduleId)
        assertEquals("kvdb", collectionChangedEventData?.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange =
                collectionChangedEventData?.items!!
        }
        assertEquals(1, collectionItemsChange.size)
        assertEquals("$kvdbId:key_2", collectionItemsChange[0].itemId)
        assertEquals("create", collectionItemsChange[0].action)


        // update entry
        assertDoesNotFail {
            kvdbApi.setEntry(
                kvdbId,
                "key_1",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "edited data 1".encodeToByteArray(),
                1
            )
            event = expectAndVerifyEvent("collectionChanged")!!
        }

        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event?.data as CollectionChangedEventData?
        }
        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
        assertEquals(kvdbId, collectionChangedEventData?.moduleId)
        assertEquals("kvdb", collectionChangedEventData?.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange =
                collectionChangedEventData?.items!!
        }
        assertEquals(1, collectionItemsChange.size)
        assertEquals("$kvdbId:key_1", collectionItemsChange[0].itemId)
        assertEquals("update", collectionItemsChange[0].action)


        // delete entry
        assertDoesNotFail {
            kvdbApi.deleteEntry(kvdbId, "key_1")
            event = expectAndVerifyEvent("collectionChanged")!!
        }

        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event?.data as CollectionChangedEventData?
        }
        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
        assertEquals(kvdbId, collectionChangedEventData?.moduleId)
        assertEquals("kvdb", collectionChangedEventData?.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange =
                collectionChangedEventData?.items!!
        }
        assertEquals(1, collectionItemsChange.size)
        assertTrue(collectionItemsChange.any { i: CollectionItemChange? -> i!!.itemId == "$kvdbId:key_1" })
        assertEquals("delete", collectionItemsChange[0].action)


        // delete entries
        assertDoesNotFail {
            kvdbApi.deleteEntries(kvdbId, setOf("key_2"))
            event = expectAndVerifyEvent("collectionChanged")!!
        }

        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event?.data as CollectionChangedEventData?
        }
        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
        assertEquals(kvdbId, collectionChangedEventData?.moduleId)
        assertEquals("kvdb", collectionChangedEventData?.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange =
                collectionChangedEventData?.items!!
        }
        assertTrue(collectionItemsChange.any { i: CollectionItemChange? -> i!!.itemId == "$kvdbId:key_2" })
        assertEquals("delete", collectionItemsChange[0].action)


        // unsubscribe
        kvdbApi.unsubscribeFrom(subscriptionIds)

        kvdbApi.close()
    }

    @Test
    @Throws(Exception::class)
    fun getEvent_KvdbCollectionChanged_KvdbId() {
        val kvdbApi = KvdbApi(eventsConnection)
        var collectionChangedEventData: CollectionChangedEventData? = null
        var collectionItemsChange: List<CollectionItemChange> = mutableListOf()
        var event: Event<*>? = null
        val subscriptionIds: List<String>
        val kvdbId: String = kvdb3Id

        // helper entry
        kvdbApi.setEntry(
            kvdbId!!,
            "key_1",
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            "data 1".encodeToByteArray()
        )

        // Delay to ensure the operation above (setEntry) finishes before subscription.
        // This avoid capturing creating first entry as CollectionChanged event
        assertDoesNotFail {
            runBlocking {
                delay(1000)
            }
        }

        // subscribe
        subscriptionIds = kvdbApi.subscribeFor(
            listOf(
                kvdbApi.buildSubscriptionQuery(
                    KvdbEventType.COLLECTION_CHANGE,
                    KvdbEventSelectorType.KVDB_ID,
                    kvdbId
                )
            )
        )

        // set entry
        assertDoesNotFail {
            kvdbApi.setEntry(
                kvdbId,
                "key_2",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "data 2".encodeToByteArray()
            )
            event = expectAndVerifyEvent("collectionChanged")!!
        }

        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event?.data as CollectionChangedEventData?
        }
        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
        assertEquals(kvdbId, collectionChangedEventData?.moduleId)
        assertEquals("kvdb", collectionChangedEventData?.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange =
                collectionChangedEventData?.items!!
        }
        assertEquals(1, collectionItemsChange.size)
        assertEquals("$kvdbId:key_2", collectionItemsChange[0].itemId)
        assertEquals("create", collectionItemsChange[0].action)


        // update entry
        assertDoesNotFail {
            kvdbApi.setEntry(
                kvdbId,
                "key_1",
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                "edited data 1".encodeToByteArray(),
                1
            )
            event = expectAndVerifyEvent("collectionChanged")!!
        }

        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event?.data as CollectionChangedEventData?
        }
        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
        assertEquals(kvdbId, collectionChangedEventData?.moduleId)
        assertEquals("kvdb", collectionChangedEventData?.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange =
                collectionChangedEventData?.items!!
        }
        assertEquals(1, collectionItemsChange.size)
        assertEquals("$kvdbId:key_1", collectionItemsChange[0].itemId)
        assertEquals("update", collectionItemsChange[0].action)


        // delete entry
        assertDoesNotFail {
            kvdbApi.deleteEntry(kvdbId, "key_1")
            event = expectAndVerifyEvent("collectionChanged")!!
        }

        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event?.data as CollectionChangedEventData?
        }
        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
        assertEquals(kvdbId, collectionChangedEventData?.moduleId)
        assertEquals("kvdb", collectionChangedEventData?.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange =
                collectionChangedEventData?.items!!
        }
        assertEquals(1, collectionItemsChange.size)
        assertTrue(collectionItemsChange.any { i: CollectionItemChange? -> i!!.itemId == "$kvdbId:key_1" })
        assertEquals("delete", collectionItemsChange[0].action)


        // delete entries
        assertDoesNotFail {
            kvdbApi.deleteEntries(kvdbId, setOf("key_2"))
            event = expectAndVerifyEvent("collectionChanged")!!
        }

        // CollectionChangedEventData
        assertDoesNotFail {
            collectionChangedEventData =
                event?.data as CollectionChangedEventData?
        }
        assertEquals(1, collectionChangedEventData?.affectedItemsCount)
        assertEquals(kvdbId, collectionChangedEventData?.moduleId)
        assertEquals("kvdb", collectionChangedEventData?.moduleType)

        // CollectionItemChange
        assertDoesNotFail {
            collectionItemsChange =
                collectionChangedEventData?.items!!
        }
        assertTrue(collectionItemsChange.any { i: CollectionItemChange? -> i!!.itemId == "$kvdbId:key_2" })
        assertEquals("delete", collectionItemsChange[0].action)


        // unsubscribe
        kvdbApi.unsubscribeFrom(subscriptionIds)

        kvdbApi.close()
    }

    @Test
    fun getEvent_ContextCustom_ContextId() {
        val eventApi: EventApi = EventApi(eventsConnection)
        var contextCustomEventData: ContextCustomEventData? = null
        lateinit var event: Event<*>
        val subscriptionIds: List<String>

        val channelName = "test_context_custom"
        val data = "data to subscribers"

        subscriptionIds = eventApi.subscribeFor(
            listOf(
                eventApi.buildSubscriptionQuery(
                    channelName,
                    CustomEventSelectorType.CONTEXT_ID,
                    context2Id!!
                )
            )
        )
        assertDoesNotFail {
            eventApi.emitEvent(
                context2Id!!,
                users,
                channelName,
                data.encodeToByteArray()
            )
            event = expectAndVerifyEvent("contextCustom")!!
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])

        assertDoesNotFail {
            contextCustomEventData = event.data as ContextCustomEventData?
        }
        assertEquals(context2Id, contextCustomEventData!!.contextId)
        assertEquals(
            data,
            contextCustomEventData!!.payload.decodeToString()
        )
        assertEquals(user1Id, contextCustomEventData!!.userId)

        // unsubscribe
        eventApi.unsubscribeFrom(subscriptionIds)

        eventApi.close()
    }

    @Test
    fun contextCustomEventDataStatusCode() {
        var contextCustomEventData: ContextCustomEventData? = null
        connection2 =
            connectAsUserAndCleanEvents(ConnectionType.User2, bridgeAddress)
        val eventApi = EventApi(eventsConnection)
        val eventApi2 = EventApi(connection2!!)
        lateinit var event: Event<*>
        var subscriptionIds: List<String>
        val channelName = "test"
        val eventData = "event data"

        // create user verifier
        val userVerifierInterface: UserVerifierInterface = object : UserVerifierInterface {
            override fun verify(request: List<VerificationRequest>): List<Boolean> {
                return request
                    .map { req: VerificationRequest? -> false }
                    .toList()
            }
        }

        // CASE 1 - user2 with verify result == false subscribes and catches event
        // set user verifier
        connection2?.setUserVerifier(userVerifierInterface)

        // user2 - subscribe
        subscriptionIds = eventApi2.subscribeFor(
            listOf(
                eventApi2.buildSubscriptionQuery(
                    channelName,
                    CustomEventSelectorType.CONTEXT_ID,
                    context2Id!!
                )
            )
        )

        // user1 - emit CustomEvent
        assertDoesNotFail {
            eventApi.emitEvent(
                context2Id!!,
                users,
                channelName,
                eventData.encodeToByteArray()
            )
            event = expectAndVerifyEvent(
                connection2?.getConnectionId()!!,
                "contextCustom"
            )!!
        }
        assertTrue(event.channel.endsWith(channelName))

        // ContextCustomEventData
        assertDoesNotFail {
            contextCustomEventData = event.data as ContextCustomEventData
        }
        assertNotEquals(0, contextCustomEventData!!.statusCode)

        // user2 - unsubscribe
        eventApi2.unsubscribeFrom(subscriptionIds)


        // CASE 2 - user1 with verify result == true subscribes and catches event
        // user1 - subscribe
        subscriptionIds = eventApi.subscribeFor(
            listOf(
                eventApi.buildSubscriptionQuery(
                    channelName,
                    CustomEventSelectorType.CONTEXT_ID,
                    context2Id!!
                )
            )
        )

        // user2 - emit event
        assertDoesNotFail {
            eventApi2.emitEvent(
                context2Id!!,
                users,
                channelName,
                eventData.encodeToByteArray()
            )
            event = expectAndVerifyEvent(
                eventsConnection.getConnectionId()!!,
                "contextCustom"
            )!!
        }
        assertTrue(event.channel.endsWith(channelName))

        // ContextCustomEventData
        assertDoesNotFail {
            contextCustomEventData = event.data as ContextCustomEventData?
        }
        assertEquals(0, contextCustomEventData!!.statusCode)

        // user1 - unsubscribe
        eventApi.unsubscribeFrom(subscriptionIds)

        eventApi.close()
        eventApi2.close()
    }

    @Test
    fun contextCustomEventCaptureByAnotherUser() {
        connection2 =
            connectAsUserAndCleanEvents(ConnectionType.User2, bridgeAddress)
        var contextCustomEventData: ContextCustomEventData? = null
        val eventApi = EventApi(eventsConnection)
        val eventApi2 = EventApi(connection2!!)
        lateinit var event: Event<*>
        val channelName = "test_custom_event"
        val eventData = "event data"

        // user2 - subscribe
        eventApi2.subscribeFor(
            listOf(
                eventApi2.buildSubscriptionQuery(
                    channelName,
                    CustomEventSelectorType.CONTEXT_ID,
                    context2Id!!
                )
            )
        )

        // user1 - emit custom event
        assertDoesNotFail {
            eventApi.emitEvent(
                context2Id!!,
                users,
                channelName,
                eventData.encodeToByteArray()
            )
            event = expectAndVerifyEvent(
                connection2?.getConnectionId()!!,
                "contextCustom"
            )!!
        }
        assertTrue(event.channel.endsWith(channelName))

        // ContextCustomEventData
        assertDoesNotFail {
            contextCustomEventData = event.data as ContextCustomEventData?
        }
        assertEquals(context2Id, contextCustomEventData!!.contextId)
        assertEquals(user1Id, contextCustomEventData!!.userId)
        assertContentEquals(eventData.encodeToByteArray(), contextCustomEventData!!.payload)

        eventApi.close()
        eventApi2.close()
    }
}