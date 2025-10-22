package Tools.Threads.UsingThreads

fun sendingMessages() {
    val threadId = "THREAD_ID"
    val privateMeta = "My private data".encodeToByteArray()
    val publicMeta = "My public data".encodeToByteArray()
    val message = "This is my message".encodeToByteArray()

    val messageId = endpointSession.threadApi?.sendMessage(
        threadId,
        publicMeta,
        privateMeta,
        message
    )
}