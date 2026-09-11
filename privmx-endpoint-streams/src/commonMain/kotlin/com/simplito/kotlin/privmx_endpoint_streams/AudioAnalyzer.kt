package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint_streams.webrtc.AudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.StatisticsReport
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.members
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.statistics
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.timestampUs
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.trackId
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SpeakingConfig(
    val rmsEmaAlpha: Float = .2f,
    val noiseEmaAlpha: Float = .02f,
    val thresholdOffset: Float = .005f,
    val activityWindowMs: Int = 300,
    val holdMs: Int = 200
)

class SpeakingStats(
    val track: AudioTrack
) {
    internal var emaRms: Double = .0
    internal var noiseFloor: Double = .0005
    var lastAboveThresholdMs: Long = 0
        internal set
    var activeToMs: Long = 0
        internal set
    internal val calculationsMutex = Mutex()
}

fun StatisticsReport.readRms(track: AudioTrack): Double? {
    return statistics
        .values
        .firstOrNull { stats -> stats.members["trackIdentifier"] == track.trackId }
        ?.members["audioLevel"] as? Double
}

suspend fun SpeakingStats.recalculate(report: StatisticsReport, config: SpeakingConfig = SpeakingConfig()) {
    val rmsValue = report.readRms(track) ?: return
    val timestampMs = report.timestampUs.toLong() / 1000L
    calculationsMutex.withLock {
        emaRms = config.rmsEmaAlpha * rmsValue + (1 - config.rmsEmaAlpha) * emaRms
        if (emaRms < noiseFloor + config.thresholdOffset) {
            noiseFloor = config.noiseEmaAlpha * emaRms +
                    (1 - config.noiseEmaAlpha) * noiseFloor
        }

        val adaptiveThreshold = noiseFloor + config.thresholdOffset
        if (emaRms >= adaptiveThreshold) {
            if (timestampMs > lastAboveThresholdMs) {
                activeToMs = timestampMs + config.holdMs
            }
        } else {
            if (timestampMs > activeToMs) {
                lastAboveThresholdMs = timestampMs + config.activityWindowMs
            }
        }
    }
}

