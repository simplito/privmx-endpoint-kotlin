package webrtc

import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle

internal actual class PeerConnectionManager(
    public val pcFactory: PeerConnectionFactory,
    private val onTrickle: (Long, String) -> Unit,
    private val setNewOfferOnReconfigure: (Long, SdpWithTypeModel) -> Unit
) {
    private val sessions = mutableMapOf<String, RoomJanusSession>()
    private val sessionHandles = mutableMapOf<Long, String>()

    actual fun createSession(streamRoomId: String): RoomJanusSession {
        return sessions.getOrPut(streamRoomId) {
            RoomJanusSession(streamRoomId, pcFactory, onTrickle, setNewOfferOnReconfigure)
        }
    }

    actual fun getSession(streamRoomId: String): RoomJanusSession? =        sessions[streamRoomId]

    actual fun getSession(handle: StreamHandle): RoomJanusSession? =         sessions[sessionHandles[handle.value]]

    actual fun createHandleToRoom(
        handle: StreamHandle,
        roomId: String
    ) {
        sessionHandles[handle.value] = roomId
    }

    actual fun leaveStreamRoom(streamRoomId: String) {
        sessions.remove(streamRoomId)
    }

    actual fun getRoomIds(): Set<String> = sessions.keys

    actual fun closeHandleToRoom(handle: StreamHandle) {
        sessionHandles.remove(handle.value)
    }

    actual fun close() {
        sessionHandles.clear()
        pcFactory.dispose()
    }
}