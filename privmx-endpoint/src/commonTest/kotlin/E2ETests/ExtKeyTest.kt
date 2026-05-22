package E2ETests

import E2ETests.BaseTest.Companion.assertDoesNotFail
import Utils.IniConfig
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.crypto.CryptoApi
import com.simplito.kotlin.privmx_endpoint.modules.crypto.ExtKey
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExtKeyTest {
    lateinit var cryptoApi: CryptoApi
    lateinit var signature: ByteArray

    @BeforeTest
    fun beforeEach() {
        cryptoApi = CryptoApi()
        signature = cryptoApi.signData(
            "data".encodeToByteArray(),
            IniConfig["Login", "userPrivKey"]
        )
    }

    @AfterTest
    fun afterEach() {
        try {
            cryptoApi.close()
        } catch (ignored: Exception) {
        }
    }

    @Test
    @Throws(Exception::class)
    fun extKeyFromSeed() {
        lateinit var extKey: ExtKey
        assertDoesNotFail{ extKey = ExtKey.fromSeed("seed".encodeToByteArray()) }
        assertDoesNotFail{ extKey.derive(4) }
        assertDoesNotFail{ extKey.deriveHardened(4).close() }

        assertDoesNotFail{ extKey.getPrivatePartAsBase58() }
        assertDoesNotFail{ extKey.getPrivateKey() }
        assertDoesNotFail{ extKey.getPrivateEncKey() }

        assertDoesNotFail{ extKey.getPublicKey() }
        assertDoesNotFail{ extKey.getPublicKeyAsBase58Address() }
        assertDoesNotFail{ extKey.getChainCode() }
        assertDoesNotFail{
            extKey.verifyCompactSignatureWithHash("message".encodeToByteArray(), signature)
        }
        assertDoesNotFail{ extKey.isPrivate() }

        assertDoesNotFail{ extKey.close() }
    }

    @Test
    @Throws(Exception::class)
    fun extKeyFromBase58() {
        val extKeyRandom: ExtKey = ExtKey.generateRandom()
        val privatePartAsBase58 = extKeyRandom.getPrivatePartAsBase58()
        lateinit var extKey: ExtKey
        var isKeyPrivate: Boolean = false

        // ExtKey generated from private key
        assertDoesNotFail{ extKey = ExtKey.fromBase58(privatePartAsBase58) }
        assertDoesNotFail{ extKey.derive(4).close() }
        assertDoesNotFail{ extKey.deriveHardened(4).close() }

        assertDoesNotFail{ extKey.getPrivatePartAsBase58() }
        assertDoesNotFail{ extKey.getPrivateKey() }
        assertDoesNotFail{ extKey.getPrivateEncKey() }

        assertDoesNotFail{ extKey.getPublicKey() }
        assertDoesNotFail{ extKey.getPublicKeyAsBase58Address() }
        assertDoesNotFail{ extKey.getChainCode() }
        assertDoesNotFail{
            extKey.verifyCompactSignatureWithHash("message".encodeToByteArray(), signature)
        }
        assertDoesNotFail{ isKeyPrivate = extKey.isPrivate() }
        assertTrue(isKeyPrivate)

        // ExtKey generated from public key
        val publicPartAsBase58 = extKeyRandom.getPublicPartAsBase58()
        assertDoesNotFail{ extKey = ExtKey.fromBase58(publicPartAsBase58) }
        assertFailsWith(PrivmxException::class) { extKey.derive(4).close() }
        assertFailsWith(PrivmxException::class) { extKey.deriveHardened(4).close() }

        assertFailsWith(PrivmxException::class) { extKey.getPrivatePartAsBase58() }
        assertFailsWith(PrivmxException::class) { extKey.getPrivateKey() }
        assertFailsWith(PrivmxException::class) { extKey.getPrivateEncKey() }

        assertDoesNotFail{ extKey.getPublicKey() }
        assertDoesNotFail{ extKey.getPublicKeyAsBase58Address() }
        assertDoesNotFail{ extKey.getChainCode() }
        assertDoesNotFail{
            extKey.verifyCompactSignatureWithHash("message".encodeToByteArray(), signature)
        }
        assertDoesNotFail{ isKeyPrivate = extKey.isPrivate() }
        assertFalse(isKeyPrivate)

        assertDoesNotFail{ extKey.close() }
        extKeyRandom.close()
    }

    @Test
    @Throws(Exception::class)
    fun extKeyGenerateRandom() {
        lateinit var extKey: ExtKey

        assertDoesNotFail{ extKey = ExtKey.generateRandom() }
        assertDoesNotFail{ extKey.derive(4).close() }
        assertDoesNotFail{ extKey.deriveHardened(4).close() }

        assertDoesNotFail{ extKey.getPrivatePartAsBase58() }
        assertDoesNotFail{ extKey.getPrivateKey() }
        assertDoesNotFail{ extKey.getPrivateEncKey() }

        assertDoesNotFail{ extKey.getPublicKey() }
        assertDoesNotFail{ extKey.getPublicKeyAsBase58Address() }
        assertDoesNotFail{ extKey.getChainCode() }
        assertDoesNotFail{
            extKey.verifyCompactSignatureWithHash("message".encodeToByteArray(), signature)
        }
        assertDoesNotFail{ extKey.isPrivate() }

        assertDoesNotFail{ extKey.close() }
    }
}
