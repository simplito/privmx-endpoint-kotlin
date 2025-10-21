package Tools.Inboxes.UsingInboxes

import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.InboxEventSelectorType
import com.simplito.kotlin.privmx_endpoint_extra.events.CallbackRegistration
import com.simplito.kotlin.privmx_endpoint_extra.events.EventType

class InboxUpdates {
    suspend fun handlingInboxEvents() {
        val inboxCallbacksGroup = "INBOX_CALLBACKS_GROUP"
        val entryCallbacksGroup = "ENTRY_CALLBACKS_GROUP"
        val inboxID = "INBOX_ID"

        // Starting the Event Loop
        endpointContainer.startListening()

        endpointSession.registerManyCallbacks(

            // Handling Inbox Events
            CallbackRegistration(
                inboxCallbacksGroup,
                EventType.InboxUpdatedEvent(
                    InboxEventSelectorType.CONTEXT_ID,
                    contextId
                )
            ) { updatedInbox ->
                println(updatedInbox.lastModifier)
            },

            // Handling Inbox Entry Events
            CallbackRegistration(
                entryCallbacksGroup,
                EventType.InboxEntryCreatedEvent(
                    InboxEventSelectorType.INBOX_ID,
                    inboxID
                )
            ) { newEntry ->
                println(newEntry.inboxId)
            }
        )

        endpointSession.unregisterCallbacks(inboxCallbacksGroup, entryCallbacksGroup)
    }
}