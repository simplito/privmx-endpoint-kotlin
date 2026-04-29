package com.simplito.kotlin.privmx_endpoint.model.stream

import com.simplito.kotlin.privmx_endpoint.model.stream.events.PublishedStreamData

data class StreamPublishResult(
    val published: Boolean,
    val data: PublishedStreamData?,
)