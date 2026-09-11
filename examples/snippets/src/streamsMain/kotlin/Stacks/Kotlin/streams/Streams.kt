package Stacks.Kotlin.streams

import com.simplito.kotlin.privmx_endpoint.model.stream.StreamHandle
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamInfo
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamPublishResult
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamSubscriber
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamSubscription
import com.simplito.kotlin.privmx_endpoint.model.stream.SubscriberStreamHandle
import com.simplito.kotlin.privmx_endpoint_streams.RemoteStreamObserver
import com.simplito.kotlin.privmx_endpoint_streams.joinStreamRoom
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.AudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.MediaStreamTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.VideoTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.kind

fun joinStreamRoom() {
    val streamRoomId = "STREAM_ROOM_ID"

    streamApi.joinStreamRoom(streamRoomId)
}

fun leaveStreamRoom() {
    val streamRoomId = "STREAM_ROOM_ID"

    streamApi.leaveStreamRoom(streamRoomId)
}

fun createStream(): StreamHandle {
    val streamRoomId = "STREAM_ROOM_ID"

    val streamHandle: StreamHandle = streamApi.createStream(streamRoomId)
    return streamHandle
}

fun addFeedsToStream(streamHandle: StreamHandle, videoTrack: VideoTrack, audioTrack: AudioTrack) {
    streamApi.addTrack(streamHandle, videoTrack)
    streamApi.addTrack(streamHandle, audioTrack)

    // If you created a data channel, it's already attached to this handle.
}

fun publishStream(streamHandle: StreamHandle) {
    val streamPublishResult: StreamPublishResult = streamApi.publishStream(streamHandle)
}

fun updateStream(streamHandle: StreamHandle) {
    val streamPublishResult: StreamPublishResult = streamApi.updateStream(streamHandle)
}

fun removeStream(streamHandle: StreamHandle) {
    streamApi.removeStream(streamHandle)
}

expect fun createVideoTrack()
expect fun createAudioTrack()

fun addTracks(streamHandle: StreamHandle, videoTrack: VideoTrack, audioTrack: AudioTrack) {
    streamApi.addTrack(streamHandle, videoTrack)
    streamApi.addTrack(streamHandle, audioTrack)
}

fun removeTracks(streamHandle: StreamHandle, videoTrack: VideoTrack, audioTrack: AudioTrack) {
    streamApi.removeTrack(streamHandle, audioTrack)
    streamApi.removeTrack(streamHandle, videoTrack)
}

suspend fun createDataChannel(streamHandle: StreamHandle) {
    streamApi.createDataChannel(streamHandle)
}

suspend fun sendMessage(streamHandle: StreamHandle) {
    val message = "Hello".encodeToByteArray()
    streamApi.sendMessage(streamHandle, message)
}

fun observeAllRemoteStreams() {
    val streamRoomId = "STREAM_ROOM_ID"

    streamApi.setRemoteStreamObserver(
        streamRoomId,
        object : RemoteStreamObserver {
            override fun onTrack(streamId: String?, track: MediaStreamTrack) {
                when (track.kind) {
                    "video" -> {
                        // handle the remote video track
                    }

                    "audio" -> {
                        // handle the remote audio track
                    }
                }
            }

            override fun onMessage(streamId: String, message: ByteArray) {
                // handle the data channel message
            }
        }
    )
}

fun observeSpecificRemoteStream() {
    val streamRoomId = "STREAM_ROOM_ID"
    val observedStreamId = "STREAM_ID"

    streamApi.setRemoteStreamObserver(
        streamRoomId,
        object : RemoteStreamObserver {
            override fun onTrack(streamId: String?, track: MediaStreamTrack) {
                when (track.kind) {
                    "video" -> {
                        // handle the remote video track
                    }

                    "audio" -> {
                        // handle the remote audio track
                    }
                }
            }

            override fun onMessage(streamId: String, message: ByteArray) {
                // handle the data channel message
            }
        },
        observedStreamId
    )
}


fun listStreams() {
    val streamRoomId = "STREAM_ROOM_ID"

    val streams: List<StreamInfo> = streamApi.listStreams(streamRoomId)
}

fun listStreamRoomParticipants() {
    val streamRoomId = "STREAM_ROOM_ID"

    val participants: List<StreamSubscriber> =
        streamApi.listStreamRoomParticipants(streamRoomId)
}


fun subscribeToAllRemoteStreams(): SubscriberStreamHandle {
    val streamRoomId = "STREAM_ROOM_ID"

    val subscriptions: List<StreamSubscription> = streamApi.listStreams(streamRoomId)
        .flatMap { stream ->
            stream.tracks
                .map { track ->
                    StreamSubscription(stream.id, track.mid)
                }
        }

    val subscriberHandle: SubscriberStreamHandle =
        streamApi.createSubscriberStream(streamRoomId, subscriptions)

    return subscriberHandle
}

fun subscribeToUserRemoteStreams(): SubscriberStreamHandle {
    val streamRoomId = "STREAM_ROOM_ID"
    val userId = "USER_ID"

    val subscriptions: List<StreamSubscription> = streamApi.listStreams(streamRoomId)
        .filter { it.userId == userId }
        .map { stream ->
            StreamSubscription(stream.id, null)
        }

    val subscriberHandle: SubscriberStreamHandle =
        streamApi.createSubscriberStream(streamRoomId, subscriptions)

    return subscriberHandle
}

fun subscribeForMessages(): SubscriberStreamHandle {
    val streamRoomId = "STREAM_ROOM_ID"

    val subscriptions: List<StreamSubscription> = streamApi.listStreams(streamRoomId)
        .mapNotNull { stream ->
            stream.tracks
                .firstOrNull { it.type == "data" }
                ?.let { dataTrack -> StreamSubscription(stream.id, dataTrack.mid) }
        }

    val subscriberHandle: SubscriberStreamHandle =
        streamApi.createSubscriberStream(streamRoomId, subscriptions)

    return subscriberHandle
}

fun updatingSubscriberStream(
    streamRoomId: String,
    subscriberHandle: SubscriberStreamHandle,
    previousSubscriptions: List<StreamSubscription>
) {
    // Fetch the latest available streams and map them to subscriptions
    val currentSubscriptions: List<StreamSubscription> =
        streamApi.listStreams(streamRoomId).flatMap { stream ->
            stream.tracks.map { track -> StreamSubscription(stream.id, track.mid) }
        }

    // Calculate differences between previous and current subscriptions
    // new tracks that were not subscribed before
    val subscriptionsToAdd = currentSubscriptions - previousSubscriptions

    // tracks that are no longer available
    val subscriptionsToRemove = previousSubscriptions - currentSubscriptions

    streamApi.updateSubscriberStream(
        subscriberHandle,
        subscriptionsToAdd,
        subscriptionsToRemove,
    )
}

fun removeSubscriberStream(subscriberHandle: SubscriberStreamHandle) {
    streamApi.removeSubscriberStream(subscriberHandle)
}
