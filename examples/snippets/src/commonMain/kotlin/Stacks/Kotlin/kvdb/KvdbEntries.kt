package Stacks.JavaKotlin.kvdb

import Stacks.Kotlin.kvdb.kvdbApi
import com.simplito.kotlin.privmx_endpoint.model.KvdbEntry
import com.simplito.kotlin.privmx_endpoint_extra.model.SortOrder
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class KvdbEntryPublicMeta(
    val schemaVersion: Long,
    val dataType: String,
    val mimeType: String
)
@Serializable
data class ContainersArray(val containersIds: List<String>)

data class KvdbEntryItem(
    val kvdbEntry: KvdbEntry,
    val decodedData: String,
    val decodedPublicMeta: KvdbEntryPublicMeta
)

// START: Setting(Creating) Entries snippets
fun settingKvdbEntryPlainData() {
    val kvdbId = "KVDB_ID"
    val kvdbEntryKey = "KVDB_ENTRY_KEY"
    val kvdbEntryData = "Plain Data"
    val publicMeta = ByteArray(0)
    val privateMeta = ByteArray(0)

    val entryId = kvdbApi.setEntry(
        kvdbId,
        kvdbEntryKey,
        publicMeta,
        privateMeta,
        kvdbEntryData.encodeToByteArray()
    )
}

fun settingKvdbEntryWithPublicMeta() {
    val kvdbId = "KVDB_ID"
    val kvdbEntryKey = "KVDB_ENTRY_KEY"
    val kvdbEntryData = ContainersArray(
        listOf(
            "THREAD_ID_1",
            "THREAD_ID_2",
            "THREAD_ID_3"
        )
    )
    val publicMeta = KvdbEntryPublicMeta(
        schemaVersion = 1,
        dataType = "favourite-threads",
        mimeType = "application/json"
    )
    val privateMeta = ByteArray(0)

    val kvdbEntryId = kvdbApi.setEntry(
        kvdbId,
        kvdbEntryKey,
        Json.encodeToString(publicMeta).encodeToByteArray(),
        privateMeta,
        Json.encodeToString(kvdbEntryData).encodeToByteArray()
    )
}
// END: Setting Entries snippets

// START: Updating Entries snippets
fun updatingKvdbEntry() {
    val kvdbId = "KVDB_ID"
    val kvdbEntryKey = "KVDB_ENTRY_KEY"
    val kvdbEntry: KvdbEntry = kvdbApi.getEntry(kvdbId, kvdbEntryKey)
    val newKvdbEntryData = "New data"

    kvdbApi.setEntry(
        kvdbId,
        kvdbEntryKey,
        kvdbEntry.publicMeta,
        kvdbEntry.privateMeta,
        newKvdbEntryData.encodeToByteArray(),
        kvdbEntry.version!! + 1
    )
}
// END: Updating Entries snippets

// START: Getting Entries snippets
fun getMostRecentKvdbEntries() {
    val kvdbId = "KVDB_ID"
    val startIndex = 0L
    val pageSize = 100L

    val kvdbEntriesPagingList = kvdbApi.listEntries(
        kvdbId,
        startIndex,
        pageSize,
        SortOrder.DESC
    )

    val kvdbEntries = kvdbEntriesPagingList.readItems.map {
        KvdbEntryItem(
            it,
            it.data.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

fun getOldestKvdbEntries() {
    val kvdbId = "KVDB_ID"
    val startIndex = 0L
    val pageSize = 100L

    val kvdbEntriesPagingList = kvdbApi.listEntries(
        kvdbId,
        startIndex,
        pageSize,
        SortOrder.ASC
    )

    val kvdbEntries = kvdbEntriesPagingList.readItems.map {
        KvdbEntryItem(
            it,
            it.data.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

fun getKvdbEntryById() {
    val kvdbId = "KVDB_ID"
    val kvdbEntryKey = "KVDB_ENTRY_KEY"

    val kvdbEntryItem = kvdbApi.getEntry(kvdbId, kvdbEntryKey).let {
        KvdbEntryItem(
            it,
            it.data.decodeToString(),
            Json.decodeFromString(it.publicMeta.decodeToString())
        )
    }
}

fun listEntriesKeys() {
    val kvdbId = "KVDB_ID"
    val startIndex = 0L
    val pageSize = 100L

    val entriesKeys = kvdbApi.listEntriesKeys(
        kvdbId,
        startIndex,
        pageSize,
        SortOrder.ASC
    )
}
// END: Getting Entries snippets

// START: Checking if KVDB entry exists
fun checkingIfHasEntry() {
    val kvdbId = "KVDB_ID"
    val kvdbEntryKey = "KVDB_ENTRY_KEY"

    val hasEntry: Boolean = kvdbApi.hasEntry(kvdbId, kvdbEntryKey)
}
// END: Checking if KVDB entry exists

// START: Deleting Entries snippets
fun deletingKvdbEntry() {
    val kvdbId = "KVDB_ID"
    val kvdbEntryKey = "KVDB_ENTRY_KEY"
    kvdbApi.deleteEntry(kvdbId, kvdbEntryKey)
}

fun deletingKvdbEntries() {
    val kvdbId = "KVDB_ID"
    val keys = setOf<String>(
        "KVDB_ENTRY_KEY_1",
        "KVDB_ENTRY_KEY_2"
    )

    kvdbApi.deleteEntries(kvdbId, keys)
}
// END: Deleting Entries snippets