package Tools.Stores.UsingStores

import com.simplito.java.privmx_endpoint_extra.storeFileStream.StoreFileStream
import com.simplito.java.privmx_endpoint_extra.storeFileStream.StoreFileStreamReader
import java.io.FileOutputStream

fun downloadingFiles() {
    val fileId = "FILE_ID"
    val controller: StoreFileStream.Controller = object : StoreFileStream.Controller() {
        override fun onChunkProcessed(processedBytes: Long) {
            println("Downloaded bytes: $processedBytes")
        }
    }

    FileOutputStream("PATH_TO_FILE").use { outputStream ->
        StoreFileStreamReader.openFile(
            endpointSession.storeApi,
            fileId,
            outputStream,
            controller
        )
    }
}

