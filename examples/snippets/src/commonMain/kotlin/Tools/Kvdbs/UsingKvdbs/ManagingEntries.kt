package Tools.Kvdbs.UsingKvdbs

import com.simplito.kotlin.privmx_endpoint.model.KvdbEntry
import com.simplito.kotlin.privmx_endpoint_extra.model.SortOrder

fun sendingEntries() {
    val kvdbID = "KVDB_ID"
    val kvdbEntryKey = "KVDB_ENTRY_KEY"
    val privateMeta: ByteArray = "My private data".encodeToByteArray()
    val publicMeta: ByteArray = "My public data".encodeToByteArray()
    val entryData: ByteArray = "Entry Data".encodeToByteArray()

    // creating entry
    endpointSession.kvdbApi?.setEntry(
        kvdbID,
        kvdbEntryKey,
        publicMeta,
        privateMeta,
        entryData
    )

    // read created entry
    val entry = endpointSession.kvdbApi?.getEntry(
        kvdbID,
        kvdbEntryKey
    )
}

fun updateEntry() {
    val kvdbID = "KVDB_ID"
    val kvdbEntryKey = "KVDB_ENTRY_KEY"
    val newEntryData: ByteArray = "New Entry Data".encodeToByteArray()

    endpointSession.kvdbApi?.let { kvdbApi ->
        val entry: KvdbEntry = kvdbApi.getEntry(kvdbID, kvdbEntryKey)

        kvdbApi.setEntry(
            kvdbID,
            kvdbEntryKey,
            entry.publicMeta,
            entry.privateMeta,
            newEntryData,
            entry.version!!
        )
    }
}

fun listEntries() {
    val kvdbId = "KVDB_ID"
    val startIndex: Long = 0
    val pageSize: Long = 30

    val entries: List<KvdbEntry> = endpointSession.kvdbApi!!.listEntries(
        kvdbId,
        startIndex,
        pageSize,
        SortOrder.DESC
    ).readItems
}

fun deleteEntry() {
    val kvdbID = "KVDB_ID"
    val kvdbEntryKey = "KVDB_ENTRY_KEY"

    endpointSession.kvdbApi?.deleteEntry(kvdbID, kvdbEntryKey)
}
