package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * Holds information about a participant of a Stream Room.
 * A participant is a user who has joined the Stream Room; they may have no subscriptions and
 * publish no Stream, in which case they act only as a listener for events about the Streams in it.
 *
 * @property userId             ID of the participant
 * @property subscriptions      list of the participant's current subscriptions
 * @property publishedStream    Stream published by the participant, or no value if they publish nothing
 */
data class StreamSubscriber(
    val userId: String,
    val subscriptions: List<StreamSubscription>,
    val publishedStream: StreamInfo?
)