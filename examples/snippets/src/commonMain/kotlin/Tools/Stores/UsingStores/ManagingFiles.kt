package Tools.Stores.UsingStores

import com.simplito.java.privmx_endpoint_extra.model.SortOrder

fun listingFiles() {
    val storeID = "STORE_ID"
    val limit = 30L
    val skip = 0L

    val files = endpointSession.storeApi?.listFiles(
        storeID,
        skip,
        limit,
        SortOrder.DESC
    )
}

fun deletingFiles() {
    val fileID = "FILE_ID"
    endpointSession.storeApi?.deleteFile(fileID)
}