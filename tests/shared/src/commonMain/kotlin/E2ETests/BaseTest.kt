package E2ETests

import Utils.IniConfig
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.Connection
import com.simplito.kotlin.privmx_endpoint.modules.core.EventQueue
import kotlin.test.fail

//TODO: In ios after test failure next tests failure too

open class BaseTest {
    var connection: Connection? = null
    val publicMeta: String = "public"
    val privateMeta: String = "private"
    val bridgeAddress = IniConfig["Login","instanceUrl"]
    enum class ConnectionType {
        User1, User2, Public
    }

    companion object {
        var throwable: Throwable? = null
        var users: ArrayList<UserWithPubKey> = ArrayList()
        var incorrectUsers: ArrayList<UserWithPubKey> = ArrayList()
        var sameUsers: ArrayList<UserWithPubKey> = ArrayList()
        var emptyUsers: ArrayList<UserWithPubKey> = ArrayList()
        var user1Id: String? = null
        var user2Id: String? = null
        var contextId: String? = null
        var context2Id: String? = null
        var connection2: Connection? = null

        @Throws(PrivmxException::class, NativeException::class)
        fun connect(userPrvKey: String, solutionId: String, platformUrl: String): Connection {
            return Connection.connect(userPrvKey, solutionId, platformUrl)
        }

        @Throws(PrivmxException::class, NativeException::class)
        fun connectAsUserAndCleanEvents(
            type: ConnectionType,
            platformUrl: String
        ): Connection = connectAsUser(type, platformUrl).also {
            EventQueue.getEvent()
        }

        fun closeConnectionAndCleanEvents(connectionToClose: Connection) {
            try {
                connectionToClose.close()
                EventQueue.getEvent()
                EventQueue.getEvent()
            } catch (_: Exception) {
            }
        }

        @Throws(PrivmxException::class, NativeException::class)
        fun connectAsUser(type: ConnectionType, platformUrl: String): Connection {
            when (type) {
                ConnectionType.User1 -> {
                    return connect(
                        IniConfig["Login", "userPrivKey"],
                        IniConfig["Login", "solutionId"],
                        platformUrl
                    )
                }

                ConnectionType.User2 -> {
                    return connect(
                        IniConfig["Login", "user2PrivKey"],
                        IniConfig["Login", "solutionId"],
                        platformUrl
                    )
                }

                ConnectionType.Public -> {
                    return Connection.connectPublic(
                        IniConfig["Login", "solutionId"],
                        platformUrl
                    )
                }
            }
        }

        init {
            try {
                contextId =
                    IniConfig["Login", "contextId"]
                context2Id =
                    IniConfig["Login", "context2Id"]
                user1Id =
                    IniConfig["Login", "userId"]
                user2Id =
                    IniConfig["Login", "user2Id"]

                users.add(
                    UserWithPubKey(
                        user1Id ?: "",
                        IniConfig["Login", "userPubKey"]
                    )
                )
                users.add(
                    UserWithPubKey(
                        user2Id ?: "",
                        IniConfig["Login", "user2PubKey"]
                    )
                )
                incorrectUsers.add(
                    UserWithPubKey(
                        IniConfig["Login", "userId"],
                        IniConfig["Login", "user2PubKey"]
                    )
                )
                incorrectUsers.add(
                    UserWithPubKey(
                        IniConfig["Login", "user2Id"],
                        IniConfig["Login", "userPubKey"]
                    )
                )
                sameUsers.add(
                    UserWithPubKey(
                        IniConfig["Login", "userId"],
                        IniConfig["Login", "userPubKey"]
                    )
                )
                sameUsers.add(
                    UserWithPubKey(
                        IniConfig["Login", "userId"],
                        IniConfig["Login", "userPubKey"]
                    )
                )
            } catch (exception: Exception) {
                throwable = exception
            }
        }

        fun assertDoesNotFail(body: () -> Unit) {
            try {
                body()
            } catch (e: Throwable) {
                fail("Expected no exception to be thrown, but got: $e")
            }
        }

    }
}