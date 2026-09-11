@file:OptIn(ExperimentalForeignApi::class)

package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import WebRTCFramework.RTCDataBuffer
import WebRTCFramework.RTCDataChannel
import WebRTCFramework.RTCDataChannelConfiguration
import WebRTCFramework.RTCDataChannelDelegateProtocol
import WebRTCFramework.RTCDataChannelState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.darwin.NSObject
import platform.posix.memcpy
import platform.posix.uint64_t

actual enum class DataChannelState(internal val platform: RTCDataChannelState) {
    CONNECTING(RTCDataChannelState.RTCDataChannelStateConnecting),
    OPEN(RTCDataChannelState.RTCDataChannelStateOpen),
    CLOSING(RTCDataChannelState.RTCDataChannelStateClosing),
    CLOSED(RTCDataChannelState.RTCDataChannelStateClosed)
}

actual typealias DataChannel = RTCDataChannel
actual typealias DataChannelInit = RTCDataChannelConfiguration

internal actual fun getDataChannelInit(): DataChannelInit = RTCDataChannelConfiguration()
internal actual fun getDataChannelInit(
    negotiated: Boolean,
    ordered: Boolean
): DataChannelInit = getDataChannelInit().apply { this.isNegotiated = negotiated; this.isOrdered = ordered }

internal fun RTCDataChannelState.toCommon(): DataChannelState =
    DataChannelState.entries.first { it.platform == this }

internal actual val DataChannel.state: DataChannelState
    get() = readyState().toCommon()

internal actual val DataChannel.label: String
    get() = label()

internal actual val DataChannel.id: Int
    get() = channelId()

internal actual val DataChannel.bufferedAmount: Long
    get() = bufferedAmount().toLong()

internal actual fun DataChannel.send(data: ByteArray, binary: Boolean): Boolean =
    sendData(RTCDataBuffer(data.toNSData(), binary))

private fun ByteArray.toNSData(): NSData =
    if (isEmpty()) NSData()
    else usePinned { NSData.dataWithBytes(it.addressOf(0), size.toULong()) }

internal actual fun DataChannel.registerObserver(observer: DataChannelObserver) {
    setDelegate(object : NSObject(), RTCDataChannelDelegateProtocol {
        override fun dataChannelDidChangeState(dataChannel: RTCDataChannel) {
            observer.onStateChange()
        }

        override fun dataChannel(
            dataChannel: RTCDataChannel,
            didReceiveMessageWithBuffer: RTCDataBuffer
        ) {
            observer.onMessage(didReceiveMessageWithBuffer.data().toByteArray())
        }

        override fun dataChannel(dataChannel: RTCDataChannel, didChangeBufferedAmount: uint64_t) {
            observer.onBufferedAmountChange(didChangeBufferedAmount.toLong())
        }
    })
}

internal actual fun DataChannel.unregisterObserver() {
    setDelegate(null)
}

private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    return ByteArray(size).apply {
        usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
    }
}

internal actual fun DataChannel.close() {
    close()
}

internal actual fun DataChannel.dispose(){}
