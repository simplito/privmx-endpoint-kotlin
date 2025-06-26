package Stacks.Kotlin.events

import Stacks.Kotlin.endpointSession
import Stacks.Kotlin.user1Id
import Stacks.Kotlin.user1PublicKey
import Stacks.Kotlin.user2Id
import Stacks.Kotlin.user2PublicKey
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.modules.event.EventApi
import com.simplito.kotlin.privmx_endpoint_extra.events.EventType

lateinit var eventApi: EventApi

fun setEventApi() {
    val eventApi = endpointSession.eventApi
}

fun emittingCustomEvents() {
    val contextId = "CONTEXT_ID"
    val channelName = "CHANNEL_NAME"

    val users: List<UserWithPubKey> = listOf(
        UserWithPubKey(user1Id, user1PublicKey),
        UserWithPubKey(user2Id, user2PublicKey)
    )
    val eventData = "Custom Event Data"

    endpointSession.eventApi?.emitEvent(
        contextId, users, channelName, eventData.encodeToByteArray()
    )
}

suspend fun handlingCustomEvents() {
    val callbacksId = "CALLBACKS_ID"
    val contextId = "CONTEXT_ID"
    val channelName = "CHANNEL_NAME"

    endpointSession.registerCallback(
        callbacksId,
        EventType.ContextCustomEvent(contextId, channelName)
    ) { customEventData ->
        // Some actions when custom event arrives
    }
}