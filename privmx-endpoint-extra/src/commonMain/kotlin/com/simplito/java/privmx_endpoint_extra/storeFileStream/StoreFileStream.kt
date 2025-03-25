//
// PrivMX Endpoint Java Extra.
// Copyright © 2024 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.simplito.java.privmx_endpoint_extra.storeFileStream

import com.simplito.java.privmx_endpoint.modules.store.StoreApi

/**
 * Base class for Store file streams. Implements progress listeners.
 *
 * @category store
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
     * Manages sending/reading files using [java.io.InputStream]/[java.io.OutputStream].
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
         * Returns information whether the stream should be stopped.
         * @return `true` if controller is set to stop
         */
        fun isStopped(): Boolean {
            return isStopped
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
        if (progressListener != null) {
            progressListener!!.onChunkProcessed(processedBytes)
        }
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

    fun close(): String {
        isClosed = true
        return ( /*closedFileId =*/storeApi.closeFile(handle)!!)
    }



}
