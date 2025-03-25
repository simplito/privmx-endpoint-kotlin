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
import java.io.InputStream

/**
 * Writes data from an [InputStream] to an Inbox file.
 *
 * @param inboxHandle      the handle of the Inbox to write to
 * @param inputStream      the [InputStream] to read data from
 * @param streamController an optional controller for monitoring and controlling the write operation.
 * @throws PrivmxException       when method encounters an exception while executing [InboxApi.writeToFile]
 * @throws NativeException       when method encounters an unknown exception while executing [InboxApi.writeToFile]
 * @throws IllegalStateException when [.inboxApi] is closed
 * @throws IOException           when [InputStream.read] thrown exception or `this` is closed
 */
/**
 * Writes data from an [InputStream] to an Inbox file.
 *
 * @param inboxHandle the handle of an Inbox to write to
 * @param inputStream the [InputStream] to read data from
 * @throws PrivmxException       when method encounters an exception while executing [InboxApi.writeToFile]
 * @throws NativeException       when method encounters an unknown exception while executing [InboxApi.writeToFile]
 * @throws IllegalStateException when [.inboxApi] is closed
 * @throws IOException           when [InputStream.read] thrown exception or `this` is closed
 */
@JvmOverloads
@Throws(
    PrivmxException::class,
    NativeException::class,
    IllegalStateException::class,
    IOException::class
)
fun InboxFileStreamWriter.writeStream(
    inboxHandle: Long,
    inputStream: InputStream,
    streamController: StoreFileStream.Controller? = null
) {
    if (streamController != null) {
        setProgressListener(streamController)
    }
    val chunk = ByteArray(InboxFileStream.OPTIMAL_SEND_SIZE as Int)
    var read: Int
    while (true) {
        if (streamController != null && streamController.isStopped()) {
            return
        }
        if ((inputStream.read(chunk).also { read = it }) <= 0) {
            return
        }
        write(inboxHandle, chunk.copyOf(read))
    }
}