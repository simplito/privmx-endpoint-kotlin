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
