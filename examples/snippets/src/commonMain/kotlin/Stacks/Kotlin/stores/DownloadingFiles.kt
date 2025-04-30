package Stacks.Kotlin.stores

import com.simplito.kotlin.privmx_endpoint_extra.storeFileStream.StoreFileStream
import com.simplito.kotlin.privmx_endpoint_extra.storeFileStream.StoreFileStreamReader
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

fun downloadFileBasic() {
    val fileID = "FILE_ID"
    var data = ByteArray(0)

    val closedFileID = StoreFileStreamReader.openFile(
        storeApi,
        fileID,
    ).also {
        do {
            val chunk = it.read(StoreFileStream.OPTIMAL_SEND_SIZE)
            data += chunk
        } while (chunk.size.toLong() == StoreFileStream.OPTIMAL_SEND_SIZE)
    }.close()
}

fun downloadFileUsingStreams() {
    val fileID = "FILE_ID"
    val sink: Sink = SystemFileSystem.sink(Path("PATH_TO_FILE")).buffered()
    val streamController = object: StoreFileStream.Controller(){
        override fun onChunkProcessed(processedBytes: Long) {
            super.onChunkProcessed(processedBytes)
            println("Full read size: $processedBytes")
        }
    }

    val closedFileID = StoreFileStreamReader.openFile(
        storeApi,
        fileID,
        sink,
        streamController
    )
}