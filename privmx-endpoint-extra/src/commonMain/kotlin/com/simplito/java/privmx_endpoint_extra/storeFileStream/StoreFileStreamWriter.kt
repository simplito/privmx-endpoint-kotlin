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

import com.simplito.java.privmx_endpoint.model.exceptions.NativeException
import com.simplito.java.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.java.privmx_endpoint.modules.store.StoreApi
import kotlinx.io.IOException
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * Manages handle for file writing.
 *
 * @category store
 */
class StoreFileStreamWriter private constructor(handle: Long, storeApi: StoreApi) :
    StoreFileStream(handle, storeApi) {
    /**
     * Writes data to Store file.
     *
     * @param data data to write (the recommended size of data chunk is [StoreFileStream.OPTIMAL_SEND_SIZE])
     * @throws PrivmxException       if there is an error while writing chunk
     * @throws NativeException       if there is an unknown error while writing chunk
     * @throws IllegalStateException when storeApi is not initialized or there's no connection
     * @throws IOException           when `this` is closed
     * @throws PrivmxException       when method encounters an exception
     * @throws NativeException       when method encounters an unknown exception
     * @throws IllegalStateException when [.storeApi] is closed
     */
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class,
        IOException::class
    )
    fun write(data: ByteArray) {
        if (isClosed) throw IOException("File handle is closed")
        storeApi.writeToFile(handle, data)
        callChunkProcessed(data.size.toLong())
    }

    companion object {
        /**
         * Creates a new file in given Store.
         *
         * @param api         reference to Store API
         * @param storeId     ID of the Store
         * @param publicMeta  byte array of any arbitrary metadata that can be read by anyone
         * @param privateMeta byte array of any arbitrary metadata that will be encrypted before sending
         * @param size        size of data to write
         * @return Instance ready to write to the created Store file
         * @throws IllegalStateException when storeApi is not initialized or there's no connection
         * @throws PrivmxException       if there is an error while creating Store file metadata
         * @throws NativeException       if there is an unknown error while creating store file metadata
         */
        @Throws(
            PrivmxException::class, NativeException::class, IllegalStateException::class
        )
        @JvmStatic
        fun createFile(
            api: StoreApi,
            storeId: String,
            publicMeta: ByteArray,
            privateMeta: ByteArray,
            size: Long
        ): StoreFileStreamWriter {
            return StoreFileStreamWriter(
                api.createFile(storeId, publicMeta, privateMeta, size)!!, api
            )
        }

        /**
         * Updates an existing file.
         *
         * @param api         reference to Store API
         * @param fileId      ID of the file to update
         * @param publicMeta  new public metadata for the matching file
         * @param privateMeta new private (encrypted) metadata for the matching file
         * @param size        size of data to write
         * @return {@link StoreFileStreamWriter} instance prepared for writing
         * @throws IllegalStateException when {@code storeApi} is not initialized or there's no connection
         * @throws PrivmxException       if there is an error while updating Store file metadata
         * @throws NativeException       if there is an unknown error while updating Store file metadata
         */
        @Throws(
            PrivmxException::class, NativeException::class, IllegalStateException::class
        )
        @JvmStatic
        fun updateFile(
            api: StoreApi, fileId: String, publicMeta: ByteArray, privateMeta: ByteArray, size: Long
        ): StoreFileStreamWriter {
            return StoreFileStreamWriter(
                api.updateFile(fileId, publicMeta, privateMeta, size)!!, api
            )
        }

        /**
         * Creates new file in given Store and writes data from given [Source].
         *
         * @param api              reference to Store API
         * @param storeId          ID of the Store
         * @param publicMeta       byte array of any arbitrary metadata that can be read by anyone
         * @param privateMeta      byte array of any arbitrary metadata that will be encrypted before sending
         * @param size             size of data to write
         * @param source      stream with data to write to the file using optimal chunk size [StoreFileStream.OPTIMAL_SEND_SIZE]
         * @param streamController controls the process of writing file
         * @return ID of the created file
         * @throws IOException           if there is an error while reading stream or `this` is closed
         * @throws IllegalStateException when `storeApi` is not initialized or there's no connection
         * @throws PrivmxException       if there is an error while creating Store file metadata
         * @throws NativeException       if there is an unknown error while creating Store file metadata
         */
        @Throws(
            IOException::class,
            PrivmxException::class,
            NativeException::class,
            IllegalStateException::class
        )
        @JvmOverloads
        @JvmStatic
        fun createFile(
            api: StoreApi,
            storeId: String,
            publicMeta: ByteArray,
            privateMeta: ByteArray,
            size: Long,
            source: Source,
            streamController: Controller? = null
        ): String {
            val output = createFile(
                api, storeId, publicMeta, privateMeta, size
            )
            if (streamController != null) {
                output.setProgressListener(streamController)
            }
            var chunk = source.readByteArray(OPTIMAL_SEND_SIZE.toInt())
            while (chunk.isNotEmpty()) {
                if (streamController?.isStopped == true) {
                    output.close()
                }
                output.write(chunk)
                chunk = source.readByteArray(OPTIMAL_SEND_SIZE.toInt())
            }
            return output.close()
        }

        /**
         * Updates existing file and writes data from passed [Source].
         *
         * @param api              reference to Store API
         * @param fileId           ID of the file to update
         * @param publicMeta       new public metadata for the matching file
         * @param privateMeta      new private (encrypted) metadata for the matching file
         * @param size             size of data to write
         * @param source      stream with data to write to the file using optimal chunk size [StoreFileStream.OPTIMAL_SEND_SIZE]
         * @param streamController controls the process of writing file
         * @return Updated file ID
         * @throws IOException           if there is an error while reading stream or `this` is closed
         * @throws IllegalStateException when `storeApi` is not initialized or there's no connection
         * @throws PrivmxException       if there is an error while updating Store file metadata
         * @throws NativeException       if there is an unknown error while updating Store file metadata
         */
        @Throws(
            IOException::class,
            PrivmxException::class,
            NativeException::class,
            IllegalStateException::class
        )
        @JvmOverloads
        @JvmStatic
        fun updateFile(
            api: StoreApi,
            fileId: String,
            publicMeta: ByteArray,
            privateMeta: ByteArray,
            size: Long,
            source: Source,
            streamController: Controller? = null
        ): String {
            val output = updateFile(api, fileId, publicMeta, privateMeta, size)
            if (streamController != null) {
                output.setProgressListener(streamController)
            }
            while (true) {
                if (streamController?.isStopped == true) {
                    output.close()
                }
                val chunk = source.readByteArray(OPTIMAL_SEND_SIZE.toInt())
                if (chunk.isEmpty()) {
                    break
                }
                output.write(chunk)
            }
            return output.close()
        }
    }
}
