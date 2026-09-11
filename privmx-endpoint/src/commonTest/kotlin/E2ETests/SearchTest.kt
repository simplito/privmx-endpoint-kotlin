package E2ETests

import Utils.Queries.json1
import Utils.Queries.json2
import Utils.Queries.query10
import com.simplito.kotlin.privmx_endpoint.model.Document
import com.simplito.kotlin.privmx_endpoint.model.IndexMode
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.SearchIndex
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.kvdb.KvdbApi
import com.simplito.kotlin.privmx_endpoint.modules.lock.LockApi
import com.simplito.kotlin.privmx_endpoint.modules.search.SearchApi
import com.simplito.kotlin.privmx_endpoint.modules.store.StoreApi
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalStdlibApi::class)
class SearchTest : BaseTest() {

    private lateinit var storeApi: StoreApi
    private lateinit var kvdbApi: KvdbApi
    private lateinit var lockApi: LockApi
    private lateinit var searchApi: SearchApi

    private lateinit var indexId: String
    private lateinit var index2Id: String
    private lateinit var index3Id: String

    private val document1Name = "document_1"
    private val document2Name = "document_2"
    private val sharedTerm = "privmx"
    private val document1Content = "the quick brown fox jumps over the lazy dog $sharedTerm"
    private val document2Content = "lorem ipsum dolor sit amet $sharedTerm"
    private var document1Id: Long = 0
    private var document2Id: Long = 0

    @BeforeTest
    fun createConnection() {
        if (connection == null) {
            connection = connectAsUser(ConnectionType.User1, bridgeAddress)
        }
        storeApi = StoreApi(connection!!)
        kvdbApi = KvdbApi(connection!!)
        lockApi = LockApi(connection!!)
        searchApi = SearchApi(connection!!, storeApi, kvdbApi, lockApi)

        indexId = searchApi.createSearchIndex(
            contextId!!,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            IndexMode.WITH_CONTENT
        )
        index2Id = searchApi.createSearchIndex(
            contextId!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            IndexMode.WITH_CONTENT
        )
        index3Id = searchApi.createSearchIndex(
            contextId!!,
            users,
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            IndexMode.WITHOUT_CONTENT
        )

        val indexHandle = searchApi.openSearchIndex(indexId)
        searchApi.beginTransaction(indexHandle)
        document1Id = searchApi.addDocument(indexHandle, document1Name, document1Content)
        document2Id = searchApi.addDocument(indexHandle, document2Name, document2Content)
        searchApi.commit(indexHandle)
        searchApi.closeSearchIndex(indexHandle)
    }

    @AfterTest
    @Throws(Exception::class)
    fun closeConnection() {
        if (::searchApi.isInitialized) {
            if (::index3Id.isInitialized) searchApi.deleteSearchIndex(index3Id)
            if (::index2Id.isInitialized) searchApi.deleteSearchIndex(index2Id)
            if (::indexId.isInitialized) searchApi.deleteSearchIndex(indexId)
            searchApi.close()
        }
        if (::lockApi.isInitialized) lockApi.close()
        if (::kvdbApi.isInitialized) kvdbApi.close()
        if (::storeApi.isInitialized) storeApi.close()
        connection?.close()?.also {
            connection = null
        }
        try {
            connection2?.close()
        } finally {
            connection2 = null
        }
    }

    private inline fun <T> withIndexHandle(indexId: String, body: (Long) -> T): T {
        val indexHandle = searchApi.openSearchIndex(indexId)
        try {
            return body(indexHandle)
        } finally {
            searchApi.closeSearchIndex(indexHandle)
        }
    }

    @Test
    fun createSearchIndexIncorrectInputData() {
        // incorrect contextId
        assertFailsWith(PrivmxException::class) {
            searchApi.createSearchIndex(
                indexId,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                IndexMode.WITH_CONTENT
            )
        }

        // incorrect users
        assertFailsWith(PrivmxException::class) {
            searchApi.createSearchIndex(
                context2Id!!,
                incorrectUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                IndexMode.WITH_CONTENT
            )
        }

        // incorrect managers
        assertFailsWith(PrivmxException::class) {
            searchApi.createSearchIndex(
                context2Id!!,
                users,
                incorrectUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                IndexMode.WITH_CONTENT
            )
        }

        // no managers
        assertFailsWith(PrivmxException::class) {
            searchApi.createSearchIndex(
                context2Id!!,
                users,
                emptyUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                IndexMode.WITH_CONTENT
            )
        }

        // creator not in managers
        assertFailsWith(PrivmxException::class) {
            searchApi.createSearchIndex(
                context2Id!!,
                users.subList(0, 1),
                users.subList(1, 2),
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                IndexMode.WITH_CONTENT
            )
        }

        //2 same users
        assertFailsWith(PrivmxException::class) {
            searchApi.createSearchIndex(
                context2Id!!,
                sameUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                IndexMode.WITH_CONTENT
            )
        }

        //2 same managers
        assertFailsWith(PrivmxException::class) {
            searchApi.createSearchIndex(
                context2Id!!,
                users,
                sameUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                IndexMode.WITH_CONTENT
            )
        }
    }

    @Test
    fun createSearchIndexCorrectInputData() {
        lateinit var id: String

        // same users and managers
        assertDoesNotFail {
            id = searchApi.createSearchIndex(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                IndexMode.WITH_CONTENT
            )
        }
        assertTrue(id.isNotEmpty())

        // no users
        assertDoesNotFail {
            searchApi.createSearchIndex(
                context2Id!!,
                emptyUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                IndexMode.WITH_CONTENT
            )
        }

        // no publicMeta
        assertDoesNotFail {
            searchApi.createSearchIndex(
                context2Id!!,
                emptyUsers,
                users,
                ByteArray(0),
                privateMeta.encodeToByteArray(),
                IndexMode.WITH_CONTENT
            )
        }

        // no privateMeta
        assertDoesNotFail {
            searchApi.createSearchIndex(
                context2Id!!,
                emptyUsers,
                users,
                publicMeta.encodeToByteArray(),
                ByteArray(0),
                IndexMode.WITH_CONTENT
            )
        }

        // WITHOUT_CONTENT mode
        assertDoesNotFail {
            searchApi.createSearchIndex(
                context2Id!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                IndexMode.WITHOUT_CONTENT
            )
        }
    }

    @Test
    fun updateSearchIndexIncorrectInputData() {
        // incorrect indexId
        assertFailsWith(PrivmxException::class) {
            searchApi.updateSearchIndex(
                contextId!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                1L,
                false
            )
        }

        // incorrect version
        assertFailsWith(PrivmxException::class) {
            searchApi.updateSearchIndex(
                indexId,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                99L,
                false
            )
        }

        // incorrect users
        assertFailsWith(PrivmxException::class) {
            searchApi.updateSearchIndex(
                indexId,
                incorrectUsers,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                1L,
                false
            )
        }

        // no managers
        assertFailsWith(PrivmxException::class) {
            searchApi.updateSearchIndex(
                indexId,
                users,
                emptyUsers,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                1L,
                false
            )
        }
    }

    @Test
    fun updateSearchIndexCorrectInputData() {
        val newPublicMeta = "new_public"
        val newPrivateMeta = "new_private"
        lateinit var index: SearchIndex

        // add user2 as a user
        assertDoesNotFail {
            searchApi.updateSearchIndex(
                indexId,
                users,
                users.subList(0, 1),
                newPublicMeta.encodeToByteArray(),
                newPrivateMeta.encodeToByteArray(),
                1L,
                false
            )
            index = searchApi.getSearchIndex(indexId)
        }
        assertEquals(2, index.users.size)
        assertEquals(1, index.managers.size)
        assertEquals("2", index.version.toString())
        assertEquals(
            newPublicMeta.encodeToByteArray().toHexString(),
            index.publicMeta.toHexString()
        )
        assertEquals(
            newPrivateMeta.encodeToByteArray().toHexString(),
            index.privateMeta.toHexString()
        )

        // force update without a matching version
        assertDoesNotFail {
            searchApi.updateSearchIndex(
                indexId,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                99L,
                true
            )
            index = searchApi.getSearchIndex(indexId)
        }
        assertEquals("3", index.version.toString())

        // regenerate the key
        assertDoesNotFail {
            searchApi.updateSearchIndex(
                indexId,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                3L,
                false,
                true
            )
            index = searchApi.getSearchIndex(indexId)
        }
        assertEquals("4", index.version.toString())

        // the documents are still readable after a re-key
        withIndexHandle(indexId) { indexHandle ->
            lateinit var document: Document
            assertDoesNotFail {
                document = searchApi.getDocument(indexHandle, document1Id)
            }
            assertEquals(document1Name, document.name)
        }
    }

    @Test
    fun deleteSearchIndex() {
        // incorrect indexId
        assertFailsWith(PrivmxException::class) {
            searchApi.deleteSearchIndex(contextId!!)
        }

        // correct indexId
        assertDoesNotFail {
            searchApi.deleteSearchIndex(index3Id)
        }
        assertFailsWith(PrivmxException::class) {
            searchApi.getSearchIndex(index3Id)
        }

        // deleting twice
        assertFailsWith(PrivmxException::class) {
            searchApi.deleteSearchIndex(index3Id)
        }
    }

    @Test
    fun getSearchIndex() {
        lateinit var index: SearchIndex

        // incorrect indexId
        assertFailsWith(PrivmxException::class) {
            searchApi.getSearchIndex(contextId!!)
        }

        // correct indexId
        assertDoesNotFail { index = searchApi.getSearchIndex(indexId) }
        assertEquals(contextId!!, index.contextId)
        assertEquals(indexId, index.indexId)
        assertEquals(user1Id!!, index.creator)
        assertEquals(user1Id!!, index.lastModifier)
        assertEquals("1", index.version.toString())
        assertEquals(1, index.users.size)
        assertEquals(1, index.managers.size)
        assertEquals(user1Id!!, index.users[0])
        assertEquals(IndexMode.WITH_CONTENT, index.mode)
        assertEquals("0", index.statusCode.toString())
        assertEquals(
            publicMeta.encodeToByteArray().toHexString(),
            index.publicMeta.toHexString()
        )
        assertEquals(
            privateMeta.encodeToByteArray().toHexString(),
            index.privateMeta.toHexString()
        )

        // the WITHOUT_CONTENT index reports its own mode
        assertDoesNotFail { index = searchApi.getSearchIndex(index3Id) }
        assertEquals(IndexMode.WITHOUT_CONTENT, index.mode)
    }

    @Test
    fun listSearchIndexesIncorrectInputData() {
        // incorrect contextId
        assertFailsWith(PrivmxException::class) {
            searchApi.listSearchIndexes(indexId, 0, 10)
        }

        // limit < 0
        assertFailsWith(PrivmxException::class) {
            searchApi.listSearchIndexes(contextId!!, 0, -10)
        }

        // limit == 0
        assertFailsWith(PrivmxException::class) {
            searchApi.listSearchIndexes(contextId!!, 0, 0)
        }

        // incorrect sortOrder
        assertFailsWith(PrivmxException::class) {
            searchApi.listSearchIndexes(contextId!!, 0, 10, "wrong")
        }

        // incorrect lastId
        assertFailsWith(PrivmxException::class) {
            searchApi.listSearchIndexes(contextId!!, 0, 10, "desc", "wrong")
        }

        // incorrect queryAsJson
        assertFailsWith(PrivmxException::class) {
            searchApi.listSearchIndexes(contextId!!, 0, 10, "desc", null, "wrong")
        }

        // incorrect sortBy
        assertFailsWith(PrivmxException::class) {
            searchApi.listSearchIndexes(contextId!!, 0, 10, "desc", null, null, "wrong")
        }
    }

    @Test
    fun listSearchIndexesCorrectInputData() {
        lateinit var indexes: PagingList<SearchIndex>

        // all indexes in the context
        assertDoesNotFail {
            indexes = searchApi.listSearchIndexes(contextId!!, 0, 10)
        }
        assertEquals(3, indexes.readItems.size)
        assertEquals("3", indexes.totalAvailable.toString())

        // limit
        assertDoesNotFail {
            indexes = searchApi.listSearchIndexes(contextId!!, 0, 1)
        }
        assertEquals(1, indexes.readItems.size)
        assertEquals("3", indexes.totalAvailable.toString())
        assertEquals(index3Id, indexes.readItems[0].indexId)

        // skip
        assertDoesNotFail {
            indexes = searchApi.listSearchIndexes(contextId!!, 1, 10)
        }
        assertEquals(2, indexes.readItems.size)
        assertEquals(index2Id, indexes.readItems[0].indexId)

        // ascending
        assertDoesNotFail {
            indexes = searchApi.listSearchIndexes(contextId!!, 0, 10, "asc")
        }
        assertEquals(3, indexes.readItems.size)
        assertEquals(indexId, indexes.readItems[0].indexId)

        // lastId
        assertDoesNotFail {
            indexes = searchApi.listSearchIndexes(contextId!!, 0, 10, "asc", indexId)
        }
        assertEquals(2, indexes.readItems.size)
        assertEquals(index2Id, indexes.readItems[0].indexId)

        // sortBy
        assertDoesNotFail {
            indexes = searchApi.listSearchIndexes(contextId!!, 0, 10, "asc", null, null, "createDate")
        }
        assertEquals(3, indexes.readItems.size)
        assertEquals(indexId, indexes.readItems[0].indexId)
    }

    @Test
    fun openSearchIndex() {
        var indexHandle: Long = 0

        // incorrect indexId
        assertFailsWith(PrivmxException::class) {
            searchApi.openSearchIndex(contextId!!)
        }

        // correct indexId
        assertDoesNotFail {
            indexHandle = searchApi.openSearchIndex(indexId)
        }
        assertNotEquals(0L, indexHandle)

        assertDoesNotFail {
            searchApi.closeSearchIndex(indexHandle)
        }
    }

    @Test
    fun closeSearchIndex() {
        val indexHandle = searchApi.openSearchIndex(indexId)

        assertDoesNotFail {
            searchApi.closeSearchIndex(indexHandle)
        }

        // closing twice
        assertFailsWith(PrivmxException::class) {
            searchApi.closeSearchIndex(indexHandle)
        }
    }

    @Test
    fun addDocument() {
        withIndexHandle(indexId) { indexHandle ->
            var documentId: Long = 0
            lateinit var document: Document

            assertDoesNotFail {
                documentId = searchApi.addDocument(indexHandle, "document_3", "third document body")
            }
            assertNotEquals(0L, documentId)
            assertNotEquals(document1Id, documentId)

            assertDoesNotFail {
                document = searchApi.getDocument(indexHandle, documentId)
            }
            assertEquals("document_3", document.name)
            assertEquals("third document body", document.content)
            assertEquals(documentId, document.documentId)
        }
    }

    @Test
    fun updateDocument() {
        withIndexHandle(indexId) { indexHandle ->
            val newContent = "an entirely different body"
            lateinit var document: Document

            assertDoesNotFail {
                searchApi.updateDocument(
                    indexHandle,
                    Document(document1Id, document1Name, newContent)
                )
                document = searchApi.getDocument(indexHandle, document1Id)
            }
            assertEquals(document1Id, document.documentId)
            assertEquals(document1Name, document.name)
            assertEquals(newContent, document.content)

            // unknown documentId
            assertFailsWith(PrivmxException::class) {
                searchApi.beginTransaction(indexHandle)
                searchApi.updateDocument(indexHandle, Document(9999L, "nope", "nope"))
            }
            searchApi.rollback(indexHandle)
        }
    }

    @Test
    fun deleteDocument() {
        withIndexHandle(indexId) { indexHandle ->
            assertDoesNotFail {
                searchApi.deleteDocument(indexHandle, document2Id)
            }

            assertFailsWith(PrivmxException::class) {
                searchApi.getDocument(indexHandle, document2Id)
            }

            lateinit var documents: PagingList<Document>
            assertDoesNotFail {
                documents = searchApi.listDocuments(indexHandle, 0, 10)
            }
            assertEquals(1, documents.readItems.size)
        }
    }

    @Test
    fun getDocument() {
        withIndexHandle(indexId) { indexHandle ->
            lateinit var document: Document

            // unknown documentId
            assertFailsWith(PrivmxException::class) {
                searchApi.getDocument(indexHandle, 9999L)
            }

            // correct documentId
            assertDoesNotFail {
                document = searchApi.getDocument(indexHandle, document1Id)
            }
            assertEquals(document1Id, document.documentId)
            assertEquals(document1Name, document.name)
            assertEquals(document1Content, document.content)
        }
    }

    @Test
    fun listDocumentsIncorrectInputData() {
        withIndexHandle(indexId) { indexHandle ->
            // limit < 0
            assertFailsWith(PrivmxException::class) {
                searchApi.listDocuments(indexHandle, 0, -10)
            }

            // limit == 0
            assertFailsWith(PrivmxException::class) {
                searchApi.listDocuments(indexHandle, 0, 0)
            }

            // incorrect sortOrder
            assertFailsWith(PrivmxException::class) {
                searchApi.listDocuments(indexHandle, 0, 10, "wrong")
            }
        }
    }

    @Test
    fun listDocumentsCorrectInputData() {
        withIndexHandle(indexId) { indexHandle ->
            lateinit var documents: PagingList<Document>

            // both seeded documents
            assertDoesNotFail {
                documents = searchApi.listDocuments(indexHandle, 0, 10)
            }
            assertEquals(2, documents.readItems.size)
            assertEquals("2", documents.totalAvailable.toString())

            // limit
            assertDoesNotFail {
                documents = searchApi.listDocuments(indexHandle, 0, 1)
            }
            assertEquals(1, documents.readItems.size)
            assertEquals("2", documents.totalAvailable.toString())

            // skip
            assertDoesNotFail {
                documents = searchApi.listDocuments(indexHandle, 1, 10)
            }
            assertEquals(1, documents.readItems.size)

            // ascending
            assertDoesNotFail {
                documents = searchApi.listDocuments(indexHandle, 0, 10, "asc")
            }
            assertEquals(2, documents.readItems.size)
            assertEquals(document1Id, documents.readItems[0].documentId)
        }
    }

    @Test
    fun searchDocuments() {
        withIndexHandle(indexId) { indexHandle ->
            lateinit var documents: PagingList<Document>

            // a term present only in document1
            assertDoesNotFail {
                documents = searchApi.searchDocuments(indexHandle, "brown", 0, 10)
            }
            assertEquals(1, documents.readItems.size)
            assertEquals(document1Id, documents.readItems[0].documentId)

            // a term present only in document2
            assertDoesNotFail {
                documents = searchApi.searchDocuments(indexHandle, "ipsum", 0, 10)
            }
            assertEquals(1, documents.readItems.size)
            assertEquals(document2Id, documents.readItems[0].documentId)

            // a term present in both documents
            assertDoesNotFail {
                documents = searchApi.searchDocuments(indexHandle, sharedTerm, 0, 10)
            }
            assertEquals(2, documents.readItems.size)
            assertEquals("2", documents.totalAvailable.toString())
            assertEquals(
                listOf(document1Id, document2Id).sorted(),
                documents.readItems.map { it.documentId }.sorted()
            )

            // a term present in both documents, paged
            assertDoesNotFail {
                documents = searchApi.searchDocuments(indexHandle, sharedTerm, 0, 1)
            }
            assertEquals(1, documents.readItems.size)
            assertEquals("2", documents.totalAvailable.toString())

            assertDoesNotFail {
                documents = searchApi.searchDocuments(indexHandle, sharedTerm, 1, 10)
            }
            assertEquals(1, documents.readItems.size)
            assertEquals("2", documents.totalAvailable.toString())

            // a term present in neither
            assertDoesNotFail {
                documents = searchApi.searchDocuments(indexHandle, "unmatchable", 0, 10)
            }
            assertEquals(0, documents.readItems.size)
        }
    }

    @Test
    fun transactionRollback() {
        withIndexHandle(indexId) { indexHandle ->
            lateinit var documents: PagingList<Document>

            // a rolled back document never lands
            assertDoesNotFail {
                searchApi.beginTransaction(indexHandle)
                searchApi.addDocument(indexHandle, "rolled_back", "rolled back body")
                searchApi.rollback(indexHandle)
                documents = searchApi.listDocuments(indexHandle, 0, 10)
            }
            assertEquals(2, documents.readItems.size)

            // and the index is still writable afterwards
            assertDoesNotFail {
                searchApi.beginTransaction(indexHandle)
                searchApi.addDocument(indexHandle, "committed", "committed body")
                searchApi.commit(indexHandle)
                documents = searchApi.listDocuments(indexHandle, 0, 10)
            }
            assertEquals(3, documents.readItems.size)
        }
    }

    @Test
    fun documentOperationsIncorrectHandle() {
        val incorrectHandle = 9999L

        assertFailsWith(PrivmxException::class) {
            searchApi.beginTransaction(incorrectHandle)
        }
        assertFailsWith(PrivmxException::class) {
            searchApi.commit(incorrectHandle)
        }
        assertFailsWith(PrivmxException::class) {
            searchApi.rollback(incorrectHandle)
        }
        assertFailsWith(PrivmxException::class) {
            searchApi.addDocument(incorrectHandle, "name", "content")
        }
        assertFailsWith(PrivmxException::class) {
            searchApi.getDocument(incorrectHandle, document1Id)
        }
        assertFailsWith(PrivmxException::class) {
            searchApi.updateDocument(incorrectHandle, Document(document1Id, "name", "content"))
        }
        assertFailsWith(PrivmxException::class) {
            searchApi.deleteDocument(incorrectHandle, document1Id)
        }
        assertFailsWith(PrivmxException::class) {
            searchApi.listDocuments(incorrectHandle, 0, 10)
        }
        assertFailsWith(PrivmxException::class) {
            searchApi.searchDocuments(incorrectHandle, "brown", 0, 10)
        }
        assertFailsWith(PrivmxException::class) {
            searchApi.closeSearchIndex(incorrectHandle)
        }
    }

    @Test
    fun otherCannotReadSearchIndex() {
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val storeApi2 = StoreApi(connection2!!)
        val kvdbApi2 = KvdbApi(connection2!!)
        val lockApi2 = LockApi(connection2!!)
        val searchApi2 = SearchApi(connection2!!, storeApi2, kvdbApi2, lockApi2)

        // user2 is not a member of indexId
        assertFailsWith(PrivmxException::class) {
            searchApi2.getSearchIndex(indexId)
        }
        assertFailsWith(PrivmxException::class) {
            searchApi2.openSearchIndex(indexId)
        }

        // user2 is a member and a manager of index2Id
        assertDoesNotFail {
            searchApi2.getSearchIndex(index2Id)
        }

        searchApi2.close()
        lockApi2.close()
        kvdbApi2.close()
        storeApi2.close()
    }

    @Test
    fun accessAsPublicUser() {
        val connectionPublic: Connection = connectAsUser(ConnectionType.Public, bridgeAddress)
        val storeApiPublic = StoreApi(connectionPublic)
        val kvdbApiPublic = KvdbApi(connectionPublic)
        val lockApiPublic = LockApi(connectionPublic)
        val searchApiPublic =
            SearchApi(connectionPublic, storeApiPublic, kvdbApiPublic, lockApiPublic)

        assertFailsWith(PrivmxException::class) {
            searchApiPublic.createSearchIndex(
                contextId!!,
                users,
                users,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                IndexMode.WITH_CONTENT
            )
        }
        assertFailsWith(PrivmxException::class) {
            searchApiPublic.getSearchIndex(indexId)
        }
        assertFailsWith(PrivmxException::class) {
            searchApiPublic.listSearchIndexes(contextId!!, 0, 10)
        }
        assertFailsWith(PrivmxException::class) {
            searchApiPublic.openSearchIndex(indexId)
        }
        assertFailsWith(PrivmxException::class) {
            searchApiPublic.deleteSearchIndex(indexId)
        }

        searchApiPublic.close()
        lockApiPublic.close()
        kvdbApiPublic.close()
        storeApiPublic.close()
        connectionPublic.close()
    }

    @Test
    fun createSearchApiWithClosedDependency() {
        val storeApiClosed = StoreApi(connection!!)
        storeApiClosed.close()

        // the constructor is documented to reject a closed dependency
        assertFailsWith(IllegalStateException::class) {
            SearchApi(connection!!, storeApiClosed, kvdbApi, lockApi)
        }
    }

    @Test
    fun closeSearchApi() {
        val storeApiToClose = StoreApi(connection!!)
        val kvdbApiToClose = KvdbApi(connection!!)
        val lockApiToClose = LockApi(connection!!)
        val searchApiToClose =
            SearchApi(connection!!, storeApiToClose, kvdbApiToClose, lockApiToClose)

        assertDoesNotFail {
            searchApiToClose.close()
        }

        // every method on a closed instance
        assertFailsWith(IllegalStateException::class) {
            searchApiToClose.getSearchIndex(indexId)
        }
        assertFailsWith(IllegalStateException::class) {
            searchApiToClose.listSearchIndexes(contextId!!, 0, 10)
        }
        assertFailsWith(IllegalStateException::class) {
            searchApiToClose.openSearchIndex(indexId)
        }

        // closing twice
        assertFailsWith(IllegalStateException::class) {
            searchApiToClose.close()
        }

        lockApiToClose.close()
        kvdbApiToClose.close()
        storeApiToClose.close()
    }

    @Test
    @Ignore
    fun filteringListSearchIndexesWithQueryAsJson() {
        lateinit var indexes: PagingList<SearchIndex>

        val filteredIndexId = searchApi.createSearchIndex(
            context2Id!!,
            users,
            users,
            json1.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            IndexMode.WITH_CONTENT
        )
        searchApi.createSearchIndex(
            context2Id!!,
            users,
            users,
            json2.encodeToByteArray(),
            privateMeta.encodeToByteArray(),
            IndexMode.WITH_CONTENT
        )

        assertDoesNotFail {
            indexes = searchApi.listSearchIndexes(context2Id!!, 0, 10, "desc", null, query10)
        }
        assertEquals(1, indexes.readItems.size)
        assertEquals(filteredIndexId, indexes.readItems[0].indexId)

        searchApi.deleteSearchIndex(filteredIndexId)
    }
}