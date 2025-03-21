package com.simplito.java.privmx_endpoint_extra.storeFileStream

import com.simplito.java.privmx_endpoint.model.exceptions.NativeException
import com.simplito.java.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.java.privmx_endpoint.modules.store.StoreApi
import com.simplito.java.privmx_endpoint_extra.storeFileStream.StoreFileStream.Companion.OPTIMAL_SEND_SIZE
import java.io.IOException
import java.io.OutputStream

/**
 * Opens Store file and writes it into [OutputStream].
 *
 * @param api          reference to Store API
 * @param fileId       ID of the file to open
 * @param outputStream stream to write downloaded data with optimized chunk size [StoreFileStream.OPTIMAL_SEND_SIZE]
 * @return ID of the read file
 * @throws IOException           if there is an error while writing the stream
 * @throws IllegalStateException when storeApi is not initialized or there's no connection
 * @throws PrivmxException       if there is an error while opening Store file
 * @throws NativeException       if there is an unknown error while opening Store file
 */
@Throws(
    IOException::class,
    IllegalStateException::class,
    PrivmxException::class,
    NativeException::class
)
fun StoreFileStreamReader.Companion.openFile(
    api: StoreApi,
    fileId: String,
    outputStream: OutputStream
): String {
    return openFile(api, fileId, outputStream, null)
}

/**
 * Opens Store file and writes it into [OutputStream].
 *
 * @param api              reference to Store API
 * @param fileId           ID of the file to open
 * @param outputStream     stream to write downloaded data with optimized chunk size [StoreFileStream.OPTIMAL_SEND_SIZE]
 * @param streamController controls the process of reading file
 * @return ID of the read file
 * @throws IOException           if there is an error while writing stream
 * @throws IllegalStateException when storeApi is not initialized or there's no connection
 * @throws PrivmxException       if there is an error while reading Store file
 * @throws NativeException       if there is an unknown error while reading Store file
 */
@Throws(
    IOException::class,
    IllegalStateException::class,
    PrivmxException::class,
    NativeException::class
)
fun StoreFileStreamReader.Companion.openFile(
    api: StoreApi,
    fileId: String,
    outputStream: OutputStream,
    streamController: StoreFileStream.Controller?
): String {
    val input = openFile(api, fileId)
    if (streamController != null) {
        input.setProgressListener(streamController)
    }
    var chunk: ByteArray
    do {
        if (streamController != null && streamController.isStopped()) {
            return input.close()
        }
        chunk = input.read(OPTIMAL_SEND_SIZE)
        outputStream.write(chunk)
    } while (chunk.size.toLong() == OPTIMAL_SEND_SIZE)

    return input.close()
}