package Stacks.Kotlin.streams

import android.content.Context
import com.simplito.kotlin.privmx_endpoint_streams.StreamApiInit
import org.webrtc.EglBase

lateinit var eglBase: EglBase
lateinit var appContext: Context    // provide App context from App

actual fun createStreamApiInit(): StreamApiInit {
    /**
     * On Android, StreamApi needs two platform objects, passed in StreamApiInit:
     * - the application Context
     * - an EglBase (the shared EGL context for video rendering).
     *   Pass the same EglBase to your renderers so everything shares one context.
     */

    eglBase = EglBase.create()

    return StreamApiInit(
        appContext,
        eglBase
    )
}