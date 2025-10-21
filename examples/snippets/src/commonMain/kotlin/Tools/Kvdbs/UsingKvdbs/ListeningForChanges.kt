package Tools.Kvdbs.UsingKvdbs

import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.KvdbEventSelectorType
import com.simplito.kotlin.privmx_endpoint_extra.events.CallbackRegistration
import com.simplito.kotlin.privmx_endpoint_extra.events.EventType

suspend fun handlingKvdbEvents() {
    val kvdbCallbacksGroup = "KVDB_CALLBACKS_GROUP"
    val entryCallbacksGroup = "ENTRY_CALLBACKS_GROUP"
    val kvdbID = "KVDB_ID"

    // Starting the Event Loop
    endpointContainer.startListening()

    endpointSession.registerManyCallbacks(

        // Handling KVDB Events
        CallbackRegistration(
            kvdbCallbacksGroup,
            EventType.KvdbStatsChangedEvent(
                KvdbEventSelectorType.CONTEXT_ID,
                contextId
            )
        ) { kvdbStats ->
            println(kvdbStats.lastEntryDate)
        },

        // Handling KVDB Entry Events
        CallbackRegistration(
            entryCallbacksGroup,
            EventType.KvdbNewEntryEvent(
                KvdbEventSelectorType.KVDB_ID,
                kvdbID
            )
        ) { newEntry ->
            println(newEntry.info.key)
        }
    )

    endpointSession.unregisterCallbacks(kvdbCallbacksGroup, entryCallbacksGroup)
}
