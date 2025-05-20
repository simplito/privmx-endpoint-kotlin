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
package com.simplito.kotlin.privmx_endpoint_extra.inboxFileStream

import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.inbox.InboxApi
import com.simplito.kotlin.privmx_endpoint_extra.storeFileStream.StoreFileStream
import kotlin.jvm.JvmStatic

/**
 * Base class for Inbox file streams.
 */
abstract class InboxFileStream
/**
 * Creates instance of [InboxFileStream].
 *
 * @param handle   handle to Inbox file
 * @param inboxApi [InboxApi] instance that calls read/write methods on files
 */ protected constructor(
    /**
     * Reference to file handle.
     */
    val fileHandle: Long,
    /**
     * Reference to [com.simplito.kotlin.privmx_endpoint.modules.inbox.InboxApi] instance.
     */
    protected val inboxApi: InboxApi
) {
    /**
     * Gets size of sent data.
     *
     * @return size of sent data
     */
    var processedBytes: Long = 0L
        private set
    private var progressListener: StoreFileStream.ProgressListener? = null

    /**
     * Returns information whether the instance is closed.
     *
     * @return `true` if file handle is closed
     */
    var isClosed: Boolean = false
        private set

    companion object {
        /**
         * Constant value with optimal size of reading/sending data.
         */
        const val OPTIMAL_SEND_SIZE: Long = 128 * 1024L
    }

    /**
     * Sets listening for single chunk sent/read.
     *
     * @param progressListener callback triggered when chunk is sent/read
     */
    fun setProgressListener(progressListener: StoreFileStream.ProgressListener?) {
        this.progressListener = progressListener
    }

    /**
     * Increases the size of current sent/read data by chunkSize and calls [StoreFileStream.ProgressListener.onChunkProcessed].
     *
     * @param chunkSize size of processed chunk
     */
    protected fun callChunkProcessed(chunkSize: Long) {
        processedBytes += chunkSize
        progressListener?.onChunkProcessed(processedBytes)
    }

    /**
     * Closes file handle.
     *
     * @return ID of the closed file
     * @throws PrivmxException       if there is an error while closing file
     * @throws NativeException       if there is an unknown error while closing file
     * @throws IllegalStateException when [.inboxApi] is not initialized or there's no connection
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun close(): String = inboxApi.closeFile(fileHandle)!!.also {
        isClosed = true
    }
}