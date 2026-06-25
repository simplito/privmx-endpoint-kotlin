@file:OptIn(ExperimentalForeignApi::class)

package webrtc

import WebRTCFramework.RTCIceServer
import WebRTCFramework.RTCPeerConnectionDelegateProtocol
import WebRTCFramework.RTCRtpSender
import kotlinx.cinterop.ExperimentalForeignApi

actual typealias RtpSender = RTCRtpSender
actual typealias IceServer = RTCIceServer
actual typealias Observer = RTCPeerConnectionDelegateProtocol