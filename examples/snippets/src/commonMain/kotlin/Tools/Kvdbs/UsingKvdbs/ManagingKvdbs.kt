package Tools.Kvdbs.UsingKvdbs

import com.simplito.kotlin.privmx_endpoint.model.Kvdb
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
var bridgeUrl: String = "YOUR_BRIDGE_URL"
var solutionId: String = "YOUR_SOLUTION_ID"
var contextId: String = "YOUR_CONTEXT_ID"

var user1Id: String = "USER_ID_1"
var user1PublicKey: String = "PUBLIC_KEY_1"
var user1PrivateKey: String = "PRIVATE_KEY_1"

var user2Id: String = "USER_ID_2"
var user2PublicKey: String = "PUBLIC_KEY_2"

var user3Id: String = "USER_ID_3"
var user3PublicKey: String = "PUBLIC_KEY_3"

var endpointContainer: PrivmxEndpointContainer = PrivmxEndpointContainer()

var initModules: Set<Modules> = setOf(Modules.KVDB)
var endpointSession: PrivmxEndpoint = endpointContainer.connect(
    initModules,
    user1PrivateKey,
    solutionId,
    bridgeUrl
)

// END: Initial Assumptions Snippets
// START: Creating KVDBs
fun creatingKvdbs() {
    val privateMeta: ByteArray = "KVDB's private data".encodeToByteArray()
    val publicMeta: ByteArray = "KVDB's public data".encodeToByteArray()
    val users: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )

    endpointSession.kvdbApi?.createKvdb(
        contextId,
        users,
        managers,
        publicMeta,
        privateMeta
    )
}

// END: Creating KVDBs
// START: Listing KVDBs
fun listingKvdbs() {
    val startIndex: Long = 0
    val pageSize: Long = 30

    val kvdbs: List<Kvdb> = endpointSession.kvdbApi!!.listKvdbs(
        contextId,
        startIndex,
        pageSize,
        SortOrder.DESC
    ).readItems
}

// END: Listing KVDBs
// START: Modifying KVDBs
fun updatingKvdbs() {
    val kvdbID = "KVDB_ID"
    val privateMeta: ByteArray = "New KVDB's private data".encodeToByteArray()
    val publicMeta: ByteArray = "New KVDB's public data".encodeToByteArray()
    val newUsersList: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user3PublicKey),
        UserWithPubKey(user3Id, user3PublicKey)
    )
    val managers: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )

    endpointSession.kvdbApi?.let { kvdbApi ->
        val kvdb = kvdbApi.getKvdb(kvdbID)

        endpointSession.kvdbApi!!.updateKvdb(
            kvdbID,
            newUsersList,
            managers,
            publicMeta,
            privateMeta,
            kvdb.version!!,
            false,
            false
        )
    }
}

// END: Modifying KVDBs
// START: Deleting KVDBs
fun deletingKvdb() {
    val kvdbID = "KVDB_ID"
    endpointSession.kvdbApi?.deleteKvdb(kvdbID)
}
// END: Deleting KVDBs
