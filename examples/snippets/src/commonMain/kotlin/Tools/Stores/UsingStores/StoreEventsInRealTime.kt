package Tools.Stores.UsingStores

import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.StoreEventSelectorType
import com.simplito.kotlin.privmx_endpoint_extra.events.CallbackRegistration
import com.simplito.kotlin.privmx_endpoint_extra.events.EventType

suspend fun handlingStoreEvents() {
    val storeCallbacksGroup = "STORE_CALLBACKS_GROUP"
    val fileCallbacksGroup = "FILE_CALLBACKS_GROUP"
    val storeID = "STORE_ID"

    // Starting the Event Loop
    endpointContainer.startListening()

    endpointSession.registerManyCallbacks(

        // Handling Store Events
        CallbackRegistration(
            storeCallbacksGroup,
            EventType.StoreCreatedEvent(contextId)
        ) { newStore ->
            println(newStore.storeId)
        },

        // Handling File Events
        CallbackRegistration(
            fileCallbacksGroup,
            EventType.StoreFileCreatedEvent(
                StoreEventSelectorType.STORE_ID,
                storeID
            )
        ) { newFile ->
            println(newFile.info.fileId)
        }
    )

    endpointSession.unregisterCallbacks(storeCallbacksGroup, fileCallbacksGroup)
}