package Tools.Threads.UsingThreads

import com.simplito.kotlin.privmx_endpoint.model.Message
import com.simplito.kotlin.privmx_endpoint_extra.model.SortOrder

fun listMessages() {
    val threadId = "THREAD_ID"
    val startIndex: Long = 0
    val pageSize: Long = 30

    val messages: List<Message> = endpointSession.threadApi!!.listMessages(
        threadId,
        startIndex,
        pageSize,
        SortOrder.DESC
    ).readItems
}

fun updatingMessages() {
    val messageId = "MESSAGE_ID"
    val privateMeta: ByteArray = "New private data".encodeToByteArray()
    val publicMeta: ByteArray = "New public data".encodeToByteArray()
    val data: ByteArray = "New message data".encodeToByteArray()

    endpointSession.threadApi?.updateMessage(
        messageId,
        publicMeta,
        privateMeta,
        data
    )
}

fun deletingMessage() {
    val messageId = "MESSAGE_ID"
    endpointSession.threadApi?.deleteMessage(messageId)
}