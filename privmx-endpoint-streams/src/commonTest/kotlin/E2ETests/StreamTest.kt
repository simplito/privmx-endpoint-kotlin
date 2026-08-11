package E2ETests

import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicyWithoutItem
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamInfo
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamRoom
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.stream.StreamApiLow
import com.simplito.kotlin.privmx_endpoint_streams.StreamApi
import com.simplito.kotlin.privmx_endpoint_streams.joinStreamRoom
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StreamTest : BaseTest() {
    lateinit var streamApiLow: StreamApiLow
    lateinit var streamApi: StreamApi

    lateinit var connection2: Connection
    lateinit var streamApiLow2: StreamApiLow
    lateinit var streamApi2: StreamApi

    @BeforeTest
    fun beforeEach() {
        connection = connectAsUser(ConnectionType.User1, bridgeAddress)
        streamApiLow = StreamApiLow(connection!!)
        streamApi = createStreamApi(streamApiLow)

        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        streamApiLow2 = StreamApiLow(connection2)
        streamApi2 = createStreamApi(streamApiLow2)
    }

    @AfterTest
    fun afterEach() {
        try {
            deleteAllRooms(
                streamApi,
                contextId!!
            )

            deleteAllRooms(
                streamApi2,
                contextId!!
            )

            streamApi.close()
            streamApiLow.close()
            connection?.close()

            streamApi2.close()
            streamApiLow2.close()
            connection2.close()

        } catch (_: Exception) {
        }
    }

    /** create room - should be same id and metadata */
    @Test
    fun createRoomIsRetrievableWithSameData() {
        val roomId = streamApi.createStreamRoom(
            contextId!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            null
        )

        val room = streamApi.getStreamRoom(roomId)
        assertEquals(roomId, room.streamRoomId)
        assertEquals(contextId, room.contextId)
        assertContentEquals(publicMeta.encodeToByteArray(), room.publicMeta)
        assertContentEquals(privateMeta.encodeToByteArray(), room.privateMeta)
    }

    /** create room - should appear in member's list, totalAvailable +1 */
    @Test
    fun createRoomAppearsInListForMember() {
        val oldSize: Int = streamApi.listStreamRooms(contextId!!, 0, 100).totalAvailable?.toInt()!!
        val roomId = createStreamRoom()

        val list: PagingList<StreamRoom> = streamApi.listStreamRooms(contextId!!, 0, 100, "desc")
        assertTrue(list.readItems.any { it.streamRoomId == roomId })
        assertEquals(oldSize + 1, list.totalAvailable?.toInt())
    }

    /** create room with null policies - default access policy applies */
    @Test
    fun createRoomNullPolicyAppliesDefaults() {
        // user2 - not a manager
        val roomId = createStreamRoom(contextId, users, users.subList(0, 1))
        val room = streamApi.getStreamRoom(roomId)

        assertDoesNotFail {
            streamApi.updateStreamRoom(
                roomId, users, users.subList(0, 1),
                room.publicMeta, room.privateMeta, room.version!!, false, false, null,
            )
        }

        val updated = streamApi.getStreamRoom(roomId)
        assertFailsWith<PrivmxException> {
            streamApi2.updateStreamRoom(
                roomId, users, users.subList(0, 1),
                updated.publicMeta, updated.privateMeta, updated.version!!, false, false, null,
            )
        }
    }

    /** user2 (non-member) - should not see the room */
    @Test
    fun createRoomNotVisibleToNonMember() {
        val roomId = createStreamRoom(contextId, users.subList(0, 1), users.subList(0, 1))

        // room is not on user2's list
        val list = streamApi2.listStreamRooms(contextId!!, 0, 100, "desc")
        assertFalse(list.readItems.any { it.streamRoomId == roomId })

        // user2 cannot get stream room
        assertFailsWith<PrivmxException> { streamApi2.getStreamRoom(roomId) }
    }

    /** add user2 - should get read access */
    @Test
    fun addedMemberGainsReadAccess() {
        // user2 - not a room member
        val roomId = createStreamRoom(contextId, users.subList(0, 1), users.subList(0, 1))
        assertFailsWith<PrivmxException> { streamApi2.getStreamRoom(roomId) }

        // user1 adds user2 to the users
        val room = streamApi.getStreamRoom(roomId)
        streamApi.updateStreamRoom(
            roomId,
            users,
            users.subList(0, 1),
            room.publicMeta,
            room.privateMeta,
            room.version!!,
            false,
            false,
            null,
        )

        // user2 can now see the room
        runBlocking { delay(1500) }
        assertEquals(roomId, streamApi2.getStreamRoom(roomId).streamRoomId)
    }

    /** remove user2 - should lose read access */
    @Test
    fun removedMemberLosesReadAccess() {
        // user2 initially is a user
        val roomId = createStreamRoom(contextId, users, users.subList(0, 1))
        assertEquals(roomId, streamApi2.getStreamRoom(roomId).streamRoomId)

        // user1 removes user2
        val room = streamApi.getStreamRoom(roomId)
        streamApi.updateStreamRoom(
            roomId,
            users.subList(0, 1),
            users.subList(0, 1),
            room.publicMeta,
            room.privateMeta,
            room.version!!,
            true,
            true,
            null
        )

        runBlocking { delay(1500) }
        assertFailsWith<PrivmxException> { streamApi2.getStreamRoom(roomId) }
    }

    /** update metadata - should be visible to all members */
    @Test
    fun updatedMetadataVisibleToAllMembers() {
        val roomId = createStreamRoom(contextId, users, users)
        val room = streamApi.getStreamRoom(roomId)
        val newPublic = "new public".encodeToByteArray()
        val newPrivate = "new private".encodeToByteArray()

        streamApi.updateStreamRoom(
            roomId,
            users,
            users,
            newPublic,
            newPrivate,
            room.version!!,
            false,
            false,
            null
        )

        // user1 (creator) can see change
        val fromUser1 = streamApi.getStreamRoom(roomId)
        assertContentEquals(newPublic, fromUser1.publicMeta)
        assertContentEquals(newPrivate, fromUser1.privateMeta)
        assertEquals(room.version!! + 1, fromUser1.version)

        // user2 can see change
        val fromUser2 = streamApi2.getStreamRoom(roomId)
        assertContentEquals(newPublic, fromUser2.publicMeta)
        assertContentEquals(newPrivate, fromUser2.privateMeta)
    }

    /** delete room - should not be readable by anyone */
    @Test
    fun deletedRoomNotRetrievableByAnyone() {
        val roomId = createStreamRoom(contextId, users, users)
        streamApi.deleteStreamRoom(roomId)

        runBlocking { delay(1500) }
        assertFailsWith<PrivmxException> { streamApi.getStreamRoom(roomId) }
        assertFailsWith<PrivmxException> { streamApi2.getStreamRoom(roomId) }
    }

    /** create room with bad input  */
    @Test
    fun createRoomRejectsInvalidInput() {
        // invalid contextId
        assertFailsWith<PrivmxException> {
            createStreamRoom(
                streamApi,
                contextId?.replace("3", "ć"),
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                null
            )
        }
        // invalid users / managers
        assertFailsWith<PrivmxException> {
            createStreamRoom(
                streamApi,
                contextId,
                incorrectUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                null
            )
        }
        assertFailsWith<PrivmxException> {
            createStreamRoom(
                streamApi,
                contextId,
                users,
                incorrectUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                null
            )
        }
        // no managers
        assertFailsWith<PrivmxException> {
            createStreamRoom(
                streamApi,
                contextId,
                users,
                emptyUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                null
            )
        }
        // creator - not manager
        assertFailsWith<PrivmxException> {
            createStreamRoom(
                streamApi,
                contextId,
                users.subList(0, 1),
                users.subList(1, 2),
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                null
            )
        }
        // duplicates
        assertFailsWith<PrivmxException> {
            createStreamRoom(
                streamApi,
                contextId,
                sameUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                null
            )
        }
        // metadata over the limit (max 4096)
        val tooLarge = ByteArray(10 * 1024) { 'x'.code.toByte() }
        assertFailsWith<PrivmxException> {
            createStreamRoom(streamApi, contextId, users, users, tooLarge, tooLarge, null)
        }
    }

    /** force new key, members kept - all members still decrypt metadata after rotation */
    @Test
    fun updateRoomForceGenerateNewKeyMembersStillDecrypt() {
        val roomId = createStreamRoom(contextId, users, users) // both are members
        assertEquals(
            roomId,
            streamApi2.getStreamRoom(roomId).streamRoomId
        )

        val room = streamApi.getStreamRoom(roomId)
        val newPublic = "rotated public".encodeToByteArray()
        val newPrivate = "rotated private".encodeToByteArray()
        streamApi.updateStreamRoom(
            roomId,
            users,
            users,
            newPublic,
            newPrivate,
            room.version!!,
            false,
            true,
            null
        )
        runBlocking { delay(1500) }

        // both members received the new key and can still read the re-encrypted metadata
        val fromUser2 = streamApi2.getStreamRoom(roomId)
        assertContentEquals(newPublic, fromUser2.publicMeta)
        assertContentEquals(newPrivate, fromUser2.privateMeta)
    }

    /** force new key while removing a member - the removed member loses access after rotation */
    @Test
    fun updateRoomForceGenerateNewKeyRemovedMemberLosesAccess() {
        val roomId = createStreamRoom(contextId, users, users) // user2 is a member
        assertEquals(roomId, streamApi2.getStreamRoom(roomId).streamRoomId)

        val room = streamApi.getStreamRoom(roomId)
        streamApi.updateStreamRoom(
            roomId,
            users.subList(0, 1),
            users.subList(0, 1),
            room.publicMeta,
            room.privateMeta,
            room.version!!,
            true,
            true,
            null,
        )
        runBlocking { delay(1500) }

        assertFailsWith<PrivmxException> { streamApi2.getStreamRoom(roomId) }
    }

    /** get non-existent room */
    @Test
    fun getRoomNonExistentId() {
        assertFailsWith<PrivmxException> { streamApi.getStreamRoom(contextId!!) }
        assertFailsWith<PrivmxException> { streamApi.getStreamRoom("invalidId") }
    }

    /** join then create stream */
    @Test
    fun joinAsMemberEnablesStreamCreation() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)

        var handle: StreamHandle? = null
        assertDoesNotFail { handle = streamApi.createStream(roomId) }
        assertNotNull(handle)
        assertTrue(handle.value >= 0)
    }

    /** create stream without join */
    @Test
    fun createStreamWithoutJoin() {
        val roomId = createStreamRoom()
        assertFailsWith<IllegalStateException> { streamApi.createStream(roomId) }
    }

    /** Join as non-member */
    @Test
    fun nonMemberCannotJoin() {
        val roomId = createStreamRoom(contextId, users.subList(0, 1), users.subList(0, 1))
        assertFailsWith<PrivmxException> { streamApi2.joinStreamRoom(roomId) }
    }

    /** join, leave, join again */
    @Test
    fun joinLeaveRejoin() {
        val roomId = createStreamRoom()

        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)

        streamApi.leaveStreamRoom(roomId)

        assertDoesNotFail {
            streamApi.joinStreamRoom(roomId)
        }

        // after re-joining a stream can still be created
        assertTrue(streamApi.createStream(roomId).value >= 0)
    }

    /** two members join same room */
    @Test
    fun twoMembersJoinSameRoom() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)

        assertDoesNotFail {
            streamApi2.joinStreamRoom(roomId)
        }
    }

    /** join twice */
    @Test
    fun joinAlreadyJoinedRoom() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)

        assertFailsWith<PrivmxException> { streamApi.joinStreamRoom(roomId) }
    }

    /** leave without join */
    @Test
    fun leaveWithoutJoin() {
        val roomId = createStreamRoom()
        assertFailsWith<PrivmxException> { streamApi.leaveStreamRoom(roomId) }
    }

    /** leave twice */
    @Test
    fun leaveTwiceSecondRejected() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi.leaveStreamRoom(roomId)
        assertFailsWith<PrivmxException> { streamApi.leaveStreamRoom(roomId) }
    }

    /** removed user2 - should not be able to join anymore */
    @Test
    fun removedMemberCannotJoinAfterwards() {
        val roomId = createStreamRoom() // user2 - room member
        val room = streamApi.getStreamRoom(roomId)

        // remove user2 from users and managers
        streamApi.updateStreamRoom(
            roomId, users.subList(0, 1), users.subList(0, 1),
            room.publicMeta, room.privateMeta, room.version!!, true, true, null,
        )

        runBlocking { delay(1500) }
        assertFailsWith<PrivmxException> { streamApi2.joinStreamRoom(roomId) }
    }

    /** force-update removing active user2 - should disconnect user2, room stays valid for user1 */
    @Test
    fun forceUpdateDisconnectsActiveMember() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)

        val room = streamApi.getStreamRoom(roomId)
        streamApi.updateStreamRoom(
            roomId,
            users.subList(0, 1),
            users.subList(0, 1),
            room.publicMeta,
            room.privateMeta,
            room.version!!,
            true,
            true,
            null
        )

        runBlocking { delay(1500) }
        val updated = streamApi.getStreamRoom(roomId)
        assertEquals(1, updated.users.size)
        assertEquals(1, updated.managers.size)


        // removed user2 cannot resume activity in the room
        assertFailsWith<PrivmxException> { streamApi2.joinStreamRoom(roomId) }
    }

    /** non-force update removing user2 (correct version, no key rotation) - user2 loses access */
    @Test
    fun updateRemovingMemberNoForceMemberLosesAccess() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)

        val room = streamApi.getStreamRoom(roomId)
        streamApi.updateStreamRoom(
            roomId,
            users.subList(0, 1),
            users.subList(0, 1),
            room.publicMeta,
            room.privateMeta,
            room.version!!,
            false,
            false, // force
            null
        )

        runBlocking { delay(1500) }
        val updated = streamApi.getStreamRoom(roomId)
        assertEquals(1, updated.users.size)
        assertEquals(1, updated.managers.size)

        // user2 cannot get or join room
        assertFailsWith<PrivmxException> { streamApi2.getStreamRoom(roomId) }
        assertFailsWith<PrivmxException> { streamApi2.joinStreamRoom(roomId) }
    }

    /** join with bad id */
    @Test
    fun joinInvalidId() {
        assertFailsWith<PrivmxException> { streamApi.joinStreamRoom(contextId!!) }
        assertFailsWith<PrivmxException> { streamApi.joinStreamRoom("invalidId") }
    }

    /** user1 (publisher) leaves - stream should disappear for the other user */
    @Test
    fun publisherLeavesStreamDisappearsForOther() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)

        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)
        streamApi.publishStream(handle)

        // user2 sees one stream
        runBlocking { delay(1500) }
        assertEquals(1, streamApi2.listStreams(roomId).size)

        // the publisher leaves the room
        streamApi.leaveStreamRoom(roomId)

        runBlocking { delay(1500) }
        assertTrue(streamApi2.listStreams(roomId).isEmpty())
    }

    /** create stream twice  */
    @Test
    fun createStreamTwiceInSameRoom() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)

        streamApi.createStream(roomId)
        assertFailsWith<IllegalStateException> { streamApi.createStream(roomId) }
    }

    @Test
    fun createStreamAfterLeave() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)

        streamApi.leaveStreamRoom(roomId)
        assertFailsWith<IllegalStateException> { streamApi.createStream(roomId) }
    }

    /** removeStream twice - second has no active stream */
    @Test
    fun removeStreamTwice() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)

        streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        streamApi.removeStream(handle)
        runBlocking { delay(1500) }
        assertFailsWith<IllegalStateException> { streamApi.removeStream(handle) }
    }

    /** publish without track  */
    @Test
    fun publishWithoutTrack() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)

        assertFailsWith<PrivmxException> { streamApi.publishStream(handle) }
    }

    /** publish with 1 track - publisher should see 1 stream, 1 track */
    @Test
    fun publishWithTracksStreamVisibleToPublisher() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)

        val result = streamApi.publishStream(handle)

        runBlocking { delay(1500) }

        assertEquals(1, result.data!!.stream.tracks.size)
        val streams: List<StreamInfo> = streamApi.listStreams(roomId)
        assertEquals(1, streams.size)
        assertEquals(1, streams[0].tracks.size)
        assertEquals(result.data!!.stream.id, streams[0].id)
    }

    /** published stream - should be visible to other member */
    @Test
    fun publishedStreamIsVisibleToOtherMember() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)

        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)
        val result = streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        val seenByUser2 = streamApi2.listStreams(roomId)
        assertEquals(1, seenByUser2.size)
        assertEquals(result.data!!.stream.id, seenByUser2[0].id)
        assertEquals(result.data!!.stream.userId, seenByUser2[0].userId)
    }

    /** unpublish - stream should disappear from list */
    @Test
    fun unpublishRemovesStreamFromList() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)

        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)

        streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        streamApi.removeStream(handle)
        runBlocking { delay(1500) }

        assertTrue(streamApi.listStreams(roomId).isEmpty())
    }

    /** two audio tracks - both should be present */
    @Test
    fun publishWithTwoAudioTracksBothPresent() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)

        addFakeAudioTrackToStream(streamApi, handle)
        addFakeAudioTrackToStream(streamApi, handle)

        val result = streamApi.publishStream(handle)
        assertEquals(2, result.data!!.stream.tracks.size)
    }

    /** two video tracks - both should be present */
    @Test
    fun publishWithTwoVideoTracksBothPresent() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)

        addFakeVideoTrackToStream(streamApi, handle)
        addFakeVideoTrackToStream(streamApi, handle)

        val result = streamApi.publishStream(handle)
        assertEquals(2, result.data!!.stream.tracks.size)
    }

    /** add same track twice */
    @Test
    fun addSameTrackTwice() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)

        val track = addFakeAudioTrackToStream(streamApi, handle)
        assertFailsWith<IllegalStateException> { streamApi.addTrack(handle, track) }
    }

    /** remove track never added */
    @Test
    fun removeTrackNeverAdded() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)

        val handle = streamApi.createStream(roomId)
        addFakeVideoTrackToStream(streamApi, handle)
        streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        // a track created but never added to this stream
        val neverAdded = streamApi.trackFactory.createAudioTrack("audioNeverAdded")

//        assertFailsWith<IllegalStateException> {
        // todo - should the user be notified?
        streamApi.removeTrack(handle, neverAdded)
//        }
    }

    /** remove then add track back */
    @Test
    fun removeThenReaddTrack() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)
        val track = addFakeAudioTrackToStream(streamApi, handle)

        streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        // remove + update
        streamApi.removeTrack(handle, track)
        streamApi.updateStream(handle)
        runBlocking { delay(1500) }

        // add back + update
        streamApi.addTrack(handle, track)
        streamApi.updateStream(handle)
        runBlocking { delay(1500) }

        val streams = streamApi.listStreams(roomId)
        assertEquals(1, streams.size)
        assertTrue(streams[0].tracks.isNotEmpty())
    }

    /** add track after leave */
    @Test
    fun addTrackAfterLeaveRoom() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)
        streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        streamApi.leaveStreamRoom(roomId)
        runBlocking { delay(1500) }

        assertFailsWith<IllegalStateException> { addFakeVideoTrackToStream(streamApi, handle) }
    }

    /** addTrack before publish is invisible to other members */
    @Test
    fun addTrackBeforePublishNotVisibleToOthers() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)
        val handle = streamApi2.createStream(roomId)

        addFakeAudioTrackToStream(streamApi2, handle)
        runBlocking { delay(1500) }

        assertTrue(streamApi.listStreams(roomId).isEmpty())
    }

    /** remove track after leave */
    @Test
    fun removeTrackAfterLeaveRoom() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)

        val handle = streamApi.createStream(roomId)
        val track = addFakeAudioTrackToStream(streamApi, handle)
        streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        streamApi.leaveStreamRoom(roomId)
        runBlocking { delay(1500) }

        assertFailsWith<IllegalStateException> { streamApi.removeTrack(handle, track) }
    }

    /** remove track before publish - track should stay but be disabled */
    @Test
    fun removeTrackBeforePublishTrackPersistsAsDisabled() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)

        val track = addFakeAudioTrackToStream(streamApi, handle)
        streamApi.removeTrack(handle, track)

        val result = streamApi.publishStream(handle)
        assertEquals(1, result.data!!.stream.tracks.size)
        assertEquals(true, result.data!!.stream.tracks[0].disabled)
    }

    /** subscribe without join */
    @Test
    fun subscribeWithoutJoin() {
        val roomId = createStreamRoom()

        assertFailsWith<IllegalStateException> {
            streamApi2.createSubscriberStream(roomId, listOf())
        }

        assertFailsWith<IllegalStateException> {
            streamApi2.createSubscriberStream(
                roomId, getStreamsToSubscribe(streamApi, roomId)
            )
        }
    }

    /** subscribe empty list */
    @Test
    fun subscribeEmptyList() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)

        assertFailsWith<PrivmxException> {
            streamApi.createSubscriberStream(roomId, emptyList())
        }
    }

    /** subscribe to published stream */
    @Test
    fun subscribeToPublishedStream() {
        val (roomId, streamId) = publishedStreamWithSecondMemberJoined()
        val subs = getStreamsToSubscribe(streamApi, roomId).filter { it.streamId == streamId }

        assertDoesNotFail {
            streamApi2.createSubscriberStream(
                roomId,
                subs
            )
        }
    }

    /** subscribe same stream twice */
    @Test
    fun subscribeSameStreamTwice() {
        val (roomId, streamId) = publishedStreamWithSecondMemberJoined()
        val subs = getStreamsToSubscribe(streamApi, roomId).filter { it.streamId == streamId }

        assertFailsWith<IllegalStateException> {
            streamApi2.createSubscriberStream(roomId, subs)
            streamApi2.createSubscriberStream(roomId, subs)
        }
    }

    /** subscribe to unpublished stream */
    @Test
    fun subscribeToUnpublishedStream() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)

        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)
        streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        // get stream before unsubscribing
        val subs = getStreamsToSubscribe(streamApi, roomId)

        streamApi.removeStream(handle)
        runBlocking { delay(1500) }

        assertFailsWith<PrivmxException> {
            streamApi2.createSubscriberStream(roomId, subs)
        }
    }

    @Test
    fun updateSubscriberStreamAfterUnsubscribe() {
        val (roomId, streamId) = publishedStreamWithSecondMemberJoined()
        val subs = getStreamsToSubscribe(streamApi, roomId).filter { it.streamId == streamId }
        val handle = streamApi2.createSubscriberStream(roomId, subs)

        streamApi2.removeSubscriberStream(handle)
        assertFailsWith<IllegalStateException> {
            streamApi2.updateSubscriberStream(handle, subs, emptyList())
        }
    }

    /** modify (add + remove, sub exists) */
    @Test
    fun modifyAddAndRemoveExisting() {
        val (roomId, streamId) = publishedStreamWithSecondMemberJoined()
        val subs = getStreamsToSubscribe(streamApi, roomId).filter { it.streamId == streamId }
        val subscriberHandle = streamApi2.createSubscriberStream(roomId, subs)

        assertDoesNotFail {
            streamApi2.updateSubscriberStream(subscriberHandle, subs, subs)
        }
    }

    /** modify (remove only) */
    @Test
    fun modifyRemoveExisting() {
        val (roomId, streamId) = publishedStreamWithSecondMemberJoined()
        val subs = getStreamsToSubscribe(streamApi, roomId).filter { it.streamId == streamId }
        val subscriberHandle = streamApi2.createSubscriberStream(roomId, subs)

        assertDoesNotFail {
            streamApi2.updateSubscriberStream(subscriberHandle, emptyList(), subs)
        }
    }

    /** modify (empty add + empty remove) */
    @Test
    fun modifyEmptyAddAndRemove() {
        val (roomId, streamId) = publishedStreamWithSecondMemberJoined()
        val subs = getStreamsToSubscribe(streamApi, roomId).filter { it.streamId == streamId }
        val subscriberHandle = streamApi2.createSubscriberStream(roomId, subs)

        assertFailsWith<PrivmxException> {
            streamApi2.updateSubscriberStream(subscriberHandle, emptyList(), emptyList())
        }
    }

    /** modify (add duplicate)  */
    @Test
    fun modifyDuplicateAdd() {
        val (roomId, streamId) = publishedStreamWithSecondMemberJoined()
        val subs = getStreamsToSubscribe(streamApi, roomId).filter { it.streamId == streamId }
        val subscriberHandle = streamApi2.createSubscriberStream(roomId, subs)

        assertDoesNotFail{
            streamApi2.updateSubscriberStream(subscriberHandle, subs, emptyList())
        }
    }

    /** unsubscribe active sub */
    @Test
    fun removeSubscriberStream() {
        val (roomId, streamId) = publishedStreamWithSecondMemberJoined()
        val subs = getStreamsToSubscribe(streamApi, roomId).filter { it.streamId == streamId }
        val subscriberHandle = streamApi2.createSubscriberStream(roomId, subs)

        assertDoesNotFail {
            streamApi2.removeSubscriberStream(subscriberHandle)
        }
    }

    /** unsubscribe twice */
    @Test
    fun unsubscribeTwiceSecondRejected() {
        val (roomId, streamId) = publishedStreamWithSecondMemberJoined()
        val subs = getStreamsToSubscribe(streamApi, roomId).filter { it.streamId == streamId }
        val subscriberHandle = streamApi2.createSubscriberStream(roomId, subs)

        streamApi2.removeSubscriberStream(subscriberHandle)
        assertFails { streamApi2.removeSubscriberStream(subscriberHandle) }
    }

    /** delete room by manager (non-creator)  */
    @Test
    fun deleteRoomByManagerNonCreator() {
        // user2 is a manager
        val roomId = createStreamRoom(contextId, users.subList(0, 1), users)

        assertDoesNotFail { streamApi2.deleteStreamRoom(roomId) }
    }

    /** delete room by plain user (not manager)  */
    @Test
    fun deleteRoomByPlainUser() {
        // user2 is a user but not a manager
        val roomId = createStreamRoom(contextId, users, users.subList(0, 1))
        assertFailsWith<PrivmxException> { streamApi2.deleteStreamRoom(roomId) }
    }

    /** delete room by non-member  */
    @Test
    fun deleteRoomByNonMember() {
        val roomId = createStreamRoom(contextId, users.subList(0, 1), users.subList(0, 1))
        assertFailsWith<PrivmxException> { streamApi2.deleteStreamRoom(roomId) }
    }

    /** delete room twice  */
    @Test
    fun deleteRoomTwiceSecondRejected() {
        val roomId = createStreamRoom()
        streamApi.deleteStreamRoom(roomId)
        assertFailsWith<PrivmxException> { streamApi.deleteStreamRoom(roomId) }
    }

    /** delete room with bad id  */
    @Test
    fun deleteRoomInvalidId() {
        assertFailsWith<PrivmxException> { streamApi.deleteStreamRoom(contextId!!) }
        assertFailsWith<PrivmxException> { streamApi.deleteStreamRoom("invalidId") }
    }

    /** delete room with active stream - should work, then room not accessible */
    @Test
    fun deleteRoomWithActiveStream() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)

        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)
        streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        assertDoesNotFail { streamApi.deleteStreamRoom(roomId) }
        runBlocking { delay(1500) }

        assertFailsWith<PrivmxException> { streamApi.getStreamRoom(roomId) }
        assertFailsWith<PrivmxException> { streamApi2.listStreams(roomId) }
    }

// ---------------

    /** update room by non-manager  */
    @Test
    fun updateRoomByNonManager() {
        // user2 is a user but not a manager
        val roomId = createStreamRoom(contextId, users, users.subList(0, 1))
        val room = streamApi.getStreamRoom(roomId)

        assertFailsWith<PrivmxException> {
            streamApi2.updateStreamRoom(
                roomId, users, users, room.publicMeta, room.privateMeta,
                room.version!!, false, false, null,
            )
        }
    }

    @Test
    fun updateRoomInvalidId() {
        assertFailsWith<PrivmxException> {
            streamApi.updateStreamRoom(
                "invalidId", users, users,
                publicMeta.encodeToByteArray(), privateMeta.encodeToByteArray(),
                1L, false, false, null
            )
        }
    }

    /** unpublish someone else's stream  */
    @Test
    fun unpublishOthersStream() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)

        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)
        streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        assertFailsWith<IllegalStateException> { streamApi2.removeStream(handle) }
    }

    /** update someone else's stream (even as manager) */
    @Test
    fun updateOthersStream() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)

        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)
        streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        streamApi2.joinStreamRoom(roomId)
        assertFailsWith<IllegalStateException> { streamApi2.updateStream(handle) }
    }

    /** update with wrong version, force=false  */
    @Test
    fun updateRoomWrongVersionNoForce() {
        val roomId = createStreamRoom()
        val wrongVersion = -1L

        assertFailsWith<PrivmxException> {
            streamApi.updateStreamRoom(
                roomId,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                wrongVersion,
                false,
                false,
                null
            )
        }
    }

    /** update with wrong version, force=true */
    @Test
    fun updateRoomWrongVersionWithForce() {
        val roomId = createStreamRoom()
        val wrongVersion = 1000L

        assertDoesNotFail {
            streamApi.updateStreamRoom(
                roomId,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                wrongVersion,
                true,
                false,
                null
            )
        }
    }

    /** update with correct version, force=true */
    @Test
    fun updateRoomCorrectVersionWithForce() {
        val roomId = createStreamRoom()
        val room = streamApi.getStreamRoom(roomId)

        streamApi.updateStreamRoom(
            roomId,
            users,
            users,
            room.publicMeta,
            room.privateMeta,
            room.version!!,
            true,
            false,
            null
        )

        val updated = streamApi.getStreamRoom(roomId)
        assertEquals(room.version!! + 1, updated.version)
    }

    /** publish same stream twice */
    @Test
    fun publishSameHandleTwiceSecondRejected() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)

        streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        assertFails { streamApi.publishStream(handle) }
    }

    /** publish old handle after being removed from room */
    @Test
    fun publishAfterRemovedFromRoom() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)

        val handle = streamApi2.createStream(roomId)
        addFakeAudioTrackToStream(streamApi2, handle)
        runBlocking { delay(500) }

        // user1 removes user2 from the room
        val room = streamApi.getStreamRoom(roomId)
        streamApi.updateStreamRoom(
            roomId, users.subList(0, 1), users.subList(0, 1),
            room.publicMeta, room.privateMeta, room.version!!, true, true, null,
        )
        runBlocking { delay(1000) }

        assertFailsWith<PrivmxException> { streamApi2.publishStream(handle) }
    }

    /** republish same handle after unpublish  */
    @Test
    fun republishSameHandleAfterUnpublish() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)

        streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        streamApi.removeStream(handle)
        runBlocking { delay(1500) }

        assertFailsWith<IllegalStateException> { streamApi.publishStream(handle) }
    }

    /** update: add track then update - track count should grow in list */
    @Test
    fun updateStreamAddTrackReflectedInList() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)

        val result = streamApi.publishStream(handle)
        assertEquals(1, result.data!!.stream.tracks.size)
        runBlocking { delay(1500) }

        addFakeVideoTrackToStream(streamApi, handle)
        streamApi.updateStream(handle)
        runBlocking { delay(1500) }

        val streams = streamApi.listStreams(roomId)
        assertEquals(1, streams.size)
        assertEquals(2, streams[0].tracks.size)
    }

    /** update: remove all tracks then update - stream should persist */
    @Test
    fun updateStreamRemoveAllTracksStreamPersists() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)

        val audio = addFakeAudioTrackToStream(streamApi, handle)
        val video = addFakeVideoTrackToStream(streamApi, handle)
        streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        streamApi.removeTrack(handle, audio)
        streamApi.removeTrack(handle, video)
        streamApi.updateStream(handle)
        runBlocking { delay(1500) }

        val streams = streamApi.listStreams(roomId)
        assertEquals(1, streams.size)
        assertTrue(streams[0].tracks.isNotEmpty())
        assertTrue(streams[0].tracks.all { it.disabled })
    }

    /** update after unpublish  */
    @Test
    fun updateAfterUnpublish() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)
        addFakeVideoTrackToStream(streamApi, handle)

        streamApi.publishStream(handle)
        runBlocking { delay(1500) }
        streamApi.removeStream(handle)
        runBlocking { delay(1500) }

        assertFailsWith<IllegalStateException> { streamApi.updateStream(handle) }
    }

    /** update after leave  */
    @Test
    fun updateAfterLeave() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)
        streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        streamApi.leaveStreamRoom(roomId)
        runBlocking { delay(1000) }

        assertFailsWith<IllegalStateException> { streamApi.updateStream(handle) }
    }

    /** addTrack + updateStream is reflected for another member */
    @Test
    fun updateStreamAddTrackVisibleToOtherMember() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)
        streamApi.publishStream(handle)
        runBlocking { delay(1500) }
        assertEquals(1, streamApi2.listStreams(roomId).first().tracks.size)

        addFakeVideoTrackToStream(streamApi, handle)
        streamApi.updateStream(handle)
        runBlocking { delay(1500) }
        assertEquals(2, streamApi2.listStreams(roomId).first().tracks.size)
    }

    /** list rooms - pagination: skip, limit, sort order, cursor */
    @Test
    fun listStreamRoomsPagination() {
        val room1 = createStreamRoom()
        val room2 = createStreamRoom()
        val room3 = createStreamRoom()

        // skip > total -> empty page, total still known
        var page: PagingList<StreamRoom> = streamApi.listStreamRooms(contextId!!, 4, 1, "desc")
        assertEquals(3, page.totalAvailable?.toInt())
        assertTrue(page.readItems.isEmpty())

        // limit 1, desc -> newest first
        page = streamApi.listStreamRooms(contextId!!, 0, 1, "desc")
        assertEquals(1, page.readItems.size)
        assertEquals(room3, page.readItems.first().streamRoomId)

        // skip 1, limit 3, asc -> two remaining, oldest-skipped-one first
        page = streamApi.listStreamRooms(contextId!!, 1, 3, "asc")
        assertEquals(2, page.readItems.size)
        assertEquals(room2, page.readItems.first().streamRoomId)

        // sortBy createDate, asc -> all three
        page = streamApi.listStreamRooms(contextId!!, 0, 3, "asc", null, "createDate")
        assertEquals(3, page.readItems.size)
    }

    /** list rooms with bad input  */
    @Test
    fun listStreamRoomsRejectsInvalidInput() {
        createStreamRoom()

        // bad contextId
        assertFailsWith<PrivmxException> {
            streamApi.listStreamRooms(
                "invalidContext",
                0,
                1,
                "desc"
            )
        }
        // limit < 0
        assertFailsWith<PrivmxException> { streamApi.listStreamRooms(contextId!!, 0, -1, "desc") }
        // limit == 0
        assertFailsWith<PrivmxException> { streamApi.listStreamRooms(contextId!!, 0, 0, "desc") }
        // bad sortOrder
        assertFailsWith<PrivmxException> { streamApi.listStreamRooms(contextId!!, 0, 1, "wrong") }
        // bad lastId
        assertFailsWith<PrivmxException> {
            streamApi.listStreamRooms(
                contextId!!,
                0,
                1,
                "desc",
                "wrong"
            )
        }
        // bad sortBy
        assertFailsWith<PrivmxException> {
            streamApi.listStreamRooms(
                contextId!!,
                0,
                1,
                "desc",
                null,
                "wrong"
            )
        }
    }

    /** list rooms after delete - deleted one should be gone */
    @Test
    fun listStreamRoomsAfterDeleteExcludesDeleted() {
        val id1 = createStreamRoom()
        val id2 = createStreamRoom()
        streamApi.deleteStreamRoom(id1)
        runBlocking { delay(1000) }

        val page = streamApi.listStreamRooms(contextId!!, 0, 100, "desc")
        val ids = page.readItems.map { it.streamRoomId }
        assertFalse(ids.contains(id1))
        assertTrue(ids.contains(id2))
        assertEquals(1, page.totalAvailable?.toInt())
    }

    /** list rooms in an empty context - empty list, not an error */
    @Test
    fun listStreamRoomsEmptyContextReturnsEmpty() {
        // todo - how does this work? is the room still visible for a while?
        val page = streamApi.listStreamRooms(contextId!!, 0, 100, "desc")

        page.readItems.forEach {
            println("PRINT:   " + it.state)
        }
        println("PRINT:   " + page.readItems.size)

        assertTrue(page.readItems.isEmpty())
        assertEquals(0, page.totalAvailable?.toInt())
    }

    @Test
    fun listStreamsInvalidIdIsRejected() {
        assertFailsWith<PrivmxException> { streamApi.listStreams("invalidId") }
    }

    /** listStreams excludes a created-but-unpublished stream */
    @Test
    fun listStreamsExcludesUnpublished() {

        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)

        addFakeVideoTrackToStream(streamApi, handle)

        runBlocking { delay(1500) }
        assertTrue(streamApi.listStreams(roomId).isEmpty())

        streamApi.publishStream(handle)
        runBlocking { delay(3000) }

        assertEquals(1, streamApi.listStreams(roomId).size)
    }

    /** two publishers - each should see both streams */
    @Test
    fun twoPublishersEachSeesTwoStreams() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)

        val handle1 = streamApi.createStream(roomId)
        val handle2 = streamApi2.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle1)
        addFakeVideoTrackToStream(streamApi2, handle2)

        streamApi.publishStream(handle1)
        streamApi2.publishStream(handle2)
        runBlocking { delay(1500) }

        assertEquals(2, streamApi.listStreams(roomId).size)
        assertEquals(2, streamApi2.listStreams(roomId).size)
    }

    /** late joiner - should see an already-published stream */
    @Test
    fun lateJoinerSeesExistingStream() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)

        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)
        val result = streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        // user2 joins after publish
        streamApi2.joinStreamRoom(roomId)
        val seen = streamApi2.listStreams(roomId)
        assertEquals(1, seen.size)
        assertEquals(result.data!!.stream.id, seen[0].id)
    }

    /** one of two publishers unpublishes - the other stays visible */
    @Test
    fun oneOfTwoPublishersUnpublishesOtherStillVisible() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)

        val handle1 = streamApi.createStream(roomId)
        val handle2 = streamApi2.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle1)
        addFakeAudioTrackToStream(streamApi2, handle2)
        streamApi.publishStream(handle1)
        streamApi2.publishStream(handle2)
        runBlocking { delay(1500) }
        assertEquals(2, streamApi2.listStreams(roomId).size)

        // user1 unpublishes -> user2 sees only one (their own)
        streamApi.removeStream(handle1)
        runBlocking { delay(1500) }
        assertEquals(1, streamApi2.listStreams(roomId).size)
    }

    /** room deleted during active stream */
    @Test
    fun roomDeletedDuringActiveStreamBothClientsGetError() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)

        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)
        streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        streamApi.deleteStreamRoom(roomId)
        runBlocking { delay(1000) }

        assertFailsWith<PrivmxException> { streamApi.getStreamRoom(roomId) }
        assertFailsWith<PrivmxException> { streamApi2.listStreams(roomId) }
    }


    // ------------------------

    /** subscribe to a single track of a multi-track stream */
    @Test
    fun subscribeToSubsetOfTracks() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)
        addFakeVideoTrackToStream(streamApi, handle)
        streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        streamApi2.joinStreamRoom(roomId)
        val singleTrack = getStreamsToSubscribe(streamApi, roomId).take(1)

        assertDoesNotFail {
            streamApi2.createSubscriberStream(roomId, singleTrack)
        }
    }

    /** modify subscriber: add a second track to an existing subscription */
    @Test
    fun modifyAddSecondTrack() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        val handle = streamApi.createStream(roomId)
        addFakeAudioTrackToStream(streamApi, handle)
        addFakeVideoTrackToStream(streamApi, handle)

        streamApi.publishStream(handle)
        runBlocking { delay(1500) }

        streamApi2.joinStreamRoom(roomId)
        val all = getStreamsToSubscribe(streamApi, roomId)
        val first = all.take(1)
        val second = all.drop(1).take(1)

        val subscriberHandle = streamApi2.createSubscriberStream(roomId, first)
        runBlocking { delay(1500) }

        assertDoesNotFail {
            streamApi2.updateSubscriberStream(subscriberHandle, second, emptyList())
        }
    }


    /** empty room - nobody joined yet */
    @Test
    fun participantsEmptyWhenNobodyJoined() {
        val roomId = createStreamRoom()
        assertTrue(streamApi.listStreamRoomParticipants(roomId).isEmpty())
    }

    /** one member joins - one participant */
    @Test
    fun participantsOneAfterSingleJoin() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        runBlocking { delay(1500) }

        assertEquals(1, streamApi.listStreamRoomParticipants(roomId).size)
    }

    /** two members join - two participants, visible to both */
    @Test
    fun participantsTwoAfterBothJoin() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)
        runBlocking { delay(1500) }

        assertEquals(2, streamApi.listStreamRoomParticipants(roomId).size)
        assertEquals(2, streamApi2.listStreamRoomParticipants(roomId).size)
    }

    /** member leaves - participant count drops */
    @Test
    fun participantsDropAfterLeave() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)
        runBlocking { delay(1500) }
        assertEquals(2, streamApi.listStreamRoomParticipants(roomId).size)

        streamApi2.leaveStreamRoom(roomId)
        runBlocking { delay(1500) }
        assertEquals(1, streamApi.listStreamRoomParticipants(roomId).size)
    }

    /** subscriber counts as a participant once joined + subscribed */
    @Test
    fun participantsIncludeSubscriber() {
        val (roomId, _) = publishedStreamWithSecondMemberJoined() // user1 publishes, user2 joined
        val subs = getStreamsToSubscribe(streamApi, roomId)
        streamApi2.createSubscriberStream(roomId, subs)
        runBlocking { delay(1500) }

        // both the publisher and the subscriber are present
        assertEquals(2, streamApi.listStreamRoomParticipants(roomId).size)
    }

    /** rejoin - participant reappears */
    @Test
    fun participantsNoDuplicateAfterRejoin() {
        val roomId = createStreamRoom()

        // user1 and user2 join room
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)

        // user1 leaves room
        streamApi.leaveStreamRoom(roomId)
        runBlocking { delay(1500) }

        // user 1 joins room again
        streamApi.joinStreamRoom(roomId)
        runBlocking { delay(1500) }

        assertEquals(2, streamApi.listStreamRoomParticipants(roomId).size)
    }

    /** non-member cannot list participants */
    @Test
    fun participantsNonMemberIsRejected() {
        val roomId = createStreamRoom(contextId, users.subList(0, 1), users.subList(0, 1))
        assertFailsWith<PrivmxException> { streamApi2.listStreamRoomParticipants(roomId) }
    }

    /** forced-out member disappears from participants */
    @Test
    fun participantsRemovedMemberDisappears() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        streamApi2.joinStreamRoom(roomId)
        runBlocking { delay(1500) }
        assertEquals(2, streamApi.listStreamRoomParticipants(roomId).size)

        val room = streamApi.getStreamRoom(roomId)
        streamApi.updateStreamRoom(
            roomId, users.subList(0, 1), users.subList(0, 1),
            room.publicMeta, room.privateMeta, room.version!!, true, true, null,
        )
        runBlocking { delay(1500) }

        assertEquals(1, streamApi.listStreamRoomParticipants(roomId).size)
    }

    /** bad id  */
    @Test
    fun participantsInvalidId() {
        assertFailsWith<PrivmxException> { streamApi.listStreamRoomParticipants(contextId!!) }
        assertFailsWith<PrivmxException> { streamApi.listStreamRoomParticipants("invalidId") }
    }

    /** create room with policy  */
    @Test
    fun createRoomWithPolicyAppliesAndIsReadable() {
        val policy = ContainerPolicyWithoutItem(
            "user",
            "manager",
            "manager",
            "manager",
            "yes",
            "no",
        )

        val roomId = streamApi.createStreamRoom(
            contextId!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            policy
        )

        val room = streamApi.getStreamRoom(roomId)
        assertEquals("user", room.policy.get)
        assertEquals("manager", room.policy.update)
        assertEquals("manager", room.policy.delete)
    }

    /** custom policy get=manager */
    @Test
    fun createRoomWithPolicyIsEnforced() {
        // get="manager"
        val policy = ContainerPolicyWithoutItem(
            "manager",
            "manager",
            "manager",
            "manager",
            "yes",
            "no"
        )

        // user2 user - not manager
        val roomId = streamApi.createStreamRoom(
            contextId!!,
            users,
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            policy
        )
        runBlocking { delay(1500) }

        // user1 (manager) 
        assertEquals(roomId, streamApi.getStreamRoom(roomId).streamRoomId)

        // user2 (plain user) 
        assertFailsWith<PrivmxException> { streamApi2.getStreamRoom(roomId) }
    }

    @Test
    fun dropBrokenFramesWithoutSession() {
        val roomId = createStreamRoom()
        assertFailsWith<IllegalStateException> { streamApi.dropBrokenFrames(roomId, true) }
    }

    @Test
    fun dropBrokenFramesWithActiveSession() {
        val roomId = createStreamRoom()
        streamApi.joinStreamRoom(roomId)
        assertDoesNotFail { streamApi.dropBrokenFrames(roomId, true) }
        assertDoesNotFail { streamApi.dropBrokenFrames(roomId, false) }
    }

    @Test
    fun setConnectionStateObserverWithoutSession() {
        val roomId = createStreamRoom()
        assertFailsWith<IllegalStateException> {
            streamApi.setConnectionStateObserver(roomId) { }
        }
    }

    @Test
    fun getSomeoneElsesRoomAsPublicUser() {
        val publicConnection = connectAsUser(ConnectionType.Public, bridgeAddress)
        val publicStreamApiLow = StreamApiLow(publicConnection)
        val publicStreamApi = createStreamApi(publicStreamApiLow)

        val roomId = createStreamRoom(contextId, users, users)
        try {
            assertFailsWith<PrivmxException> {
                publicStreamApi.getStreamRoom(roomId)
            }
        } finally {
            publicStreamApi.close()
            publicStreamApiLow.close()
            publicConnection.close()
        }
    }

    @Test
    fun createStreamRoomAsPublicUser() {
        val publicConnection = connectAsUser(ConnectionType.Public, bridgeAddress)
        val publicStreamApiLow = StreamApiLow(publicConnection)
        val publicStreamApi = createStreamApi(publicStreamApiLow)

        try {
            assertFailsWith<PrivmxException> {
                publicStreamApi.createStreamRoom(
                    contextId!!,
                    users,
                    users,
                    publicMeta.encodeToByteArray(),
                    privateMeta.encodeToByteArray(),
                    null
                )
            }
        } finally {
            publicStreamApi.close()
            publicStreamApiLow.close()
            publicConnection.close()
        }
    }

    @Test
    fun joinStreamRoomAsPublicUser() {
        val publicConnection = connectAsUser(ConnectionType.Public, bridgeAddress)
        val publicStreamApiLow = StreamApiLow(publicConnection)
        val publicStreamApi = createStreamApi(publicStreamApiLow)

        val roomId = createStreamRoom(contextId, users, users)

        try {
            assertFailsWith<PrivmxException> { publicStreamApi.joinStreamRoom(roomId) }
        } finally {
            publicStreamApi.close()
            publicStreamApiLow.close()
            publicConnection.close()
        }
    }
}