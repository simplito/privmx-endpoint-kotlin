package Stacks.Kotlin.events

import Stacks.Kotlin.endpointContainer
import Stacks.Kotlin.endpointSession
import com.simplito.kotlin.privmx_endpoint_extra.events.EventType

// START: Quick start snippets
suspend fun quickStart(){
    // Step 1: Start the event loop
    endpointContainer.startListening()

    // Step 2: Add event listener for 'ThreadCreatedEvent' related with callbackID
    val callbackID = "CALLBACK_ID"
    endpointSession.registerCallback(
        callbackID,
        EventType.ThreadCreatedEvent
    ){ newThreadData ->
        // some actions with newThreadData
    }

    // Step 3: Remove the event listener when no longer needed
    endpointSession.unregisterCallbacks(callbackID)
}
// END: Quick start snippets


// START: Unregister callbacks snippets
suspend fun unregisterCallbacksById(){
    val callbacksID = "CALLBACKS_ID"
    endpointSession.unregisterCallbacks(callbacksID)
}

suspend fun unregisterCallbacksForConnection(){
    endpointSession.unregisterAll()
}
// END: Unregister callbacks snippets