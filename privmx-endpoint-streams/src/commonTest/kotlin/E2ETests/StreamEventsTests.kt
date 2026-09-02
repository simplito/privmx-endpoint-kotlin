import E2ETests.BaseTest
import E2ETests.addFakeAudioTrackToStream
import E2ETests.addFakeVideoTrackToStream
import E2ETests.createStreamApi
import E2ETests.deleteAllRooms
import E2ETests.getStreamsToSubscribe
import E2ETests.waitForServerSync

import com.simplito.kotlin.privmx_endpoint.model.Event
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamRoom
import com.simplito.kotlin.privmx_endpoint.model.stream.events.StreamPublishedEventData
import com.simplito.kotlin.privmx_endpoint.model.stream.events.StreamRoomDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.stream.events.StreamRoomParticipantEventData
import com.simplito.kotlin.privmx_endpoint.model.stream.events.StreamSubscriptionEventData
import com.simplito.kotlin.privmx_endpoint.model.stream.events.StreamUnpublishedEventData
import com.simplito.kotlin.privmx_endpoint.model.stream.events.StreamUpdatedEventData
import com.simplito.kotlin.privmx_endpoint.model.stream.events.eventSelectorTypes.StreamEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.stream.events.eventTypes.StreamEventType
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.core.EventQueue
import com.simplito.kotlin.privmx_endpoint.modules.stream.StreamApiLow
import com.simplito.kotlin.privmx_endpoint_streams.StreamApi
import com.simplito.kotlin.privmx_endpoint_streams.joinStreamRoom
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
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


class StreamEventsTests : BaseTest() {
    lateinit var streamApiLow: StreamApiLow
    lateinit var streamApi: StreamApi

    var connection2: Connection? = null
    lateinit var streamApiLow2: StreamApiLow
    var streamApi2: StreamApi? = null

    private val pubMeta get() = publicMeta.encodeToByteArray()
    private val privMeta get() = privateMeta.encodeToByteArray()

    @BeforeTest
    fun beforeEach() {
        connection = connectAsUserAndCleanEvents(ConnectionType.User1, bridgeAddress)
        streamApiLow = StreamApiLow(connection!!)
        streamApi = createStreamApi(streamApiLow)

        connection2 = connectAsUserAndCleanEvents(ConnectionType.User2, bridgeAddress)
        streamApiLow2 = StreamApiLow(connection2!!)
        streamApi2 = createStreamApi(streamApiLow2)
    }

    @AfterTest
    fun afterEach() {
        try {
            streamApi2?.close()
        } catch (_: Exception) {
        }
        try {
            if (connection2 != null) closeConnectionAndCleanEvents(connection2!!)
        } catch (_: Exception) {
        } finally {
            connection2 = null
            streamApi2 = null
        }

        try {
            deleteAllRooms(streamApi, contextId!!)
        } catch (_: Exception) {
        }

        try {
            streamApi.close()
            streamApiLow.close()
        } catch (_: Exception) {
        }
        try {
            if (connection != null) closeConnectionAndCleanEvents(connection!!)
        } catch (_: Exception) {
        } finally {
            connection = null
        }
    }

    fun waitForEventWithTimeout(duration: Duration = 3.seconds): Event<*>? = runBlocking {
        val waitResult = async(Dispatchers.IO) {
            EventQueue.waitEvent()
        }
        val delayJob = launch(Dispatchers.IO) {
            delay(duration)
            EventQueue.emitBreakEvent()
        }
        waitResult.await()
            .also { delayJob.cancel() }
            .takeIf { it.type != "libBreak" }
    }

    fun expectAndVerifyEvent(eventType: String): Event<*> =
        expectAndVerifyEvent(connection?.getConnectionId()!!, eventType)

    fun expectAndVerifyEvent(connectionId: Long, eventType: String): Event<*> {
        val event: Event<*> = waitForEventWithTimeout() ?: throw AssertionError(
            "Expected event '$eventType', but none arrived within the timeout"
        )

        println("EVENT!!")
        println("type   " + event.type)
        println("connectionId:   " + event.connectionId)
        println("channel  " + event.channel)

        if (connectionId != event.connectionId)   throw AssertionError(
            "Wrong connectionId for event '${event.type}': expected $connectionId, but got ${event.connectionId} (channel '${event.channel}')"
        )

        if(eventType != event.type) throw AssertionError(
            "Wrong event type: expected '$eventType', but got '${event.type}' (connectionId=${event.connectionId}, channel '${event.channel}')"
        )

        return event
    }

    fun expectNoEventOccurs() {
        waitForServerSync()
        assertNull(EventQueue.getEvent())
    }

    @Test
    fun getEvent_StreamRoomCreated_ContextId() {
        lateinit var event: Event<*>
        val subscriptionIds = streamApi.subscribeFor(
            listOf(
                streamApi.buildSubscriptionQuery(
                    StreamEventType.STREAMROOM_CREATE,
                    StreamEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )
        assertDoesNotFail {
            streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)
            event = expectAndVerifyEvent("streamRoomCreated")
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        val createdRoom = event.data as StreamRoom
        assertEquals(contextId, createdRoom.contextId)
        assertContentEquals(pubMeta, createdRoom.publicMeta)
        assertContentEquals(privMeta, createdRoom.privateMeta)

        streamApi.unsubscribeFrom(subscriptionIds)
    }

    @Test
    fun getEvent_StreamRoomUpdated_ContextId() {
        lateinit var event: Event<*>
        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)
        val room = streamApi.getStreamRoom(id)
        val newPublic = "new public".encodeToByteArray()
        val newPrivate = "new private".encodeToByteArray()

        val subscriptionIds = streamApi.subscribeFor(
            listOf(
                streamApi.buildSubscriptionQuery(
                    StreamEventType.STREAMROOM_UPDATE,
                    StreamEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        assertDoesNotFail {
            streamApi.updateStreamRoom(
                id, users, users.subList(0, 1), newPublic, newPrivate,
                room.version!!, false, false, null
            )
            event = expectAndVerifyEvent("streamRoomUpdated")
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        val updatedRoom = event.data as StreamRoom
        assertEquals(id, updatedRoom.streamRoomId)
        assertContentEquals(newPublic, updatedRoom.publicMeta)
        assertContentEquals(newPrivate, updatedRoom.privateMeta)
        assertEquals(room.version!! + 1, updatedRoom.version)

        streamApi.unsubscribeFrom(subscriptionIds)
    }

    @Test
    fun getEvent_StreamRoomUpdated_StreamRoomId() {
        lateinit var event: Event<*>
        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)
        val room = streamApi.getStreamRoom(id)
        val newPublic = "new public".encodeToByteArray()
        val newPrivate = "new private".encodeToByteArray()

        val subscriptionIds = streamApi.subscribeFor(
            listOf(
                streamApi.buildSubscriptionQuery(
                    StreamEventType.STREAMROOM_UPDATE,
                    StreamEventSelectorType.STREAMROOM_ID,
                    id
                )
            )
        )

        assertDoesNotFail {
            streamApi.updateStreamRoom(
                id, users, users, newPublic, newPrivate,
                room.version!!, false, false, null
            )
            event = expectAndVerifyEvent("streamRoomUpdated")
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        val updatedRoom = event.data as StreamRoom
        assertEquals(id, updatedRoom.streamRoomId)
        assertContentEquals(newPublic, updatedRoom.publicMeta)
        assertContentEquals(newPrivate, updatedRoom.privateMeta)
        assertEquals(room.version!! + 1, updatedRoom.version)

        streamApi.unsubscribeFrom(subscriptionIds)
    }

    @Test
    fun getEvent_StreamRoomDeleted_ContextId() {
        lateinit var event: Event<*>
        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)

        val subscriptionIds = streamApi.subscribeFor(
            listOf(
                streamApi.buildSubscriptionQuery(
                    StreamEventType.STREAMROOM_DELETE,
                    StreamEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        assertDoesNotFail {
            streamApi.deleteStreamRoom(id)
            event = expectAndVerifyEvent("streamRoomDeleted")
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        val data = event.data as StreamRoomDeletedEventData
        assertEquals(id, data.streamRoomId)

        streamApi.unsubscribeFrom(subscriptionIds)
    }

    @Test
    fun getEvent_StreamRoomDeleted_StreamRoomId() {
        lateinit var event: Event<*>
        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)

        val subscriptionIds = streamApi.subscribeFor(
            listOf(
                streamApi.buildSubscriptionQuery(
                    StreamEventType.STREAMROOM_DELETE,
                    StreamEventSelectorType.STREAMROOM_ID,
                    id
                )
            )
        )

        assertDoesNotFail {
            streamApi.deleteStreamRoom(id)
            event = expectAndVerifyEvent("streamRoomDeleted")
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        val data = event.data as StreamRoomDeletedEventData
        assertEquals(id, data.streamRoomId)

        streamApi.unsubscribeFrom(subscriptionIds)
    }

    @Test
    fun getEvent_StreamRoomJoined_ContextId() {
        lateinit var event: Event<*>
        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)

        streamApi.joinStreamRoom(id)

        val subscriptionIds = streamApi.subscribeFor(
            listOf(
                streamApi.buildSubscriptionQuery(
                    StreamEventType.STREAMROOM_JOIN,
                    StreamEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        assertDoesNotFail {
            streamApi2?.joinStreamRoom(id)
        }
        event = expectAndVerifyEvent("streamRoomJoined")

        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        val data = event.data as StreamRoomParticipantEventData
        assertEquals(id, data.streamRoomId)

        streamApi.leaveStreamRoom(id)
        streamApi.unsubscribeFrom(subscriptionIds)
    }

    @Test
    fun getEvent_StreamRoomJoined_StreamRoomId() {
        lateinit var event: Event<*>
        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)

        val subscriptionIds = streamApi.subscribeFor(
            listOf(
                streamApi.buildSubscriptionQuery(
                    StreamEventType.STREAMROOM_JOIN,
                    StreamEventSelectorType.STREAMROOM_ID,
                    id
                )
            )
        )

        assertDoesNotFail {
            streamApi.joinStreamRoom(id)
            event = expectAndVerifyEvent("streamRoomJoined")
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        val data = event.data as StreamRoomParticipantEventData
        assertEquals(id, data.streamRoomId)

        streamApi.leaveStreamRoom(id)
        streamApi.unsubscribeFrom(subscriptionIds)
    }

    @Test
    fun getEvent_StreamRoomLeft_ContextId() {
        lateinit var event: Event<*>
        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)

        streamApi.joinStreamRoom(id)

        val subscriptionIds = streamApi.subscribeFor(
            listOf(
                streamApi.buildSubscriptionQuery(
                    StreamEventType.STREAMROOM_LEAVE,
                    StreamEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        assertDoesNotFail {
            streamApi.leaveStreamRoom(id)
            event = expectAndVerifyEvent("streamRoomLeft")
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        val data = event.data as StreamRoomParticipantEventData
        assertEquals(id, data.streamRoomId)

        streamApi.unsubscribeFrom(subscriptionIds)
    }

    @Test
    fun getEvent_StreamRoomLeft_StreamRoomId() {
        lateinit var event: Event<*>
        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)
        streamApi.joinStreamRoom(id)

        val subscriptionIds = streamApi.subscribeFor(
            listOf(
                streamApi.buildSubscriptionQuery(
                    StreamEventType.STREAMROOM_LEAVE,
                    StreamEventSelectorType.STREAMROOM_ID,
                    id
                )
            )
        )

        waitForServerSync()
        assertDoesNotFail {
            streamApi.leaveStreamRoom(id)
        }

        event = expectAndVerifyEvent("streamRoomLeft")

        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        val data = event.data as StreamRoomParticipantEventData
        assertEquals(id, data.streamRoomId)

        streamApi.unsubscribeFrom(subscriptionIds)
    }

    @Test
    fun getEvent_StreamPublished_StreamRoomId() {
        lateinit var event: Event<*>
        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)
        streamApi.joinStreamRoom(id)
        val handle = streamApi.createStream(id)
        addFakeAudioTrackToStream(streamApi, handle)

        val subscriptionIds = streamApi.subscribeFor(
            listOf(
                streamApi.buildSubscriptionQuery(
                    StreamEventType.STREAM_PUBLISH,
                    StreamEventSelectorType.STREAMROOM_ID,
                    id
                )
            )
        )

        assertDoesNotFail {
            streamApi.publishStream(handle)
            event = expectAndVerifyEvent("streamPublished")
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        val data = event.data as StreamPublishedEventData
        assertEquals(id, data.streamRoomId)
        assertNotNull(data.stream)
        assertNotNull(data.stream.id)
        assertFalse(data.stream.tracks.isEmpty())
        assertNotNull(data.userId)

        streamApi.removeStream(handle) // unpublish
        waitForServerSync(1000) 
        streamApi.unsubscribeFrom(subscriptionIds)
    }

    @Test
    fun getEvent_StreamPublished_ContextId() {
        lateinit var event: Event<*>
        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)
        streamApi.joinStreamRoom(id)
        val handle = streamApi.createStream(id)
        addFakeAudioTrackToStream(streamApi, handle)

        val subscriptionIds = streamApi.subscribeFor(
            listOf(
                streamApi.buildSubscriptionQuery(
                    StreamEventType.STREAM_PUBLISH,
                    StreamEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        assertDoesNotFail {
            streamApi.publishStream(handle)
            event = expectAndVerifyEvent("streamPublished")
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        val data = event.data as StreamPublishedEventData
        assertEquals(id, data.streamRoomId)
        assertNotNull(data.stream)
        assertNotNull(data.stream.id)
        assertFalse(data.stream.tracks.isEmpty())
        assertNotNull(data.userId)

        streamApi.removeStream(handle) // unpublish
        waitForServerSync(1000) 
        streamApi.unsubscribeFrom(subscriptionIds)
    }

    @Test
    fun getEvent_StreamUnpublished_StreamRoomId() {
        lateinit var event: Event<*>
        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)
        streamApi.joinStreamRoom(id)

        val handle = streamApi.createStream(id)
        addFakeAudioTrackToStream(streamApi, handle)

        val pub = streamApi.publishStream(handle)
        waitForServerSync()

        val subscriptionIds = streamApi.subscribeFor(
            listOf(
                streamApi.buildSubscriptionQuery(
                    StreamEventType.STREAM_UNPUBLISH,
                    StreamEventSelectorType.STREAMROOM_ID,
                    id
                )
            )
        )

        assertDoesNotFail {
            streamApi.removeStream(handle)
            event = expectAndVerifyEvent("streamUnpublished")
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        val data = event.data as StreamUnpublishedEventData
        assertEquals(id, data.streamRoomId)
        assertEquals(pub.data!!.stream.id, data.streamId)

        streamApi.unsubscribeFrom(subscriptionIds)
    }

    @Test
    fun getEvent_StreamUnpublished_ContextId() {
        lateinit var event: Event<*>
        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)
        streamApi.joinStreamRoom(id)
        val handle = streamApi.createStream(id)
        addFakeAudioTrackToStream(streamApi, handle)
        val pub = streamApi.publishStream(handle)
        waitForServerSync()

        val subscriptionIds = streamApi.subscribeFor(
            listOf(
                streamApi.buildSubscriptionQuery(
                    StreamEventType.STREAM_UNPUBLISH,
                    StreamEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        assertDoesNotFail {
            streamApi.removeStream(handle)
            event = expectAndVerifyEvent("streamUnpublished")
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        val data = event.data as StreamUnpublishedEventData
        assertEquals(id, data.streamRoomId)
        assertEquals(pub.data!!.stream.id, data.streamId)

        streamApi.unsubscribeFrom(subscriptionIds)
    }

    @Test
    fun getEvent_NoEventAfterUnsubscribe() {
        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)

        // subscribe then immediately unsubscribe
        val subscriptionIds = streamApi.subscribeFor(
            listOf(
                streamApi.buildSubscriptionQuery(
                    StreamEventType.STREAMROOM_DELETE,
                    StreamEventSelectorType.STREAMROOM_ID,
                    id
                )
            )
        )
        streamApi.unsubscribeFrom(subscriptionIds)

        streamApi.deleteStreamRoom(id)

        // no event should arrive after unsubscribe
        expectNoEventOccurs()

        // the room is really gone - only the event was suppressed
        assertFailsWith<PrivmxException> { streamApi.getStreamRoom(id) }
    }

    @Test
    fun getEvent_StreamPublished_CapturedBySecondUser() {
        lateinit var event: Event<*>

        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)
        streamApi.joinStreamRoom(id)
        streamApi2!!.joinStreamRoom(id)

        val handle = streamApi.createStream(id)
        addFakeAudioTrackToStream(streamApi, handle)

        // user2 subscribes
        val subscriptionIds = streamApi2!!.subscribeFor(
            listOf(
                streamApi2!!.buildSubscriptionQuery(
                    StreamEventType.STREAM_PUBLISH,
                    StreamEventSelectorType.STREAMROOM_ID,
                    id
                )
            )
        )

        // user1 publishes - the event must be attributed to user2's connection
        assertDoesNotFail {
            streamApi.publishStream(handle)
            event = expectAndVerifyEvent(connection2!!.getConnectionId()!!, "streamPublished")
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])

        val data = event.data as StreamPublishedEventData
        assertEquals(id, data.streamRoomId)
        assertNotNull(data.stream)
        assertNotNull(data.stream.id)
        assertFalse(data.stream.tracks.isEmpty())
        assertEquals(user1Id, data.userId) // it was user1 who published

        // user1 was NOT subscribed, so nothing else should be waiting
        expectNoEventOccurs()

        streamApi.removeStream(handle)
        waitForServerSync(1000)
        streamApi2!!.unsubscribeFrom(subscriptionIds)
    }

    @Test
    fun getEvent_StreamSubscribed_StreamRoomId() {
        lateinit var event: Event<*>

        // user1 publishes a stream
        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)
        streamApi.joinStreamRoom(id)
        val handle = streamApi.createStream(id)

        addFakeAudioTrackToStream(streamApi, handle)
        streamApi.publishStream(handle)
        waitForServerSync()

        // user2 joins and subscribes to STREAM_SUBSCRIBE events
        streamApi2!!.joinStreamRoom(id)
        val subscriptionIds = streamApi2!!.subscribeFor(
            listOf(
                streamApi2!!.buildSubscriptionQuery(
                    StreamEventType.STREAM_SUBSCRIBE,
                    StreamEventSelectorType.STREAMROOM_ID,
                    id
                )
            )
        )

        // user2 subscribes to the published stream -> streamSubscribed
        val subs = getStreamsToSubscribe(streamApi, id)
        assertDoesNotFail {
            streamApi2!!.createSubscriberStream(id, subs)
            event = expectAndVerifyEvent(connection2!!.getConnectionId()!!, "streamSubscribed")
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        val data = event.data as StreamSubscriptionEventData
        assertNotNull(data)
        assertEquals(id, data.streamRoomId)
        assertEquals(user2Id, data.userId)
        assertEquals(1, data.subscriptions.size)

        streamApi2!!.unsubscribeFrom(subscriptionIds)
    }

    @Test
    fun getEvent_StreamUnsubscribed_StreamRoomId() {
        lateinit var event: Event<*>

        // user1 - creates stream room and publishes stream
        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)
        streamApi.joinStreamRoom(id)
        streamApi2!!.joinStreamRoom(id)

        val handle = streamApi.createStream(id)
        addFakeVideoTrackToStream(streamApi, handle)
        streamApi.publishStream(handle)
        waitForServerSync()

        // user2 - (stream) subscriber
        val subs = getStreamsToSubscribe(streamApi2!!, id)
        val subscriberHandle = streamApi2!!.createSubscriberStream(id, subs)
        waitForServerSync()

        val subscriptionIds = streamApi.subscribeFor(
            listOf(
                streamApi.buildSubscriptionQuery(
                    StreamEventType.STREAM_UNSUBSCRIBE,
                    StreamEventSelectorType.STREAMROOM_ID,
                    id
                )
            )
        )

        assertDoesNotFail {
            streamApi2!!.removeSubscriberStream(subscriberHandle)
            event = expectAndVerifyEvent(connection!!.getConnectionId()!!, "streamUnsubscribed")
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        val data = event.data as StreamSubscriptionEventData
        assertNotNull(data)
        assertEquals(id, data.streamRoomId)
        assertEquals(user2Id, data.userId)
        assertEquals(1, data.subscriptions.size)

        runBlocking {  delay(5000)}
        streamApi2!!.unsubscribeFrom(subscriptionIds)
    }


    @Test
    fun getEvent_StreamUpdated_StreamRoomId() {
        lateinit var event: Event<*>
        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)
        streamApi.joinStreamRoom(id)

        val handle = streamApi.createStream(id)
        addFakeVideoTrackToStream(streamApi, handle)
        streamApi.publishStream(handle)
        waitForServerSync(5000)

        val subscriptionIds = streamApi.subscribeFor(
            listOf(
                streamApi.buildSubscriptionQuery(
                    StreamEventType.STREAM_UPDATE,
                    StreamEventSelectorType.STREAMROOM_ID,
                    id
                )
            )
        )

        // add a track + update -> streamUpdated
        assertDoesNotFail {
            addFakeVideoTrackToStream(streamApi, handle)
            streamApi.updateStream(handle)
            event = expectAndVerifyEvent("streamUpdated")
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        val data = event.data as StreamUpdatedEventData
        assertNotNull(data)
        assertEquals(id, data.streamRoomId)
        assertEquals(user1Id, data.userId)
        assertEquals(1, data.tracksAdded.size)
        assertTrue(data.tracksRemoved.isEmpty())

        streamApi.removeStream(handle)
        waitForServerSync(1000)
        streamApi.unsubscribeFrom(subscriptionIds)
    }

    @Test
    fun getEvent_StreamUpdated_ContextId() {
        lateinit var event: Event<*>
        val id = streamApi.createStreamRoom(contextId!!, users, users, pubMeta, privMeta, null)
        streamApi.joinStreamRoom(id)
        val handle = streamApi.createStream(id)
        addFakeAudioTrackToStream(streamApi, handle)
        waitForServerSync(5000)

        streamApi.publishStream(handle)
        waitForServerSync(5000)

        val subscriptionIds = streamApi.subscribeFor(
            listOf(
                streamApi.buildSubscriptionQuery(
                    StreamEventType.STREAM_UPDATE,
                    StreamEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
        )

        assertDoesNotFail {
            addFakeVideoTrackToStream(streamApi, handle)
            streamApi.updateStream(handle)
            event = expectAndVerifyEvent("streamUpdated")
        }
        assertEquals(1, event.subscriptions.size)
        assertEquals(subscriptionIds[0], event.subscriptions[0])
        assertNotNull(event.timestamp)

        val data = event.data as StreamUpdatedEventData
        assertNotNull(data)
        assertEquals(id, data.streamRoomId)
        assertEquals(user1Id, data.userId)
        assertEquals(1, data.tracksAdded.size)
        assertTrue(data.tracksRemoved.isEmpty())


        streamApi.removeStream(handle)
        waitForServerSync(1000)
        streamApi.unsubscribeFrom(subscriptionIds)
    }
}
