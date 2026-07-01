package com.simplito.kotlin.privmx_endpoint.model.stream

import kotlin.jvm.JvmInline

@JvmInline
value class Handle internal constructor(val value: Long)
typealias StreamHandle = Handle
typealias SubscriberStreamHandle = Handle