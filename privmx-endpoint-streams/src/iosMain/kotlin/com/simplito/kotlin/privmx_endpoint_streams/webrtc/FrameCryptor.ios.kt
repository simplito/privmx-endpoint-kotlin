@file:OptIn(ExperimentalForeignApi::class)

package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import WebRTCFramework.PMXFrameCryptorTransformer
import kotlinx.cinterop.ExperimentalForeignApi

actual typealias FrameCryptor = PMXFrameCryptorTransformer

actual data class PmxFrameCryptorOptions actual constructor(
    val dropFrameIfCryptionFailed: Boolean
)

internal actual fun FrameCryptor.disposeCryptor() {}

internal actual fun FrameCryptor.applyOptions(options: PmxFrameCryptorOptions) {
    setDropFramesIfCryptionFailed(options.dropFrameIfCryptionFailed)
}