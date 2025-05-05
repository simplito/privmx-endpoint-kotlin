package Tools.Stores.UsingStores

import com.simplito.kotlin.privmx_endpoint_extra.storeFileStream.StoreFileStream
import com.simplito.kotlin.privmx_endpoint_extra.storeFileStream.StoreFileStreamReader
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

fun downloadingFiles() {
    val fileId = "FILE_ID"
    val controller: StoreFileStream.Controller = object : StoreFileStream.Controller() {
        override fun onChunkProcessed(processedBytes: Long) {
            println("Downloaded bytes: $processedBytes")
        }
    }

    SystemFileSystem.sink(Path("PATH_TO_FILE")).use { sink ->
        StoreFileStreamReader.openFile(
            endpointSession.storeApi!!,
            fileId,
            sink.buffered(),
            controller
        )
    }
}

