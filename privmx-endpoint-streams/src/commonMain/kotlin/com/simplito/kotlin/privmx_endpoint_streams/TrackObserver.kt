package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint_streams.webrtc.MediaStreamTrack

fun interface TrackObserver {
    fun onRemoteTrack(streamId: String?, track: MediaStreamTrack)
}
