package Tools.Threads.UsingThreads

import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.ThreadEventSelectorType
import com.simplito.kotlin.privmx_endpoint_extra.events.CallbackRegistration
import com.simplito.kotlin.privmx_endpoint_extra.events.EventType

suspend fun handlingThreadAndMessageEvents() {
    val threadCallbacksGroup = "THREAD_CALLBACKS_GROUP"
    val messageCallbacksGroup = "MESSAGE_CALLBACKS_GROUP"
    val threadID = "THREAD_ID"

    // Starting the Event Loop
    endpointContainer.startListening()

    endpointSession.registerManyCallbacks(

        // Handling Thread events
        CallbackRegistration(
            threadCallbacksGroup,
            EventType.ThreadCreatedEvent(contextId)
        ) { newThread ->
            println(newThread.threadId)
        },

        //Handling message Events
        CallbackRegistration(
            messageCallbacksGroup,
            EventType.ThreadNewMessageEvent(
                ThreadEventSelectorType.THREAD_ID,
                threadID
            )
        ) { newMessage ->
            println(newMessage.info.messageId)
        }
    )

    endpointSession.unregisterCallbacks(threadCallbacksGroup, messageCallbacksGroup)
}