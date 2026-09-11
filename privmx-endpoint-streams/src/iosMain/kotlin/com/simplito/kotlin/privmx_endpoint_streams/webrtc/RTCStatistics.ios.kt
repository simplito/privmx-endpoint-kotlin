@file:
OptIn(ExperimentalForeignApi::class)
package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import WebRTCFramework.RTCStatistics
import WebRTCFramework.RTCStatisticsReport
import kotlinx.cinterop.ExperimentalForeignApi

actual typealias StatisticsReport = RTCStatisticsReport


actual val StatisticsReport.timestampUs: Double
    get() = this.timestamp_us

@Suppress("UNCHECKED_CAST")
actual val StatisticsReport.statistics: Map<String, RTCStatistics>
    get() = this.statistics as Map<String, RTCStatistics>

actual typealias RTCStatistics = RTCStatistics


actual val RTCStatistics.timestampUs: Double
    get() = timestamp_us
actual val RTCStatistics.type: String
    get() = type
actual val RTCStatistics.id: String
    get() = id
@Suppress("UNCHECKED_CAST")
actual val RTCStatistics.members: Map<String, Any>
    get() = values as Map<String,Any>