package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint_streams.webrtc.IceCandidate
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.IceConnectionState
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.KeyStore
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.Observer
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnectionFactory
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PmxFrameCryptorOptions

expect class PcObserver(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: KeyStore,
    trackObserver: TrackObserver?,
    onIceCandidateCallback: (candidate: IceCandidate) -> Unit,
    onRenegotiationNeededCallback: () -> Unit = {},
    onIceConnectionChangeCallback: (candidate: IceConnectionState) -> Unit = {}
): Observer {
    fun setFrameCryptorOptions(options: PmxFrameCryptorOptions)
    fun dispose()
}