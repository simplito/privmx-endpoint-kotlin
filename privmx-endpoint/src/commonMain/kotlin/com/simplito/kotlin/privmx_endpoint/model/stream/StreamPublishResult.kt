package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * Represents the result of a stream publish/update operation.
 *
 * @property published Whether the stream was successfully published
 * @property data Additional information about the published stream
*/
data class StreamPublishResult(
    val published: Boolean,
    val data: PublishedStreamData?,
)