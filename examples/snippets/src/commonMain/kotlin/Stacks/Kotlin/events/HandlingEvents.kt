package Stacks.Kotlin.events

import com.simplito.kotlin.privmx_endpoint_extra.events.EventType
import Stacks.Kotlin.endpointSession

// START: Connection events snippets
suspend fun handlingConnectionEvents(){
    val callbacksId = "CALLBACKS_ID"

    endpointSession.registerCallback(
        callbacksId,
        EventType.ConnectedEvent
    ){
        // some actions when lib was connected
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.DisconnectedEvent
    ){
        // some actions when lib was disconnected
    }
}
// END: Connection events snippets


// START: Threads events snippets
suspend fun handlingThreadEvents(){
    val callbacksId = "CALLBACKS_ID"

    endpointSession.registerCallback(
        callbacksId,
        EventType.ThreadCreatedEvent
    ){ newThreadData ->
        // some actions when new thread created
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.ThreadUpdatedEvent
    ){ threadUpdateData ->
        // some actions when thread updated
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.ThreadStatsChangedEvent
    ){ threadStatsUpdateData ->
        // some actions when thread stats changed
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.ThreadDeletedEvent
    ){ deletedThreadData ->
        // some actions when thread deleted
    }
}

suspend fun handlingMessageEvents(){
    val callbacksId = "CALLBACKS_ID"
    val threadID = "THREAD_ID"

    endpointSession.registerCallback(
        callbacksId,
        EventType.ThreadNewMessageEvent(threadID)
    ){ newMessageData ->
        // some actions on new message
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.ThreadMessageUpdatedEvent(threadID)
    ){ updatedMessageData ->
        // some actions when message updated
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.ThreadMessageDeletedEvent(threadID)
    ){ deletedMessageData ->
        // some actions when message deleted
    }
}
// END: Threads events snippets


// START: Stores events snippets
suspend fun handlingStoreEvents(){
    val callbacksId = "CALLBACKS_ID"

    endpointSession.registerCallback(
        callbacksId,
        EventType.StoreCreatedEvent
    ){ newStoreData ->
        // some actions when new store created
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.StoreUpdatedEvent
    ){ storeUpdateData ->
        // some actions when store updated
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.StoreStatsChangedEvent
    ){ storeStatsUpdateData ->
        // some actions when store stats changed
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.StoreDeletedEvent
    ){ deletedStoreData ->
        // some actions when store deleted
    }
}

suspend fun handlingFileEvents(){
    val callbacksId = "CALLBACKS_ID"
    val storeID = "STORE_ID"

    endpointSession.registerCallback(
        callbacksId,
        EventType.StoreFileCreatedEvent(storeID)
    ){ newFileData ->
        // some actions on new file
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.StoreFileUpdatedEvent(storeID)
    ){ updatedFileData ->
        // some actions when file updated
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.StoreFileDeletedEvent(storeID)
    ){ deletedFileData ->
        // some actions when file deleted
    }
}
// END: Stores events snippets


// START: Inboxes events snippets
suspend fun handlingInboxEvents(){
    val callbacksId = "CALLBACKS_ID"

    endpointSession.registerCallback(
        callbacksId,
        EventType.InboxCreatedEvent
    ){ newInboxData ->
        // some actions when new inbox created
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.InboxUpdatedEvent
    ){ inboxUpdateData ->
        // some actions when inbox updated
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.InboxDeletedEvent
    ){ deletedInboxData ->
        // some actions when inbox deleted
    }
}

suspend fun handlingEntriesEvents(){
    val callbacksId = "CALLBACKS_ID"
    val inboxID = "INBOX_ID"

    endpointSession.registerCallback(
        callbacksId,
        EventType.InboxEntryCreatedEvent(inboxID)
    ){ newEntryData ->
        // some actions on new entry
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.InboxEntryDeletedEvent(inboxID)
    ){ deletedEntryData ->
        // some actions when entry deleted
    }
}
// END: Inboxes events snippets