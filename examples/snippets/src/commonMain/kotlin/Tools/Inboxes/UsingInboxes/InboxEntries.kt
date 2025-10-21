package Tools.Inboxes.UsingInboxes

import com.simplito.kotlin.privmx_endpoint.model.File
import com.simplito.kotlin.privmx_endpoint.model.InboxEntry
import com.simplito.kotlin.privmx_endpoint_extra.inboxFileStream.InboxFileStreamWriter
import com.simplito.kotlin.privmx_endpoint_extra.model.SortOrder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun sendingInboxEntryBasic() {
    // 1. Preparing Entry
    val inboxID = "INBOX_ID"

    val entryDataAnswer = "USER_PROVIDED_TEXT"
    val entryDataVersion = 1L
    val entryDataType = "TEXT_ANSWER"

    val entryData: ByteArray = """
                 {
                    "content": {
                        "answer": "$entryDataAnswer"
                    },
                    "version": $entryDataVersion,
                    "type": "$entryDataType"
                 }
                
                """.trimIndent().encodeToByteArray()

    val entryHandle = endpointSession.inboxApi?.prepareEntry(
        inboxID,
        entryData,
        emptyList()
    )!!

    // 2. Sending Entry
    endpointSession.inboxApi?.sendEntry(entryHandle)
}

fun attachingFiles(fileID: String?) {
    // 1. Preparing Entry & sending File Contents
    val inboxID = "INBOX_ID"
    val publicMeta: ByteArray = "My public data".encodeToByteArray()

    val fileName = "FILE_NAME"
    val fileType = "FILE_TYPE"
    val fileContent: ByteArray = "FILE_CONTENT".encodeToByteArray()
    val fileSize = fileContent.size.toLong()

    val entryDataAnswer = "USER_PROVIDED_TEXT"
    val entryDataVersion = 1L
    val entryDataType = "TEXT_ANSWER"

    val entryData: ByteArray = """
                 {
                    "content": {
                        "answer": "$entryDataAnswer"
                    },
                    "version": $entryDataVersion,
                    "type": "$entryDataType"
                 }
                
                """.trimIndent().encodeToByteArray()

    val filePrivateMeta: ByteArray = """
                {
                    "name": "$fileName",
                    "mimetype": "$fileType"
                }
                
                """.trimIndent().encodeToByteArray()

    val inboxFileStreamWriter: InboxFileStreamWriter = InboxFileStreamWriter.createFile(
        endpointSession.inboxApi!!,
        publicMeta,
        filePrivateMeta,
        fileSize
    )

    val fileHandle = inboxFileStreamWriter.fileHandle
    val inboxHandle = endpointSession.inboxApi?.prepareEntry(
        inboxID,
        entryData,
        listOf(fileHandle)
    )!!

    // 2. Sending File Contents
    inboxFileStreamWriter.write(
        inboxHandle,
        fileContent
    )

    // 3. Sending Entry
    endpointSession.inboxApi?.sendEntry(inboxHandle)
}

fun fetchingEntriesBasic() {
    val inboxID = "INBOX_ID"
    val startIndex = 0L
    val pageSize = 100L

    val entriesPagingList = endpointSession.inboxApi?.listEntries(
        inboxID,
        startIndex,
        pageSize,
        SortOrder.DESC
    )
}

fun fetchingEntriesWithFiles() {
    val inboxID = "INBOX_ID"
    val startIndex = 0L
    val pageSize = 1L

    // getting last entry
    val entriesPagingList = endpointSession.inboxApi?.listEntries(
        inboxID,
        startIndex,
        pageSize,
        SortOrder.DESC
    )!!

    val entry: InboxEntry = entriesPagingList.readItems[0]
    val entryFile: File = entry.files[0]

    // decoded privateMeta
    val privateMeta = entryFile.privateMeta.decodeToString()
    val privateMetaJson = Json.parseToJsonElement(privateMeta).jsonObject
    val fileName = privateMetaJson["name"]?.jsonPrimitive?.content
    val fileType = privateMetaJson["mimetype"]?.jsonPrimitive?.content

    // decoded data
    val entryData = entry.data.decodeToString()
    val entryDataJson = Json.parseToJsonElement(entryData).jsonObject
    val answer = entryDataJson["content"]!!
        .jsonObject["answer"]!!
        .jsonPrimitive.content
}
