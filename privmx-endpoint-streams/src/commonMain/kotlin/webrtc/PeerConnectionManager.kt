package webrtc

import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle

internal expect class PeerConnectionManager {
    fun createSession(streamRoomId: String): RoomJanusSession
    fun getSession(streamRoomId: String): RoomJanusSession?
    fun getSession(handle: StreamHandle): RoomJanusSession?
    fun createHandleToRoom(handle: StreamHandle, roomId: String)
    fun leaveStreamRoom(streamRoomId: String)
    fun getRoomIds(): Set<String>
    fun closeHandleToRoom(handle: StreamHandle)
    fun close()
}