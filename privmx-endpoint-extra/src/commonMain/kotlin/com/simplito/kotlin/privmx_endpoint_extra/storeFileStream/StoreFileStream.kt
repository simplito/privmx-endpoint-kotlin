//
// PrivMX Endpoint Kotlin Extra.
// Copyright © 2025 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.simplito.kotlin.privmx_endpoint_extra.storeFileStream

import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.store.StoreApi
import kotlin.jvm.JvmStatic

/**
 * Base class for Store file streams. Implements progress listeners.
 */
abstract class StoreFileStream
/**
 * Creates instance of `StoreFileStream`.
 * @param handle handle to Store file
 * @param storeApi [StoreApi] instance that calls read/write methods on files
 */ protected constructor(
    /**
     * Reference to file handle.
     */
    protected val handle: Long,
    /**
     * Reference to [StoreApi].
     */
    protected val storeApi: StoreApi
) {
    private var processedBytes = 0L
    private var progressListener: ProgressListener? = null
    var isClosed: Boolean = false
        private set

    companion object {
        /**
         * Constant value with optimal size of reading/sending data.
         */
        const val OPTIMAL_SEND_SIZE: Long = 128 * 1024L

    }

    /**
     * Manages sending/reading files using [kotlinx.io.Source]/[kotlinx.io.Sink].
     */
    open class Controller() : ProgressListener {
        var isStopped = false
            private set

        /**
         * Stops reading/writing file after processing the current chunk.
         */
        fun stop() {
            isStopped = true
        }

        /**
         * Override this method to handle event when each chunk was sent successfully.
         *
         * @param processedBytes full size of current sent/read data
         */
        override fun onChunkProcessed(processedBytes: Long) {
        }

    }

    /**
     * Sets listening for single chunk sent/read.
     * @param progressListener callback triggered when chunk is sent/read
     */
    fun setProgressListener(progressListener: ProgressListener) {
        this.progressListener = progressListener
    }

    /**
     * Increases the size of current sent/read data by chunkSize and calls [ProgressListener.onChunkProcessed].
     * @param chunkSize size of processed chunk
     */
    protected fun callChunkProcessed(chunkSize: Long) {
        processedBytes += chunkSize
        progressListener?.onChunkProcessed(processedBytes)
    }

    /**
     * Interface to listen to progress of sending/reading files.
     */
    interface ProgressListener {
        /**
         * A callback called after each successful read/write operation.
         * @param processedBytes full size of current sent/read data
         */
        fun onChunkProcessed(processedBytes: Long)
    }

    /**
     * Closes file handle.
     * @return ID of the closed file
     * @throws PrivmxException if there is an error while closing file
     * @throws NativeException if there is an unknown error while closing file
     * @throws IllegalStateException when `storeApi` is not initialized or there's no connection
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun close(): String {
        return storeApi.closeFile(handle)!!.also {
            isClosed = true
        }
    }
}