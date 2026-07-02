package com.simplito.kotlin.privmx_endpoint.model.stream.events

import com.simplito.kotlin.privmx_endpoint.model.stream.StreamSubscription

/**
 * @property streamRoomId StreamRoom ID
 * @property userId User ID of the subscriber
 * @property subscriptions List of stream subscriptions
 */
data class StreamSubscriptionEventData(
    val streamRoomId: String,
    val userId: String,
    val subscriptions: List<StreamSubscription>
)