package com.simplito.kotlin.privmx_endpoint_streams

import android.content.Context
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.IceServer
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnectionFactory
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnection
import org.webrtc.audio.JavaAudioDeviceModule
import kotlin.Throws

/**
 * Android-specific data required to initialize [StreamApi].
 *
 * @property appContext the application [Context]. Prefer the application context over an
 *                       Activity to avoid leaking the Activity for the lifetime of the stream.
 * @property rootEglBase the root [EglBase] whose EGL context is shared across the WebRTC
 *                       video pipeline (encoding, decoding, and rendering). Pass the same
 *                       instance to any renderers so they share this context.
 */
actual data class StreamApiInit(
    val appContext: Context,
    val rootEglBase: EglBase,
)

private fun createDefaultPeerConnectionFactory(
    appContext: Context,
    eglBase: EglBase,
    options: org.webrtc.PeerConnectionFactory.Options = org.webrtc.PeerConnectionFactory.Options()
): PeerConnectionFactory {
    val adm = JavaAudioDeviceModule.builder(appContext).createAudioDeviceModule()
    val factory = PeerConnectionFactory.builder()
        .setVideoEncoderFactory(
            DefaultVideoEncoderFactory(
                eglBase.eglBaseContext,
                true,
                false
            )
        )
        .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
        .setOptions(options)
        .setAudioDeviceModule(adm)
        .createPeerConnectionFactory()
    adm.release()
    return factory
}

internal actual fun StreamApi.getRTCConfiguration(): List<IceServer> {
    return this.api.getTurnCredentials().map { item ->
        PeerConnection.IceServer.builder(item.url)
            .setUsername(item.username)
            .setPassword(item.password)
            .createIceServer()
    }
}

/**
 * Joins a StreamRoom and prepares the session for WebRTC communication.
 *
 * Required before working with streams and stream events in the room.
 *
 * @param streamRoomId ID of the StreamRoom to join
 *
 * @throws PrivmxException       thrown when method encounters an exception
 * @throws NativeException       thrown when method encounters an unknown exception
 * @throws IllegalStateException thrown when instance is closed
 */
@Throws(
    PrivmxException::class,
    NativeException::class,
    IllegalStateException::class
)
actual fun StreamApi.joinStreamRoom(streamRoomId: String) {
    val session = pcManager.createSession(streamRoomId)
    api.joinStreamRoom(streamRoomId, session.webrtc)
}

actual fun StreamApi.createDefaultPeerConnectionFactory(
    init: StreamApiInit,
): PeerConnectionFactory = createDefaultPeerConnectionFactory(
    init.appContext,
    init.rootEglBase
)

internal actual fun StreamApi.initPeerConnectionFactory(
    init: StreamApiInit
) {
    PeerConnectionFactory.initialize(
        org.webrtc.PeerConnectionFactory.InitializationOptions
            .builder(init.appContext)
            .createInitializationOptions()
    )
}