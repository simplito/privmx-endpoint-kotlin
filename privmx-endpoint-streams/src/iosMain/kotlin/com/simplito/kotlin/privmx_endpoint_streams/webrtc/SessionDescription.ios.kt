@file:OptIn(ExperimentalForeignApi::class)

package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import WebRTCFramework.RTCSdpType
import WebRTCFramework.RTCSessionDescription
import kotlinx.cinterop.ExperimentalForeignApi

actual typealias SessionDescription = RTCSessionDescription

internal actual val SessionDescription.sdp: String get() = this.sdp

internal actual fun sessionDescription(
    type: SdpType,
    sdp: String
): SessionDescription = RTCSessionDescription(type.type, sdp)

actual enum class SdpType(internal val type: RTCSdpType) {
    OFFER(RTCSdpType.RTCSdpTypeOffer),
    ANSWER(RTCSdpType.RTCSdpTypeAnswer),
    PRANSWER(RTCSdpType.RTCSdpTypePrAnswer),
    ROLLBACK(RTCSdpType.RTCSdpTypeRollback)
}

internal fun RTCSdpType.toCommon(): SdpType =
    SdpType.entries.first { it.type == this }

internal actual fun fromCanonicalForm(
    canonical: String
): SdpType = RTCSessionDescription.typeForString(canonical).toCommon()
