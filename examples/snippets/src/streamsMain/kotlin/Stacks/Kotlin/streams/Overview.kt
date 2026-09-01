package Stacks.Kotlin.streams

import Stacks.Kotlin.endpointSession
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamRoom
import com.simplito.kotlin.privmx_endpoint.modules.stream.StreamApiLow
import com.simplito.kotlin.privmx_endpoint_streams.StreamApi
import com.simplito.kotlin.privmx_endpoint_streams.StreamApiInit
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.IceConnectionState
import kotlinx.serialization.Serializable

lateinit var streamApiLow: StreamApiLow
lateinit var streamApi: StreamApi

@Serializable
data class StreamRoomPublicMeta(
    val title: String,
    val type: String,
    val scheduledAt: String
)

data class StreamRoomItem(
    val streamRoom: StreamRoom,
    val decodedPrivateMeta: String,
    val decodedPublicMeta: StreamRoomPublicMeta
)

expect fun createStreamApiInit(): StreamApiInit

fun initStreamApiLow() {
    streamApiLow = endpointSession.initializeStreamApiLow()
    val init = createStreamApiInit()

    streamApi = StreamApi(
        streamApiLow,
        init
    )
}

fun observeConnectionState() {
    val streamRoomId = "STREAM_ROOM_ID"

    streamApi.setConnectionStateObserver(streamRoomId) { state ->
        when (state) {
            IceConnectionState.CONNECTED -> {
                // handle successful connection
            }

            IceConnectionState.DISCONNECTED -> {
                // handle disconnection
            }

            IceConnectionState.FAILED -> {
                // handle connection failure
            }

            else -> {
                // handle the remaining WebRTC states
            }
        }
    }
}
