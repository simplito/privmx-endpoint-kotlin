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
package com.simplito.java.privmx_endpoint_extra.inboxFileStream

import com.simplito.java.privmx_endpoint.model.exceptions.NativeException
import com.simplito.java.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.java.privmx_endpoint.modules.inbox.InboxApi
import com.simplito.java.privmx_endpoint_extra.storeFileStream.StoreFileStream
import kotlinx.io.IOException
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlin.jvm.JvmOverloads

/**
 * Manages handle for file writing.
 *
 * @category inbox
 */
class InboxFileStreamWriter private constructor(handle: Long, inboxApi: InboxApi) :
    InboxFileStream(handle, inboxApi) {
    companion object {
        /**
         * Creates a new file in given Inbox.
         *
         * @param api         reference to Inbox API
         * @param publicMeta  byte array of any arbitrary metadata that can be read by anyone
         * @param privateMeta byte array of any arbitrary metadata that will be encrypted before sending
         * @param size        size of data to write
         * @return Instance ready to write to the created Inbox file
         * @throws IllegalStateException when inboxApi is not initialized or there's no connection
         * @throws PrivmxException       if there is an error while creating Inbox file metadata
         * @throws NativeException       if there is an unknown error while creating inbox file metadata
         */
        @Throws(
            PrivmxException::class,
            NativeException::class,
            IllegalStateException::class
        )
        fun createFile(
            api: InboxApi,
            publicMeta: ByteArray,
            privateMeta: ByteArray,
            size: Long
        ): InboxFileStreamWriter {
            if (api == null) throw NullPointerException("api cannot be null")
            return InboxFileStreamWriter(
                api.createFileHandle(publicMeta, privateMeta, size)!!,
                api
            )
        }
    }

    /**
     * Writes data to Inbox file.
     *
     * @param inboxHandle the handle of the Inbox to write to
     * @param data        data to write (the recommended size of data chunk is [InboxFileStream.OPTIMAL_SEND_SIZE])
     * @throws PrivmxException       when method encounters an exception while executing [InboxApi.writeToFile]
     * @throws NativeException       when method encounters an unknown exception while executing [InboxApi.writeToFile]
     * @throws IllegalStateException when [.inboxApi] is closed
     * @throws IOException           when `this` is closed
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class,
        IOException::class
    )
    fun write(inboxHandle: Long, data: ByteArray) {
        if (isClosed) throw IOException("File handle is closed")
        inboxApi.writeToFile(inboxHandle, fileHandle, data)
        callChunkProcessed(data.size.toLong())
    }

    /**
     * Writes data from an [Source] to an Inbox file.
     *
     * @param inboxHandle the handle of an Inbox to write to
     * @param source the [Source] to read data from
     * @throws PrivmxException       when method encounters an exception while executing [InboxApi.writeToFile]
     * @throws NativeException       when method encounters an unknown exception while executing [InboxApi.writeToFile]
     * @throws IllegalStateException when [.inboxApi] is closed
     * @throws IOException           when [Source.read] thrown exception or `this` is closed
     */
    @JvmOverloads
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class,
        IOException::class
    )
    fun writeStream(
        inboxHandle: Long,
        source: Source,
        streamController: StoreFileStream.Controller? = null
    ) {
        if (streamController != null) {
            setProgressListener(streamController)
        }
        while (true) {
            if (streamController?.isStopped() == true) {
                return
            }
            val chunk = source.readByteArray(OPTIMAL_SEND_SIZE.toInt())
            if (chunk.isEmpty()) {
                return
            }
            write(inboxHandle, chunk)
        }
    }
}