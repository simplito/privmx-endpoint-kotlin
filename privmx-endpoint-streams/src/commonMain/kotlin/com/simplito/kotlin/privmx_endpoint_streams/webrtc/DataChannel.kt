package com.simplito.kotlin.privmx_endpoint_streams.webrtc

expect enum class DataChannelState {
    CONNECTING, OPEN, CLOSING, CLOSED
}

expect class DataChannel
expect class DataChannelInit

internal expect fun getDataChannelInit(): DataChannelInit
internal expect fun getDataChannelInit(negotiated: Boolean, ordered: Boolean): DataChannelInit

internal expect val DataChannel.state: DataChannelState
internal expect val DataChannel.label: String
internal expect val DataChannel.id: Int
internal expect val DataChannel.bufferedAmount: Long

/** Queues [data] for sending over the channel. Returns `false` if it could not be buffered. */
internal expect fun DataChannel.send(data: ByteArray, binary: Boolean = true): Boolean


class DataChannelClosedException(message: String = "Data channel is already closed"): Exception(message)


interface DataChannelObserver{
    fun onStateChange()
    fun onMessage(message: ByteArray)
    fun onBufferedAmountChange(bufferedAmount: Long)
}

internal expect fun DataChannel.close()
internal expect fun DataChannel.dispose()
internal expect fun DataChannel.registerObserver(observer: DataChannelObserver)
internal expect fun DataChannel.unregisterObserver()