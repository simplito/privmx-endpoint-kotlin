package Tools.Stores.UsingStores

import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint_extra.lib.PrivmxEndpoint
import com.simplito.kotlin.privmx_endpoint_extra.lib.PrivmxEndpointContainer
import com.simplito.kotlin.privmx_endpoint_extra.model.Modules
import com.simplito.kotlin.privmx_endpoint_extra.model.SortOrder

// START: Initial Assumptions Snippets
/*
    All the values below like BRIDGE_URL, SOLUTION_ID, CONTEXT_ID
    should be replaced by the ones corresponding to your Bridge Server instance.

    The private keys here are for demonstration purposes only.
    Normally, they should be kept separately by each user and stored in a safe place,
    or generated from a password (see the derivePrivateKey2() method in the Crypto API)
*/

val bridgeUrl = "YOUR_BRIDGE_URL"
val solutionId = "YOUR_SOLUTION_ID"
val contextId = "YOUR_CONTEXT_ID"

val user1Id = "USER_ID_1"
val user1PublicKey = "PUBLIC_KEY_1"
val user1PrivateKey = "PRIVATE_KEY_1"

val user2Id = "USER_ID_2"
val user2PublicKey = "PUBLIC_KEY_2"

val user3Id = "USER_ID_3"
val user3PublicKey = "PUBLIC_KEY_3"

val endpointContainer = PrivmxEndpointContainer()

val initModules = setOf(Modules.STORE)
val endpointSession: PrivmxEndpoint = endpointContainer.connect(
    initModules,
    user1PrivateKey,
    solutionId,
    bridgeUrl
)
// END: Initial Assumptions Snippets

fun creatingStores() {
    val privateMeta = "My private data".encodeToByteArray()
    val publicMeta = "My public data".encodeToByteArray()
    val users: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )

    val storeID = endpointSession.storeApi?.createStore(
        contextId,
        users,
        managers,
        publicMeta,
        privateMeta
    )
}

fun listingStores() {
    val limit = 30L
    val skip = 0L

    val stores = endpointSession.storeApi?.listStores(
        contextId,
        skip,
        limit,
        SortOrder.DESC
    )
}


fun modifyingStores() {
    val storeID = "STORE_ID"
    endpointSession.storeApi?.let { storeApi ->
        val store = storeApi.getStore(storeID)
        val newUsers: List<UserWithPubKey> = listOf(
            UserWithPubKey(user1Id, user1PublicKey),
            UserWithPubKey(user2Id, user2PublicKey),
            UserWithPubKey(user3Id, user3PublicKey)
        )
        val newManagers = listOf(
            UserWithPubKey(user1Id, user1PublicKey),
            UserWithPubKey(user2Id, user2PublicKey),
        )
        val newPrivateMeta = "New store name".encodeToByteArray()

        storeApi.updateStore(
            storeID,
            newUsers,
            newManagers,
            store.publicMeta,
            newPrivateMeta,
            store.version!!,
            false
        )
    }
}