package Tools.Stores.UsingStores

import com.simplito.java.privmx_endpoint_extra.events.EventType

fun handlingStoreEvents() {
    val callbacksID = "CALLBACK_ID"
    val storeID = "STORE_ID"

    // Starting the Event Loop
    endpointContainer.startListening()

    // Handling Store Events
    endpointSession.registerCallback(
        callbacksID,
        EventType.StoreCreatedEvent
    ) { newStore ->
        println(newStore.storeId)
    }

    // Handling File Events
    endpointSession.registerCallback(
        callbacksID,
        EventType.StoreFileCreatedEvent(storeID)
    ) { newFile ->
        println(newFile.info.fileId)
    }
}