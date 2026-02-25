import WebRTCFramework.RTCPeerConnectionFactory
import com.simplito.java.privmx_endpoint.modules.stream.RoomJanusSession
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
internal class PeerConnectionManager(
    private val pcFactory: RTCPeerConnectionFactory,
    private val onTrickle: (Long, String) -> Unit
) {

    // Map: roomId -> session
    private val sessions: MutableMap<String, RoomJanusSession> = mutableMapOf()

    // Map: handle value -> roomId
    private val sessionHandles: MutableMap<Long, String> = mutableMapOf()

    /**
     * Create or return existing session for given room id.
     */
    fun createSession(streamRoomId: String): RoomJanusSession {
        return sessions.getOrPut(streamRoomId) {
            RoomJanusSession(streamRoomId, pcFactory, onTrickle)
        }
    }

    /**
     * Get session by room id, or null if not found.
     */
    fun getSession(streamRoomId: String): RoomJanusSession? {
        return sessions[streamRoomId]
    }

    /**
     * Get session by StreamHandle, or null if not found.
     */
    fun getSession(handle: StreamHandle): RoomJanusSession? {
        val streamRoomId = sessionHandles[handle] ?: return null
        return sessions[streamRoomId]
    }

    /**
     * Associate a handle with a room id.
     */
    fun createHandleToRoom(
        handle: StreamHandle,
        roomID: String
    ) {
        sessionHandles[handle] = roomID
    }

    /**
     * Leave room: remove session and dispose its peer connections.
     */
    fun leaveStreamRoom(streamRoomId: String) {
        val session: RoomJanusSession = sessions.remove(streamRoomId) ?: return
        session.subscriber?.close()
        session.publisher?.close()
    }
}