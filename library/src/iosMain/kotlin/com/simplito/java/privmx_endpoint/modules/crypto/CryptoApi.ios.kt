package com.simplito.java.privmx_endpoint.modules.crypto

import cnames.structs.pson_value
import com.simplito.java.privmx_endpoint.model.exceptions.NativeException
import com.simplito.java.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.java.privmx_endpoint.utils.asResponse
import com.simplito.java.privmx_endpoint.utils.makeArgs
import com.simplito.java.privmx_endpoint.utils.pson
import com.simplito.java.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execCryptoApi
import libprivmxendpoint.privmx_endpoint_freeCryptoApi
import libprivmxendpoint.privmx_endpoint_newCryptoApi

/**
 * Defines cryptographic methods.
 *
 * @category crypto
 */
@OptIn(ExperimentalForeignApi::class)
actual class CryptoApi : AutoCloseable {
    private val _nativeCryptoApi = nativeHeap.allocPointerTo<cnames.structs.CryptoApi>()
    private val nativeCryptoApi
        get() = _nativeCryptoApi.value?.let { _nativeCryptoApi }
            ?: throw IllegalStateException("CryptoApi has been closed.")

    init {
        privmx_endpoint_newCryptoApi(_nativeCryptoApi.ptr)
        memScoped {
            allocPointerTo<pson_value>().apply {
                privmx_endpoint_execCryptoApi(nativeCryptoApi.value, 0, makeArgs(), ptr)
            }.value!!.asResponse?.getResultOrThrow()
        }
    }

    /**
     * Generates a new private ECC key.
     *
     * @param randomSeed optional string used as the base to generate the new key
     * @return Generated ECC key in WIF format
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual fun generatePrivateKey(randomSeed: String?): String? = memScoped {
        allocPointerTo<pson_value>().apply {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                2,
                makeArgs(randomSeed!!.pson),
                ptr
            )
        }.value!!.asResponse
            ?.getResultOrThrow()
            ?.typedValue()
    }

    /**
     * Generates a new private ECC key from a password using pbkdf2.
     *
     * @param password the password used to generate the new key
     * @param salt     random string (additional input for the hashing function)
     * @return Generated ECC key in WIF format
     */
    @Deprecated("Use {@link CryptoApi#derivePrivateKey2(String, String)} instead.")
    @Throws(PrivmxException::class, NativeException::class)
    actual fun derivePrivateKey(password: String?, salt: String?): String? = memScoped {
        allocPointerTo<pson_value>().apply {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                3,
                makeArgs(password!!.pson, salt!!.pson),
                ptr
            )
        }.value!!.asResponse
            ?.getResultOrThrow()
            ?.typedValue()
    }

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
    actual fun derivePrivateKey2(password: String?, salt: String?): String? = memScoped {
        allocPointerTo<pson_value>().apply {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                4,
                makeArgs(password!!.pson, salt!!.pson),
                ptr
            )
        }.value!!.asResponse
            ?.getResultOrThrow()
            ?.typedValue()
    }

    /**
     * Generates a new public ECC key as a pair for an existing private key.
     *
     * @param privateKey private ECC key in WIF format
     * @return Generated ECC key in BASE58DER format
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual fun derivePublicKey(privateKey: String?): String? = memScoped {
        allocPointerTo<pson_value>().apply {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                5,
                makeArgs(privateKey!!.pson),
                ptr
            )
        }.value!!.asResponse
            ?.getResultOrThrow()
            ?.typedValue()
    }

    /**
     * Encrypts buffer with a given key using AES.
     *
     * @param data         buffer to encrypt
     * @param symmetricKey key used to encrypt data
     * @return Encrypted data buffer
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual fun encryptDataSymmetric(
        data: ByteArray?,
        symmetricKey: ByteArray?
    ): ByteArray? = memScoped {
        allocPointerTo<pson_value>().apply {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                7,
                makeArgs(data!!.pson, symmetricKey!!.pson),
                ptr
            )
        }.value!!.asResponse
            ?.getResultOrThrow()
            ?.typedValue()
    }

    /**
     * Decrypts buffer with a given key using AES.
     *
     * @param data         buffer to decrypt
     * @param symmetricKey key used to decrypt data
     * @return Plain (decrypted) data buffer
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual fun decryptDataSymmetric(
        data: ByteArray?,
        symmetricKey: ByteArray?
    ): ByteArray? = memScoped {
        allocPointerTo<pson_value>().apply {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                8,
                makeArgs(data!!.pson, symmetricKey!!.pson),
                ptr
            )
        }.value!!.asResponse
            ?.getResultOrThrow()
            ?.typedValue()
    }

    /**
     * Creates a signature of data using given key.
     *
     * @param data       data the buffer to sign
     * @param privateKey the key used to sign data
     * @return Signature of data
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual fun signData(data: ByteArray?, privateKey: String?): ByteArray? = memScoped {
        allocPointerTo<pson_value>().apply {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                1,
                makeArgs(data!!.pson, privateKey!!.pson),
                ptr
            )
        }.value!!.asResponse
            ?.getResultOrThrow()
            ?.typedValue()
    }

    /**
     * Validate a signature of data using given key.
     *
     * @param data      buffer
     * @param signature of data
     * @param publicKey public ECC key in BASE58DER format used to validate data
     * @return data validation result
     */
    actual fun verifySignature(
        data: ByteArray?,
        signature: ByteArray?,
        publicKey: String?
    ): Boolean = memScoped {
        allocPointerTo<pson_value>().apply {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                10,
                makeArgs(data!!.pson, signature!!.pson, publicKey!!.pson),
                ptr
            )
        }.value!!.asResponse
            ?.getResultOrThrow()
            ?.typedValue<Boolean>() == true
    }

    /**
     * Converts given private key in PEM format to its WIF format.
     *
     * @param pemKey private key to convert
     * @return Private key in WIF format
     */
    @Throws(PrivmxException::class, NativeException::class)
    actual fun convertPEMKeyToWIFKey(pemKey: String?): String? = memScoped {
        allocPointerTo<pson_value>().apply {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                9,
                makeArgs(pemKey!!.pson),
                ptr
            )
        }.value!!.asResponse
            ?.getResultOrThrow()
            ?.typedValue()
    }

    /**
     * Generates a new symmetric key.
     *
     * @return Generated key
     */
    actual fun generateKeySymmetric(): ByteArray? = memScoped {
        allocPointerTo<pson_value>().apply {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                6,
                makeArgs(),
                ptr
            )
        }.value!!.asResponse
            ?.getResultOrThrow()
            ?.typedValue()
    }

    actual override fun close() {
        if (_nativeCryptoApi.value == null) return
        privmx_endpoint_freeCryptoApi(_nativeCryptoApi.value)
        _nativeCryptoApi.value = null
    }
}