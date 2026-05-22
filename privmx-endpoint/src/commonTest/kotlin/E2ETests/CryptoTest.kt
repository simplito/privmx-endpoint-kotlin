package E2ETests

import Utils.IniConfig
import Utils.getResource
import com.simplito.kotlin.privmx_endpoint.model.BIP39
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.crypto.CryptoApi
import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.readString
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

//TODO: Add tests for close method
@OptIn(ExperimentalAtomicApi::class)
class CryptoTest : BaseTest() {
    private val cryptoApi: CryptoApi = CryptoApi()
    private val eccKeyPath = "private-key.pem"
    private val pgpKeyPath = "pgp-public-key.asc"

    @Test
    fun signData() {
        val data = "Very important data.".encodeToByteArray()
        val emptyData = "".encodeToByteArray()
        lateinit var signature: ByteArray

        // correct
        assertDoesNotFail {
            signature = cryptoApi.signData(
                data,
                IniConfig["Login", "userPrivKey"]
            )
        }
        assertNotNull(signature)

        // empty data
        assertDoesNotFail {
            cryptoApi.signData(
                emptyData,
                IniConfig["Login", "userPrivKey"]
            )
        }

        // empty privKey
        assertFailsWith(PrivmxException::class) { cryptoApi.signData(data, "") }

        // pubKey instead of privKey
        assertFailsWith(PrivmxException::class) {
            cryptoApi.signData(data, IniConfig["Login", "userPubKey"])
        }
    }

    @Test
    fun verifySignature() {
        val data = "Very important data.".encodeToByteArray()
        var result = false

        // sign
        val signature = cryptoApi.signData(data, IniConfig["Login", "userPrivKey"])

        // correct
        assertDoesNotFail {
            result = cryptoApi.verifySignature(
                data,
                signature,
                IniConfig["Login", "userPubKey"]
            )
        }
        assertTrue(result)

        // incorrect user public key
        assertDoesNotFail {
            result = cryptoApi.verifySignature(
                data,
                signature,
                IniConfig["Login", "user2PubKey"]
            )
        }
        assertFalse(result)
    }

    @Test
    fun generatePrivateKey() {
        lateinit var key: String
        // new private ECC key
        assertDoesNotFail { key = cryptoApi.generatePrivateKey("randomSeed") }
        assertNotNull(key)
    }

    @Test
    fun derivePrivateKey2() {
        lateinit var key: String
        // new public ECC key from a password using pbkdf2.
        assertDoesNotFail {
            key = cryptoApi.derivePrivateKey2("Strong password", "salt")
        }
        assertNotNull(key)
    }

    @Test
    fun derivePublicKey() {
        lateinit var key: String
        // new public ECC key
        assertDoesNotFail {
            key = cryptoApi.derivePublicKey(IniConfig["Login", "userPrivKey"])
        }
        assertNotNull(key)
    }

    @Test
    fun generateKeySymmetric() {
        lateinit var key: ByteArray
        assertDoesNotFail {
            key = cryptoApi.generateKeySymmetric()
        }
        assertNotNull(key)
    }

    @Test
    fun encryptAndDecryptDataSymmetric() {
        val key = cryptoApi.generateKeySymmetric()
        val data = "Very important data.".encodeToByteArray()
        lateinit var encryptedData: ByteArray
        lateinit var decryptedData: ByteArray

        // encrypt data
        assertDoesNotFail {
            encryptedData = cryptoApi.encryptDataSymmetric(data, key)
        }
        assertNotNull(encryptedData)

        // decrypt data
        assertDoesNotFail {
            decryptedData = cryptoApi.decryptDataSymmetric(encryptedData, key)
        }
        assertNotNull(decryptedData)

        assertContentEquals(data, decryptedData)
    }

    @Test
    @Throws(IOException::class)
    fun convertPEMKeyToWIFKey() {
        lateinit var key: String
        val pemKey = getResource(eccKeyPath).buffered().use {
            it.readString()
        }
        assertDoesNotFail { key = cryptoApi.convertPEMKeyToWIFKey(pemKey) }
        assertNotNull(key)
    }

    @Test
    @Throws(IOException::class)
    fun convertPGPAsn1KeyToBase58DERKey() {
        lateinit var key: String

        // key read from file
        val pgpKey = getResource(pgpKeyPath).buffered().use {
            it.readString()
        }
        assertDoesNotFail {
            key = cryptoApi.convertPGPAsn1KeyToBase58DERKey(pgpKey)
        }
        assertNotNull(key)
        assertNotNull(pgpKey)
    }

    @Test
    @Throws(Exception::class)
    fun generateBip39() {
        lateinit var bip39: BIP39
        lateinit var bip39WithPass: BIP39

        assertFailsWith(PrivmxException::class) {
            bip39 = cryptoApi.generateBip39(64)
        }

        // multiple of 32
        assertDoesNotFail {
            bip39 = cryptoApi.generateBip39(128)
        }
        assertDoesNotFail {
            bip39WithPass = cryptoApi.generateBip39(128, "bip39_password")
        }

        assertDoesNotFail {
            bip39 = cryptoApi.generateBip39(160)
        }
        assertDoesNotFail {
            bip39WithPass = cryptoApi.generateBip39(160, "bip39_password_160")
        }

        assertFalse(bip39.entropy.contentEquals(bip39WithPass.entropy))
        assertNotEquals(bip39.mnemonic, bip39WithPass.mnemonic)
    }

    @Test
    fun fromMnemonic() {
        lateinit var bip39FromMnemonic: BIP39
        lateinit var bip39FromMnemonicWithPass: BIP39

        val bip39 = cryptoApi.generateBip39(128)
        val bip39WithPass = cryptoApi.generateBip39(128, "bip39_password")
        assertFailsWith(PrivmxException::class) {
            bip39FromMnemonic = cryptoApi.fromMnemonic("0")
        }

        assertFailsWith(PrivmxException::class) {
            bip39FromMnemonicWithPass = cryptoApi.fromMnemonic(
                "0",
                "mnemonic_password"
            )
        }

        //bip39 generated without password & method fromMnemonic without password
        assertDoesNotFail {
            bip39FromMnemonic = cryptoApi.fromMnemonic(bip39.mnemonic)
        }
        assertContentEquals(bip39.entropy, bip39FromMnemonic.entropy)
        assertEquals(bip39.extKey.getPublicKey(), bip39FromMnemonic.extKey.getPublicKey())
        assertEquals(bip39.extKey.getPrivateKey(), bip39FromMnemonic.extKey.getPrivateKey())
        assertContentEquals(
            bip39.extKey.getPrivateEncKey(),
            bip39FromMnemonic.extKey.getPrivateEncKey()
        )

        //bip39 generated with password & method fromMnemonic without password
        assertDoesNotFail {
            bip39FromMnemonic = cryptoApi.fromMnemonic(bip39WithPass.mnemonic)
        }
        assertContentEquals(bip39WithPass.entropy, bip39FromMnemonic.entropy)

        assertNotEquals(
            bip39WithPass.extKey.getPublicKey(),
            bip39FromMnemonic.extKey.getPublicKey()
        )
        assertNotEquals(
            bip39WithPass.extKey.getPrivateKey(),
            bip39FromMnemonic.extKey.getPrivateKey()
        )
        assertFalse(
            bip39WithPass.extKey.getPrivateEncKey().contentEquals(
                bip39FromMnemonic.extKey.getPrivateEncKey()
            )
        )

        //bip39 generated without password & method fromMnemonic with password
        assertDoesNotFail {
            bip39FromMnemonicWithPass = cryptoApi.fromMnemonic(
                bip39.mnemonic,
                "mnemonic_password"
            )
        }
        assertContentEquals(bip39.entropy, bip39FromMnemonicWithPass.entropy)
        assertFalse(bip39WithPass.entropy.contentEquals(bip39FromMnemonicWithPass.entropy))

        assertNotEquals(
            bip39.extKey.getPublicKey(),
            bip39FromMnemonicWithPass.extKey.getPublicKey()
        )
        assertNotEquals(
            bip39.extKey.getPrivateKey(),
            bip39FromMnemonicWithPass.extKey.getPrivateKey()
        )
        assertFalse(
            bip39.extKey.getPrivateEncKey().contentEquals(
                bip39FromMnemonicWithPass.extKey.getPrivateEncKey()
            )
        )

        //bip39 generated with password & method fromMnemonic with password
        assertDoesNotFail {
            bip39FromMnemonicWithPass = cryptoApi.fromMnemonic(
                bip39WithPass.mnemonic,
                "mnemonic_password"
            )
        }
        assertContentEquals(bip39WithPass.entropy, bip39FromMnemonicWithPass.entropy)

        assertNotEquals(
            bip39WithPass.extKey.getPublicKey(),
            bip39FromMnemonicWithPass.extKey.getPublicKey()
        )
        assertNotEquals(
            bip39WithPass.extKey.getPrivateKey(),
            bip39FromMnemonicWithPass.extKey.getPrivateKey()
        )
        assertFalse(
            bip39WithPass.extKey.getPrivateEncKey().contentEquals(
                bip39FromMnemonicWithPass.extKey.getPrivateEncKey()
            )
        )
    }

    @Test
    fun fromEntropy() {
        lateinit var bip39FromEntropy: BIP39
        lateinit var bip39FromEntropyWithPass: BIP39

        val bip39: BIP39 = cryptoApi.generateBip39(128)
        val bip39WithPass: BIP39 = cryptoApi.generateBip39(128, "bip39_password")

        assertFailsWith(PrivmxException::class) {
            bip39FromEntropy = cryptoApi.fromEntropy("wong_entropy".encodeToByteArray())
        }

        assertFailsWith(PrivmxException::class) {
            bip39FromEntropyWithPass = cryptoApi.fromEntropy(
                "wong_entropy".encodeToByteArray(),
                "entropy_password"
            )
        }

        //bip39 generated without password & method fromEntropy without password
        assertDoesNotFail {
            bip39FromEntropy = cryptoApi.fromEntropy(bip39.entropy)
        }
        assertEquals(bip39.mnemonic, bip39FromEntropy.mnemonic)
        assertEquals(bip39.extKey.getPublicKey(), bip39FromEntropy.extKey.getPublicKey())
        assertEquals(bip39.extKey.getPrivateKey(), bip39FromEntropy.extKey.getPrivateKey())
        assertContentEquals(
            bip39.extKey.getPrivateEncKey(),
            bip39FromEntropy.extKey.getPrivateEncKey()
        )

        //bip39 generated with password & method fromEntropy without password
        assertDoesNotFail {
            bip39FromEntropy = cryptoApi.fromEntropy(bip39WithPass.entropy)
        }
        assertEquals(bip39WithPass.mnemonic, bip39FromEntropy.mnemonic)

        assertNotEquals(
            bip39WithPass.extKey.getPublicKey(),
            bip39FromEntropy.extKey.getPublicKey()
        )
        assertNotEquals(
            bip39WithPass.extKey.getPrivateKey(),
            bip39FromEntropy.extKey.getPrivateKey()
        )
        assertFalse(

            bip39WithPass.extKey.getPrivateEncKey().contentEquals(
                bip39FromEntropy.extKey.getPrivateEncKey()
            )
        )

        //bip39 generated without password & method fromEntropy with password
        assertDoesNotFail {
            bip39FromEntropyWithPass = cryptoApi.fromEntropy(bip39.entropy, "entropy_password")
        }
        assertEquals(bip39.mnemonic, bip39FromEntropyWithPass.mnemonic)
        assertNotEquals(bip39WithPass.mnemonic, bip39FromEntropyWithPass.mnemonic)

        assertNotEquals(
            bip39.extKey.getPublicKey(),
            bip39FromEntropyWithPass.extKey.getPublicKey()
        )
        assertNotEquals(
            bip39.extKey.getPrivateKey(),
            bip39FromEntropyWithPass.extKey.getPrivateKey()
        )
        assertFalse(
            bip39.extKey.getPrivateEncKey().contentEquals(
                bip39FromEntropyWithPass.extKey.getPrivateEncKey()
            )
        )

        //bip39 generated with password & method fromEntropy with password
        assertDoesNotFail {
            bip39FromEntropyWithPass = cryptoApi.fromEntropy(
                bip39WithPass.entropy,
                "entropy_password"
            )
        }
        assertEquals(bip39WithPass.mnemonic, bip39FromEntropyWithPass.mnemonic)

        assertNotEquals(
            bip39WithPass.extKey.getPublicKey(),
            bip39FromEntropyWithPass.extKey.getPublicKey()
        )
        assertNotEquals(
            bip39WithPass.extKey.getPrivateKey(),
            bip39FromEntropyWithPass.extKey.getPrivateKey()
        )
        assertFalse(
            bip39WithPass.extKey.getPrivateEncKey().contentEquals(
                bip39FromEntropyWithPass.extKey.getPrivateEncKey()
            )
        )

    }

    @Test
    fun entropyToMnemonic() {
        lateinit var mnemonicFromEntropy: String
        lateinit var mnemonicFromEntropyWithPass: String

        val bip39: BIP39 = cryptoApi.generateBip39(128)
        val bip39WithPass: BIP39 = cryptoApi.generateBip39(128, "bip39_password")

        assertFailsWith(PrivmxException::class) {
            mnemonicFromEntropy = cryptoApi.entropyToMnemonic("wrong_entropy".encodeToByteArray())
        }

        assertDoesNotFail {
            mnemonicFromEntropy = cryptoApi.entropyToMnemonic(bip39.entropy)
        }
        assertEquals(bip39.mnemonic, mnemonicFromEntropy)
        assertContentEquals(bip39.entropy, cryptoApi.fromMnemonic(mnemonicFromEntropy).entropy)
        assertEquals(
            bip39.extKey.getPrivateKey(),
            cryptoApi.fromMnemonic(mnemonicFromEntropy).extKey.getPrivateKey()
        )
        assertContentEquals(
            bip39.extKey.getPrivateEncKey(),
            cryptoApi.fromMnemonic(mnemonicFromEntropy).extKey.getPrivateEncKey()
        )

        assertDoesNotFail {
            mnemonicFromEntropyWithPass = cryptoApi.entropyToMnemonic(bip39WithPass.entropy)
        }
        assertEquals(bip39WithPass.mnemonic, mnemonicFromEntropyWithPass)
        assertNotEquals(bip39WithPass.mnemonic, mnemonicFromEntropy)

        assertContentEquals(
            bip39WithPass.entropy,
            cryptoApi.fromMnemonic(mnemonicFromEntropyWithPass).entropy
        )
        assertNotEquals(
            bip39WithPass.extKey.getPrivateKey(),
            cryptoApi.fromMnemonic(mnemonicFromEntropyWithPass).extKey.getPrivateKey()
        )
        assertFalse(
            bip39WithPass.extKey.getPrivateEncKey().contentEquals(
                cryptoApi.fromMnemonic(mnemonicFromEntropyWithPass).extKey.getPrivateEncKey()
            )
        )
    }

    @Test
    fun mnemonicToEntropy() {
        lateinit var entropyFromMnemonic: ByteArray
        lateinit var entropyFromMnemonicWithPass: ByteArray

        val bip39: BIP39 = cryptoApi.generateBip39(128)
        val bip39WithPass: BIP39 = cryptoApi.generateBip39(128, "bip39_password")

        assertFailsWith(PrivmxException::class) {
            entropyFromMnemonic = cryptoApi.mnemonicToEntropy("wrong_mnemonic")
        }

        assertDoesNotFail {
            entropyFromMnemonic = cryptoApi.mnemonicToEntropy(bip39.mnemonic)
        }
        assertContentEquals(bip39.entropy, entropyFromMnemonic)
        assertEquals(bip39.mnemonic, cryptoApi.fromEntropy(entropyFromMnemonic).mnemonic)
        assertEquals(
            bip39.extKey.getPrivateKey(),
            cryptoApi.fromEntropy(entropyFromMnemonic).extKey.getPrivateKey()
        )
        assertContentEquals(
            bip39.extKey.getPrivateEncKey(),
            cryptoApi.fromEntropy(entropyFromMnemonic).extKey.getPrivateEncKey()
        )

        assertDoesNotFail {
            entropyFromMnemonicWithPass = cryptoApi.mnemonicToEntropy(bip39WithPass.mnemonic)
        }
        assertContentEquals(bip39WithPass.entropy, entropyFromMnemonicWithPass)
        assertFalse(bip39.entropy.contentEquals(entropyFromMnemonicWithPass))
        assertEquals(
            bip39WithPass.mnemonic,
            cryptoApi.fromEntropy(entropyFromMnemonicWithPass).mnemonic
        )

        assertNotEquals(
            bip39WithPass.extKey.getPublicKey(),
            cryptoApi.fromEntropy(entropyFromMnemonicWithPass).extKey.getPublicKey()
        )
        assertNotEquals(
            bip39WithPass.extKey.getPrivateKey(),
            cryptoApi.fromEntropy(entropyFromMnemonicWithPass).extKey.getPrivateKey()
        )
        assertFalse(
            bip39WithPass.extKey.getPrivateEncKey().contentEquals(
                cryptoApi.fromEntropy(entropyFromMnemonicWithPass).extKey.getPrivateEncKey()
            )
        )
    }

    @Test
    fun mnemonicToSeed() {
        lateinit var seedFromMnemonic: ByteArray
        lateinit var seedFromMnemonicWithPass: ByteArray

        val bip39: BIP39 = cryptoApi.generateBip39(128)
        val bip39WithPass: BIP39 = cryptoApi.generateBip39(128, "bip39_password")

        assertDoesNotFail {
            seedFromMnemonic = cryptoApi.mnemonicToSeed(bip39.mnemonic)
        }

        assertDoesNotFail {
            seedFromMnemonicWithPass = cryptoApi.mnemonicToSeed(bip39WithPass.mnemonic)
        }

        assertFalse(seedFromMnemonic.contentEquals(seedFromMnemonicWithPass))
    }
}