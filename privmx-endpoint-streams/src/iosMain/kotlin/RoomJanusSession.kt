//package com.simplito.java.privmx_endpoint.modules.stream
//
//import WebRTCFramework.RTCPeerConnectionFactory
//import kotlinx.cinterop.ExperimentalForeignApi
//
//
//@OptIn(ExperimentalForeignApi::class)
//actual class RoomJanusSession(
//    val roomId: String,
//    private val pcFactory: RTCPeerConnectionFactory,
//    private val onTrickle: (Long, String) -> Unit
//) {
//
//    init {
//    }
//    /*
//        var subscriber: JanusSubscriber? = null
//            private set
//
//        var publisher: JanusPublisher? = null
//            private set
//
//        private val keyStore: PMXKeyStore = PMXKeyStore()
//
//        //TODO: Add error logging for webrtcImpl
//        internal val webrtc: WebRTCImpl = WebRTCImpl()
//        private val trackObserversByStreamId: MutableMap<String?, TrackObserver> = mutableMapOf()
//        private val trackObserver: TrackObserver = TrackObserverImpl()
//
//
//        fun createSubscriber() {
//            createSubscriber(trackObserver)
//        }
//
//        //TODO: Mutex
//        fun createSubscriber(observer: TrackObserver?) {
//            if (subscriber == null) {
//                subscriber = JanusSubscriber(pcFactory, keyStore, observer, onTrickle)
//            } else if (subscriber!!.isEnded) {
//                subscriber!!.close()
//                subscriber = JanusSubscriber(pcFactory, keyStore, observer, onTrickle)
//            } else {
//                throw IllegalStateException("Subscriber is currently active.")
//            }
//        }
//
//        fun createPublisher() {
//            createPublisher(null)
//        }
//
//        //TODO: Mutex
//        fun createPublisher(observer: TrackObserver?) {
//            if (publisher == null) {
//                publisher = JanusPublisher(pcFactory, keyStore, observer, onTrickle)
//            } else if (publisher!!.isEnded) {
//                publisher!!.close()
//                publisher = JanusPublisher(pcFactory, keyStore, observer, onTrickle)
//            } else {
//                throw IllegalStateException("Publisher is currently active.")
//            }
//        }
//
//        fun setTrackObserver(
//            trackObserver: TrackObserver
//        ) {
//            setTrackObserver(null, trackObserver)
//        }
//
//        fun setTrackObserver(
//            streamId: String?,
//            trackObserver: TrackObserver
//        ) {
//            //TODO: Mutex
//            trackObserversByStreamId[streamId] = trackObserver
//        }
//
//        //TODO: Find PmxFrameCryptorOptions
//    //    fun setFrameCryptorOptions(options: PmxFrameCryptor.PmxFrameCryptorOptions?) {
//    //        if (subscriber != null) {
//    //            subscriber.setFrameCryptorOptions(options)
//    //        }
//    //        if (publisher != null) {
//    //            publisher.setFrameCryptorOptions(options)
//    //        }
//    //    }
//
//        internal inner class WebRTCImpl constructor() : WebRtcInterface {
//            private val keysDispatcher = Dispatchers.IO.limitedParallelism(1)
//            private val coroutineScope = CoroutineScope(Dispatchers.IO)
//
//            override fun createOfferAndSetLocalDescription(streamRoomId: String): String {
//                try {
//                    if (publisher == null) throw RuntimeException("Create publisher first")
//                    return runBlocking(Dispatchers.IO) {
//                        publisher!!.createOffer()
//                    }
//                } catch (ignored: Exception) {
//                    return ""
//                }
//            }
//
//            override fun createAnswerAndSetDescriptions(
//                streamRoomId: String,
//                sdp: String,
//                type: String
//            ): String {
//                try {
//                    if (subscriber == null) throw RuntimeException("Create subscriber first")
//                    return runBlocking(Dispatchers.IO) {
//                        subscriber!!.createAnswer(sdp)
//                    }
//                } catch (ignored: Exception) {
//                    return ""
//                }
//            }
//
//            override fun setAnswerAndSetRemoteDescription(
//                streamRoomId: String,
//                sdp: String,
//                type: String
//            ) {
//                try {
//                    if (publisher == null) throw RuntimeException("Create publisher first")
//                    return runBlocking(Dispatchers.IO) {
//                        publisher!!.setAnswer(sdp)
//                    }
//                } catch (ignored: Exception) {
//                }
//            }
//
//            override fun updateSessionId(
//                streamRoomId: String,
//                sessionId: Long,
//                connectionType: String
//            ) {
//                when (connectionType) {
//                    "subscriber" -> subscriber?.updateSessionId(sessionId)
//                    "publisher" -> publisher?.updateSessionId(sessionId)
//                }
//            }
//
//            override fun close(streamRoomId: String) {
//                //TODO: Clean all objects correctly
//                publisher?.close()
//                subscriber?.close()
//            }
//
//            @OptIn(BetaInteropApi::class)
//            override fun updateKeys(
//                streamRoomId: String,
//                keys: List<Key>
//            ) {
//                val list: List<PMXKSKey> = keys.map { key ->
//                    PMXKSKey(
//                        key.keyId,
//                        key.key.usePinned {
//                            NSData.dataWithBytes(it.addressOf(0), it.get().size.toULong())
//                        },
//                        if (key.type === KeyType.LOCAL) PMXKSKeyTypeLOCAL else PMXKSKeyTypeREMOTE
//                    )
//                }
//                coroutineScope.launch(keysDispatcher) {
//                    keyStore.webrtc.setKeys(list)
//                }
//            }
//        }
//
//        private inner class TrackObserverImpl : TrackObserver {
//            override fun onRemoteTrack(streamId: String?, track: RTCMediaStreamTrack) {
//                //TODO: Mutex
//                streamId?.let { streamId ->
//                    trackObserversByStreamId[streamId]?.onRemoteTrack(streamId, track)
//                }
//                trackObserversByStreamId[null]?.onRemoteTrack(streamId, track)
//            }
//        }
//        */
//}