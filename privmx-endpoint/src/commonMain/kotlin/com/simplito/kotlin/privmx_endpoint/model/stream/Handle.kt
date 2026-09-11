package com.simplito.kotlin.privmx_endpoint.model.stream

import kotlin.jvm.JvmInline

/**
 * Local handle to a Publisher Stream, returned by `createStream`.
 * Allows to manage the Stream and the feeds it publishes.
 */
@JvmInline
value class StreamHandle internal constructor(val value: Long)

/**
 * Local handle to a Subscriber Stream, returned by `createSubscriberStream`.
 * Allows to manage the Stream and the feeds it is subscribed to.
 */
@JvmInline
value class SubscriberStreamHandle internal constructor(val value: Long)