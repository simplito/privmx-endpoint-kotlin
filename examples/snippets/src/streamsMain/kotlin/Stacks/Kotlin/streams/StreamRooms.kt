package Stacks.Kotlin.streams

import Stacks.Kotlin.contextId
import Stacks.Kotlin.user1Id
import Stacks.Kotlin.user1PublicKey
import Stacks.Kotlin.user2Id
import Stacks.Kotlin.user2PublicKey
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamRoom
import com.simplito.kotlin.privmx_endpoint_extra.model.SortOrder
import kotlinx.serialization.json.Json


fun createStreamRoomBasic() {
    val publicMeta = ByteArray(0)
    val privateMeta = ByteArray(0)

    val users: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )

    val streamRoomId: String = streamApi.createStreamRoom(
        contextId,
        users,
        managers,
        publicMeta,
        privateMeta,
        null,       // policies
        null        // emptyRoomTtl
    )
}

fun createStreamRoomWithPublicMeta() {
    val users: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )
    val publicMeta = StreamRoomPublicMeta(
        title = "Daily meeting #34",
        type = "meeting",
        scheduledAt = "2026-02-23T10:00:00Z"
    )
    val privateMeta = ByteArray(0)

    val streamRoomId: String = streamApi.createStreamRoom(
        contextId,
        users,
        managers,
        Json.encodeToString(publicMeta).encodeToByteArray(),
        privateMeta,
        null,   // policies
        null    // emptyRoomTtl
    )
}

fun createStreamRoomWithEmptyRoomTtl() {
    val publicMeta = ByteArray(0)
    val privateMeta = ByteArray(0)

    val users: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )

    // Grace period (in milliseconds) the StreamRoom
    // stays open after the last participant leaves.
    val emptyRoomTtl = 5 * 60 * 1000L

    val streamRoomId: String = streamApi.createStreamRoom(
        contextId,
        users,
        managers,
        ByteArray(0),
        ByteArray(0),
        null,           // policies
        emptyRoomTtl
    )
}


fun getMostRecentStreamRooms() {
    val startIndex = 0L
    val pageSize = 100L

    val streamRoomsPagingList: PagingList<StreamRoom> = streamApi.listStreamRooms(
        contextId,
        startIndex,
        pageSize,
        SortOrder.DESC,
        null,   // lastId
        null,   // queryAsJson
        null    // sortBy
    )

    val streamRooms = streamRoomsPagingList.readItems.map {
        StreamRoomItem(
            it,
            it.privateMeta.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

fun getOldestStreamRooms() {
    val startIndex = 0L
    val pageSize = 100L

    val streamRoomsPagingList: PagingList<StreamRoom> = streamApi.listStreamRooms(
        contextId,
        startIndex,
        pageSize,
        SortOrder.ASC,
        null,   // lastId
        null,   // queryAsJson
        null    // sortBy
    )

    val streamRooms = streamRoomsPagingList.readItems.map {
        StreamRoomItem(
            it,
            it.privateMeta.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

fun getStreamRoomById() {
    val streamRoomId = "STREAM_ROOM_ID"

    val streamRoomItem = streamApi.getStreamRoom(streamRoomId).let {
        StreamRoomItem(
            it,
            it.privateMeta.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

fun renameStreamRoom() {
    val streamRoomId = "STREAM_ROOM_ID"
    val streamRoom: StreamRoom = streamApi.getStreamRoom(streamRoomId)

    val users = streamRoom.users.map { userId ->
        // Your application must provide a way
        // to get user's public key from their userId.
        UserWithPubKey(userId, "USER_PUBLIC_KEY")
    }

    val managers = streamRoom.managers.map { userId ->
        UserWithPubKey(userId, "USER_PUBLIC_KEY")
    }

    val newStreamRoomNameAsPrivateMeta = "New stream room name"

    streamApi.updateStreamRoom(
        streamRoomId,
        users,
        managers,
        streamRoom.publicMeta,
        newStreamRoomNameAsPrivateMeta.encodeToByteArray(),
        streamRoom.version,
        false,  // force
        false,  // forceGenerateNewKey
        null    // policies
    )
}

fun removeUserFromStreamRoom() {
    val streamRoomId = "STREAM_ROOM_ID"
    val streamRoom: StreamRoom = streamApi.getStreamRoom(streamRoomId)
    val userToRemove = "USERID_TO_REMOVE"

    val newUsers = streamRoom.users
        .filter { it != userToRemove }
        .map { userId ->
            // Your application must provide a way
            // to get user's public key from their userId.
            UserWithPubKey(userId, "USER_PUBLIC_KEY")
        }

    val managers = streamRoom.managers
        .filter { it != userToRemove }
        .map { userId ->
            // Your application must provide a way,
            // to get user's public key from their userId.
            UserWithPubKey(userId, "USER_PUBLIC_KEY")
        }

    streamApi.updateStreamRoom(
        streamRoomId,
        newUsers,
        managers,
        streamRoom.publicMeta,
        streamRoom.privateMeta,
        streamRoom.version,
        false,  // force
        false,  // forceGenerateNewKey
        null    // policies
    )
}

fun deleteStreamRoom() {
    val streamRoomId = "STREAM_ROOM_ID"

    streamApi.deleteStreamRoom(streamRoomId)
}
