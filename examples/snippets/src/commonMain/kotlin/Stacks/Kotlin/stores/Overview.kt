package Stacks.Kotlin.stores

import Stacks.Kotlin.contextId
import com.simplito.java.privmx_endpoint.model.Store
import com.simplito.java.privmx_endpoint.model.UserWithPubKey
import Stacks.Kotlin.endpointSession
import Stacks.Kotlin.user1Id
import Stacks.Kotlin.user1PublicKey
import Stacks.Kotlin.user2Id
import Stacks.Kotlin.user2PublicKey
import com.simplito.java.privmx_endpoint.modules.store.StoreApi
import com.simplito.java.privmx_endpoint_extra.model.SortOrder
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

lateinit var storeApi: StoreApi

fun setStoreApi() {
    val storeApi = endpointSession.storeApi
}

@Serializable
data class StorePublicMeta(val tags: List<String>)

data class StoreItem(
    val store: Store,
    val decodedPrivateMeta: String,
    val decodedPublicMeta: StorePublicMeta
)

// START: Creating Stores snippets

fun createStoreBasic(){
    val users : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )
    val publicMeta = ByteArray(0)
    val privateMeta = ByteArray(0)

    val storeId = storeApi.createStore(
        contextId,
        users,
        managers,
        publicMeta,
        privateMeta
    )
}

fun createStoreWithName(){
    val users : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )
    val publicMeta = ByteArray(0)
    val storeNameAsPrivateMeta = "New store"
    val storeId = storeApi.createStore(
        contextId,
        users,
        managers,
        publicMeta,
        storeNameAsPrivateMeta.encodeToByteArray()
    )
}

fun createStoreWithPublicMeta(){
    val users : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )
    val publicMeta = StorePublicMeta(
        listOf("TAG1", "TAG2", "TAG3")
    )
    val storeNameAsPrivateMeta = "New store"

    val storeID = storeApi.createStore(
        contextId,
        users,
        managers,
        Json.encodeToString(publicMeta).encodeToByteArray(),
        storeNameAsPrivateMeta.encodeToByteArray()
    )
}
// END: Creating Stores snippets


// START: Getting Stores snippets

fun getMostRecentStores(){
    val startIndex = 0L
    val pageSize = 100L

    val storesPagingList = storeApi.listStores(
        contextId,
        startIndex,
        pageSize,
        SortOrder.DESC
    )

    val stores = storesPagingList.readItems.map {
        StoreItem(
            it,
            it.privateMeta.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

fun getOldestStores(){
    val startIndex = 0L
    val pageSize = 100L

    val storesPagingList = storeApi.listStores(
        contextId,
        startIndex,
        pageSize,
        SortOrder.ASC
    )

    val stores = storesPagingList.readItems.map {
        StoreItem(
            it,
            it.privateMeta.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

fun getStoreById(){
    val storeId = "STORE_ID"

    val storeItem = storeApi.getStore(storeId).let {
        StoreItem(
            it,
            it.privateMeta.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}
// END: Getting Stores snippets


// START: Managing Stores snippets
fun renamingStore() {
    val storeID = "STORE_ID"
    val store: Store = storeApi.getStore(storeID)
    val users = store
        .users
        .map { userId ->
            //Your application must provide a way,
            //to get user's public key from their userId.
            UserWithPubKey(
                userId,
                "USER_PUBLIC_KEY"
            )
        }
    val managers = store
        .managers
        .map { userId ->
            //Your application must provide a way,
            //to get user's public key from their userId.
            UserWithPubKey(
                userId,
                "USER_PUBLIC_KEY"
            )
        }
    val newStoreNameAsPrivateMeta = "New store name"

    storeApi.updateStore(
        storeID,
        users,
        managers,
        store.publicMeta,
        newStoreNameAsPrivateMeta.encodeToByteArray(),
        store.version,
        false
    )
}


fun removingUser() {
    val storeID = "STORE_ID"
    val store: Store = storeApi.getStore(storeID)
    val userToRemove = "USERID_TO_REMOVE"
    val newUsers = store
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
    val managers = store
        .managers
        .map { userId ->
            //Your application must provide a way,
            //to get user's public key from their userId.
            UserWithPubKey(
                userId,
                "USER_PUBLIC_KEY"
            )
        }

    storeApi.updateStore(
        storeID,
        newUsers,
        managers,
        store.publicMeta,
        store.privateMeta,
        store.version,
        false
    )
}

fun deletingStore() {
    val storeID = "STORE_ID"
    storeApi.deleteStore(storeID)
}

// END: Managing Stores snippets