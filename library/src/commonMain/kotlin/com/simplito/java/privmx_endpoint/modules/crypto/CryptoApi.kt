//
// PrivMX Endpoint Java.
// Copyright © 2024 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.simplito.java.privmx_endpoint.modules.crypto

import com.simplito.java.privmx_endpoint.model.exceptions.NativeException
import com.simplito.java.privmx_endpoint.model.exceptions.PrivmxException

/**
 * Defines cryptographic methods.
 *
 * @category crypto
 */
expect class CryptoApi() : AutoCloseable {

    /**
     * Generates a new private ECC key.
     *
     * @param randomSeed optional string used as the base to generate the new key
     * @return Generated ECC key in WIF format
     */
    @Throws(PrivmxException::class, NativeException::class)
    fun generatePrivateKey(randomSeed: String?): String?

    /**
     * Generates a new private ECC key from a password using pbkdf2.
     *
     * @param password the password used to generate the new key
     * @param salt     random string (additional input for the hashing function)
     * @return Generated ECC key in WIF format
     */
    @Deprecated("Use {@link CryptoApi#derivePrivateKey2(String, String)} instead.")
    @Throws(PrivmxException::class, NativeException::class)
    fun derivePrivateKey(password: String?, salt: String?): String?

    /**
     * Generates a new private ECC key from a password using pbkdf2.
     * This version of the derive function has a rounds count increased to 200k.
     * This makes using this function a safer choice, but it makes the received key
     * different than in the original version.
     *
     * @param password the password used to generate the new key
     * @param salt     random string (additional input for the hashing function)
     * @return generated ECC key in WIF format
     */
    @Throws(PrivmxException::class, NativeException::class)
    fun derivePrivateKey2(password: String?, salt: String?): String?

    /**
     * Generates a new public ECC key as a pair for an existing private key.
     *
     * @param privateKey private ECC key in WIF format
     * @return Generated ECC key in BASE58DER format
     */
    @Throws(PrivmxException::class, NativeException::class)
    fun derivePublicKey(privateKey: String?): String?

    /**
     * Encrypts buffer with a given key using AES.
     *
     * @param data         buffer to encrypt
     * @param symmetricKey key used to encrypt data
     * @return Encrypted data buffer
     */
    @Throws(PrivmxException::class, NativeException::class)
    fun encryptDataSymmetric(data: ByteArray?, symmetricKey: ByteArray?): ByteArray?

    /**
     * Decrypts buffer with a given key using AES.
     *
     * @param data         buffer to decrypt
     * @param symmetricKey key used to decrypt data
     * @return Plain (decrypted) data buffer
     */
    @Throws(PrivmxException::class, NativeException::class)
    fun decryptDataSymmetric(data: ByteArray?, symmetricKey: ByteArray?): ByteArray?

    /**
     * Creates a signature of data using given key.
     *
     * @param data       data the buffer to sign
     * @param privateKey the key used to sign data
     * @return Signature of data
     */
    @Throws(PrivmxException::class, NativeException::class)
    fun signData(data: ByteArray?, privateKey: String?): ByteArray?

    /**
     * Validate a signature of data using given key.
     *
     * @param data      buffer
     * @param signature of data
     * @param publicKey public ECC key in BASE58DER format used to validate data
     * @return data validation result
     */
    fun verifySignature(
        data: ByteArray?,
        signature: ByteArray?,
        publicKey: String?
    ): Boolean

    /**
     * Converts given private key in PEM format to its WIF format.
     *
     * @param pemKey private key to convert
     * @return Private key in WIF format
     */
    @Throws(PrivmxException::class, NativeException::class)
    fun convertPEMKeyToWIFKey(pemKey: String?): String?

    /**
     * Generates a new symmetric key.
     *
     * @return Generated key
     */
    fun generateKeySymmetric(): ByteArray?

    override fun close()
}
