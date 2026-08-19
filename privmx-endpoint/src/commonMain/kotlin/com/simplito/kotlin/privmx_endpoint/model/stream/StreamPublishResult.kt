package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * Represents the result of a stream publish/update operation.
 *
 * @property published  whether the stream was successfully published
 * @property data       additional information about the published stream
 */
data class StreamPublishResult(
    val published: Boolean,
    val data: PublishedStreamData?,
)