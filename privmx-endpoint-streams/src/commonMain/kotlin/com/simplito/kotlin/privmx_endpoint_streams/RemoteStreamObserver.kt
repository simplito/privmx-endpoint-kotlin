package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint_streams.webrtc.MediaStreamTrack

interface RemoteStreamObserver {
    fun onTrack(streamId: String?, track: MediaStreamTrack)
    fun onMessage(streamId: String, message: ByteArray)
}
