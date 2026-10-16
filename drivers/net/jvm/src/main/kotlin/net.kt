package com.simplito.kotlin.drivers

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.util.concurrent.TimeUnit

private object PrivmxDrvNet {

    class HttpOptions(
        @JvmField val baseUrl: String,
        @JvmField val keepAlive: Boolean,
    )

    class HttpHeader(
        @JvmField val name: String,
        @JvmField val value: String,
    )

    class HttpRequestOptions(
        @JvmField val path: String,
        @JvmField val method: String,
        @JvmField val contentType: String,
        @JvmField val headers: List<HttpHeader>,
        @JvmField val headerslen: Int,
        @JvmField val keepAlive: Boolean,
    )

    class WsOptions(
        @JvmField val url: String,
    )

    class HttpResponse(
        @JvmField val statusCode: Int,
        @JvmField val out: ByteArray,
    )

    class HttpSession(baseUrl: String) {
        @JvmField val baseUrl: String = baseUrl.toHttpUrl().let {
            "${if(it.isHttps) "https" else "http"}://${it.host}:${it.port}"
        }

        fun makeRequest(data: ByteArray?, options: HttpRequestOptions): HttpResponse {
            val method = options.method.uppercase()
            val mediaType = options.contentType.toMediaTypeOrNull() ?: OCTET_STREAM
            val builder = Request.Builder().url(resolve(baseUrl, options.path))
            for (header in options.headers) {
                builder.addHeader(header.name, header.value)
            }
            builder.header("Connection", if (options.keepAlive) "keep-alive" else "close")
            builder.method(
                method,
                if (method in BODYLESS_METHODS) null else (data ?: EMPTY).toRequestBody(mediaType),
            )

            client.newCall(builder.build()).execute().use { response ->
                return HttpResponse(response.code, response.body?.bytes() ?: EMPTY)
            }
        }
    }

    class WebSocket(@JvmField val url: String) {

        private val listener = WebsocketListener()

        @Volatile
        private var socket: okhttp3.WebSocket? = null

        fun connect(options: WsOptions, listener_ptr: Long) {
            listener.listener_ptr = listener_ptr
            val request = Request.Builder().url(options.url).build()
            socket = client.newWebSocket(request, listener)
        }

        fun send(data: ByteArray) {
            socket?.send(data.toByteString())
        }

        fun disconnect() {
            socket?.close(NORMAL_CLOSURE, null)
        }

        class WebsocketListener : okhttp3.WebSocketListener() {
            @JvmField
            @Volatile
            var listener_ptr: Long = 0L

            override fun onOpen(webSocket: okhttp3.WebSocket, response: okhttp3.Response) {
                onOpenNative()
            }

            override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
                onMessageNative(text.toByteArray())
            }

            override fun onMessage(webSocket: okhttp3.WebSocket, bytes: ByteString) {
                onMessageNative(bytes.toByteArray())
            }

            override fun onFailure(
                webSocket: okhttp3.WebSocket,
                t: Throwable,
                response: okhttp3.Response?,
            ) {
                onErrorNative(t.message ?: t.toString())
            }

            override fun onClosed(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
                onCloseNative(code == NORMAL_CLOSURE)
            }

            external fun onOpenNative()
            external fun onMessageNative(message: ByteArray)
            external fun onErrorNative(message: String)
            external fun onCloseNative(wasClean: Boolean)
        }
    }

    private const val NORMAL_CLOSURE = 1000
    private val EMPTY = ByteArray(0)
    private val OCTET_STREAM = "application/octet-stream".toMediaTypeOrNull()

    private val BODYLESS_METHODS = setOf("GET", "HEAD")

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private fun resolve(baseUrl: String, path: String) =
        (baseUrl.trimEnd('/') + "/" + path.trimStart('/')).toHttpUrl()
}
