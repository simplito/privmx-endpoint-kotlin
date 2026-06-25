@file:OptIn(ExperimentalForeignApi::class)

package webrtc

import WebRTCFramework.RTCMediaConstraints
import kotlinx.cinterop.ExperimentalForeignApi

internal fun mediaConstraints() =
    RTCMediaConstraints(mandatoryConstraints = emptyMap<Any?, Any>(), optionalConstraints = null)