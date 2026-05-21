package webrtc

import WebRTCFramework.RTCPeerConnectionFactory
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import kotlinx.cinterop.ExperimentalForeignApi
@OptIn(ExperimentalForeignApi::class)
internal actual class PeerConnectionManager (
    internal val pcFactory: RTCPeerConnectionFactory,
    private val onTrickle: (Long, String) -> Unit
){
    // Map: roomId -> session
    private val sessions: MutableMap<String, RoomJanusSession> = mutableMapOf()

    // Map: handle value -> roomId
    private val sessionHandles: MutableMap<Long, String> = mutableMapOf()


    actual fun createSession(streamRoomId: String): RoomJanusSession {
        return sessions.getOrPut(streamRoomId) {
            RoomJanusSession(
                streamRoomId,
                pcFactory,
                onTrickle
            )
        }
    }

    actual fun getSession(streamRoomId: String): RoomJanusSession? {
        return sessions[streamRoomId]

    }

    actual fun getSession(handle: StreamHandle): RoomJanusSession? {
        val streamRoomId = sessionHandles[handle.value] ?: return null
        return sessions[streamRoomId]
    }

    actual fun createHandleToRoom(
        handle: StreamHandle,
        roomId: String
    ) {
        sessionHandles[handle.value] = roomId
    }

    actual fun leaveStreamRoom(streamRoomId: String) {
        val session: RoomJanusSession = sessions.remove(streamRoomId) ?: return
        session.subscriber?.close()
        session.publisher?.close()
    }

    actual fun getRoomIds(): Set<String> = sessions.keys

    actual fun closeHandleToRoom(handle: StreamHandle) {
        sessionHandles.remove(handle.value)
    }

    actual fun close() {
        sessionHandles.clear()
//        pcFactory.dispose()       // todo - jaki jest odpowiednik?
    }
}