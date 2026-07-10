package com.simplito.kotlin.privmx_endpoint.model.stream

import kotlin.jvm.JvmInline

/**
 * Unique handle to a stream, used to perform operations on the stream.
 *
 * @property value  The value of the [StreamHandle]
 */
@JvmInline
value class StreamHandle internal constructor(val value: Long?)