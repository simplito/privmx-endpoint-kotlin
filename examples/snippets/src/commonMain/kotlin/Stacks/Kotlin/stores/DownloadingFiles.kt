package Stacks.Kotlin.stores

import com.simplito.java.privmx_endpoint_extra.storeFileStream.StoreFileStream
import com.simplito.java.privmx_endpoint_extra.storeFileStream.StoreFileStreamReader
import java.io.File
import java.io.OutputStream

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
    val file = File("PATH_TO_FILE")
    val outputStream: OutputStream = file.outputStream()
    val streamController = object: StoreFileStream.Controller(){
        override fun onChunkProcessed(processedBytes: Long?) {
            super.onChunkProcessed(processedBytes)
            println("Full read size: $processedBytes")
        }
    }

    val closedFileID = StoreFileStreamReader.openFile(
        storeApi,
        fileID,
        outputStream,
        streamController
    )
}