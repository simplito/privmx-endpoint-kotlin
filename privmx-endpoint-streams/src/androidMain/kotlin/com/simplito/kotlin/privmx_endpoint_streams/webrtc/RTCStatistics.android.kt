package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import org.webrtc.RTCStats
import org.webrtc.RTCStatsReport

actual typealias StatisticsReport = RTCStatsReport

actual val StatisticsReport.timestampUs: Double get() = this.timestampUs
actual val StatisticsReport.statistics: Map<String, RTCStatistics>
    get() = this.statsMap

actual typealias RTCStatistics = RTCStats

actual val RTCStatistics.timestampUs: Double get() = timestampUs
actual val RTCStatistics.type: String get() = type
actual val RTCStatistics.id: String get() = id
actual val RTCStatistics.members: Map<String, Any> get() = members