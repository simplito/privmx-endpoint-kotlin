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

import cnames.structs.pson_value
import com.simplito.kotlin.privmx_endpoint.model.BIP39
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.utils.PsonValue
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import com.simplito.kotlin.privmx_endpoint.utils.makeArgs
import com.simplito.kotlin.privmx_endpoint.utils.nullablePson
import com.simplito.kotlin.privmx_endpoint.utils.pson
import com.simplito.kotlin.privmx_endpoint.utils.toBip39
import com.simplito.kotlin.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execCryptoApi
import libprivmxendpoint.privmx_endpoint_freeCryptoApi
import libprivmxendpoint.privmx_endpoint_newCryptoApi
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_free_value

/**
 * Defines cryptographic methods.
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
            val args = makeArgs()
            val result = allocPointerTo<pson_value>()
            try {
                privmx_endpoint_execCryptoApi(nativeCryptoApi.value, 0, args, result.ptr)
                result.value!!.asResponse?.getResultOrThrow()
            } finally {
                pson_free_result(result.value)
                pson_free_value(args)
            }
        }
    }

    /**
     * Generates a new private ECC key.
     *
     * @param randomSeed optional string used as the base to generate the new key
     * @return Generated ECC key in WIF format
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun generatePrivateKey(randomSeed: String): String = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(randomSeed.pson)
        try {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                2,
                args,
                result.ptr
            )
            return result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
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
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun derivePrivateKey2(password: String, salt: String): String = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(password.pson, salt.pson)
        try {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                4,
                args,
                result.ptr
            )
            result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }

    /**
     * Generates a new public ECC key as a pair for an existing private key.
     *
     * @param privateKey private ECC key in WIF format
     * @return Generated ECC key in BASE58DER format
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun derivePublicKey(privateKey: String): String = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(privateKey.pson)
        try {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                5,
                args,
                result.ptr
            )
            result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }

    /**
     * Encrypts buffer with a given key using AES.
     *
     * @param data         buffer to encrypt
     * @param symmetricKey key used to encrypt data
     * @return Encrypted data buffer
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun encryptDataSymmetric(
        data: ByteArray,
        symmetricKey: ByteArray
    ): ByteArray = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(data.pson, symmetricKey.pson)
        try {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                7,
                args,
                result.ptr
            )
            result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }

    /**
     * Decrypts buffer with a given key using AES.
     *
     * @param data         buffer to decrypt
     * @param symmetricKey key used to decrypt data
     * @return Plain (decrypted) data buffer
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun decryptDataSymmetric(
        data: ByteArray,
        symmetricKey: ByteArray
    ): ByteArray = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(data.pson, symmetricKey.pson)
        try {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                8,
                args,
                result.ptr
            )
            result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }

    /**
     * Creates a signature of data using given key.
     *
     * @param data       data the buffer to sign
     * @param privateKey the key used to sign data
     * @return Signature of data
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun signData(data: ByteArray, privateKey: String): ByteArray = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(data.pson, privateKey.pson)
        try {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                1,
                args,
                result.ptr
            )
            result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }

    /**
     * Validate a signature of data using given key.
     *
     * @param data      buffer
     * @param signature of data
     * @param publicKey public ECC key in BASE58DER format used to validate data
     * @return data validation result
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun verifySignature(
        data: ByteArray,
        signature: ByteArray,
        publicKey: String
    ): Boolean = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(data!!.pson, signature!!.pson, publicKey!!.pson)
        try {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                10,
                args,
                result.ptr
            )
            result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue<Boolean>() == true
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }

    /**
     * Converts given private key in PEM format to its WIF format.
     *
     * @param pemKey private key to convert
     * @return Private key in WIF format
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun convertPEMKeyToWIFKey(pemKey: String): String = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(pemKey.pson)
        try {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                9,
                args,
                result.ptr
            )
            result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }

    /**
     * Generates a new symmetric key.
     *
     * @return Generated key
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun generateKeySymmetric(): ByteArray = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                6,
                args,
                result.ptr
            )
            result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }

    /**
     * Converts given public key in PGP format to its base58DER format.
     *
     * @param pgpKey public key to convert
     * @return public key in base58DER format
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun convertPGPAsn1KeyToBase58DERKey(pgpKey: String): String = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(pgpKey.pson)
        try {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                17,
                args,
                result.ptr
            )
            result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }

    /**
     * Generates ECC key and BIP-39 mnemonic from a password using BIP-39.
     *
     * @param strength size of BIP-39 entropy, must be a multiple of 32
     * @param password the password used to generate the Key
     * @return BIP39 object containing ECC Key and associated with it BIP-39 mnemonic and entropy
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun generateBip39(
        strength: Long,
        password: String
    ): BIP39 = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            strength.nullablePson,
            password.pson
        )
        try {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                11,
                args,
                pson_result.ptr
            )
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toBip39()
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

    /**
     * Generates ECC key using BIP-39 mnemonic.
     *
     * @param mnemonic the BIP-39 entropy used to generate the Key
     * @param password the password used to generate the Key
     * @return BIP39 object containing ECC Key and associated with it BIP-39 mnemonic and entropy
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun fromMnemonic(
        mnemonic: String,
        password: String
    ): BIP39 = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            mnemonic.pson,
            password.pson
        )
        try {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                12,
                args,
                pson_result.ptr
            )
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toBip39()
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

    /**
     * Generates ECC key using BIP-39 entropy.
     *
     * @param entropy  the BIP-39 entropy used to generate the Key
     * @param password the password used to generate the Key
     * @return BIP39 object containing ECC Key and associated with it BIP-39 mnemonic and entropy
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun fromEntropy(
        entropy: ByteArray,
        password: String
    ): BIP39 = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            entropy.pson,
            password.pson
        )
        try {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                13,
                args,
                pson_result.ptr
            )
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toBip39()
        } finally {
            pson_free_result(pson_result.value)
            pson_free_value(args)
        }
    }

    /**
     * Converts BIP-39 entropy to mnemonic.
     *
     * @param entropy BIP-39 entropy
     * @return BIP-39 mnemonic
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun entropyToMnemonic(entropy: ByteArray): String = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(entropy.pson)
        try {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                14,
                args,
                result.ptr
            )
            result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }

    /**
     * Converts BIP-39 mnemonic to entropy.
     *
     * @param mnemonic BIP-39 mnemonic
     * @return BIP-39 entropy
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun mnemonicToEntropy(mnemonic: String): ByteArray = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(mnemonic.pson)
        try {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                15,
                args,
                result.ptr
            )
            result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }

    /**
     * Generates a seed used to generate a key using BIP-39 mnemonic with PBKDF2.
     *
     * @param mnemonic BIP-39 mnemonic
     * @param password the password used to generate the seed
     * @return generated seed
     * @throws PrivmxException thrown when method encounters an exception
     * @throws NativeException thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun mnemonicToSeed(mnemonic: String, password: String): ByteArray = memScoped {
        val result = allocPointerTo<pson_value>()
        val args = makeArgs(mnemonic.pson, password.pson)
        try {
            privmx_endpoint_execCryptoApi(
                nativeCryptoApi.value,
                16,
                args,
                result.ptr
            )
            result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_result(result.value)
            pson_free_value(args)
        }
    }

    /**
     * Frees memory.
     *
     * @throws Exception when instance is currently closed.
     */
    actual override fun close() {
        privmx_endpoint_freeCryptoApi(nativeCryptoApi.value)
        _nativeCryptoApi.value = null
    }
}