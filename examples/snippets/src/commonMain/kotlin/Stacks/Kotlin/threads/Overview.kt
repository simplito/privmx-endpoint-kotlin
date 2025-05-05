package Stacks.Kotlin.threads

import Stacks.Kotlin.contextId
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import Stacks.Kotlin.endpointSession
import Stacks.Kotlin.user1Id
import Stacks.Kotlin.user1PublicKey
import Stacks.Kotlin.user2Id
import Stacks.Kotlin.user2PublicKey
import com.simplito.kotlin.privmx_endpoint.model.Thread
import com.simplito.kotlin.privmx_endpoint.modules.thread.ThreadApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.simplito.kotlin.privmx_endpoint_extra.model.SortOrder

@Serializable
data class ThreadPublicMeta(val tags: List<String>)

lateinit var threadApi: ThreadApi

fun setThreadApi() {
    val threadApi = endpointSession.threadApi
}

// START: Creating thread snippets

fun createThreadBasic(){
    val users : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )
    val publicMeta = ByteArray(0)
    val privateMeta = ByteArray(0)

    val threadId = threadApi.createThread(
        contextId,
        users,
        managers,
        publicMeta,
        privateMeta
    )
}

fun createThreadWithName(){
    val users : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers : List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )
    val threadNameAsPrivateMeta = "New thread"
    val publicMeta = ByteArray(0)

    val threadId = threadApi.createThread(
        contextId,
        users,
        managers,
        publicMeta,
        threadNameAsPrivateMeta.encodeToByteArray()
    )
}

fun createThreadWithPublicMeta() {
    val users: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val managers: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey)
    )
    val threadNameAsPrivateMeta = "New thread"
    val publicMeta = ThreadPublicMeta(
        listOf("TAG1", "TAG2", "TAG3")
    )

    val threadId = threadApi.createThread(
        contextId,
        users,
        managers,
        Json.encodeToString(publicMeta).encodeToByteArray(),
        threadNameAsPrivateMeta.encodeToByteArray()
    )
}

// END: Creating thread snippets


// START: Getting thread snippets

data class ThreadItem(
    val thread: Thread,
    val decodedPrivateMeta: String,
    val decodedPublicMeta: ThreadPublicMeta
)

fun getMostRecentThreads(){
    val startIndex = 0L
    val pageSize = 100L

    val threadsPagingList = threadApi.listThreads(
        contextId,
        startIndex,
        pageSize,
        SortOrder.DESC
    )
    val threads = threadsPagingList.readItems.map {
        ThreadItem(
            it,
            it.privateMeta.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

fun getOldestThreads(){
    val startIndex = 0L
    val pageSize = 100L

    val threadsPagingList = threadApi.listThreads(
        contextId,
        startIndex,
        pageSize,
        SortOrder.ASC
    )
    val threads = threadsPagingList.readItems.map {
        ThreadItem(
            it,
            it.privateMeta.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

fun getThreadById(){
    val threadId = "THREAD_ID"
    val threadItem = threadApi.getThread(threadId).let {
        ThreadItem(
            it,
            it.privateMeta.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

// END: Getting thread snippets


// START: Managing thread snippets

fun renamingThread() {
    val threadID = "THREAD_ID"
    val thread: Thread = threadApi.getThread(threadID)
    val users = thread
        .users
        .map { userId ->
            //Your application must provide a way,
            //to get user's public key from their userId.
            UserWithPubKey(
                userId,
                "USER_PUBLIC_KEY"
            )
        }
    val managers = thread
        .managers
        .map { userId ->
            //Your application must provide a way,
            //to get user's public key from their userId.
            UserWithPubKey(
                userId,
                "USER_PUBLIC_KEY"
            )
        }
    val newThreadNameAsPrivateMeta = "New thread name"

    threadApi.updateThread(
        thread.threadId,
        users,
        managers,
        thread.publicMeta,
        newThreadNameAsPrivateMeta.encodeToByteArray(),
        thread.version!!,
        false
    )
}


fun removingUser() {
    val threadID = "THREAD_ID"
    val thread: Thread = threadApi.getThread(threadID)
    val userToRemove = "USER_ID_TO_REMOVE"
    val newUsers = thread
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
    val managers = thread
        .managers
        .map { userId ->
            //Your application must provide a way,
            //to get user's public key from their userId.
            UserWithPubKey(
                userId,
                "USER_PUBLIC_KEY"
            )
        }

    threadApi.updateThread(
        thread.threadId,
        newUsers,
        managers,
        thread.publicMeta,
        thread.privateMeta,
        thread.version!!,
        false
    )
}

fun deletingThread() {
    val threadId = "THREAD_ID"
    threadApi.deleteThread(threadId)
}

// END: Managing thread snippets