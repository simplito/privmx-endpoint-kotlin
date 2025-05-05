package Tools.Stores.UsingStores

import com.simplito.kotlin.privmx_endpoint_extra.storeFileStream.StoreFileStream
import com.simplito.kotlin.privmx_endpoint_extra.storeFileStream.StoreFileStreamWriter
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

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
    val filePath = Path("PATH_TO_FILE")
    val fileSize = SystemFileSystem.metadataOrNull(filePath)!!.size
    val fileId = SystemFileSystem.source(filePath).use { source ->
        StoreFileStreamWriter.createFile(
            endpointSession.storeApi!!,
            storeID,
            publicMeta,
            privateMeta,
            fileSize,
            source.buffered(),
            controller
        )
    }
}