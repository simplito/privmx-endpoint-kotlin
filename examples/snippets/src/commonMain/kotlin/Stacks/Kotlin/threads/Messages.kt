package Stacks.Kotlin.threads

import com.simplito.kotlin.privmx_endpoint.model.Message
import com.simplito.kotlin.privmx_endpoint_extra.model.SortOrder
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class MessagePublicMeta(
    val responseTo: String
)

data class MessageItem(
    val message: Message,
    val decodedData: String,
    val decodedPublicMeta: MessagePublicMeta
)

// START: Sending Messages snippets
fun sendMessagePlainText() {
    val threadID = "THREAD_ID"
    val message = "Message text"
    val publicMeta = ByteArray(0)
    val privateMeta = ByteArray(0)

    val messageID = threadApi.sendMessage(
        threadID,
        publicMeta,
        privateMeta,
        message.encodeToByteArray()
    )
}

fun sendMessageWithPublicMeta() {
    val threadID = "THREAD_ID"
    val message = "Message text"
    val publicMeta = MessagePublicMeta(responseTo = "MESSAGE_ID_TO_RESPOND")
    val privateMeta = ByteArray(0)

    val messageID = threadApi.sendMessage(
        threadID,
        Json.encodeToString(publicMeta).encodeToByteArray(),
        privateMeta,
        message.encodeToByteArray()
    )
}

// END: Sending Messages snippets

// START: Getting Messages snippets
fun getMostRecentMessages() {
    val threadId = "THREAD_ID"
    val startIndex = 0L
    val pageSize = 100L

    val messagesPagingList = threadApi.listMessages(
        threadId,
        startIndex,
        pageSize,
        SortOrder.DESC
    )

    val messages = messagesPagingList.readItems.map {
        MessageItem(
            it,
            it.data.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

fun getOldestMessages() {
    val threadId = "THREAD_ID"
    val startIndex = 0L
    val pageSize = 100L

    val messagesPagingList = threadApi.listMessages(
        threadId,
        startIndex,
        pageSize,
        SortOrder.ASC
    )

    val messages = messagesPagingList.readItems.map {
        MessageItem(
            it,
            it.data.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

fun getMessageById() {
    val messageId = "MESSAGE_ID"

    val messageItem = threadApi.getMessage(messageId).let {
        MessageItem(
            it,
            it.data.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

// END: Getting Messages snippets

// START: Managing Messages snippets
fun updatingMessage() {
    val messageID = "MESSAGE_ID"
    val message: Message = threadApi.getMessage(messageID)
    val newMessage = "New message"

    threadApi.updateMessage(
        messageID,
        message.publicMeta,
        message.privateMeta,
        newMessage.encodeToByteArray()
    )
}

fun deletingMessage() {
    val messageID = "MESSAGE_ID"
    threadApi.deleteMessage(messageID)
}

// END: Managing Messages snippets