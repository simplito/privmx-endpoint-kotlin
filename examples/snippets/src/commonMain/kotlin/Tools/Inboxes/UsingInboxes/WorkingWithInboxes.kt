package Tools.Inboxes.UsingInboxes

import com.simplito.kotlin.privmx_endpoint.model.Inbox
import com.simplito.kotlin.privmx_endpoint.model.InboxPublicView
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

var initModules = setOf(Modules.INBOX)
var endpointSession: PrivmxEndpoint = endpointContainer.connect(
    initModules,
    user1PrivateKey,
    solutionId,
    bridgeUrl
)
// END: Initial Assumptions Snippets

fun creatingInboxes() {
    val privateMeta: ByteArray = "My private data".encodeToByteArray()
    val publicMeta: ByteArray = "My public data".encodeToByteArray()
    val users: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )

    val inboxID = endpointSession.inboxApi?.createInbox(
        contextId,
        users,
        managers,
        publicMeta,
        privateMeta
    )
}

fun listingInboxes() {
    val limit = 30L
    val skip = 0L

    val inboxes: List<Inbox> = endpointSession.inboxApi!!.listInboxes(
        contextId,
        skip,
        limit,
        SortOrder.DESC
    ).readItems
}

fun modifyingInboxes() {
    val inboxID = "INBOX_ID"
    val newUsers: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey),
        UserWithPubKey(user3Id, user3PublicKey)
    )
    val newManagers: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val newPrivateMeta: ByteArray = "New inbox name".encodeToByteArray()

    endpointSession.inboxApi?.let { inboxApi ->
        val inbox = inboxApi.getInbox(inboxID)

        inboxApi.updateInbox(
            inboxID,
            newUsers,
            newManagers,
            inbox.publicMeta,
            newPrivateMeta,
            filesConfig = null,
            version = inbox.version!!,
            force = false,
            forceGenerateNewKey = false,
            policies = inbox.policy
        )
    }
}

fun deletingInboxes() {
    val inboxID = "INBOX_ID"
    endpointSession.inboxApi!!.deleteInbox(inboxID)
}

fun usingPublicView() {
    // Retrieve and print the public view of the Inbox
    val inboxID = "INBOX_ID"

    val inboxPublicView: InboxPublicView = endpointSession.inboxApi!!.getInboxPublicView(inboxID)
    val publicMeta: String = inboxPublicView.publicMeta.decodeToString()
    println(publicMeta)
}