@file:OptIn(ExperimentalForeignApi::class)

package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import WebRTCFramework.RTCSdpType
import WebRTCFramework.RTCSessionDescription
import kotlinx.cinterop.ExperimentalForeignApi

actual typealias SessionDescription = RTCSessionDescription

internal actual val SessionDescription.sdp: String get() = this.sdp

internal actual fun sessionDescription(type: String, sdp: String): SessionDescription =
    RTCSessionDescription(type.toSdpType(), sdp)

private fun String.toSdpType(): RTCSdpType = when (this) {
    "offer" -> RTCSdpType.RTCSdpTypeOffer
    "answer" -> RTCSdpType.RTCSdpTypeAnswer
    "pranswer" -> RTCSdpType.RTCSdpTypePrAnswer
    else -> RTCSdpType.RTCSdpTypeOffer
}