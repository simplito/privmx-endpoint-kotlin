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
import java.io.OutputStream

/**
 * Opens Inbox file and writes it into [OutputStream] with optimized chunk size [InboxFileStream.OPTIMAL_SEND_SIZE].
 *
 * @param api          reference to Inbox API
 * @param fileId       ID of the file to open
 * @param outputStream stream to write downloaded data
 * @return ID of the read file
 * @throws IOException           if there is an error while writing the stream
 * @throws IllegalStateException when inboxApi is not initialized or there's no connection
 * @throws PrivmxException       if there is an error while opening Inbox file
 * @throws NativeException       if there is an unknown error while opening Inbox file
 */
@Throws(
    IOException::class,
    IllegalStateException::class,
    PrivmxException::class,
    NativeException::class
)
fun InboxFileStreamReader.openFile(
    api: InboxApi,
    fileId: String,
    outputStream: OutputStream
): String {
    return openFile(api, fileId, outputStream, null)
}

/**
 * Opens Inbox file and writes it into [OutputStream] with optimized chunk size [InboxFileStream.OPTIMAL_SEND_SIZE].
 *
 * @param api              reference to Inbox API
 * @param fileId           ID of the file to open
 * @param outputStream     stream to write downloaded data
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
fun InboxFileStreamReader.openFile(
    api: InboxApi,
    fileId: String,
    outputStream: OutputStream,
    streamController: StoreFileStream.Controller?
): String {
    val input = InboxFileStreamReader.openFile(api, fileId)
    if (streamController != null) {
        input.setProgressListener(streamController)
    }
    var chunk: ByteArray
    do {
        if (streamController != null && streamController.isStopped()) {
            input.close()
        }
        chunk = input.read(InboxFileStream.OPTIMAL_SEND_SIZE)
        outputStream.write(chunk)
    } while (chunk.size.toLong() == InboxFileStream.OPTIMAL_SEND_SIZE)

    return input.close()!!
}


