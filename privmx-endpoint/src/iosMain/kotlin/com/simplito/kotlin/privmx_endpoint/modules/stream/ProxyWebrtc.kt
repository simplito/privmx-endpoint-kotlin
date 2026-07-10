package com.simplito.kotlin.privmx_endpoint.modules.stream

import com.simplito.kotlin.privmx_endpoint.model.stream.Key
import com.simplito.kotlin.privmx_endpoint.model.stream.KeyType
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.cValue
import kotlinx.cinterop.cstr
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_stream_KeyType
import platform.posix.memcpy
import kotlin.getValue

@OptIn(ExperimentalForeignApi::class)
private fun privmx_endpoint_stream_KeyType.KType(): KeyType = KeyType.entries[ordinal]

@OptIn(ExperimentalForeignApi::class)
private val COpaquePointer?.webrtc: StableRef<WebRTCInterface>
    get() = memScoped {
        this@webrtc!!.asStableRef<WebRTCInterface>()
    }

@OptIn(ExperimentalForeignApi::class)
private fun WebRTCInterface.toCWebRtcPointer(): CValue<libprivmxendpoint.privmx_endpoint_stream_WebRTCInterface> =
//    memScoped {
    cValue {
        ctx = StableRef.create(this@toCWebRtcPointer).asCPointer()
        closeCallback = staticCFunction { ctx, streamRoomId ->
            val webRtcInterfaceRef = ctx.webrtc
            val webRtcInterface = webRtcInterfaceRef.get()
            webRtcInterface.close(streamRoomId!!.toKStringFromUtf8())
            webRtcInterfaceRef.dispose()
        }
        createOfferAndSetLocalDescriptionCallback = staticCFunction { ctx, streamRoomId ->
            val webRtcInterface = ctx.webrtc.get()
            return@staticCFunction try {
                webRtcInterface.createOfferAndSetLocalDescription(
                    streamRoomId!!.toKStringFromUtf8()
                ).encodeToByteArray().usePinned { pinned ->
                    //TODO: This memory should be clean, but waiting for refactor in cinterface endpoint
                    nativeHeap.allocArray<ByteVar>(pinned.get().size).apply {
                        memcpy(this, pinned.addressOf(0), pinned.get().size.toULong())
                    }
                }
            }catch (e: Throwable){
                e.printStackTrace()
                memScoped { "".cstr.ptr }
            }
        }
        createAnswerAndSetDescriptionsCallback = staticCFunction { ctx, streamRoomId, sdp, type ->
            val webRtcInterface = ctx.webrtc.get()
            return@staticCFunction try {
                webRtcInterface.createAnswerAndSetDescriptions(
                    streamRoomId!!.toKStringFromUtf8(),
                    sdp!!.toKStringFromUtf8(),
                    type!!.toKStringFromUtf8()
                ).encodeToByteArray().usePinned { pinned ->
                    //TODO: This memory should be clean, but waiting for refactor in cinterface endpoint
                    nativeHeap.allocArray<ByteVar>(pinned.get().size).apply {
                        memcpy(this, pinned.addressOf(0), pinned.get().size.toULong())
                    }
                }
            }catch (e: Throwable){
                e.printStackTrace()
                memScoped { "".cstr.ptr }
            }
        }
        setAnswerAndSetRemoteDescriptionCallback = staticCFunction { ctx, streamRoomId, sdp, type ->
            val webRtcInterface = ctx.webrtc.get()
            webRtcInterface.setAnswerAndSetRemoteDescription(
                streamRoomId!!.toKStringFromUtf8(),
                sdp!!.toKStringFromUtf8(),
                type!!.toKStringFromUtf8()
            )
        }

        updateSessionIdCallback = staticCFunction { ctx, streamRoomId, sessionId, connectionType ->
            val webRtcInterface = ctx.webrtc.get()
            webRtcInterface.updateSessionId(
                streamRoomId!!.toKStringFromUtf8(),
                sessionId,
                connectionType!!.toKStringFromUtf8()
            )
        }

        updateKeysCallback = staticCFunction { ctx, streamRoomId, keys, keysSize ->
            val webRtcInterface = ctx.webrtc.get()
            val kotlinKeys = (0uL..<keysSize).map {
                val k = keys!![it.toInt()]
                Key(
                    k.keyId!!.toKStringFromUtf8(),
                    k.key!!.readBytes(k.keySize.toInt()),
                    k.type.KType()
                )
            }

            webRtcInterface.updateKeys(
                streamRoomId!!.toKStringFromUtf8(),
                kotlinKeys
            )
        }
    }
//    }

@OptIn(ExperimentalForeignApi::class)
class ProxyWebrtc(
    webRtcInterface: WebRTCInterface
) : AutoCloseable {
    private val cWebRtc = webRtcInterface.toCWebRtcPointer()

    private val _proxy: CPointerVarOf<CPointer<cnames.structs.privmx_endpoint_stream_ProxyWebRTC>> by lazy {
        //TODO: Should be free
        nativeHeap.allocPointerTo<cnames.structs.privmx_endpoint_stream_ProxyWebRTC>().apply {
            libprivmxendpoint.privmx_endpoint_stream_newProxyWebRTC(cWebRtc, ptr)
        }
    }
    val proxy: CPointer<cnames.structs.privmx_endpoint_stream_ProxyWebRTC> get() = _proxy.value!!

    override fun close() {
        memScoped {
            cWebRtc.useContents {
                ctx.webrtc.dispose()
            }
        }
        libprivmxendpoint.privmx_endpoint_stream_freeProxyWebRTC(proxy)
    }
}

