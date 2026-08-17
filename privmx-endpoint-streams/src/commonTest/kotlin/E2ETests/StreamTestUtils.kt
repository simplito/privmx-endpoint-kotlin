package E2ETests

import E2ETests.BaseTest.Companion.contextId
import E2ETests.BaseTest.Companion.users
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamSubscription
import com.simplito.kotlin.privmx_endpoint.modules.stream.StreamApiLow
import com.simplito.kotlin.privmx_endpoint_streams.StreamApi
import com.simplito.kotlin.privmx_endpoint_streams.StreamApiInit
import com.simplito.kotlin.privmx_endpoint_streams.joinStreamRoom
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.AudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.VideoTrack
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

expect fun createStreamApiInit(): StreamApiInit

fun createStreamApi(streamApiLow: StreamApiLow): StreamApi {

    val init = createStreamApiInit()
    return StreamApi(
        streamApiLow,
        init
    )
}

fun createStreamRoom(
    streamApi: StreamApi,
    contextId: String?,
    users: List<UserWithPubKey>,
    managers: List<UserWithPubKey>,
    publicMeta: ByteArray,
    privateMeta: ByteArray,
    policies: ContainerPolicy?,
    emptyRoomTtl: Long?
): String = streamApi.createStreamRoom(
    contextId!!,
    users,
    managers,
    publicMeta,
    privateMeta,
    policies,
    emptyRoomTtl
)

fun StreamTest.createStreamRoom(
    contextId: String?,
    users: List<UserWithPubKey>,
    managers: List<UserWithPubKey>,
): String = createStreamRoom(
    streamApi,
    contextId!!,
    users,
    managers,
    publicMeta.encodeToByteArray(),
    privateMeta.encodeToByteArray(),
    null,
    null
)

fun StreamTest.createStreamRoom(): String =
    createStreamRoom(
        streamApi,
        contextId!!,
        users,
        users,
        publicMeta.encodeToByteArray(),
        privateMeta.encodeToByteArray(),
        null,
        null
    )

fun deleteAllRooms(streamApi: StreamApi, contextId: String) {
    streamApi.listStreamRooms(contextId, 0, 100, "desc").readItems.forEach { streamRoom ->
        streamApi.deleteStreamRoom(streamRoom.streamRoomId)
    }
}

/** user1 publishes, user2 joins*/
fun StreamTest.publishedStreamWithSecondMemberJoined(): Pair<String, Long> {
    val roomId = createStreamRoom()
    streamApi.joinStreamRoom(roomId)

    val handle = streamApi.createStream(roomId)
    addFakeAudioTrackToStream(streamApi, handle)
    val result = streamApi.publishStream(handle)
    runBlocking { delay(1500) }

    streamApi2.joinStreamRoom(roomId)

    return roomId to result.data?.stream?.id!!
}

fun getStreamsToSubscribe(
    streamApi: StreamApi,
    streamRoomId: String
): List<StreamSubscription> {
    return streamApi.listStreams(streamRoomId)
        .flatMap { stream ->
            stream.tracks.map { track ->
                StreamSubscription(stream.id!!, track.mid)
            }
        }
}

// helper time to connect to the server
fun waitForServerSync(ms: Long = 1500) = runBlocking { delay(ms) }

expect fun addFakeVideoTrackToStream(
    streamApi: StreamApi,
    streamHandle: StreamHandle,
): VideoTrack

expect fun addFakeAudioTrackToStream(streamApi: StreamApi, streamHandle: StreamHandle): AudioTrack