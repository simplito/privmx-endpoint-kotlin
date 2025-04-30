package Stacks.Kotlin.inboxes

import Stacks.Kotlin.bridgeUrl
import Stacks.Kotlin.solutionId
import com.simplito.java.privmx_endpoint.model.File
import com.simplito.java.privmx_endpoint.model.InboxEntry
import com.simplito.java.privmx_endpoint.modules.core.Connection
import com.simplito.java.privmx_endpoint.modules.inbox.InboxApi
import com.simplito.java.privmx_endpoint_extra.model.SortOrder
import com.simplito.java.privmx_endpoint_extra.storeFileStream.StoreFileStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class InboxPublicEntryData(
    val name: String,
    val surname: String,
    val email: String,
    val comment: String
)

private fun setupPublicInboxApi(value: InboxApi) {
    publicInboxApi = value
}

private lateinit var publicInboxApi: InboxApi

// START: Creating Public Connection snippets

fun createPublicConnection() {
    //Start snippet
    val connection = Connection.connectPublic(
        solutionId,
        bridgeUrl,
    )
    val publicInboxApi = InboxApi(connection)
    //End snippet

    setupPublicInboxApi(publicInboxApi)
}

// END: Creating Public Connection snippets


// START: Sending Entries snippets

fun sendInboxEntryBasic() {
    val inboxID = "INBOX_ID"
    val inboxPublicEntryData = InboxPublicEntryData(
        "NAME",
        "SURNAME",
        "EMAIL",
        "COMMENT"
    )

    publicInboxApi.prepareEntry(
        inboxID,
        Json.encodeToString(inboxPublicEntryData).encodeToByteArray(),
    ).let {
        publicInboxApi.sendEntry(it)
    }
}

fun sendInboxEntryWithFiles() {
//    1. Prepare handle to inbox files.
    val fileContents: List<ByteArray> = (0..5).map {
        "File content $it".encodeToByteArray()
    }
    val filePublicMeta = ByteArray(0)
    val filePrivateMeta = ByteArray(0)
    val fileHandles = fileContents.map {
        publicInboxApi.createFileHandle(
            filePublicMeta,
            filePrivateMeta,
            it.size.toLong()
        )
    }

    // 2. Prepare entry
    val inboxID = "INBOX_ID"
    val inboxPublicEntryData = InboxPublicEntryData(
        "NAME",
        "SURNAME",
        "EMAIL",
        "COMMENT"
    )

    val inboxHandle = publicInboxApi.prepareEntry(
        inboxID,
        Json.encodeToString(inboxPublicEntryData).encodeToByteArray(),
        fileHandles
    )
    // 3. Write files
    fileContents.zip(fileHandles).forEach { (content, fileHandle) ->
        publicInboxApi.writeToFile(inboxHandle, fileHandle, content)
    }

    // 4. Send Entry
    publicInboxApi.sendEntry(inboxHandle)
}

// END: Sending Entries snippets


// START: Getting Entries snippets

data class EntryItem(
    val entry: InboxEntry,
    val decodedData: InboxPublicEntryData
)

fun getMostRecentEntries() {
    val inboxID = "INBOX_ID"
    val startIndex = 0L
    val pageSize = 100L

    val entriesPagingList = inboxApi.listEntries(
        inboxID,
        startIndex,
        pageSize,
        SortOrder.DESC
    )

    val entries = entriesPagingList.readItems.map {
        EntryItem(
            it,
            Json.decodeFromString(it.data.decodeToString())
        )
    }
}

fun getOldestEntries() {
    val inboxID = "INBOX_ID"
    val startIndex = 0L
    val pageSize = 100L

    val entriesPagingList = inboxApi.listEntries(
        inboxID,
        startIndex,
        pageSize,
        SortOrder.ASC
    )

    val entries = entriesPagingList.readItems.map {
        EntryItem(
            it,
            Json.decodeFromString(it.data.decodeToString())
        )
    }
}
// END: Getting Entries snippets


// START: Reading Entries snippets

fun readingEntry() {
    val entryID = "ENTRY_ID"
    val inboxEntry = inboxApi.readEntry(entryID)

    val files: List<File> = inboxEntry.files
    val filesContent: List<ByteArray> = files.map { file ->
        inboxApi.openFile(file.info.fileId)
            .let { fileHandle ->
                var fileContent = ByteArray(0)
                do {
                    val chunk = inboxApi.readFromFile(fileHandle, StoreFileStream.OPTIMAL_SEND_SIZE)
                    fileContent += chunk
                } while (chunk.size.toLong() == StoreFileStream.OPTIMAL_SEND_SIZE)
                inboxApi.closeFile(fileHandle)
                fileContent
            }
    }
}

// END: Reading Entries snippets
