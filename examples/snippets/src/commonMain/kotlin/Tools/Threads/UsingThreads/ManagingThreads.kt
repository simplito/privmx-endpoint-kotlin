package Tools.Threads.UsingThreads

import com.simplito.java.privmx_endpoint.model.UserWithPubKey
import com.simplito.java.privmx_endpoint_extra.lib.PrivmxEndpoint
import com.simplito.java.privmx_endpoint_extra.lib.PrivmxEndpointContainer
import com.simplito.java.privmx_endpoint_extra.model.Modules
import com.simplito.java.privmx_endpoint_extra.model.SortOrder
import java.nio.charset.StandardCharsets

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

val initModules = setOf(Modules.THREAD)
val endpointSession: PrivmxEndpoint = endpointContainer.connect(
    initModules,
    user1PrivateKey,
    solutionId,
    bridgeUrl
)
// END: Initial Assumptions Snippets

fun creatingThreads() {
    val privateMeta = "My private data".encodeToByteArray()
    val publicMeta = "My public data".encodeToByteArray()
    val users : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )

    val threadID = endpointSession.threadApi?.createThread(
        contextId,
        users,
        managers,
        publicMeta,
        privateMeta
    )
}

fun listingThreads() {
    val limit = 30L
    val skip = 0L

    val threads = endpointSession.threadApi?.listThreads(
        contextId,
        skip,
        limit,
        SortOrder.DESC
    )
}


fun modifyingThreads() {
    val threadID = "THREAD_ID"
    endpointSession.threadApi?.let { threadApi ->
        val thread = threadApi.getThread(threadID)
        val newUsers: List<UserWithPubKey> = listOf(
            UserWithPubKey(user1Id, user1PublicKey),
            UserWithPubKey(user2Id, user2PublicKey),
            UserWithPubKey(user3Id, user3PublicKey)
        )
        val newManagers = listOf(
            UserWithPubKey(user1Id, user1PublicKey),
            UserWithPubKey(user2Id, user2PublicKey),
        )
        val newPrivateMeta = "New thread name".toByteArray(StandardCharsets.UTF_8)

        threadApi.updateThread(
            threadID,
            newUsers,
            newManagers,
            thread.publicMeta,
            newPrivateMeta,
            thread.version,
            false
        )
    }
}