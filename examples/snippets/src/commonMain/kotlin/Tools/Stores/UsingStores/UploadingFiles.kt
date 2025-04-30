package Tools.Stores.UsingStores

import com.simplito.java.privmx_endpoint_extra.storeFileStream.StoreFileStream
import com.simplito.java.privmx_endpoint_extra.storeFileStream.StoreFileStreamWriter
import java.io.FileInputStream

fun uploadingSmallFiles() {
    val storeID = "STORE_ID"
    val publicMeta = "File public meta".encodeToByteArray()
    val privateMeta = "File private meta".encodeToByteArray()
    val fileContent = "Text file content".encodeToByteArray()

    val fileID = endpointSession.storeApi?.createFile(
        storeID,
        publicMeta,
        privateMeta,
        fileContent.size.toLong()
    )?.also { fileHandle ->
        endpointSession.storeApi?.writeToFile(fileHandle,fileContent)
        endpointSession.storeApi?.closeFile(fileHandle)
    }
}


fun uploadingFilesStream() {
    val storeID = "STORE_ID"
    val publicMeta = "File public meta".encodeToByteArray()
    val privateMeta = "File private meta".encodeToByteArray()
    val controller: StoreFileStream.Controller = object : StoreFileStream.Controller() {
        override fun onChunkProcessed(processedBytes: Long) {
            println("Uploaded bytes: $processedBytes")
        }
    }

    val fileId = FileInputStream("PATH_TO_FILE").use { inputStream ->
        StoreFileStreamWriter.createFile(
            endpointSession.storeApi,
            storeID,
            publicMeta,
            privateMeta,
            inputStream.available().toLong(),
            inputStream,
            controller
        )
    }
}