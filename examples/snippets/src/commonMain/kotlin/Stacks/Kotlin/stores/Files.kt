package Stacks.Kotlin.stores

import com.simplito.java.privmx_endpoint_extra.storeFileStream.StoreFileStream
import com.simplito.java.privmx_endpoint_extra.storeFileStream.StoreFileStreamWriter
import com.simplito.java.privmx_endpoint_extra.model.SortOrder
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.nio.file.Files

data class FileItem(
    val file: com.simplito.java.privmx_endpoint.model.File,
    val decodedPrivateMeta: FilePrivateMeta
)

// START: Uploading Files snippets

fun uploadFileBasic() {
    val storeID = "STORE_ID"
    val fileContent = "Text file content".encodeToByteArray()
    val publicMeta = ByteArray(0)
    val privateMeta = ByteArray(0)

    val fileId = StoreFileStreamWriter.createFile(
        storeApi,
        storeID,
        publicMeta,
        privateMeta,
        fileContent.size.toLong(),
    ).also {
        it.write(fileContent)
    }.close()
}

@Serializable
data class FilePrivateMeta(
    val name: String,
    val mimetype: String
)

fun uploadFileWithMeta() {
    val storeID = "STORE_ID"
    val fileContent = "Text file content".encodeToByteArray()
    val publicMeta = ByteArray(0)
    val privateMeta = FilePrivateMeta(
        name = "Example text file",
        mimetype = "text/plain"
    )

    val fileId = StoreFileStreamWriter.createFile(
        storeApi,
        storeID,
        publicMeta,
        Json.encodeToString(privateMeta).encodeToByteArray(),
        fileContent.size.toLong(),
    ).also {
        it.write(fileContent)
    }.close()
}

fun uploadFileUsingStreams() {
    val storeID = "STORE_ID"
    val file = File("PATH_TO_FILE")
    val inputStream: InputStream = file.inputStream()
    val publicMeta = ByteArray(0)
    val privateMeta = FilePrivateMeta(
        name = file.name,
        mimetype = Files.probeContentType(file.toPath())
    )
    val streamController = object : StoreFileStream.Controller() {
        override fun onChunkProcessed(processedBytes: Long?) {
            super.onChunkProcessed(processedBytes)
            println("Processed size: $processedBytes")
        }
    }

    val fileId = StoreFileStreamWriter.createFile(
        storeApi,
        storeID,
        publicMeta,
        Json.encodeToString(privateMeta).encodeToByteArray(),
        inputStream.available().toLong(),
        inputStream,
        streamController
    )
}

// END: Uploading Files snippets


// START: Getting Files snippets

fun getMostRecentFiles() {
    val storeId = "STORE_ID"
    val startIndex = 0L
    val pageSize = 100L

    val filesPagingList = storeApi.listFiles(
        storeId,
        startIndex,
        pageSize,
        SortOrder.DESC
    )

    val files = filesPagingList.readItems.map {
        FileItem(
            it,
            Json.decodeFromString(it.privateMeta.decodeToString())
        )
    }
}

fun getOldestFiles() {
    val storeId = "STORE_ID"
    val startIndex = 0L
    val pageSize = 100L

    val filesPagingList = storeApi.listFiles(
        storeId,
        startIndex,
        pageSize,
        SortOrder.ASC
    )

    val files = filesPagingList.readItems.map {
        FileItem(
            it,
            Json.decodeFromString(it.privateMeta.decodeToString())
        )
    }
}

fun getFileById() {
    val fileID = "FILE_ID"

    val fileItem = storeApi.getFile(fileID).let {
        FileItem(
            it,
            Json.decodeFromString(it.privateMeta.decodeToString())
        )
    }
}
// END: Getting Files snippets


// START: Managing Files snippets

fun changingFileName() {
    val fileID = "FILE_ID"
    val file = storeApi.getFile(fileID)
    var filePrivateMeta: FilePrivateMeta = Json.decodeFromString(file.privateMeta.decodeToString())
    filePrivateMeta = filePrivateMeta.copy(
        name = "New file name"
    )

    storeApi.updateFileMeta(
        fileID,
        file.publicMeta,
        Json.encodeToString(filePrivateMeta).encodeToByteArray(),
    )
}

fun overwritingFileContent() {
    val fileID = "FILE_ID"
    val newFile = File("PATH_TO_FILE")
    val inputStream: InputStream = newFile.inputStream()
    val streamController = object : StoreFileStream.Controller() {
        override fun onChunkProcessed(processedBytes: Long?) {
            super.onChunkProcessed(processedBytes)
            println("Processed size: $processedBytes")
        }
    }
    val file = storeApi.getFile(fileID)

    StoreFileStreamWriter.updateFile(
        storeApi,
        fileID,
        file.publicMeta,
        file.privateMeta,
        inputStream.available().toLong(),
        inputStream,
        streamController
    )
}

fun deletingFile() {
    val fileID = "FILE_ID"
    storeApi.deleteFile(fileID)
}
// END: Managing Files snippets