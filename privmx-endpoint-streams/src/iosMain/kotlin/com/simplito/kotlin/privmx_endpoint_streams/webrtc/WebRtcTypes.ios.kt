@file:OptIn(ExperimentalForeignApi::class)

package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import WebRTCFramework.RTCIceServer
import WebRTCFramework.RTCPeerConnectionDelegateProtocol
import WebRTCFramework.RTCRtpSender
import kotlinx.cinterop.ExperimentalForeignApi

actual typealias RtpSender = RTCRtpSender
actual typealias IceServer = RTCIceServer
actual typealias Observer = RTCPeerConnectionDelegateProtocol