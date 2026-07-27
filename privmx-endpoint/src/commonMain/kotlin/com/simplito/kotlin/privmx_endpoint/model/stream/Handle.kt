package com.simplito.kotlin.privmx_endpoint.model.stream

import kotlin.jvm.JvmInline

@JvmInline
value class  StreamHandle internal constructor(val value: Long)

@JvmInline
value class  SubscriberStreamHandle internal constructor(val value: Long)