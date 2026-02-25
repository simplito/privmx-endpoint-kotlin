package com.simplito.kotlin.privmx_endpoint.modules.stream

import com.simplito.kotlin.privmx_endpoint.model.stream.Key
import com.simplito.kotlin.privmx_endpoint.model.stream.KeyType
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaque
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.CValue
import kotlinx.cinterop.CValues
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.cValue
import kotlinx.cinterop.cstr
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.useContents
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_stream_KeyType
import kotlin.getValue

@OptIn(ExperimentalForeignApi::class)
private fun privmx_endpoint_stream_KeyType.KType(): KeyType = KeyType.entries[ordinal]

@OptIn(ExperimentalForeignApi::class)
private val COpaquePointer?.webrtc: StableRef<WebRtcInterface>
    get() = memScoped {
        this@webrtc!!.asStableRef<WebRtcInterface>()
    }

@OptIn(ExperimentalForeignApi::class)
private fun WebRtcInterface.toCWebRtcPointer(): CValue<libprivmxendpoint.privmx_endpoint_stream_WebRTCInterface> =
    memScoped {
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
                return@staticCFunction webRtcInterface.createOfferAndSetLocalDescription(
                    streamRoomId!!.toKStringFromUtf8()
                ).cstr.ptr
            }
            createAnswerAndSetDescriptionsCallback = staticCFunction { ctx, streamRoomId, sdp, type ->
                val webRtcInterface = ctx.webrtc.get()
                return@staticCFunction webRtcInterface.createAnswerAndSetDescriptions(
                    streamRoomId!!.toKStringFromUtf8(),
                    sdp!!.toKStringFromUtf8(),
                    type!!.toKStringFromUtf8()
                ).cstr.ptr
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
    }

@OptIn(ExperimentalForeignApi::class)
class ProxyWebrtc(
    webRtcInterface: WebRtcInterface
): AutoCloseable {
    private val cWebRtc = webRtcInterface.toCWebRtcPointer()

    private val _proxy: CPointerVarOf<CPointer<cnames.structs.privmx_endpoint_stream_ProxyWebRTC>> by lazy {
        memScoped {
            allocPointerTo<cnames.structs.privmx_endpoint_stream_ProxyWebRTC>().apply {
                libprivmxendpoint.privmx_endpoint_stream_newProxyWebRTC(cWebRtc, ptr)
            }
        }
    }
    val proxy:CPointer<cnames.structs.privmx_endpoint_stream_ProxyWebRTC> get() = _proxy.value!!

    override fun close() {
        memScoped {
            cWebRtc.useContents {
                ctx.webrtc.dispose()
            }
        }
        libprivmxendpoint.privmx_endpoint_stream_freeProxyWebRTC(proxy.value)
    }
}

