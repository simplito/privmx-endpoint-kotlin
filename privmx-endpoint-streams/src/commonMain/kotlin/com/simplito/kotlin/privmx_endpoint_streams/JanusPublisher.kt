package com.simplito.kotlin.privmx_endpoint_streams

import com.simplito.kotlin.privmx_endpoint.model.stream.DataChannelMessage
import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.AudioTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.AudioTrackInfo
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.DataChannelClosedException
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.DataChannelObserver
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.DataChannelState
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.IceConnectionState
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.KeyStore
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnection
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PeerConnectionFactory
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.PmxFrameCryptorOptions
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.SessionDescription
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.VideoTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.VideoTrackInfo
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.addTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.applyOptions
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.bufferedAmount
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.close
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.createDataChannel
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.createOffer
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.createSenderFrameCryptor
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.dispose
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.disposeCryptor
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.fromCanonicalForm
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.getDataChannelInit
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.registerObserver
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.removeTrack
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.sdp
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.send
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.sessionDescription
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.setLocalDescription
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.setRemoteDescription
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.state
import com.simplito.kotlin.privmx_endpoint_streams.webrtc.trackId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class JanusPublisher(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: KeyStore,
    roomId: String,
    dataChannelCryptoProvider: InternalDataChannelMessageCryptoProvider,
    remoteStreamObserver: RemoteStreamObserver?,
    onTrickle: (Long, String) -> Unit,
    private val acceptOfferOnReconfigure: (Long, SdpWithTypeModel) -> Unit = { _, _ -> },
    onConnectionChange: (IceConnectionState) -> Unit = {}
) : JanusConnection(
    peerConnectionFactory,
    keyStore,
    roomId,
    dataChannelCryptoProvider,
    remoteStreamObserver,
    onTrickle,
    onConnectionChange
) {

    private val audioTracks: MutableMap<String, AudioTrackInfo> = mutableMapOf()
    private val videoTracks: MutableMap<String, VideoTrackInfo> = mutableMapOf()
    private var dataChannel: JanusDataChannel? = null

    fun addAudioTrack(audioTrack: AudioTrack) {
        val sender = peerConnection.addTrack(audioTrack)
        val cryptor = peerConnectionFactory.createSenderFrameCryptor(sender, keyStore)
        audioTracks[audioTrack.trackId] = AudioTrackInfo(audioTrack, sender, cryptor)
    }

    fun addVideoTrack(videoTrack: VideoTrack) {
        val sender = peerConnection.addTrack(videoTrack)
        val cryptor = peerConnectionFactory.createSenderFrameCryptor(sender, keyStore)
        videoTracks[videoTrack.trackId] = VideoTrackInfo(videoTrack, sender, cryptor)
    }

    fun removeAudioTrack(id: String) {
        val info = audioTracks.remove(id) ?: return
        peerConnection.removeTrack(info.sender)
        info.frameCryptor.disposeCryptor()
    }

    fun removeVideoTrack(id: String) {
        val info = videoTracks.remove(id) ?: return
        peerConnection.removeTrack(info.sender)
        info.frameCryptor.disposeCryptor()
    }

    suspend fun createOffer(): String = configurationMutex.withLock {
        val offer = peerConnection.createOffer()
        peerConnection.setLocalDescription(offer)
        offer.sdp
    }

    suspend fun setAnswer(sdp: String?, type: String): SessionDescription =
        configurationMutex.withLock {
            sessionDescription(fromCanonicalForm(type), sdp ?: "").also { peerConnection.setRemoteDescription(it) }
        }

    override fun setFrameCryptorOptions(options: PmxFrameCryptorOptions) {
        audioTracks.values.forEach { it.frameCryptor.applyOptions(options) }
        videoTracks.values.forEach { it.frameCryptor.applyOptions(options) }
    }

    override fun onRenegotiationNeeded() {
        if (sessionId > -1) {
            val offer = runBlocking(EmptyCoroutineContext) { createOffer() }
            acceptOfferOnReconfigure(sessionId, SdpWithTypeModel(offer, "offer"))


        }
    }

    @Throws(CancellationException::class, IllegalStateException::class)
    suspend fun openDataChannel() {
        val lastState = dataChannel?.dataChannelStateFlow?.lastOrNull()
        if ((lastState
                ?: DataChannelState.CLOSED) <= DataChannelState.OPEN
        ) throw IllegalStateException("Data channel is currently open. You can have one open data channel per room.")
        dataChannel = JanusDataChannel(peerConnection, roomId, dataChannelCryptoProvider)
    }

    suspend fun closeDataChannel() {
        dataChannel?.close()
        dataChannel = null
    }

    @Throws(CancellationException::class, DataChannelClosedException::class)
    suspend fun sendMessage(message: ByteArray) {
        if (dataChannel == null) throw IllegalStateException("You have not created data channel. Create it first using openDataChannel method.")
        dataChannel?.sendMessage(message)
    }

    override fun close() {
        runCatching {
            audioTracks.values.forEach { it.frameCryptor.disposeCryptor() }
            videoTracks.values.forEach { it.frameCryptor.disposeCryptor() }
        }
        audioTracks.clear()
        videoTracks.clear()
        super.close()
    }
}

private const val MAX_BUFFERED_AMOUNT = 128 * 1024 * 1024

internal class JanusDataChannel(
    peerConnection: PeerConnection,
    private val roomId: String,
    private val dataChannelCryptoProvider: InternalDataChannelMessageCryptoProvider
) {
    private val dataChannel = peerConnection.createDataChannel("JanusDataChannel", getDataChannelInit(false, true))
    private val observer = object : DataChannelObserver {
        override fun onStateChange() {
            dataChannelStateFlow.tryEmit(dataChannel.state)
        }

        override fun onMessage(message: ByteArray) {
        }

        override fun onBufferedAmountChange(bufferedAmount: Long) {
        }

    }
    private var seq = 0
    val dataChannelStateFlow: Flow<DataChannelState> field = MutableSharedFlow<DataChannelState>().apply {
        tryEmit(
            dataChannel.state
        )
    }

    init {
        dataChannel.registerObserver(observer)
    }

    suspend fun sendMessage(message: ByteArray) {
        waitForDataChannelOpen(30.seconds)
        if (dataChannel.bufferedAmount + message.size > MAX_BUFFERED_AMOUNT) throw RuntimeException("Message size exceeded")
        val encryptedMessage =
            dataChannelCryptoProvider.encryptMessage(roomId, DataChannelMessage(message, seq.toLong()))
        dataChannel.send(encryptedMessage)
        seq++
    }

    suspend fun waitForDataChannelOpen(timeout: Duration? = null) {
        when (dataChannel.state) {
            DataChannelState.OPEN -> return
            DataChannelState.CONNECTING -> {
                if (timeout != null) {
                    withTimeout(timeout) {
                        dataChannelStateFlow.first {
                            dataChannel.state >= DataChannelState.OPEN
                        }
                    }
                } else {
                    dataChannelStateFlow.first {
                        dataChannel.state >= DataChannelState.OPEN
                    }
                }
                if (dataChannel.state != DataChannelState.OPEN) {
                    throw DataChannelClosedException()
                }
            }

            else -> throw DataChannelClosedException()
        }
    }

    fun close() {
        dataChannel.close()
        dataChannel.dispose()
    }
}