package com.simplito.kotlin.privmx_endpoint_streams.webrtc

actual enum class DataChannelState(internal val platform: org.webrtc.DataChannel.State) {
    CONNECTING(org.webrtc.DataChannel.State.CONNECTING),
    OPEN(org.webrtc.DataChannel.State.OPEN),
    CLOSING(org.webrtc.DataChannel.State.CLOSING),
    CLOSED(org.webrtc.DataChannel.State.CLOSED)
}
internal fun org.webrtc.DataChannel.State.toCommon(): DataChannelState =
    DataChannelState.entries.first { it.platform == this }

actual typealias DataChannel = org.webrtc.DataChannel
actual typealias DataChannelInit = org.webrtc.DataChannel.Init

internal actual fun getDataChannelInit(): DataChannelInit = org.webrtc.DataChannel.Init()
internal actual fun getDataChannelInit(
    negotiated: Boolean,
    ordered: Boolean
): DataChannelInit = getDataChannelInit().apply { this.negotiated = negotiated; this.ordered = ordered }


internal actual val DataChannel.state: DataChannelState
    get() = state().toCommon()

internal actual val DataChannel.label: String
    get() = label()

internal actual val DataChannel.id: Int
    get() = id()

internal actual val DataChannel.bufferedAmount: Long
    get() = bufferedAmount()

internal actual fun DataChannel.send(data: ByteArray, binary: Boolean): Boolean =
    send(org.webrtc.DataChannel.Buffer(java.nio.ByteBuffer.wrap(data), binary))

internal actual fun DataChannel.registerObserver(observer: DataChannelObserver) {
    registerObserver(object : org.webrtc.DataChannel.Observer {
        override fun onBufferedAmountChange(previousAmount: Long) {
            observer.onBufferedAmountChange(previousAmount)
        }

        override fun onStateChange() {
            observer.onStateChange()
        }

        override fun onMessage(buffer: org.webrtc.DataChannel.Buffer) {
            val data = buffer.data
            val bytes = ByteArray(data.remaining())
            data.get(bytes)
            observer.onMessage(bytes)
        }
    })
}

internal actual fun DataChannel.unregisterObserver() {
    unregisterObserver()
}

internal actual fun DataChannel.close() {
    close()
}

internal actual fun DataChannel.dispose() {
    dispose()
}