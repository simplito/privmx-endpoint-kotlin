package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import com.simplito.kotlin.privmx_endpoint.model.stream.SubscriberStreamHandle
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnectionFactory
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.disposeFactory

internal class PeerConnectionManager(
    val pcFactory: PeerConnectionFactory,
    private val onTrickle: (Long, String) -> Unit,
    private val setNewOfferOnReconfigure: (Long, SdpWithTypeModel) -> Unit = { _, _ -> }
) : AutoCloseable {
    // roomId -> session
    private val sessions = mutableMapOf<String, RoomJanusSession>()
    // handle.value -> roomId
    private val sessionHandles = mutableMapOf<Long, String>()
    private val sessionSubscriberHandles = mutableMapOf<Long, String>()

    fun createSession(streamRoomId: String): RoomJanusSession =
        sessions.getOrPut(streamRoomId) {
            RoomJanusSession(streamRoomId, pcFactory, onTrickle, setNewOfferOnReconfigure)
        }

    fun getSession(streamRoomId: String): RoomJanusSession? = sessions[streamRoomId]

    fun getSession(handle: StreamHandle): RoomJanusSession? =
        sessionHandles[handle.value]?.let(sessions::get)

    fun getSession(handle: SubscriberStreamHandle): RoomJanusSession? =
        sessionSubscriberHandles[handle.value]?.let(sessions::get)

    fun createHandleToRoom(handle: StreamHandle, roomId: String) {
        sessionHandles[handle.value] = roomId
    }

    fun createHandleToRoom(handle: SubscriberStreamHandle, roomId: String) {
        sessionSubscriberHandles[handle.value] = roomId
    }

    fun leaveStreamRoom(streamRoomId: String) {
        val session = sessions.remove(streamRoomId) ?: return

        sessionSubscriberHandles.entries.removeAll { it.value == streamRoomId }
        sessionHandles.entries.removeAll { it.value == streamRoomId }

        session.subscriber?.close()
        session.publisher?.close()
    }

    fun getRoomIds(): Set<String> = sessions.keys

    fun closeHandleToRoom(handle: StreamHandle) {
        sessionHandles.remove(handle.value)
    }

    fun closeHandleToRoom(handle: SubscriberStreamHandle) {
        sessionSubscriberHandles.remove(handle.value)
    }

    override fun close() {
        sessionHandles.clear()
        sessions.clear()
        pcFactory.disposeFactory()
    }
}