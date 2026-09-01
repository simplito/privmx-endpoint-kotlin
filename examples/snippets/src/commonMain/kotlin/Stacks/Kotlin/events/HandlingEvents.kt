package Stacks.Kotlin.events

import Stacks.Kotlin.contextId
import Stacks.Kotlin.endpointSession
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamRoom
import com.simplito.kotlin.privmx_endpoint.model.stream.events.*
import com.simplito.kotlin.privmx_endpoint.model.stream.events.eventSelectorTypes.StreamEventSelectorType
import com.simplito.kotlin.privmx_endpoint_extra.events.CallbackRegistration
import com.simplito.kotlin.privmx_endpoint_extra.events.EventType

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


// START: KVDBs events snippets
suspend fun handlingKvdbsEvents() {
    val callbacksId = "CALLBACKS_ID"

    endpointSession.registerCallback(
        callbacksId,
        EventType.KvdbCreatedEvent
    ) { kvdbCreatedData ->
        // some actions when new KVDB created
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.KvdbUpdatedEvent
    ) { kvdbUpdatedData ->
        // some actions when KVDB updated
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.KvdbDeletedEvent
    ) { kvdbDeletedData ->
        // some actions when KVDB deleted
    }
}

suspend fun handlingKvdbEntriesEvents() {
    val callbacksId = "CALLBACKS_ID"
    val kvdbID = "KVDB_ID"

    endpointSession.registerCallback(
        callbacksId,
        EventType.KvdbNewEntryEvent(kvdbID)
    ) { newEntryData ->
        // some actions on new KVDB entry
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.KvdbEntryUpdatedEvent(kvdbID)
    ) { updatedEntryData ->
        // some actions when KVDB entry updated
    }

    endpointSession.registerCallback(
        callbacksId,
        EventType.KvdbEntryDeletedEvent(kvdbID)
    ) { deletedEntryData ->
        // some actions when KVDB entry deleted
    }
}
// END: KVDBs events snippets

// START: Stream events snippets

suspend fun handleStreamRoomEvents() {
    val callbacksGroup = "CALLBACKS_GROUP"

    endpointSession.registerManyCallbacks(
        CallbackRegistration(
            callbacksGroup,
            EventType.StreamRoomCreatedEvent(contextId)
        ) { newStreamRoom: StreamRoom ->
            // some actions when a new stream room is created
        },

        CallbackRegistration(
            callbacksGroup,
            EventType.StreamRoomUpdatedEvent(
                StreamEventSelectorType.CONTEXT_ID,
                contextId
            )
        ) { updatedStreamRoom: StreamRoom ->
            // some actions when a stream room is updated
        },

        CallbackRegistration(
            callbacksGroup,
            EventType.StreamRoomDeletedEvent(
                StreamEventSelectorType.CONTEXT_ID,
                contextId
            )
        ) { deletedStreamRoomData: StreamRoomDeletedEventData ->
            // some actions when a stream room is deleted
        },

        CallbackRegistration(
            callbacksGroup,
            EventType.StreamRoomJoinedEvent(
                StreamEventSelectorType.CONTEXT_ID,
                contextId
            )
        ) { joinedData: StreamRoomParticipantEventData ->
            // some actions when a participant joins the stream room
        },

        CallbackRegistration(
            callbacksGroup,
            EventType.StreamRoomLeftEvent(
                StreamEventSelectorType.CONTEXT_ID,
                contextId
            )
        ) { leftData: StreamRoomParticipantEventData ->
            // some actions when a participant leaves the stream room
        }
    )
}

suspend fun handleStreamEvents() {
    val callbacksGroup = "CALLBACKS_GROUP"
    val streamRoomId = "STREAM_ROOM_ID"

    endpointSession.registerManyCallbacks(
        CallbackRegistration(
            callbacksGroup,
            EventType.StreamPublishedEvent(
                StreamEventSelectorType.STREAMROOM_ID,
                streamRoomId
            )
        ) { publishedStreamData: StreamPublishedEventData ->
            // some actions when a stream is published in the specified room
        },

        CallbackRegistration(
            callbacksGroup,
            EventType.StreamUnpublishedEvent(
                StreamEventSelectorType.STREAMROOM_ID,
                streamRoomId
            )
        ) { unpublishedStreamData: StreamUnpublishedEventData ->
            // some actions when a stream is unpublished from the specified room
        },

        CallbackRegistration(
            callbacksGroup,
            EventType.StreamSubscribedEvent(
                StreamEventSelectorType.STREAMROOM_ID,
                streamRoomId
            )
        ) { subscribedData: StreamSubscriptionEventData ->
            // some actions when a stream is subscribed
        },

        CallbackRegistration(
            callbacksGroup,
            EventType.StreamUnsubscribedEvent(
                StreamEventSelectorType.STREAMROOM_ID,
                streamRoomId
            )
        ) { unsubscribedData: StreamSubscriptionEventData ->
            // some actions when a stream is unsubscribed
        },

        CallbackRegistration(
            callbacksGroup,
            EventType.StreamUpdatedEvent(
                StreamEventSelectorType.CONTEXT_ID,
                contextId
            )
        ) { updatedStreamData: StreamUpdatedEventData ->
            // some actions when stream data is updated
        }
    )
}

// END: Stream events snippets
