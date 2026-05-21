package webrtc

import TrackObserver
import WebRTCFramework.RTCConfiguration
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@OptIn(ExperimentalForeignApi::class)
actual open class JanusConnection
actual constructor(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: KeyStore,
    trackObserver: TrackObserver?,
    onTrickle: (Long, String) -> Unit,
    onConnectionChange: (IceConnectionState) -> Unit
) : AutoCloseable {
    actual var sessionId: Long
        get() {
            TODO()
        }
        set(value) {}
    actual var pcObserver: Observer
        get() {
            TODO()
        }
        set(value) {}

    actual var peerConnection: PeerConnection
        get() {
            TODO()
        }
        set(value) {}


    actual val connectionState: PeerConnectionState
        get() = TODO("Not yet implemented")
    actual val isEnded: Boolean
        get() = TODO("Not yet implemented")

    actual override fun close() {
    }

    protected actual fun createPeerConnection(pcObserver: Observer): PeerConnection {
        TODO("Not yet implemented")
    }

    actual fun setRTCConfiguration(configuration: List<IceServer>) {
        RTCConfiguration().also {
            it.setIceServers(configuration)
            peerConnection.setConfiguration(it)
        }
    }

    actual fun setFrameCryptorOptions(options: FrameCryptorOptions) {
    }

    protected actual val configurationMutex: Mutex = Mutex()



}