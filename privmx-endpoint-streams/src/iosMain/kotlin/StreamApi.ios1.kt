@file:OptIn(ExperimentalForeignApi::class)

import WebRTCFramework.RTCDefaultVideoDecoderFactory
import WebRTCFramework.RTCDefaultVideoEncoderFactory
import WebRTCFramework.RTCIceServer
import WebRTCFramework.RTCPeerConnectionFactory
import WebRTCFramework.RTCPeerConnectionFactoryOptions
import kotlinx.cinterop.ExperimentalForeignApi
import webrtc.IceServer
import webrtc.PeerConnectionManager
import webrtc.RoomJanusSession

actual data class StreamApiInit(
    val a: String
)

private fun DefaultPeerConnectionFactory(
    options: RTCPeerConnectionFactoryOptions
): RTCPeerConnectionFactory = RTCPeerConnectionFactory(
    RTCDefaultVideoEncoderFactory(),
    RTCDefaultVideoDecoderFactory()
)

actual fun StreamApi.initialFun() {
    this.pcManager = PeerConnectionManager(
        DefaultPeerConnectionFactory(RTCPeerConnectionFactoryOptions())
    ) { sessionId, rtcConfiguration ->
        this.api.trickle(sessionId, rtcConfiguration)
    }
}


actual fun StreamApi.joinStreamRoom(streamRoomId: String) {
    //TODO: Rollback this change, it is do only for run test
    val session: RoomJanusSession = pcManager.createSession(streamRoomId)
    api.joinStreamRoom(streamRoomId, session.webrtc)
}


// todo - idk if correct
internal actual fun StreamApi.getRTCConfiguration(): List<IceServer> {
    return this.api.getTurnCredentials().map { item ->
        RTCIceServer(
            listOf(item.url),
            item.username,
            item.password
        )
    }
}

actual fun StreamApi.dropBrokenFrames(streamRoomId: String, enable: Boolean) {
    TODO("Not implemented")
}