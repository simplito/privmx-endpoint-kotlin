//
// PrivMX Endpoint Kotlin.
// Copyright © 2025 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.simplito.kotlin.privmx_endpoint.modules.crypto

import com.simplito.java.privmx_endpoint.model.BIP39
import com.simplito.kotlin.privmx_endpoint.LibLoader
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import java.lang.AutoCloseable

/**
 * Defines cryptographic methods.
 */
actual class CryptoApi : AutoCloseable {

    companion object {
        init {
            LibLoader.load()
        }
    }

    private val api: Long? = init()

    private external fun init(): Long?

    @Throws(IllegalStateException::class)
    private external fun deinit()

    /**
     * Generates a new private ECC key.
     *
     * @param randomSeed optional string used as the base to generate the new key
     * @return Generated ECC key in WIF format
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual external fun generatePrivateKey(randomSeed: String): String

    /**
     * Generates a new private ECC key from a password using pbkdf2.
     * This version of the derive function has a rounds count increased to 200k.
     * This makes using this function a safer choice, but it makes the received key
     * different than in the original version.
     *
     * @param password the password used to generate the new key
     * @param salt     random string (additional input for the hashing function)
     * @return generated ECC key in WIF format
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual external fun derivePrivateKey2(password: String, salt: String): String

    /**
     * Generates a new public ECC key as a pair for an existing private key.
     *
     * @param privateKey private ECC key in WIF format
     * @return Generated ECC key in BASE58DER format
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual external fun derivePublicKey(privateKey: String): String

    /**
     * Encrypts buffer with a given key using AES.
     *
     * @param data         buffer to encrypt
     * @param symmetricKey key used to encrypt data
     * @return Encrypted data buffer
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual external fun encryptDataSymmetric(data: ByteArray, symmetricKey: ByteArray): ByteArray

    /**
     * Decrypts buffer with a given key using AES.
     *
     * @param data         buffer to decrypt
     * @param symmetricKey key used to decrypt data
     * @return Plain (decrypted) data buffer
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual external fun decryptDataSymmetric(data: ByteArray, symmetricKey: ByteArray): ByteArray

    /**
     * Creates a signature of data using given key.
     *
     * @param data       data the buffer to sign
     * @param privateKey the key used to sign data
     * @return Signature of data
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual external fun signData(data: ByteArray, privateKey: String): ByteArray

    /**
     * Validate a signature of data using given key.
     *
     * @param data      buffer
     * @param signature of data
     * @param publicKey public ECC key in BASE58DER format used to validate data
     * @return data validation result
     */
    actual external fun verifySignature(
        data: ByteArray,
        signature: ByteArray,
        publicKey: String
    ): Boolean

    /**
     * Converts given private key in PEM format to its WIF format.
     *
     * @param pemKey private key to convert
     * @return Private key in WIF format
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual external fun convertPEMKeyToWIFKey(pemKey: String): String

    /**
     * Converts given public key in PGP format to its base58DER format.
     *
     * @param pgpKey public key to convert
     * @return public key in base58DER format
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual external fun convertPGPAsn1KeyToBase58DERKey(pgpKey: String): String

    /**
     * Generates ECC key and BIP-39 mnemonic from a password using BIP-39.
     *
     * @param strength size of BIP-39 entropy, must be a multiple of 32
     * @param password the password used to generate the Key
     * @return BIP39 object containing ECC Key and associated with it BIP-39 mnemonic and entropy
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     */
    @JvmOverloads
    @Throws(PrivmxException::class, NativeException::class)
    actual external fun generateBip39(strength: Long?, password: String): BIP39

    /**
     * Generates ECC key using BIP-39 mnemonic.
     *
     * @param mnemonic the BIP-39 entropy used to generate the Key
     * @param password the password used to generate the Key
     * @return BIP39 object containing ECC Key and associated with it BIP-39 mnemonic and entropy
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     */
    @JvmOverloads
    @Throws(PrivmxException::class, NativeException::class)
    actual external fun fromMnemonic(mnemonic: String, password: String): BIP39

    /**
     * Generates ECC key using BIP-39 entropy.
     *
     * @param entropy  the BIP-39 entropy used to generate the Key
     * @param password the password used to generate the Key
     * @return BIP39 object containing ECC Key and associated with it BIP-39 mnemonic and entropy
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     */
    @JvmOverloads
    @Throws(PrivmxException::class, NativeException::class)
    actual external fun fromEntropy(entropy: ByteArray, password: String): BIP39

    /**
     * Converts BIP-39 entropy to mnemonic.
     *
     * @param entropy BIP-39 entropy
     * @return BIP-39 mnemonic
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual external fun entropyToMnemonic(entropy: ByteArray): String

    /**
     * Converts BIP-39 mnemonic to entropy.
     *
     * @param mnemonic BIP-39 mnemonic
     * @return BIP-39 entropy
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual external fun mnemonicToEntropy(mnemonic: String): ByteArray

    /**
     * Generates a seed used to generate a key using BIP-39 mnemonic with PBKDF2.
     *
     * @param mnemonic BIP-39 mnemonic
     * @param password the password used to generate the seed
     * @return generated seed
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     */
    @JvmOverloads
    @Throws(PrivmxException::class, NativeException::class)
    actual external fun mnemonicToSeed(mnemonic: String, password: String): ByteArray

    /**
     * Generates a new symmetric key.
     *
     * @return Generated key
     */
    actual external fun generateKeySymmetric(): ByteArray

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed.
     */
    @Throws(Exception::class)
    actual override fun close() {
        deinit()
    }
}