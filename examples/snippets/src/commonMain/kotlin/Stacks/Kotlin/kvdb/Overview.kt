package Stacks.Kotlin.kvdb

import Stacks.Kotlin.contextId
import Stacks.Kotlin.endpointSession
import Stacks.Kotlin.user1Id
import Stacks.Kotlin.user1PublicKey
import Stacks.Kotlin.user2Id
import Stacks.Kotlin.user2PublicKey
import com.simplito.kotlin.privmx_endpoint.model.Kvdb
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.modules.kvdb.KvdbApi
import com.simplito.kotlin.privmx_endpoint_extra.model.SortOrder
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class KvdbPublicMeta(val tags: List<String>)

lateinit var kvdbApi: KvdbApi

fun setKvdbApi() {
    endpointSession.kvdbApi
}

// START: Creating KVDB snippets
fun createKvdbBasic() {
    val users: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )
    val publicMeta = ByteArray(0)
    val privateMeta = ByteArray(0)

    val kvdbId = kvdbApi.createKvdb(
        contextId,
        users,
        managers,
        publicMeta,
        privateMeta
    )
}

fun createKvdbWithName() {
    val users: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )
    val kvdbNameAsPrivateMeta = "New KVDB"
    val publicMeta = ByteArray(0)

    val kvdbId = kvdbApi.createKvdb(
        contextId,
        users,
        managers,
        publicMeta,
        kvdbNameAsPrivateMeta.encodeToByteArray()
    )
}

fun createKvdbWithPublicMeta() {
    val users: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )
    val kvdbNameAsPrivateMeta = "New KVDB"
    val publicMeta = KvdbPublicMeta(
        listOf("TAG1", "TAG2", "TAG3")
    )

    val kvdbId = kvdbApi.createKvdb(
        contextId,
        users,
        managers,
        Json.encodeToString(publicMeta).encodeToByteArray(),
        kvdbNameAsPrivateMeta.encodeToByteArray()
    )
}
// END: Creating KVDB snippets

// START: Getting KVDB snippets
data class KvdbItem(
    val kvdb: Kvdb,
    val decodedPrivateMeta: String,
    val decodedPublicMeta: KvdbPublicMeta
)

fun getMostRecentKvdbs() {
    val startIndex = 0L
    val pageSize = 100L

    val kvdbsPagingList = kvdbApi.listKvdbs(
        contextId,
        startIndex,
        pageSize,
        SortOrder.DESC
    )
    val kvdbs = kvdbsPagingList.readItems.map {
        KvdbItem(
            it,
            it.privateMeta.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

fun getOldestKvdbs() {
    val startIndex = 0L
    val pageSize = 100L

    val kvdbsPagingList = kvdbApi.listKvdbs(
        contextId,
        startIndex,
        pageSize,
        SortOrder.ASC
    )

    val kvdbs = kvdbsPagingList.readItems.map {
        KvdbItem(
            it,
            it.privateMeta.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

fun getKvdbById() {
    val kvdbId = "KVDB_ID"
    val kvdbItem = kvdbApi.getKvdb(kvdbId).let {
        KvdbItem(
            it,
            it.privateMeta.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}
// END: Getting KVDB snippets

// START: Managing KVDB snippets
fun renamingKvdb() {
    val kvdbId = "KVDB_ID"
    val kvdb: Kvdb = kvdbApi.getKvdb(kvdbId)
    val users = kvdb
        .users
        .map { userId ->
            //Your application must provide a way,
            //to get user's public key from their userId.
            UserWithPubKey(
                userId,
                "USER_PUBLIC_KEY"
            )
        }
    val managers = kvdb
        .managers
        .map { userId ->
            //Your application must provide a way,
            //to get user's public key from their userId.
            UserWithPubKey(
                userId,
                "USER_PUBLIC_KEY"
            )
        }
    val newKvdbNameAsPrivateMeta = "New KVDB name"

    kvdbApi.updateKvdb(
        kvdb.kvdbId,
        users,
        managers,
        kvdb.publicMeta,
        newKvdbNameAsPrivateMeta.encodeToByteArray(),
        kvdb.version!! + 1,
        false
    )
}

fun removingUser() {
    val kvdbId = "KVDB_ID"
    val kvdb: Kvdb = kvdbApi.getKvdb(kvdbId)
    val userToRemove = "USER_ID_TO_REMOVE"
    val newUsers = kvdb
        .users
        .filter {
            it != userToRemove
        }.map { userId ->
            //Your application must provide a way,
            //to get user's public key from their userId.
            UserWithPubKey(
                userId,
                "USER_PUBLIC_KEY"
            )
        }
    val managers = kvdb
        .managers
        .map { userId ->
            //Your application must provide a way,
            //to get user's public key from their userId.
            UserWithPubKey(
                userId,
                "USER_PUBLIC_KEY"
            )
        }

    kvdbApi.updateKvdb(
        kvdb.kvdbId,
        newUsers,
        managers,
        kvdb.publicMeta,
        kvdb.privateMeta,
        kvdb.version!! + 1,
        false
    )
}

fun deletingKvdb() {
    val kvdbId = "KVDB_ID"
    kvdbApi.deleteKvdb(kvdbId)
}
// END: Managing KVDB snippets