package Stacks.Kotlin.inboxes

import Stacks.Kotlin.contextId
import com.simplito.kotlin.privmx_endpoint.model.FilesConfig
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import Stacks.Kotlin.endpointSession
import Stacks.Kotlin.user1Id
import Stacks.Kotlin.user1PublicKey
import Stacks.Kotlin.user2Id
import Stacks.Kotlin.user2PublicKey
import com.simplito.kotlin.privmx_endpoint.model.Inbox
import com.simplito.kotlin.privmx_endpoint.modules.inbox.InboxApi
import com.simplito.kotlin.privmx_endpoint_extra.model.SortOrder
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

lateinit var inboxApi: InboxApi

fun setInboxApi() {
    val inboxApi = endpointSession.inboxApi
}

@Serializable
data class InboxPublicMeta(val tags: List<String>)

data class InboxItem(
    val inbox: Inbox,
    val decodedPrivateMeta: String,
    val decodedPublicMeta: InboxPublicMeta
)

// START: Creating Inboxes snippets

fun createInboxBasic(){
    val users : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )
    val publicMeta = ByteArray(0)
    val privateMeta = ByteArray(0)

    val inboxId = inboxApi.createInbox(
        contextId,
        users,
        managers,
        publicMeta,
        privateMeta
    )
}

fun createInboxWithConfig(){
    val filesConfig = FilesConfig(
        0L, //minCount
        10L, //maxCount
        500L, //maxFileSize
        2000L //maxWholeUploadSize
    )
    val users : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )
    val publicMeta = ByteArray(0)
    val privateMeta = ByteArray(0)

    val inboxId = inboxApi.createInbox(
        contextId,
        users,
        managers,
        publicMeta,
        privateMeta,
        filesConfig
    )
}

fun createInboxWithName() {
    val users : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )
    val publicMeta = ByteArray(0)
    val inboxNameAsPrivateMeta = "New inbox"

    val inboxId = inboxApi.createInbox(
        contextId,
        users,
        managers,
        publicMeta,
        inboxNameAsPrivateMeta.encodeToByteArray()
    )
}

fun createInboxWithPublicMeta(){
    val users : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )
    val publicMeta = InboxPublicMeta(
        listOf("TAG1", "TAG2", "TAG3")
    )
    val inboxNameAsPrivateMeta = "New inbox"

    val inboxID = inboxApi.createInbox(
        contextId,
        users,
        managers,
        Json.encodeToString(publicMeta).encodeToByteArray(),
        inboxNameAsPrivateMeta.encodeToByteArray()
    )
}

// END: Creating Inboxes snippets


// START: Getting Inboxes snippets

fun getMostRecentInboxes(){
    val startIndex = 0L
    val pageSize = 100L

    val inboxesPagingList = inboxApi.listInboxes(
        contextId,
        startIndex,
        pageSize,
        SortOrder.DESC
    )

    val inboxes = inboxesPagingList.readItems.map {
        InboxItem(
            it,
            it.privateMeta.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

fun getOldestInboxes(){
    val startIndex = 0L
    val pageSize = 100L

    val inboxesPagingList = inboxApi.listInboxes(
        contextId,
        startIndex,
        pageSize,
        SortOrder.ASC
    )

    val inboxes = inboxesPagingList.readItems.map {
        InboxItem(
            it,
            it.privateMeta.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

fun getInboxById(){
    val inboxID = "INBOX_ID"

    val inboxItem = inboxApi.getInbox(inboxID).let {
        InboxItem(
            it,
            it.privateMeta.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

// END: Getting Inboxes snippets


// START: Getting Public View snippets

fun gettingPublicView() {
    val inboxID = "INBOX_ID"
    val inboxPublicView = inboxApi.getInboxPublicView(inboxID)
    val inboxPublicMeta: InboxPublicMeta = Json.decodeFromString(
        inboxPublicView.publicMeta.decodeToString()
    )
}

// END: Getting Public View snippets


// START: Managing Inboxes snippets

fun renamingInbox() {
    val inboxID = "INBOX_ID"
    val inbox: Inbox = inboxApi.getInbox(inboxID)
    val users = inbox
        .users
        .map { userId ->
            //Your application must provide a way,
            //to get user's public key from their userId.
            UserWithPubKey(
                userId,
                "USER_PUBLIC_KEY"
            )
        }
    val managers = inbox
        .managers
        .map { userId ->
            //Your application must provide a way,
            //to get user's public key from their userId.
            UserWithPubKey(
                userId,
                "USER_PUBLIC_KEY"
            )
        }
    val newInboxNameAsPrivateMeta = "New inbox name"

    inboxApi.updateInbox(
        inboxID,
        users,
        managers,
        inbox.publicMeta,
        newInboxNameAsPrivateMeta.encodeToByteArray(),
        inbox.filesConfig,
        inbox.version!!,
        false
    )
}


fun removingUser() {
    val inboxID = "INBOX_ID"
    val inbox: Inbox = inboxApi.getInbox(inboxID)
    val userToRemove = "USERID_TO_REMOVE"
    val newUsers = inbox
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
    val managers = inbox
        .managers
        .map { userId ->
            //Your application must provide a way,
            //to get user's public key from their userId.
            UserWithPubKey(
                userId,
                "USER_PUBLIC_KEY"
            )
        }

    inboxApi.updateInbox(
        inboxID,
        newUsers,
        managers,
        inbox.publicMeta,
        inbox.privateMeta,
        inbox.filesConfig,
        inbox.version!!,
        false
    )
}

fun deletingInbox() {
    val inboxID = "INBOX_ID"
    inboxApi.deleteInbox(inboxID)
}

// END: Managing Inboxes snippets