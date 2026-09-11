@file:OptIn(ExperimentalForeignApi::class)

package com.simplito.kotlin.privmx_endpoint_streams

import kotlinx.cinterop.ExperimentalForeignApi
import WebRTCFramework.RTCDefaultVideoDecoderFactory
import WebRTCFramework.RTCDefaultVideoEncoderFactory
import WebRTCFramework.RTCIceServer
import WebRTCFramework.RTCPeerConnectionFactory
import WebRTCFramework.RTCPeerConnectionFactoryOptions
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.IceServer
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnectionFactory

/**
 * iOS requires no platform-specific data to initialize [StreamApi];
 * this type carries no parameters. Create it with `StreamApiInit()`.
*/
actual class StreamApiInit

private fun DefaultPeerConnectionFactory(
    options: RTCPeerConnectionFactoryOptions
): RTCPeerConnectionFactory = RTCPeerConnectionFactory(
    RTCDefaultVideoEncoderFactory(),
    RTCDefaultVideoDecoderFactory()
)


/**
 * Joins a StreamRoom and prepares the session for WebRTC communication.
 *
 * Required before working with streams and stream events in the room.
 *
 * @param streamRoomId ID of the StreamRoom to join
 *
 * @throws PrivmxException       thrown when method encounters an exception
 * @throws NativeException       thrown when method encounters an unknown exception
 * @throws IllegalStateException thrown when instance is closed
 */
@Throws(
    PrivmxException::class,
    NativeException::class,
    IllegalStateException::class
)
actual fun StreamApi.joinStreamRoom(streamRoomId: String) {
    val session: RoomJanusSession = pcManager.createSession(streamRoomId)
    api.joinStreamRoom(streamRoomId, session.webrtc)
}

internal actual fun StreamApi.getRTCConfiguration(): List<IceServer> {
    return this.api.getTurnCredentials().map { item ->
        RTCIceServer(
            listOf(item.url),
            item.username,
            item.password
        )
    }
}

actual fun StreamApi.createDefaultPeerConnectionFactory(
    init: StreamApiInit
): PeerConnectionFactory {
    return  DefaultPeerConnectionFactory(
        RTCPeerConnectionFactoryOptions(),
    )
}

internal actual fun StreamApi.initPeerConnectionFactory(
    init: StreamApiInit
) {
    PeerConnectionFactory.initialize()
}