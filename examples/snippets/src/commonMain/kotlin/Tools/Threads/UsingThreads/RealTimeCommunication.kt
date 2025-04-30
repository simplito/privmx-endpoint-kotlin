package Tools.Threads.UsingThreads

import com.simplito.kotlin.privmx_endpoint_extra.events.EventType

suspend fun handlingThreadAndMessageEvents() {
    val callbacksID = "CALLBACK_ID"
    val threadID = "THREAD_ID"
    // Starting the Event Loop
    endpointContainer.startListening()

    // Handling Thread events
    endpointSession.registerCallback(
        callbacksID,
        EventType.ThreadCreatedEvent
    ){ newThread ->
        println(newThread.threadId)
    }

    //Handling message Events
    endpointSession.registerCallback(
        callbacksID,
        EventType.ThreadNewMessageEvent(threadID)
    ){ newMessage ->
        println(newMessage.info.messageId)
    }
}