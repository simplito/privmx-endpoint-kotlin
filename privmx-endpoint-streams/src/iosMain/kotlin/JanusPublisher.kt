package com.simplito.java.privmx_endpoint.modules.stream

import AudioTrackInfo
import JanusConnection
import TrackObserver
import VideoTrackInfo
import WebRTCFramework.PMXFrameCryptorTransformer
import WebRTCFramework.PMXKeyStore
import WebRTCFramework.RTCAudioTrack
import WebRTCFramework.RTCMediaConstraints
import WebRTCFramework.RTCPeerConnectionFactory
import WebRTCFramework.RTCRtpTransceiverDirection
import WebRTCFramework.RTCRtpTransceiverInit
import WebRTCFramework.RTCSdpType
import WebRTCFramework.RTCSessionDescription
import WebRTCFramework.RTCVideoCapturer
import WebRTCFramework.RTCVideoTrack
import kotlinx.atomicfu.locks.synchronized
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalForeignApi::class)
class JanusPublisher(
    pcFactory: RTCPeerConnectionFactory,
    keyStore: PMXKeyStore,
    observer: TrackObserver?,
    onTrickle: (Long, String) -> Unit
) : JanusConnection(pcFactory, keyStore, observer, onTrickle) {
    val audioTracks: MutableMap<String, AudioTrackInfo> = mutableMapOf()
    val videoTracks: MutableMap<String, VideoTrackInfo> = mutableMapOf()

    //TODO: Add videoCapturer to VideoTrackInfo
    val videoCapturers: MutableMap<String, RTCVideoCapturer> = mutableMapOf()


    fun addAudioTrack(audioTrack: RTCAudioTrack) {
        val transceiver = peerConnection.addTransceiverWithTrack(
            audioTrack,
            RTCRtpTransceiverInit().apply {
                direction = RTCRtpTransceiverDirection.RTCRtpTransceiverDirectionSendOnly
            }
        )!!
        val frameCryptor = PMXFrameCryptorTransformer(
            transceiver.sender,
            peerConnectionFactory,
            keyStore // options ?
        )
////        //TODO: add mutex
        audioTracks[audioTrack.trackId] = AudioTrackInfo(
            audioTrack,
            transceiver.sender,
            frameCryptor
        )
    }

    fun addVideoTrack(
        videoTrack: RTCVideoTrack,
        videoCapturer: RTCVideoCapturer? = null
    ) {
        //TODO: add mutex
        val transceiver = peerConnection.addTransceiverWithTrack(videoTrack)!!
        val frameCryptor = PMXFrameCryptorTransformer(
            transceiver.sender,
            peerConnectionFactory,
            keyStore // options ?
        )

        videoTracks[videoTrack.trackId] = VideoTrackInfo(
            videoTrack,
            transceiver.sender,
            frameCryptor
        )
        if (videoCapturer != null) {
            videoCapturers[videoTrack.trackId] = videoCapturer
        }
    }

    fun removeAudioTrack(id: String) {
        //TODO: Add Mutex
//        synchronized(audioTracks) {
        val audioTrackInfo: AudioTrackInfo = audioTracks[id] ?: return
        peerConnection.removeTrack(audioTrackInfo.sender)
        audioTracks.remove(id)
//        }
    }

    fun removeVideoTrack(id: String) {
        //TODO: Add Mutex
        val videoTrackInfo: VideoTrackInfo = videoTracks[id] ?: return
        peerConnection.removeTrack(videoTrackInfo.sender)
        videoTracks.remove(id)
        videoCapturers.remove(id)
    }

    suspend fun createOffer(): String {
        return suspendCancellableCoroutine { continuation ->
            peerConnection.offerForConstraints(RTCMediaConstraints()){ sdp, error ->
                if(error == null) continuation.resume(sdp!!)
                else continuation.resumeWithException(RuntimeException(error.description))
            }
        }.run {
            suspendCancellableCoroutine { continuation ->
                peerConnection.setLocalDescription(this) {
                    if(it != null) continuation.resumeWithException(RuntimeException(it.description))
                    else continuation.resume(Unit)
                }
            }
            description!!
        }
    }

    fun setAnswer(sdp: String) {
        peerConnection.setRemoteDescription(
            RTCSessionDescription(RTCSdpType.RTCSdpTypeAnswer,sdp)
        ){}
    }

    //TODO: add mutex
    fun getVideoCapturer(trackId: String?): RTCVideoCapturer? {
        return videoCapturers[trackId]
    }

    override fun close() {
        super.close()
        audioTracks.clear()
        videoTracks.clear()
        videoCapturers.clear()
    }


//    fun setFrameCryptorOptions(options: PMXFrameCryptorTransformer) {
//        this.videoTracks.values().forEach({ it ->
//            it.frameCryptor.setOptions(options)
//        })
//        this.audioTracks.values().forEach({ it ->
//            it.frameCryptor.setOptions(options)
//        })
//    }
}
