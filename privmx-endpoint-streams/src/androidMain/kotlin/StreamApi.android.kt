import android.content.Context
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnection
import org.webrtc.PmxFrameCryptor
import org.webrtc.audio.JavaAudioDeviceModule

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

actual fun StreamApi.initialFun() {
    val factory = createDefaultPeerConnectionFactory(this.apiInit.appContext, this.apiInit.rootEglBase)
    this.pcManager = PeerConnectionManager(
        factory,
        onTrickle = { sessionId, rtcConfiguration ->
            this.api.trickle(sessionId, rtcConfiguration)
        },
        acceptOfferOnReconfigure = { sessionId, sdp ->
            this.api.acceptOfferOnReconfigure(sessionId, sdp)
        }
    )
}

internal actual fun StreamApi.getRTCConfiguration(): List<IceServer> {
    return this.api.getTurnCredentials().map { item ->
        PeerConnection.IceServer.builder(item.url)
            .setUsername(item.username)
            .setPassword(item.password)
            .createIceServer()
    }
}

actual fun StreamApi.joinStreamRoom(streamRoomId: String) {
    val session = pcManager.createSession(streamRoomId)
    api.joinStreamRoom(streamRoomId, session.webrtc)
}