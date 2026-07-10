package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnectionFactory
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.disposeFactory

internal class PeerConnectionManager(
    val pcFactory: PeerConnectionFactory,
    private val onTrickle: (Long, String) -> Unit,
    private val acceptOfferOnReconfigure: (Long, SdpWithTypeModel) -> Unit = { _, _ -> }
) : AutoCloseable {
    // roomId -> session
    private val sessions = mutableMapOf<String, RoomJanusSession>()
    // handle.value -> roomId
    private val sessionHandles = mutableMapOf<Long, String>()

    fun createSession(streamRoomId: String): RoomJanusSession =
        sessions.getOrPut(streamRoomId) {
            RoomJanusSession(streamRoomId, pcFactory, onTrickle, acceptOfferOnReconfigure)
        }

    fun getSession(streamRoomId: String): RoomJanusSession? = sessions[streamRoomId]

    fun getSession(handle: StreamHandle): RoomJanusSession? =
        sessionHandles[handle.value]?.let(sessions::get)

    fun createHandleToRoom(handle: StreamHandle, roomId: String) {
        sessionHandles[handle.value!!] = roomId
    }

    fun leaveStreamRoom(streamRoomId: String) {
        val session = sessions.remove(streamRoomId) ?: return
        session.subscriber?.close()
        session.publisher?.close()
    }

    fun getRoomIds(): Set<String> = sessions.keys

    fun closeHandleToRoom(handle: StreamHandle) {
        sessionHandles.remove(handle.value)
    }

    override fun close() {
        sessionHandles.clear()
        sessions.clear()
        pcFactory.disposeFactory()
    }
}