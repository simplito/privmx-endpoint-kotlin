package modules

import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import org.webrtc.PeerConnectionFactory
import java.lang.AutoCloseable

internal class PeerConnectionManager(
    public val pcFactory: PeerConnectionFactory,
    private val onTrickle: (Long, String) -> Unit,
    private val setNewOfferOnReconfigure: (Long, SdpWithTypeModel) -> Unit
) : AutoCloseable {
    private val sessions = mutableMapOf<String, RoomJanusSession>()
    private val sessionHandles = mutableMapOf<Long, String>()

    fun createSession(streamRoomId: String): RoomJanusSession {
        return sessions.getOrPut(streamRoomId) {
            RoomJanusSession(streamRoomId, pcFactory, onTrickle, setNewOfferOnReconfigure)
        }
    }

    fun getSession(streamRoomId: String): RoomJanusSession? =
        sessions[streamRoomId]

    fun getSession(handle: StreamHandle): RoomJanusSession? =
        sessions[sessionHandles[handle.value]]

    fun createHandleToRoom(
        handle: StreamHandle,
        roomId: String
    ) {
        sessionHandles.put(handle.value, roomId)
    }

    fun leaveStreamRoom(streamRoomId: String) {
        sessions.remove(streamRoomId)
    }

    fun getRoomIds(): Set<String> = sessions.keys

    fun closeHandleToRoom(handle: StreamHandle) {
        sessionHandles.remove(handle.value)
    }

    override fun close() {
        sessionHandles.clear()
        pcFactory.dispose()
    }
}