package E2ETests

import Utils.IniConfig
import com.simplito.kotlin.privmx_endpoint.model.Context
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.Thread
import com.simplito.kotlin.privmx_endpoint.model.UserInfo
import com.simplito.kotlin.privmx_endpoint.model.VerificationRequest
import com.simplito.kotlin.privmx_endpoint.model.events.eventSelectorTypes.CoreEventSelectorType
import com.simplito.kotlin.privmx_endpoint.model.events.eventTypes.CoreEventType
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.core.UserVerifierInterface
import com.simplito.kotlin.privmx_endpoint.modules.thread.ThreadApi
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

//TODO: Add tests for close method
@OptIn(ExperimentalAtomicApi::class)
class CoreTest : BaseTest() {

    @BeforeTest
    @Throws(PrivmxException::class, NativeException::class)
    fun beforeEach() {
        connection = connectAsUser(ConnectionType.User1, bridgeAddress)
    }

    @AfterTest
    fun afterEach() {
        connection?.close()?.also {
            connection = null
        }
        try {
            connection2?.close()
        } finally {
            connection2 = null
        }
    }

    @Test
    fun correctConnection() {
        assertDoesNotFail {
            connection2 = connectAsUser(
                ConnectionType.User2,
                bridgeAddress
            )
        }

        assertNotNull(connection2)
        assertNotNull(connection)
    }

    @Test
    fun incorrectValuesConnection() {
        val privKey: String = IniConfig["Login", "user2PrivKey"]

        // wrong userPrvKey
        try {
            assertFailsWith(PrivmxException::class) {
                connect(
                    contextId!!,
                    IniConfig["Login", "solutionId"],
                    bridgeAddress
                )
            }
        } catch (e: AssertionError) {
            fail("Connected with wrong user private key")
        }

        // wrong solutionId
        try {
            assertFailsWith(PrivmxException::class) {
                connect(
                    privKey,
                    contextId!!,
                    bridgeAddress
                )
            }
        } catch (e: AssertionError) {
            fail("Connected with wrong solutionId")
        }

        // wrong platformUrl
        try {
            assertFailsWith(PrivmxException::class) {
                connect(
                    privKey, IniConfig["Login", "solutionId"], "http://localhost:9110"
                )
            }
        } catch (e: AssertionError) {
            fail("Connected with wrong platform URL")
        }
    }

    @Test
    fun platformConnectMultipleInstances() {
        lateinit var connectionOtherUser: Connection
        lateinit var connectionPublicUser: Connection


        // user connected
        assertNotNull(connection)

        // connect as already connected user
        assertFailsWith(PrivmxException::class) {
            val connectionSameUser: Connection =
                connectAsUser(ConnectionType.User1, bridgeAddress)
            connectionSameUser.close()
        }

        // connect as another user
        assertDoesNotFail {
            connectionOtherUser = connectAsUser(
                ConnectionType.User2,
                bridgeAddress
            )
        }
        assertNotNull(connectionOtherUser)

        // connect as public user
        assertDoesNotFail {
            connectionPublicUser = connectAsUser(
                ConnectionType.Public,
                bridgeAddress
            )

        }
        assertNotNull(connectionPublicUser)

        connectionOtherUser.close()
        connectionPublicUser.close()
    }

    @Test
    fun platformUserContextsList() {
        connection2 =
            connectAsUser(ConnectionType.User2, bridgeAddress)
        var contextList: PagingList<Context>? = null
        lateinit var contextList2: PagingList<Context>

        assertDoesNotFail {
            contextList = connection?.listContexts(0, 1, "desc")
        }
        assertNotNull(contextList)

        assertEquals(2, contextList?.totalAvailable)
        assertEquals(
            context2Id!!,
            contextList?.readItems?.get(0)?.contextId
        )

        assertDoesNotFail {
            contextList2 = connection2!!.listContexts(0, 1, "desc")
        }
        assertEquals(2, contextList2.totalAvailable)
        assertEquals(
            context2Id!!,
            contextList2.readItems[0].contextId
        )
    }

    @Test
    fun platformDisconnect() {
        val connection2: Connection = connectAsUser(
            ConnectionType.User2,
            bridgeAddress
        )
        val connectionPublic: Connection = connectAsUser(
            ConnectionType.Public,
            bridgeAddress
        )

        // close method also disconnects from platform
        assertDoesNotFail { connectionPublic.close() }
        assertDoesNotFail { connection2.disconnect() }
        assertDoesNotFail { connection2.close() }
    }

    @Test
    fun listContextIncorrectInputData() {
        //limit < 0
        assertFailsWith(PrivmxException::class) {
            connection?.listContexts(0, -1, "desc")
        }

        //limit == 0
        assertFailsWith(PrivmxException::class) {
            connection?.listContexts(0, 0, "desc")
        }

        //incorrect sortOrder
        assertFailsWith(PrivmxException::class) {
            connection?.listContexts(0, 1, "wrong")
        }

        //incorrect lastId
        assertFailsWith(PrivmxException::class) {
            connection?.listContexts(0, 1, "desc", "wrong")
        }

        // wrong queryAsJson
        assertFailsWith(PrivmxException::class) {
            connection!!.listContexts(0, 1, "asc", null, "wrong")
        }

        // wrong sortBy
        assertFailsWith(PrivmxException::class) {
            connection!!.listContexts(0, 1, "asc", null, null, "wrong")
        }
    }

    @Test
    @Throws(PrivmxException::class)
    fun listContextUsers() {
        var contextUsers: List<UserInfo> = emptyList()

        // incorrect contextId
        assertFailsWith(PrivmxException::class) {
            contextUsers = connection!!.listContextUsers(contextId + "0", 0, 100, "desc").readItems
        }

        // limit < 0
        assertFailsWith(PrivmxException::class) {
            contextUsers = connection!!.listContextUsers(contextId!!, -1, 100, "desc").readItems
        }

        // limit == 0
        assertFailsWith(PrivmxException::class) {
            contextUsers = connection!!.listContextUsers(contextId!!, 0, 0, "desc").readItems
        }

        // incorrect sortOrder
        assertFailsWith(PrivmxException::class) {
            contextUsers = connection!!.listContextUsers(contextId!!, 0, 0, "wrong").readItems

        }

        // incorrect lastId
        assertFailsWith(PrivmxException::class) {
            contextUsers =
                connection!!.listContextUsers(contextId!!, 0, 100, "desc", user1Id).readItems
        }

        // incorrect queryAsJson
        assertFailsWith(PrivmxException::class) {
            contextUsers =
                connection!!.listContextUsers(contextId!!, 0, 100, "desc", null, "wrong").readItems
        }

        // incorrect sortBy
        assertFailsWith(PrivmxException::class) {
            contextUsers = connection!!.listContextUsers(
                contextId!!,
                0,
                100,
                "desc",
                null,
                null,
                "wrong"
            ).readItems
        }


        // connected as user1
        assertDoesNotFail {
            contextUsers = connection!!.listContextUsers(contextId!!, 0, 100, "desc").readItems
        }

        var user1: UserInfo =
            contextUsers.first { u: UserInfo -> u.user.userId == user1Id }
        var user2: UserInfo =
            contextUsers.first { u: UserInfo -> u.user.userId == user2Id }

        assertEquals(2, contextUsers.size)
        assertNotNull(user1)
        assertNotNull(user2)
        assertEquals(user1.user.pubKey, IniConfig["Login", "userPubKey"])
        assertEquals(user2.user.pubKey, IniConfig["Login", "user2PubKey"])
        assertTrue(user1.isActive)
        assertFalse(user2.isActive)

        // connect as user2 ( 2 users connected )
        connection2 = connectAsUser(ConnectionType.User2, bridgeAddress)
        assertDoesNotFail {
            contextUsers = connection2!!.listContextUsers(contextId!!, 0, 100, "desc").readItems
        }

        user1 = contextUsers.first { u: UserInfo -> u.user.userId == user1Id }
        user2 = contextUsers.first { u: UserInfo -> u.user.userId == user2Id }

        assertEquals(2, contextUsers.size)
        assertNotNull(user1)
        assertNotNull(user2)
        assertEquals(user1.user.pubKey, IniConfig["Login", "userPubKey"])
        assertEquals(user2.user.pubKey, IniConfig["Login", "user2PubKey"])
        assertTrue(user1.isActive)
        assertTrue(user2.isActive)
    }

    @Test
    @Throws(PrivmxException::class)
    fun listContextCorrectInputData() {
        val connectionPublic: Connection = connectAsUser(
            ConnectionType.Public,
            bridgeAddress
        )
        var contextList: PagingList<Context>? = null

        // connection for user
        assertDoesNotFail {
            contextList = connection?.listContexts(1, 1, "desc")
        }
        assertEquals(2, contextList?.totalAvailable)
        assertEquals(1, contextList?.readItems?.size)
        assertEquals(contextId, contextList?.readItems?.get(0)?.contextId)

        assertDoesNotFail {
            contextList = connection?.listContexts(0, 3, "desc")
        }
        assertEquals(2, contextList?.totalAvailable)
        assertEquals(2, contextList?.readItems?.size)
        assertEquals(contextId, contextList?.readItems?.get(1)?.contextId)
        assertEquals(context2Id, contextList?.readItems?.get(0)?.contextId)

        // lastId
        assertDoesNotFail {
            contextList = connection?.listContexts(0, 2, "asc", contextId)
        }
        assertEquals(1, contextList?.totalAvailable)
        assertEquals(1, contextList?.readItems?.size)
        assertEquals(context2Id, contextList?.readItems?.get(0)?.contextId)

        // connection for public user
        assertFailsWith(PrivmxException::class) {
            connectionPublic.listContexts(0, 1, "desc")
        }

        connectionPublic.close()
    }

        @Test
        @Throws(Exception::class)
        fun setUserVerifier() {
            val threadApi: ThreadApi = ThreadApi(connection!!)
            lateinit var thread: Thread
            val threadId = threadApi.createThread(
                contextId!!,
                users,
                users,
                ByteArray(0),
                ByteArray(0)
            )

            // user1 is verified
            val userVerifierById = object : UserVerifierInterface {
                override fun verify(request: List<VerificationRequest>): List<Boolean> {
                    return request.map { req -> req.senderId.contains(user1Id!!) }
                }
            }

            assertDoesNotFail {
                connection!!.setUserVerifier(
                    userVerifierById
                )
            }
            assertDoesNotFail {
                thread = threadApi.getThread(threadId)
            }
            assertEquals(0, thread.statusCode)
            // user2 is verified
            val userVerifierById2: UserVerifierInterface = object : UserVerifierInterface {
                override fun verify(request: List<VerificationRequest>): List<Boolean> {
                    return request.map { req: VerificationRequest -> req.senderId.contains("user_2") }
                }
            }

            assertDoesNotFail {
                connection!!.setUserVerifier(
                    userVerifierById2
                )
            }
            assertDoesNotFail {
                thread = threadApi.getThread(threadId)
            }
            assertNotEquals(0, thread.statusCode)

            // todo: Tests (for shorter result) will be written when the library throws exception
            // result list size < requests list size
            val userVerifierShort: UserVerifierInterface = object : UserVerifierInterface {
                override fun verify(request: List<VerificationRequest>): List<Boolean> {
                    val result = request.map { req: VerificationRequest -> false }
                    return result.subList(0, result.size - 1)
                }
            }

            // result list size > requests list size
            val userVerifierLong: UserVerifierInterface = object : UserVerifierInterface {
                override fun verify(request: List<VerificationRequest>): List<Boolean> {
                    val result = request.map { req: VerificationRequest -> false }.toMutableList()
                    result.add(true)
                    return result
                }
            }

            assertDoesNotFail {
                connection!!.setUserVerifier(
                    userVerifierLong
                )
            }
            assertFailsWith(PrivmxException::class) {
                threadApi.getThread(threadId)
            }

            // throwing function
            val userVerifierThrows = object : UserVerifierInterface {
                override fun verify(request: List<VerificationRequest>): List<Boolean> {
                    throw IllegalStateException()
                }
            }

            assertDoesNotFail {
                connection!!.setUserVerifier(userVerifierThrows)
            }
            assertFailsWith(IllegalStateException::class) {
                thread = threadApi.getThread(threadId)
            }

            // empty list
            val userVerifierEmpty = object : UserVerifierInterface {
                override fun verify(request: List<VerificationRequest>): List<Boolean> {
                    return mutableListOf()
                }
            }

            // public connection
            val publicConnection = connectAsUser(ConnectionType.Public, bridgeAddress)
            assertDoesNotFail {
                publicConnection.setUserVerifier(userVerifierById)
            }

            publicConnection.close()
            threadApi.close()
        }

    @Test
    fun subscribeForCoreEvents() {
        var subscriptionIds: List<String> = emptyList()

        // subscribe
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()

            queries.add(
                connection!!.buildSubscriptionQuery(
                    CoreEventType.USER_ADD,
                    CoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                connection!!.buildSubscriptionQuery(
                    CoreEventType.USER_STATUS,
                    CoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                connection!!.buildSubscriptionQuery(
                    CoreEventType.USER_REMOVE,
                    CoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            subscriptionIds = connection!!.subscribeFor(queries)
        }

        // subscribe again
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()

            queries.add(
                connection!!.buildSubscriptionQuery(
                    CoreEventType.USER_ADD,
                    CoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            connection!!.subscribeFor(queries)
        }

        // 2 same queries
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()

            queries.add(
                connection!!.buildSubscriptionQuery(
                    CoreEventType.USER_REMOVE,
                    CoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            queries.add(
                connection!!.buildSubscriptionQuery(
                    CoreEventType.USER_REMOVE,
                    CoreEventSelectorType.CONTEXT_ID,
                    contextId!!
                )
            )
            connection!!.subscribeFor(queries)
        }

        // subscribe with nonexisting selectorId
        assertDoesNotFail {
            val queries: MutableList<String> = mutableListOf()

            queries.add(
                connection!!.buildSubscriptionQuery(
                    CoreEventType.USER_ADD,
                    CoreEventSelectorType.CONTEXT_ID,
                    "wrong-3524-4579-error-215368c4b2a4"
                )
            )
            connection!!.subscribeFor(queries)
        }

        // unsubscribe
        assertDoesNotFail {
            // todo - this method does not work in version 2.6 - will be fixed
//            connection!!.unsubscribeFrom(subscriptionIds)
        }

        // unsubscribe again
        assertDoesNotFail {
            // todo - this method does not work in version 2.6 - will be fixed
//            connection!!.unsubscribeFrom(subscriptionIds)
        }
    }

    @Throws(Exception::class)
    fun getUsersLastStatus() {
        var contextUsers: List<UserInfo> = emptyList()
        var user2PubKey = IniConfig["Login", "user2PubKey"]

        // connect as user2
        var connection2: Connection =
            connectAsUser(ConnectionType.User2, bridgeAddress)

        assertDoesNotFail {
            contextUsers = connection!!.listContextUsers(contextId!!, 1, 1, "asc").readItems
        }

        // UserInfo
        var userInfo = contextUsers.get(0)
        assertEquals(user2Id, userInfo.user.userId);
        assertEquals(user2PubKey, userInfo.user.pubKey);
        assertTrue(userInfo.isActive)

        // userStatusChange
        var userStatusChange = userInfo.lastStatusChange!!
        assertEquals("login", userStatusChange.action);

        // disconnect as user2
        connection2.disconnect();

        assertDoesNotFail {
            contextUsers = connection!!.listContextUsers(contextId!!, 1, 1, "asc").readItems
        }

        userInfo = contextUsers.get(0)
        assertEquals(user2Id, userInfo.user.userId)
        assertEquals(user2PubKey, userInfo.user.pubKey)
        assertFalse(userInfo.isActive);

        userStatusChange = userInfo.lastStatusChange!!
        assertEquals("logout", userStatusChange.action)
    }
}