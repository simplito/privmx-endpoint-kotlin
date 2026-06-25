@file:OptIn(ExperimentalForeignApi::class)

import kotlinx.cinterop.ExperimentalForeignApi
import WebRTCFramework.RTCDefaultVideoDecoderFactory
import WebRTCFramework.RTCDefaultVideoEncoderFactory
import WebRTCFramework.RTCIceServer
import WebRTCFramework.RTCPeerConnectionFactory
import WebRTCFramework.RTCPeerConnectionFactoryOptions
import webrtc.IceServer
import webrtc.PeerConnectionFactory

actual class StreamApiInit

private fun DefaultPeerConnectionFactory(
    options: RTCPeerConnectionFactoryOptions
): RTCPeerConnectionFactory = RTCPeerConnectionFactory(
    RTCDefaultVideoEncoderFactory(),
    RTCDefaultVideoDecoderFactory()
)

actual fun StreamApi.joinStreamRoom(streamRoomId: String) {
    //TODO: Rollback this change, it is do only for run test
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