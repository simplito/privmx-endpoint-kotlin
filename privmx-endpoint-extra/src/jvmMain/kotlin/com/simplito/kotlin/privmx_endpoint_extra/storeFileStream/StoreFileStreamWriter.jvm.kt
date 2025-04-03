package com.simplito.kotlin.privmx_endpoint_extra.storeFileStream

import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.store.StoreApi
import com.simplito.kotlin.privmx_endpoint_extra.storeFileStream.StoreFileStream.Companion.OPTIMAL_SEND_SIZE
import com.simplito.kotlin.privmx_endpoint_extra.storeFileStream.StoreFileStream.Controller
import java.io.IOException
import java.io.InputStream

/**
 * Creates new file in given Store and writes data from given [InputStream].
 *
 * @param api              reference to Store API
 * @param storeId          ID of the Store
 * @param publicMeta       byte array of any arbitrary metadata that can be read by anyone
 * @param privateMeta      byte array of any arbitrary metadata that will be encrypted before sending
 * @param size             size of data to write
 * @param inputStream      stream with data to write to the file using optimal chunk size [StoreFileStream.OPTIMAL_SEND_SIZE]
 * @param streamController controls the process of writing file
 * @return ID of the created file
 * @throws IOException           if there is an error while reading stream or `this` is closed
 * @throws IllegalStateException when `storeApi` is not initialized or there's no connection
 * @throws PrivmxException       if there is an error while creating Store file metadata
 * @throws NativeException       if there is an unknown error while creating Store file metadata
 */
@Throws(
    IOException::class, PrivmxException::class, NativeException::class, IllegalStateException::class
)
@JvmOverloads
fun StoreFileStreamWriter.Companion.createFile(
    api: StoreApi,
    storeId: String,
    publicMeta: ByteArray,
    privateMeta: ByteArray,
    size: Long,
    inputStream: InputStream,
    streamController: Controller? = null
): String {
    val output = createFile(api, storeId, publicMeta, privateMeta, size)

    if (streamController != null) {
        output.setProgressListener(streamController)
    }
    val chunk = ByteArray(OPTIMAL_SEND_SIZE.toInt())
    var read: Int
    while ((inputStream.read(chunk).also { read = it }) >= 0) {
        if (streamController?.isStopped == true) {
            output.close()
        }
        output.write(chunk.copyOf(read))
    }
    return output.close()
}

/**
 * Updates existing file and writes data from passed [InputStream].
 *
 * @param api              reference to Store API
 * @param fileId           ID of the file to update
 * @param publicMeta       new public metadata for the matching file
 * @param privateMeta      new private (encrypted) metadata for the matching file
 * @param size             size of data to write
 * @param inputStream      stream with data to write to the file using optimal chunk size [StoreFileStream.OPTIMAL_SEND_SIZE]
 * @param streamController controls the process of writing file
 * @return Updated file ID
 * @throws IOException           if there is an error while reading stream or `this` is closed
 * @throws IllegalStateException when `storeApi` is not initialized or there's no connection
 * @throws PrivmxException       if there is an error while updating Store file metadata
 * @throws NativeException       if there is an unknown error while updating Store file metadata
 */
@Throws(
    IOException::class, PrivmxException::class, NativeException::class, IllegalStateException::class
)
@JvmOverloads
fun StoreFileStreamWriter.Companion.updateFile(
    api: StoreApi,
    fileId: String,
    publicMeta: ByteArray,
    privateMeta: ByteArray,
    size: Long,
    inputStream: InputStream,
    streamController: Controller? = null
): String {
    val output = updateFile(api, fileId, publicMeta, privateMeta, size)
    if (streamController != null) {
        output.setProgressListener(streamController)
    }
    val chunk = ByteArray(OPTIMAL_SEND_SIZE.toInt())
    var read: Int
    while (true) {
        if (streamController?.isStopped == true) {
            output.close()
        }
        if ((inputStream.read(chunk).also { read = it }) <= 0) {
            break
        }
        output.write(chunk.copyOf(read))
    }
    return output.close()
}