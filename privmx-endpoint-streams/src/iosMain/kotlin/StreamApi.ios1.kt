@file:OptIn(ExperimentalForeignApi::class)

import WebRTCFramework.RTCDefaultVideoDecoderFactory
import WebRTCFramework.RTCDefaultVideoEncoderFactory
import WebRTCFramework.RTCIceServer
import WebRTCFramework.RTCPeerConnectionFactory
import WebRTCFramework.RTCPeerConnectionFactoryOptions
import kotlinx.cinterop.ExperimentalForeignApi

actual class StreamApiInit

private fun DefaultPeerConnectionFactory(
    options: RTCPeerConnectionFactoryOptions
): RTCPeerConnectionFactory = RTCPeerConnectionFactory(
    RTCDefaultVideoEncoderFactory(),
    RTCDefaultVideoDecoderFactory()
)

actual fun StreamApi.initialFun() {
    this.pcManager = PeerConnectionManager(
        DefaultPeerConnectionFactory(
            RTCPeerConnectionFactoryOptions(),
        ),
        onTrickle = { sessionId, rtcConfiguration ->
            this.api.trickle(sessionId, rtcConfiguration)
        },
        acceptOfferOnReconfigure = {_ , _ -> TODO("Not implemented yet.") }
    )
}

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

actual fun StreamApi.dropBrokenFrames(streamRoomId: String, enable: Boolean) {
    TODO("Not implemented - probably not available in iOS.")
}