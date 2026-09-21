package E2ETests

import com.simplito.kotlin.privmx_endpoint.model.LockLevel
import com.simplito.kotlin.privmx_endpoint.model.LockOperationResult
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.lock.LockApi
import com.simplito.kotlin.privmx_endpoint.modules.store.StoreApi
import kotlin.properties.Delegates
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class LockTest : BaseTest() {

    private lateinit var storeApi: StoreApi
    private lateinit var lockApi: LockApi
    private lateinit var storeId: String
    private lateinit var store2Id: String
    private lateinit var resourceId: String
    private lateinit var noRandomWriteResourceId: String

    @BeforeTest
    fun createConnection() {
        if (connection == null) {
            connection = connectAsUser(ConnectionType.User1, bridgeAddress)
        }
        storeApi = StoreApi(connection!!)
        lockApi = LockApi(connection!!)
        storeId = storeApi.createStore(
            contextId!!,
            users.subList(0, 1),
            users.subList(0, 1),
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        store2Id = storeApi.createStore(
            contextId!!,
            users,
            users,
            publicMeta.encodeToByteArray(),
            privateMeta.encodeToByteArray()
        )
        resourceId = createLockableResource(storeId)
        noRandomWriteResourceId = createResource(storeId, randomWriteSupport = false)
    }

    @AfterTest
    @Throws(Exception::class)
    fun closeConnection() {
        if (::lockApi.isInitialized) {
            lockApi.close()
        }
        if (::storeApi.isInitialized) {
            if (::store2Id.isInitialized) storeApi.deleteStore(store2Id)
            if (::storeId.isInitialized) storeApi.deleteStore(storeId)
            storeApi.close()
        }
        connection?.close()?.also {
            connection = null
        }
        try {
            connection2?.close()
        } finally {
            connection2 = null
        }
    }

    // only files with random write support can be locked
    private fun createLockableResource(storeId: String): String =
        createResource(storeId, randomWriteSupport = true)

    private fun createResource(storeId: String, randomWriteSupport: Boolean): String =
        storeApi.closeFile(
            storeApi.createFile(
                storeId,
                publicMeta.encodeToByteArray(),
                privateMeta.encodeToByteArray(),
                0,
                randomWriteSupport
            )!!
        )

    private fun newUuid(): String = Uuid.random().toString()

    private fun randomReadableString(length: Int): String {
        val possibleCharacters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        return (1..length).map { possibleCharacters.random() }.joinToString("")
    }

    @Test
    fun lockIncorrectInputData() {
        val uuid = newUuid()

        // incorrect resourceId
        assertFailsWith(PrivmxException::class) {
            lockApi.lock(contextId!!, uuid, LockLevel.SHARED)
        }

        // resourceId out of the allowed charset
        assertFailsWith(PrivmxException::class) {
            lockApi.lock("resource:id", uuid, LockLevel.SHARED)
        }

        // resourceId too long
        assertFailsWith(PrivmxException::class) {
            lockApi.lock(randomReadableString(61), uuid, LockLevel.SHARED)
        }

        // uuid out of the allowed charset
        assertFailsWith(PrivmxException::class) {
            lockApi.lock(resourceId, "uuid:1", LockLevel.SHARED)
        }

        // file without random write support
        assertFailsWith(PrivmxException::class) {
            lockApi.lock(noRandomWriteResourceId, uuid, LockLevel.SHARED)
        }

        // lockLevel NONE - lock() only acquires, it never releases
        assertFailsWith(PrivmxException::class) {
            lockApi.lock(resourceId, uuid, LockLevel.NONE)
        }
    }

    @Test
    fun lockCorrectInputData() {
        val uuid = newUuid()
        lateinit var result: LockOperationResult

        // NONE -> SHARED
        assertDoesNotFail {
            result = lockApi.lock(resourceId, uuid, LockLevel.SHARED)
        }
        assertTrue(result.success)
        assertEquals(LockLevel.SHARED, result.currentLevel)

        // SHARED again - renews the lease
        assertDoesNotFail {
            result = lockApi.lock(resourceId, uuid, LockLevel.SHARED)
        }
        assertTrue(result.success)
        assertEquals(LockLevel.SHARED, result.currentLevel)

        // SHARED -> RESERVED
        assertDoesNotFail {
            result = lockApi.lock(resourceId, uuid, LockLevel.RESERVED)
        }
        assertTrue(result.success)
        assertEquals(LockLevel.RESERVED, result.currentLevel)

        // RESERVED -> EXCLUSIVE, no other readers
        assertDoesNotFail {
            result = lockApi.lock(resourceId, uuid, LockLevel.EXCLUSIVE)
        }
        assertTrue(result.success)
        assertEquals(LockLevel.EXCLUSIVE, result.currentLevel)

        // weaker level - lock() never downgrades
        assertDoesNotFail {
            result = lockApi.lock(resourceId, uuid, LockLevel.SHARED)
        }
        assertTrue(result.success)
        assertEquals(LockLevel.EXCLUSIVE, result.currentLevel)

        assertDoesNotFail {
            lockApi.unlock(resourceId, uuid, LockLevel.NONE)
        }
    }

    @Test
    fun lockMultipleHolders() {
        val writerUuid = newUuid()
        val readerUuid = newUuid()
        val otherReaderUuid = newUuid()
        lateinit var result: LockOperationResult

        // RESERVED still admits new readers
        assertDoesNotFail {
            result = lockApi.lock(resourceId, writerUuid, LockLevel.RESERVED)
        }
        assertTrue(result.success)
        assertEquals(LockLevel.RESERVED, result.currentLevel)

        assertDoesNotFail {
            result = lockApi.lock(resourceId, readerUuid, LockLevel.SHARED)
        }
        assertTrue(result.success)
        assertEquals(LockLevel.SHARED, result.currentLevel)

        // EXCLUSIVE with a reader present - refused, but the writer parks on PENDING
        assertDoesNotFail {
            result = lockApi.lock(resourceId, writerUuid, LockLevel.EXCLUSIVE)
        }
        assertFalse(result.success)
        assertEquals(LockLevel.PENDING, result.currentLevel)

        // PENDING blocks new readers
        assertDoesNotFail {
            result = lockApi.lock(resourceId, otherReaderUuid, LockLevel.SHARED)
        }
        assertFalse(result.success)
        assertEquals(LockLevel.NONE, result.currentLevel)

        // the reader that was already in drains
        assertDoesNotFail {
            result = lockApi.unlock(resourceId, readerUuid, LockLevel.NONE)
        }
        assertTrue(result.success)
        assertEquals(LockLevel.NONE, result.currentLevel)

        // PENDING -> EXCLUSIVE
        assertDoesNotFail {
            result = lockApi.lock(resourceId, writerUuid, LockLevel.EXCLUSIVE)
        }
        assertTrue(result.success)
        assertEquals(LockLevel.EXCLUSIVE, result.currentLevel)

        assertDoesNotFail {
            lockApi.unlock(resourceId, writerUuid, LockLevel.NONE)
        }
    }

    @Test
    fun unlockIncorrectInputData() {
        val uuid = newUuid()

        // incorrect resourceId
        assertFailsWith(PrivmxException::class) {
            lockApi.unlock(contextId!!, uuid, LockLevel.NONE)
        }

        // resourceId out of the allowed charset
        assertFailsWith(PrivmxException::class) {
            lockApi.unlock("resource:id", uuid, LockLevel.NONE)
        }

        // resourceId too long
        assertFailsWith(PrivmxException::class) {
            lockApi.unlock(randomReadableString(61), uuid, LockLevel.NONE)
        }

        // uuid out of the allowed charset
        assertFailsWith(PrivmxException::class) {
            lockApi.unlock(resourceId, "uuid:1", LockLevel.NONE)
        }
    }

    @Test
    fun unlockCorrectInputData() {
        val uuid = newUuid()
        lateinit var result: LockOperationResult

        assertDoesNotFail {
            lockApi.lock(resourceId, uuid, LockLevel.EXCLUSIVE)
        }

        // EXCLUSIVE -> SHARED keeps a reader lock
        assertDoesNotFail {
            result = lockApi.unlock(resourceId, uuid, LockLevel.SHARED)
        }
        assertTrue(result.success)
        assertEquals(LockLevel.SHARED, result.currentLevel)

        // NONE releases fully
        assertDoesNotFail {
            result = lockApi.unlock(resourceId, uuid, LockLevel.NONE)
        }
        assertTrue(result.success)
        assertEquals(LockLevel.NONE, result.currentLevel)

        // releasing an unheld lock is not an error
        assertDoesNotFail {
            result = lockApi.unlock(resourceId, uuid, LockLevel.NONE)
        }
        assertEquals(LockLevel.NONE, result.currentLevel)
    }

    @Test
    fun checkReservedLock() {
        val uuid = newUuid()
        var reserved by Delegates.notNull<Boolean>()

        // nothing is held yet
        assertDoesNotFail {
            reserved = lockApi.checkReservedLock(resourceId, uuid)
        }
        assertFalse(reserved)

        // SHARED is below RESERVED
        assertDoesNotFail {
            lockApi.lock(resourceId, uuid, LockLevel.SHARED)
            reserved = lockApi.checkReservedLock(resourceId, uuid)
        }
        assertFalse(reserved)

        // RESERVED
        assertDoesNotFail {
            lockApi.lock(resourceId, uuid, LockLevel.RESERVED)
            reserved = lockApi.checkReservedLock(resourceId, uuid)
        }
        assertTrue(reserved)

        // EXCLUSIVE is above RESERVED
        assertDoesNotFail {
            lockApi.lock(resourceId, uuid, LockLevel.EXCLUSIVE)
            reserved = lockApi.checkReservedLock(resourceId, uuid)
        }
        assertTrue(reserved)

        // released again
        assertDoesNotFail {
            lockApi.unlock(resourceId, uuid, LockLevel.NONE)
            reserved = lockApi.checkReservedLock(resourceId, uuid)
        }
        assertFalse(reserved)
    }

    @Test
    fun checkReservedLockIncorrectInputData() {
        val uuid = newUuid()

        // incorrect resourceId
        assertFailsWith(PrivmxException::class) {
            lockApi.checkReservedLock(contextId!!, uuid)
        }

        // resourceId out of the allowed charset
        assertFailsWith(PrivmxException::class) {
            lockApi.checkReservedLock("resource:id", uuid)
        }

        // uuid out of the allowed charset
        assertFailsWith(PrivmxException::class) {
            lockApi.checkReservedLock(resourceId, "uuid:1")
        }
    }

    @Test
    fun lockUnlockOtherUser() {
        val uuid = newUuid()

        // user1 creates a resource in a store user2 has no access to
        val privateResourceId = createLockableResource(storeId)

        // user1 creates a resource in a store shared with user2
        val sharedResourceId = createLockableResource(store2Id)

        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        val lockApi2 = LockApi(connection2!!)

        // no access to the store the resource lives in
        assertFailsWith(PrivmxException::class) {
            lockApi2.lock(privateResourceId, uuid, LockLevel.SHARED)
        }

        // shared store - user2 can take the lock
        lateinit var result: LockOperationResult
        assertDoesNotFail {
            result = lockApi2.lock(sharedResourceId, uuid, LockLevel.SHARED)
        }
        assertTrue(result.success)
        assertEquals(LockLevel.SHARED, result.currentLevel)

        // user1 sees the reservation taken by user2
        assertDoesNotFail {
            result = lockApi2.lock(sharedResourceId, uuid, LockLevel.RESERVED)
        }
        assertTrue(result.success)
        assertTrue(lockApi.checkReservedLock(sharedResourceId, newUuid()))

        assertDoesNotFail {
            lockApi2.unlock(sharedResourceId, uuid, LockLevel.NONE)
        }
        lockApi2.close()
    }

    @Test
    fun accessAsPublicUser() {
        val uuid = newUuid()
        val connectionPublic: Connection = connectAsUser(ConnectionType.Public, bridgeAddress)
        val lockApiPublic = LockApi(connectionPublic)

        assertFailsWith(PrivmxException::class) {
            lockApiPublic.lock(resourceId, uuid, LockLevel.SHARED)
        }
        assertFailsWith(PrivmxException::class) {
            lockApiPublic.unlock(resourceId, uuid, LockLevel.NONE)
        }
        assertFailsWith(PrivmxException::class) {
            lockApiPublic.checkReservedLock(resourceId, uuid)
        }

        lockApiPublic.close()
        connectionPublic.close()
    }

    @Test
    fun closeLockApi() {
        val uuid = newUuid()
        val lockApiToClose = LockApi(connection!!)

        assertDoesNotFail {
            lockApiToClose.close()
        }

        // every method on a closed instance
        assertFailsWith(IllegalStateException::class) {
            lockApiToClose.lock(resourceId, uuid, LockLevel.SHARED)
        }
        assertFailsWith(IllegalStateException::class) {
            lockApiToClose.unlock(resourceId, uuid, LockLevel.NONE)
        }
        assertFailsWith(IllegalStateException::class) {
            lockApiToClose.checkReservedLock(resourceId, uuid)
        }

        // closing twice
        assertFailsWith(IllegalStateException::class) {
            lockApiToClose.close()
        }
    }
}
