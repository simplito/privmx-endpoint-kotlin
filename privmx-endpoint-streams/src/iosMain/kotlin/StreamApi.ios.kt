package com.simplito.kotlin.privmx_endpoint.modules.stream

import PeerConnectionManager
import TrackObserver
import WebRTCFramework.RTCAudioTrack
import WebRTCFramework.RTCDefaultVideoDecoderFactory
import WebRTCFramework.RTCDefaultVideoEncoderFactory
import WebRTCFramework.RTCMediaStreamTrack
import WebRTCFramework.RTCPeerConnectionFactory
import WebRTCFramework.RTCPeerConnectionFactoryOptions
import WebRTCFramework.RTCRtpMediaType
import WebRTCFramework.RTCVideoTrack
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.java.privmx_endpoint.model.events.eventSelectorTypes.StreamEventSelectorType
import com.simplito.java.privmx_endpoint.model.events.eventTypes.StreamEventType
import com.simplito.java.privmx_endpoint.modules.stream.JanusPublisher
import com.simplito.java.privmx_endpoint.modules.stream.RoomJanusSession
import com.simplito.kotlin.privmx_endpoint.model.stream.Settings
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamInfo
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamPublishResult
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamRoom
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamSubscription
import kotlinx.cinterop.ExperimentalForeignApi


//TODO: Good to remove context from StreamApi
@OptIn(ExperimentalForeignApi::class)
class StreamApi(
    private val api: StreamApiLow,
) {
    private val pcManager: PeerConnectionManager
//    val trackFactory: TrackFactory

    init {
        pcManager = PeerConnectionManager(
            DefaultPeerConnectionFactory(RTCPeerConnectionFactoryOptions())
        ) { sessionId, rtcConfiguration ->
            this.api.trickle(sessionId, rtcConfiguration)
        }
//        trackFactory = TrackFactory(pcManager)
    }

    fun createStreamRoom(
        contextId: String,
        users: MutableList<UserWithPubKey>,
        managers: MutableList<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        policies: ContainerPolicy
    ): String {
        return api.createStreamRoom(contextId, users, managers, publicMeta, privateMeta, policies)
    }

    fun updateStreamRoom(
        streamRoomId: String,
        users: MutableList<UserWithPubKey>,
        managers: MutableList<UserWithPubKey>,
        publicMeta: ByteArray,
        privateMeta: ByteArray,
        version: Long,
        force: Boolean,
        forceGenerateNewKey: Boolean,
        policies: ContainerPolicy?
    ) {
        api.updateStreamRoom(
            streamRoomId,
            users,
            managers,
            publicMeta,
            privateMeta,
            version,
            force,
            forceGenerateNewKey,
            policies
        )
    }

    fun listStreamRooms(
        contextId: String,
        skip: Long,
        limit: Long,
        sortOrder: String,
        lastId: String?,
        sortBy: String?
    ): PagingList<StreamRoom> {
        return api.listStreamRooms(contextId, skip, limit, sortOrder, lastId, sortBy)
    }

    fun getStreamRoom(streamRoomId: String): StreamRoom {
        return api.getStreamRoom(streamRoomId)
    }

    fun deleteStreamRoom(streamRoomId: String) {
        api.deleteStreamRoom(streamRoomId)
    }

    fun listStreams(streamRoomId: String): List<StreamInfo> {
        return api.listStreams(streamRoomId)
    }

    fun joinStreamRoom(
        streamRoomId: String
    ) {
        //TODO: Rollback this change, it is do only for run test
        val session: RoomJanusSession = pcManager.createSession(streamRoomId)
        api.joinStreamRoom(streamRoomId, session.webrtc)
    }

    fun leaveStreamRoom(streamRoomId: String) {
        pcManager.leaveStreamRoom(streamRoomId)
        api.leaveStreamRoom(streamRoomId)
    }

    fun createStream(streamRoomId: String): StreamHandle {
        val session: RoomJanusSession? = pcManager.getSession(streamRoomId)
        checkNotNull(session) { "Session to this room is not exists. Call joinStreamRoom first" }
        try {
            session.createPublisher()
        } catch (e: IllegalStateException) {
            throw IllegalStateException("Publisher is now active, try use modifyRemoteStreamsSubscriptions")
        }

        val handle: StreamHandle = api.createStream(streamRoomId)
        pcManager.createHandleToRoom(handle, streamRoomId)
        return handle
    }

    /**
     * @param streamHandle
     * @param track
     * @throws IllegalStateException if call addTrack before call createStream
     */
    @Throws(IllegalStateException::class)
    fun addTrack(
        streamHandle: StreamHandle,
        track: RTCMediaStreamTrack
    ) {
        val session: RoomJanusSession? = pcManager.getSession(streamHandle)
        checkNotNull(session) { "Stream not exists. Create stream first." }
        val publisher: JanusPublisher? = session.publisher
        checkNotNull(publisher) { "This StreamHandle has not created companion publisher." }
        when (track.kind()) {
            RTCRtpMediaType.RTCRtpMediaTypeVideo.name -> {
                publisher.addVideoTrack(track as RTCVideoTrack)
            }

            RTCRtpMediaType.RTCRtpMediaTypeAudio.name -> {
                publisher.addAudioTrack(track as RTCAudioTrack)
            }
        }
    }

    fun setTrackObserver(
        roomId: String,
        observer: TrackObserver,
        streamId: String? = null
    ) {
        val session: RoomJanusSession? = pcManager.getSession(roomId)
        checkNotNull(session) { "Session to this room is not exists. Call joinStreamRoom first." }
        session.setTrackObserver(streamId, observer)
    }

    /**
     * @param streamHandle
     * @param track
     * @throws IllegalStateException when Stream with this StreamHandle doesn't exist.
     */
    @Throws(IllegalStateException::class)
    fun removeTrack(
        streamHandle: StreamHandle,
        track: RTCMediaStreamTrack
    ) {
        val session: RoomJanusSession? = pcManager.getSession(streamHandle)
        checkNotNull(session) { "Stream with this StreamHandle doesn't exist." }
        val publisher: JanusPublisher? = session.publisher
        checkNotNull(publisher) { "This StreamHandle has not created companion publisher." }
        if (track is RTCAudioTrack) {
            publisher.removeAudioTrack(track.trackId)
        } else if (track is RTCVideoTrack) {
            publisher.removeVideoTrack(track.trackId)
        }
    }

    fun publishStream(streamHandle: StreamHandle): StreamPublishResult {
        return api.publishStream(streamHandle)
    }

    fun updateStream(streamHandle: StreamHandle): StreamPublishResult {
        return api.updateStream(streamHandle)
    }

    fun unpublishStream(streamHandle: StreamHandle) {
        api.unpublishStream(streamHandle)
    }

    fun subscribeToRemoteStreams(
        streamRoomId: String,
        subscriptions: MutableList<StreamSubscription>
    ) {
        subscribeToRemoteStreams(streamRoomId, subscriptions, Settings())
    }

    @Throws(IllegalStateException::class)
    fun subscribeToRemoteStreams(
        streamRoomId: String,
        subscriptions: MutableList<StreamSubscription>,
        options: Settings
    ) {
        val session: RoomJanusSession? = pcManager.getSession(streamRoomId)
        checkNotNull(session) { "No active session to this Stream Room. Join stream room first" }
        try {
            session.createSubscriber()
        } catch (e: IllegalStateException) {
            throw IllegalStateException("Subscriber is now active, try use modifyRemoteStreamsSubscriptions")
        }
        api.subscribeToRemoteStreams(streamRoomId, subscriptions, options)
    }

    fun modifyRemoteStreamsSubscriptions(
        streamRoomId: String,
        subscriptionsToAdd: MutableList<StreamSubscription>,
        subscriptionsToRemove: MutableList<StreamSubscription>
    ) {
        modifyRemoteStreamsSubscriptions(
            streamRoomId,
            subscriptionsToAdd,
            subscriptionsToRemove,
            Settings()
        )
    }

    fun modifyRemoteStreamsSubscriptions(
        streamRoomId: String,
        subscriptionsToAdd: List<StreamSubscription>,
        subscriptionsToRemove: List<StreamSubscription>,
        options: Settings
    ) {
        api.modifyRemoteStreamsSubscriptions(
            streamRoomId,
            subscriptionsToAdd,
            subscriptionsToRemove,
            options
        )
    }

    fun unsubscribeFromRemoteStreams(
        streamRoomId: String,
        subscriptionsToRemove: List<StreamSubscription>
    ) {
        api.unsubscribeFromRemoteStreams(
            streamRoomId,
            subscriptionsToRemove
        )
    }

    fun dropBrokenFrames(
        streamRoomId: String?,
        enable: Boolean
    ) {
        //TODO: Implement setting options
//        val session: RoomJanusSession? = pcManager.getSession(streamRoomId)
//        if (session != null) {
//            val options: PmxFrameCryptor.PmxFrameCryptorOptions = PmxFrameCryptorOptions()
//            options.dropFrameIfCryptionFailed = enable
//            session.setFrameCryptorOptions(options)
//        }
    }

    fun subscribeFor(subscriptionQueries: List<String>): List<String> {
        return api.subscribeFor(subscriptionQueries)
    }

    fun unsubscribeFrom(subscriptionIds: List<String>) {
        api.unsubscribeFrom(subscriptionIds)
    }

    fun buildSubscriptionQuery(
        eventType: StreamEventType,
        selectorType: StreamEventSelectorType,
        selectorId: String
    ): String {
        return api.buildSubscriptionQuery(
            eventType,
            selectorType,
            selectorId
        )
    }
}

private const val VIDEO_TRACK_ID: String = "ARDAMSv0"
private const val AUDIO_TRACK_ID: String = "ARDAMSa0"
private const val VIDEO_TRACK_TYPE: String = "video"
private const val TAG = "StreamApi"

@OptIn(ExperimentalForeignApi::class)
private fun DefaultPeerConnectionFactory(
    options: RTCPeerConnectionFactoryOptions
): RTCPeerConnectionFactory = RTCPeerConnectionFactory(
    RTCDefaultVideoEncoderFactory(),
    RTCDefaultVideoDecoderFactory()
)