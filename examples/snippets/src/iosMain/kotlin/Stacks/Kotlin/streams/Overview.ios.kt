@file:OptIn(ExperimentalForeignApi::class)

package Stacks.Kotlin.streams

import com.simplito.kotlin.privmx_endpoint_streams.StreamApiInit
import kotlinx.cinterop.ExperimentalForeignApi

actual fun createStreamApiInit(): StreamApiInit {
    // iOS needs no extra dependencies
    return StreamApiInit()
}