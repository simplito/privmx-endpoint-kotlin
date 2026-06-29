@file:OptIn(ExperimentalForeignApi::class)

package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import WebRTCFramework.RTCMediaConstraints
import kotlinx.cinterop.ExperimentalForeignApi

internal fun mediaConstraints() =
    RTCMediaConstraints(mandatoryConstraints = emptyMap<Any?, Any>(), optionalConstraints = null)