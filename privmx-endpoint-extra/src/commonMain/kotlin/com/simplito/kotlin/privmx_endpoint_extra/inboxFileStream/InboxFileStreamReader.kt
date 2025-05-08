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
import kotlinx.io.IOException
import kotlinx.io.Sink
import kotlin.invoke
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * Manages handle for file reading from Inbox.
 */
class InboxFileStreamReader private constructor(
    fileHandle: Long,
    api: InboxApi
) : InboxFileStream(fileHandle, api) {
    companion object {
        /**
         * Opens Inbox file.
         *
         * @param api    reference to Inbox API
         * @param fileId ID of the file to open
         * @return Instance ready to read from the Inbox file
         * @throws IllegalStateException when `inboxApi` is not initialized or there's no connection
         * @throws PrivmxException       if there is an error while opening Inbox file
         * @throws NativeException       if there is an unknown error while opening Inbox file
         */
        @Throws(
            IllegalStateException::class,
            PrivmxException::class,
            NativeException::class
        )
        @JvmStatic
        fun openFile(
            api: InboxApi,
            fileId: String
        ) = InboxFileStreamReader(
            api.openFile(fileId)!!,
            api
        )

        /**
         * Opens Inbox file and writes it into [Sink] with optimized chunk size [InboxFileStream.OPTIMAL_SEND_SIZE].
         *
         * @param api              reference to Inbox API
         * @param fileId           ID of the file to open
         * @param sink     stream to write downloaded data
         * @param streamController controls the process of reading file
         * @return ID of the read file
         * @throws IOException           if there is an error while writing stream
         * @throws IllegalStateException when inboxApi is not initialized or there's no connection
         * @throws PrivmxException       if there is an error while reading Inbox file
         * @throws NativeException       if there is an unknown error while reading Inbox file
         */
        @Throws(
            IOException::class,
            IllegalStateException::class,
            PrivmxException::class,
            NativeException::class
        )
        @JvmStatic
        @JvmOverloads
        fun openFile(
            api: InboxApi,
            fileId: String,
            sink: Sink,
            streamController: StoreFileStream.Controller? = null
        ): String {
            val input = openFile(api, fileId)

            if (streamController != null) {
                input.setProgressListener(streamController)
            }
            do {
                if (streamController?.isStopped == true) {
                    input.close()
                }
                val chunk = input.read(OPTIMAL_SEND_SIZE)
                sink.write(chunk)
                sink.flush()
            } while (chunk.size.toLong() == OPTIMAL_SEND_SIZE)

            return input.close()
        }

    }

    /**
     * Reads file data and moves the cursor. If read data size is less than length, then EOF.
     *
     * @param size size of data to read (the recommended size is [InboxFileStream.OPTIMAL_SEND_SIZE])
     * @return Read data
     * @throws IOException           when `this` is closed
     * @throws PrivmxException       when method encounters an exception
     * @throws NativeException       when method encounters an unknown exception
     * @throws IllegalStateException when [.inboxApi] is closed
     */
    @Throws(
        IOException::class,
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun read(size: Long): ByteArray {
        if (isClosed) throw IOException("File handle is closed")
        return inboxApi.readFromFile(fileHandle, size)!!.also {
            callChunkProcessed(it.size.toLong())
        }
    }

    /**
     * Moves read cursor.
     *
     * @param position new cursor position
     * @throws IllegalStateException if `inboxApi` is not initialized or connected
     * @throws PrivmxException       if there is an error while seeking
     * @throws NativeException       if there is an unknown error while seeking
     */
    @Throws(IllegalStateException::class, PrivmxException::class, NativeException::class)
    fun seek(position: Long) {
        inboxApi.seekInFile(fileHandle, position)
    }
}