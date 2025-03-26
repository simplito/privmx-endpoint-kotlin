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
package com.simplito.java.privmx_endpoint_extra.inboxEntryStream

import com.simplito.java.privmx_endpoint.model.exceptions.NativeException
import com.simplito.java.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.java.privmx_endpoint.modules.inbox.InboxApi
import com.simplito.java.privmx_endpoint_extra.inboxFileStream.InboxFileStreamWriter
import com.simplito.java.privmx_endpoint_extra.storeFileStream.StoreFileStream
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.io.IOException
import kotlinx.io.Source
import kotlin.jvm.Synchronized

/**
 * Provides a streamlined process for creating and sending Inbox entries
 * with optional file attachments.
 *
 *  This class simplifies interacting with the Inbox API for sending entries,
 * especially when dealing with multiple files. It manages the lifecycle of the entry creation
 * process, including file uploads and final entry submission.
 *
 * @category inbox
 */
class InboxEntryStream private constructor(
    api: InboxApi,
    inboxFiles: Map<FileInfo, InboxFileStreamWriter>,
    inboxHandle: Long,
    entryStreamListener: EntryStreamListener
) {
    private val inboxApi: InboxApi
    private val inboxFiles: Map<FileInfo, InboxFileStreamWriter>
    private val inboxHandle: Long
    private val entryStreamListener: EntryStreamListener
    private val sendingFiles: ArrayList<Deferred<*>> = ArrayList()
    private var streamState = State.PREPARED

    init {
        this.inboxApi = api
        this.inboxFiles = inboxFiles
        this.inboxHandle = inboxHandle
        this.entryStreamListener = entryStreamListener
        if (inboxFiles.isEmpty()) {
            streamState = State.FILES_SENT
        }
    }

    /**
     * Initiates the process of sending files using the provided executor.
     *
     * This method submits each file for sending to the `fileStreamExecutor`
     * and wait for completion.
     *
     * @param fileStreamExecutor the executor service responsible for executing file sending tasks
     * @throws IllegalStateException If the stream is not in the [State.PREPARED] state.
     */
    @Synchronized
    @Throws(IllegalStateException::class)
    fun sendFiles(
        fileStreamExecutor: CoroutineDispatcher = Dispatchers.Default
    ) {
        check(streamState == State.PREPARED) { "Stream should be in state PREPARED. Current state is: " + streamState.name }
        val coroutineScope = CoroutineScope(fileStreamExecutor)
        inboxFiles.forEach { (fileInfo: FileInfo?, fileHandle: InboxFileStreamWriter?) ->
            sendingFiles.add(
                coroutineScope.async() {
                    try {
                        sendFile(fileInfo, fileHandle)
                        entryStreamListener.onEndFileSending(fileInfo)
                    } catch (e: Exception) {
                        stopFileStreams()
                        onError(e)
                        entryStreamListener.onErrorDuringSending(fileInfo, e)
                    }
                }
            )

        }

        runBlocking {
        for (job in sendingFiles) {
            try {
                job.await()
            } catch (e: InterruptedException) {
                // catch when in async mode someone call cancel on result Future.
                cancel()
                return@runBlocking
            } catch (e: concurrent.CancellationException) {
                cancel()
                return@runBlocking
            } catch (ignore: Exception) {
            }
        }
        if (sendingFiles.all { it.isCompleted }) {
            updateState(State.FILES_SENT)
        } else {
            onError(IllegalStateException("Some files cannot be sent"))
        }  }
    }

    /**
     * Sends files using a single-threaded executor (see: [Executors.newSingleThreadExecutor]).
     *
     * @throws IllegalStateException when this stream is not in state [State.PREPARED].
     */
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    fun sendFiles() {
        newSingleThreadContext("").use{
            sendFiles(it)
        }
    }

    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class,
        IOException::class
    )
    private fun sendFile(
        fileInfo: FileInfo,
        fileHandle: InboxFileStreamWriter
    ) {
        val controller: StoreFileStream.Controller = object : StoreFileStream.Controller() {
            override fun onChunkProcessed(processedBytes: Long) {
                entryStreamListener.onFileChunkProcessed(fileInfo, processedBytes)
//                if (java.lang.Thread.interrupted()) {
//                    this.stop()
//                }
            }
        }

        entryStreamListener.onStartFileSending(fileInfo)
        if (fileInfo.fileStream == null) {
            fileHandle.setProgressListener(controller)
            while (fileInfo.fileSize > fileHandle.processedBytes && !fileHandle.isClosed) {
                if (controller.isStopped()) {
                    break
                }
                val nextChunk = entryStreamListener.onNextChunkRequest(fileInfo)
                    ?: throw NullPointerException("Data chunk cannot be null")
                fileHandle.write(inboxHandle, nextChunk)
            }
        } else {
            fileHandle.writeStream(inboxHandle, fileInfo.fileStream, controller)
        }
    }

    @Synchronized
    private fun onError(t: Throwable) {
        stopFileStreams()
        closeFileHandles()
        updateState(State.ERROR)
        entryStreamListener.onError(t)
    }

    private fun updateState(newState: State) {
        if (streamState != newState) {
            synchronized(this) {
                streamState = newState
                entryStreamListener.onUpdateState(streamState)
            }
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
        if (streamState == State.ERROR) return
        if (streamState == State.ABORTED) return
        if (streamState == State.SENT) return
        synchronized(this) {
            stopFileStreams()
            closeFileHandles()
            updateState(State.ABORTED)
        }
    }

    private fun stopFileStreams() {
        if (streamState == State.PREPARED && !sendingFiles.isEmpty()) {
            synchronized(streamState) {
                sendingFiles.forEach{ it.cancel() }
            }
        }
    }

    private fun closeFileHandles() {
        synchronized(inboxFiles) {
            inboxFiles.values.forEach(function.Consumer<InboxFileStreamWriter> { file: InboxFileStreamWriter ->
                try {
                    if (!file.isClosed) {
                        file.close()
                    }
                } catch (ignore: Exception) {
                }
            })
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
    @Synchronized
    @Throws(
        PrivmxException::class,
        NativeException::class,
        IllegalStateException::class
    )
    fun sendEntry() {
        check(streamState == State.FILES_SENT) { "Stream should be in state FILES_SENT. Current state is: " + streamState.name }
        try {
            inboxApi.sendEntry(inboxHandle)
            updateState(State.SENT)
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
    class FileInfo(
        /**
         * Byte array of any arbitrary metadata that can be read by anyone.
         */
        val publicMeta: ByteArray,
        /**
         * Byte array of any arbitrary metadata that will be encrypted before sending.
         */
        val privateMeta: ByteArray,
        /**
         * The total size of the file data.
         */
        val fileSize: Long,
        fileStream: Source?
    ) {
        /**
         * An optional [InputStream] providing the file data.
         *
         * If this value is `null`, the stream will call
         * [EntryStreamListener.onNextChunkRequest] to request chunks of data
         * for sending.
         */
        val fileStream: InputStream? = fileStream
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
        fun onStartFileSending(file: FileInfo?) {
        }

        /**
         * Override this method to handle when file was sent successfully.
         *
         * @param file information about the sent file
         */
        fun onEndFileSending(file: FileInfo?) {
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
         * Creates [InboxEntryStream] instance ready for streaming.
         *
         *  This method initializes an [InboxEntryStream] and prepares it for sending
         * an entry with the provided data. It creates an Inbox handle and sets the
         * initial state of the stream to [State.FILES_SENT].
         *
         * @param inboxApi            reference to Inbox API
         * @param inboxId             ID of the Inbox
         * @param entryStreamListener the listener for stream state changes
         * @param data                entry data to send
         * @return instance of [InboxEntryStream] prepared for streaming
         * @throws PrivmxException       when method encounters an exception while creating handles for Inbox or file
         * @throws NativeException       when method encounters an unknown exception while creating handles for Inbox or file
         * @throws IllegalStateException when [IllegalStateException] was thrown while creating handles for Inbox or file
         */
        @Throws(
            PrivmxException::class,
            NativeException::class,
            IllegalStateException::class
        )
        fun prepareEntry(
            inboxApi: InboxApi,
            inboxId: String?,
            entryStreamListener: EntryStreamListener,
            data: ByteArray?
        ): InboxEntryStream {
            return prepareEntry(inboxApi, inboxId, entryStreamListener, data, null)
        }

        /**
         * Creates [InboxEntryStream] instance ready for streaming.
         *
         * This method initializes an [InboxEntryStream] and prepares it for sending an entry with the
         * associated files and empty data. It creates Inbox and file handles, setting the initial state of the stream
         * to [State.PREPARED], indicating readiness for file transfer.
         *
         * @param inboxApi            reference to Inbox API
         * @param inboxId             ID of the Inbox
         * @param entryStreamListener the listener for stream state changes
         * @param fileInfos           information about each entry's file to send
         * @return instance of [InboxEntryStream] prepared for streaming
         * @throws PrivmxException       when method encounters an exception while creating handles for Inbox or file
         * @throws NativeException       when method encounters an unknown exception while creating handles for Inbox or file
         * @throws IllegalStateException when [IllegalStateException] was thrown while creating handles for Inbox or file
         */
        @Throws(
            PrivmxException::class,
            NativeException::class,
            IllegalStateException::class
        )
        fun prepareEntry(
            inboxApi: InboxApi,
            inboxId: String?,
            entryStreamListener: EntryStreamListener,
            fileInfos: List<FileInfo?>?
        ): InboxEntryStream {
            return prepareEntry(
                inboxApi,
                inboxId,
                entryStreamListener,
                "".encodeToByteArray(),
                fileInfos
            )
        }

        /**
         * Creates an [InboxEntryStream] instance ready for streaming, with optional files and encryption.
         *
         * This method initializes an [InboxEntryStream] and prepares it for sending an entry with
         * the provided data and optional associated files. It creates Inbox and file handles (if
         * `fileInfos` is provided), setting the initial state of the stream to
         * [State.PREPARED], indicating readiness for data and file transfer.
         *
         * @param inboxApi            reference to Inbox API
         * @param inboxId             ID of the Inbox
         * @param entryStreamListener the listener for stream state changes
         * @param data                entry data to send
         * @param fileInfos           information about each entry's file to send
         * @return instance of [InboxEntryStream] prepared for streaming
         * @throws PrivmxException       when method encounters an exception while creating handles for Inbox or file
         * @throws NativeException       when method encounters an unknown exception while creating handles for Inbox or file
         * @throws IllegalStateException when [IllegalStateException] was thrown while creating handles for Inbox or file
         */
        @Throws(
            PrivmxException::class,
            NativeException::class,
            IllegalStateException::class
        )
        fun prepareEntry(
            inboxApi: InboxApi,
            inboxId: String?,
            entryStreamListener: EntryStreamListener,
            data: ByteArray?,
            fileInfos: List<FileInfo?>?
        ): InboxEntryStream {
            return prepareEntry(inboxApi, inboxId, entryStreamListener, data, fileInfos, null)
        }

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
            PrivmxException::class,
            NativeException::class,
            IllegalStateException::class
        )
        fun prepareEntry(
            inboxApi: InboxApi,
            inboxId: String?,
            entryStreamListener: EntryStreamListener,
            data: ByteArray?,
            fileInfos: List<FileInfo?>?,
            userPrivKey: String?
        ): InboxEntryStream {
            val files: Map<FileInfo, InboxFileStreamWriter> =
                Optional.ofNullable<List<FileInfo>>(fileInfos)
                    .orElse(emptyList<FileInfo>())
                    .stream()
                    .collect<Map<FileInfo, InboxFileStreamWriter>, Any>(
                        stream.Collectors.toMap<Any, FileInfo, Any>(
                            function.Function<Any, FileInfo> { fileInfo: FileInfo? -> fileInfo },
                            function.Function<Any, Any> { config: FileInfo ->
                                InboxFileStreamWriter.createFile(
                                    inboxApi,
                                    config.publicMeta,
                                    config.privateMeta,
                                    config.fileSize
                                )
                            }
                        )
                    )
            val fileHandles: List<Long?> =
                files.values.stream().map<Long>(InboxFileStreamWriter::fileHandle)
                    .collect<List<Long>, Any>(stream.Collectors.toList<Long>())
            val inboxHandle = inboxApi.prepareEntry(inboxId, data, fileHandles, userPrivKey)
            return InboxEntryStream(
                inboxApi, files,
                inboxHandle!!, entryStreamListener
            )
        }
    }
}
