package com.simplito.kotlin.privmx_endpoint_streams.webrtc

expect class StatisticsReport
expect val StatisticsReport.timestampUs: Double
expect val StatisticsReport.statistics: Map<String, RTCStatistics>

expect class RTCStatistics
expect val RTCStatistics.timestampUs: Double
expect val RTCStatistics.type: String
expect val RTCStatistics.id: String
expect val RTCStatistics.members: Map<String, Any>
