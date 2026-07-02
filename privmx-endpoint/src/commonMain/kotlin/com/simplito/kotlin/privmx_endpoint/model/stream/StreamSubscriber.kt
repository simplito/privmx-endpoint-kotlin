package com.simplito.kotlin.privmx_endpoint.model.stream

data class StreamSubscriber (
    val userId: String,
    val subscriptions: List<StreamSubscription>,
    val publishedStream: StreamInfo?
)