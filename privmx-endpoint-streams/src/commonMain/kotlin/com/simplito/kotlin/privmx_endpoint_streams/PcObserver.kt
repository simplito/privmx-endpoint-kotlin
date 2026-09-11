package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint_streams.webrtc.*

internal expect class PcObserver internal constructor(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: KeyStore,
    roomId: String,
    dataChannelCryptoProvider: InternalDataChannelMessageCryptoProvider,
    remoteStreamObserver: RemoteStreamObserver?,
    onIceCandidateCallback: (candidate: IceCandidate) -> Unit,
    onRenegotiationNeededCallback: () -> Unit = {},
    onIceConnectionChangeCallback: (candidate: IceConnectionState) -> Unit = {},
) : Observer {
    fun setFrameCryptorOptions(options: PmxFrameCryptorOptions)
    fun dispose()
}

internal fun DataChannel.registerDataChannel(
    roomId: String,
    dataChannelCryptoProvider: InternalDataChannelMessageCryptoProvider,
    getRemoteStreamObserver: () -> RemoteStreamObserver?
) {
    dataChannelCryptoProvider.registerDataChannel(roomId,label)
    registerObserver(object : DataChannelObserver {
        override fun onStateChange() {}

        override fun onMessage(message: ByteArray) {
            val message = dataChannelCryptoProvider.decryptMessage(roomId,label,message)
            if(message.statusCode == 0L) {
                getRemoteStreamObserver()?.onMessage(label, message.data)
            }
        }

        override fun onBufferedAmountChange(bufferedAmount: Long) {}
    })
}