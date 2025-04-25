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
package com.simplito.kotlin.privmx_endpoint_extra.inboxEntryStream

import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.inbox.InboxApi
import com.simplito.kotlin.privmx_endpoint_extra.inboxFileStream.InboxFileStreamWriter
import com.simplito.kotlin.privmx_endpoint_extra.storeFileStream.StoreFileStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.io.Source
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmOverloads

/**
 * Provides a streamlined process for creating and sending Inbox entries
 * with optional file attachments.
 *
 *  This class simplifies interacting with the Inbox API for sending entries,
 * especially when dealing with multiple files. It manages the lifecycle of the entry creation
 * process, including file uploads and final entry submission.
 */
@OptIn(ExperimentalAtomicApi::class)
class InboxEntryStream private constructor(
    private val inboxApi: InboxApi,
    private val inboxFiles: Map<FileInfo, InboxFileStreamWriter>,
    private val inboxHandle: Long,
    private val entryStreamListener: EntryStreamListener
) {
    private val sendingChunkContext = Dispatchers.IO + CoroutineName("PrivMX chunk sending context")
    private val sendingFilesContext = Dispatchers.Default + CoroutineName("PrivMX files sending context")
    private val coroutineScope = CoroutineScope(sendingFilesContext)
    private lateinit var sendingFiles: List<Deferred<*>>
    private var streamState = AtomicReference(
        if (inboxFiles.isEmpty()) {
            State.FILES_SENT
        } else {
            State.PREPARED
        }
    )

    /**
     * Initiates the process of sending files using the provided executor.
     *
     * This method submits each file for sending to the `fileStreamExecutor`
     * and wait for completion.
     *
     * @param fileStreamExecutor the executor service responsible for executing file sending tasks
     * @throws IllegalStateException If the stream is not in the [State.PREPARED] state
     */
    @Throws(IllegalStateException::class)
    @JvmOverloads
    fun sendFiles(
        coroutineContext: CoroutineContext = sendingFilesContext
    ): Deferred<Unit> {
        check(streamState.load() == State.PREPARED) { "Stream should be in state PREPARED. Current state is: " + streamState.load().name }
        return coroutineScope.async(coroutineContext) sendingFileAsync@{
            check(!::sendingFiles.isInitialized) { "Files are being sent" }
            sendingFiles = inboxFiles.map { (fileInfo, fileHandle) ->
                async {
                    try {
                        sendFile(fileInfo, fileHandle)
                        entryStreamListener.onEndFileSending(fileInfo)
                    } catch (e: Exception) {
                        onError(e)
                        entryStreamListener.onErrorDuringSending(fileInfo, e)
                    }
                }
            }

            sendingFiles.forEach {
                try {
                    it.await()
                } catch (_: CancellationException) {
                    cancel()
                    return@sendingFileAsync
                }
            }

            if (sendingFiles.all { it.isCompleted }) {
                updateState(State.FILES_SENT)
            } else {
                onError(IllegalStateException("Some files cannot be sent"))
            }
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class,
        IOException::class
    )
    private suspend fun sendFile(
        fileInfo: FileInfo,
        fileHandle: InboxFileStreamWriter,
    ) = withContext(sendingFilesContext) {
        val controller: StoreFileStream.Controller = object : StoreFileStream.Controller() {
            override fun onChunkProcessed(processedBytes: Long) {
                entryStreamListener.onFileChunkProcessed(fileInfo, processedBytes)
                if (!isActive) {
                    stop()
                }
            }
        }

        entryStreamListener.onStartFileSending(fileInfo)
        if (fileInfo.fileStream == null) {
            fileHandle.setProgressListener(controller)
            while (fileInfo.fileSize > fileHandle.processedBytes && !fileHandle.isClosed) {
                if (controller.isStopped) {
                    break
                }
                val nextChunk = entryStreamListener.onNextChunkRequest(fileInfo)
                    ?: throw NullPointerException("Data chunk cannot be null")
                withContext(sendingChunkContext) {
                    fileHandle.write(inboxHandle, nextChunk)
                }
            }
        } else {
            withContext(sendingChunkContext) {
                fileHandle.writeStream(inboxHandle, fileInfo.fileStream, controller)
            }
        }
    }

    private fun onError(t: Throwable) {
        stopFileStreams()
        closeFileHandles()
        updateState(State.ERROR)
        entryStreamListener.onError(t)
        coroutineScope.cancel()
    }

    private fun updateState(newState: State) {
        if (streamState.exchange(newState) != newState) {
            entryStreamListener.onUpdateState(newState)
        }
    }

    /**
     * Cancels the stream and sets its state to [State.ABORTED].
     *
     * If the stream is currently sending files, all pending file operations will be canceled.
     *
     * If the stream is in the process of sending the entry, this operation will not have any effect.
     */
    fun cancel() {
        when (streamState.load()) {
            State.ERROR, State.ABORTED, State.SENT -> return

            else -> Unit
        }

        stopFileStreams()
        closeFileHandles()
        updateState(State.ABORTED)
        coroutineScope.cancel()
    }

    private fun stopFileStreams() {
        if (streamState.load() == State.PREPARED) {
            sendingFiles.forEach { it.cancel() }
        }
    }

    private fun closeFileHandles() {
        inboxFiles.values.forEach { file: InboxFileStreamWriter ->
            try {
                if (!file.isClosed) {
                    file.close()
                }
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Sends the entry data and closes this stream, transitioning it to the [State.SENT] state.
     *
     * This method should only be called after all files associated with the entry have been
     * successfully sent, indicated by the stream being in the [State.FILES_SENT] state.
     *
     * @throws PrivmxException       when method encounters an exception while calling [InboxApi.sendEntry] method
     * @throws NativeException       when method encounters an unknown exception while calling [InboxApi.sendEntry] method
     * @throws IllegalStateException if the stream is not in the [State.FILES_SENT] state or [InboxApi.sendEntry] thrown [IllegalStateException]
     */
    @Throws(
        PrivmxException::class, NativeException::class, IllegalStateException::class
    )
    fun sendEntry() {
        check(streamState.load() == State.FILES_SENT) { "Stream should be in state FILES_SENT. Current state is: " + streamState.load().name }
        try {
            inboxApi.sendEntry(inboxHandle)
            updateState(State.SENT)
            coroutineScope.cancel()
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Contains available states of [InboxEntryStream].
     */
    enum class State {
        /**
         * The initial state, indicating that [InboxEntryStream] is ready to send files.
         */
        PREPARED,

        /**
         * Indicates that all files have been sent successfully and the entry is ready to be sent.
         * This state is set when:
         * 1. The [InboxEntryStream] has been initialized and there are no files to send.
         * 2. All files have been sent successfully.
         */
        FILES_SENT,

        /**
         * Indicates that an error occurred while sending files or the Entry.
         */
        ERROR,

        /**
         * Indicates that the entry was sent successfully.
         */
        SENT,

        /**
         * Indicates that the [InboxEntryStream] was canceled.
         */
        ABORTED
    }

    /**
     * Represents information about a file to be sent by [InboxEntryStream].
     */
    data class FileInfo(
        val publicMeta: ByteArray,
        val privateMeta: ByteArray,
        val fileSize: Long,
        val fileStream: Source?
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as FileInfo

            if (fileSize != other.fileSize) return false
            if (!publicMeta.contentEquals(other.publicMeta)) return false
            if (!privateMeta.contentEquals(other.privateMeta)) return false
            if (fileStream != other.fileStream) return false

            return true
        }

        override fun hashCode(): Int {
            var result = fileSize.hashCode()
            result = 31 * result + publicMeta.contentHashCode()
            result = 31 * result + privateMeta.contentHashCode()
            result = 31 * result + fileStream.hashCode()
            return result
        }
    }

    /**
     * Interface for listening to state changes and exchanging data with an [InboxEntryStream] instance.
     *
     *
     * This interface provides callbacks for various events that occur during the lifecycle of an Inbox entry stream,
     * such as starting and ending file sending, requesting file chunks, handling errors, and updating the stream state.
     *
     *
     * Implement this interface to monitor and interact with the entry stream.
     */
    abstract class EntryStreamListener {
        /**
         * Override this method to handle when the process of sending file starts.
         *
         * @param file information about the file being sent
         */
        fun onStartFileSending(file: FileInfo) {
        }

        /**
         * Override this method to handle when file was sent successfully.
         *
         * @param file information about the sent file
         */
        fun onEndFileSending(file: FileInfo) {
        }

        /**
         * Override this method to handle event when [FileInfo.fileStream] is `null`
         * and the stream requests a chunk of the file to send.
         *
         * If you override this method, you should return the next chunk of the file.
         *
         * Returning `null` will cause a
         * [NullPointerException] while sending the file and stop the [InboxEntryStream] instance with
         * the state [State.ERROR].
         *
         * @param file info about the file, which chunk is requested
         * @return next chunk of the file
         */
        fun onNextChunkRequest(file: FileInfo): ByteArray? {
            return null
        }

        /**
         * Override this method to handle event when each chunk of a file was sent successfully.
         *
         * @param file           information about the file, which chunk was processed
         * @param processedBytes accumulated size of sent data
         */
        fun onFileChunkProcessed(file: FileInfo, processedBytes: Long) {
        }

        /**
         * Override this method to handle event when an error occurs while sending files.
         *
         * @param file      information about the file that caused the error
         * @param throwable exception that occurred while sending files
         */
        fun onErrorDuringSending(file: FileInfo, throwable: Throwable) {
        }

        /**
         * Override this method to handle event when an error occurs while creating entry.
         *
         * @param throwable exception that occurred while creating an entry
         */
        fun onError(throwable: Throwable) {
        }

        /**
         * Override this method to handle event when stream state has been updated.
         *
         * @param currentState current state of the stream
         */
        fun onUpdateState(currentState: State) {
        }
    }

    companion object {

        /**
         * Creates an [InboxEntryStream] instance ready for streaming, with optional files and encryption.
         *
         * This method initializes an [InboxEntryStream] and prepares it for sending an entry with the provided data,
         * optional associated files, and optional encryption using the sender's private key. It creates an Inbox handle
         * and initializes file handles for any associated files. The initial state of the stream is determined based
         * on the presence of files: if no files are provided, the state is set to [State.FILES_SENT]. Otherwise,
         * it's set to [State.PREPARED], indicating readiness for file transfer.
         *
         * @param inboxApi            reference to Inbox API
         * @param inboxId             ID of the Inbox
         * @param entryStreamListener the listener for stream state changes
         * @param data                entry data to send
         * @param fileInfos           information about each entry's file to send
         * @param userPrivKey         sender's private key which can be used later to encrypt data for that sender
         * @return instance of [InboxEntryStream] prepared for streaming
         * @throws PrivmxException       when method encounters an exception while creating handles for Inbox or file
         * @throws NativeException       when method encounters an unknown exception while creating handles for Inbox or file
         * @throws IllegalStateException when [IllegalStateException] was thrown while creating handles for Inbox or file
         */
        @Throws(
            PrivmxException::class, NativeException::class, IllegalStateException::class
        )
        @JvmOverloads
        fun prepareEntry(
            inboxApi: InboxApi,
            inboxId: String,
            entryStreamListener: EntryStreamListener,
            data: ByteArray = ByteArray(0),
            fileInfos: List<FileInfo> = emptyList(),
            userPrivKey: String? = null
        ): InboxEntryStream {
            val files = mapOf(
                *(fileInfos.map {
                    it to InboxFileStreamWriter.createFile(
                        inboxApi, it.publicMeta, it.privateMeta, it.fileSize
                    )
                }).toTypedArray()
            )
            val fileHandles = files.values.map(InboxFileStreamWriter::fileHandle)
            val inboxHandle = inboxApi.prepareEntry(inboxId, data, fileHandles, userPrivKey)
            return InboxEntryStream(
                inboxApi, files, inboxHandle!!, entryStreamListener
            )
        }
    }
}
