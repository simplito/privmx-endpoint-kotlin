package com.simplito.kotlin.privmx_endpoint.modules.crypto

import com.simplito.kotlin.privmx_endpoint.LibLoader
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException

/**
 * Class representing Extended keys and operations on it.
 */
actual class ExtKey : AutoCloseable {

    private val key: Long

    private constructor(key: Long) {
        this.key = key
    }

    actual companion object {
        init {
            LibLoader.load()
        }

        /**
         * Creates ExtKey from given seed.
         *
         * @param seed the seed used to generate Key
         * @return ExtKey object
         */
        @JvmStatic
        @Throws(PrivmxException::class, NativeException::class)
        actual external fun fromSeed(seed: ByteArray): ExtKey

        /**
         * Decodes ExtKey from Base58 format.
         *
         * @param base58 the ExtKey in Base58
         * @return ExtKey object
         */
        @JvmStatic
        @Throws(PrivmxException::class, NativeException::class)
        actual external fun fromBase58(base58: String): ExtKey

        /**
         * Generates a new ExtKey.
         *
         * @return ExtKey object
         */
        @JvmStatic
        @Throws(PrivmxException::class, NativeException::class)
        actual external fun generateRandom(): ExtKey
    }


    /**
     * Generates child ExtKey from a current ExtKey using BIP32.
     *
     * @param index number from 0 to 2^31-1
     * @return ExtKey object
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun derive(index: Int): ExtKey

    /**
     * Generates hardened child ExtKey from a current ExtKey using BIP32.
     *
     * @param index number from 0 to 2^31-1
     * @return ExtKey object
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun deriveHardened(index: Int): ExtKey

    /**
     * Converts ExtKey to Base58 string.
     *
     * @return ExtKey in Base58 format
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun getPrivatePartAsBase58(): String

    /**
     * Converts the public part of ExtKey to Base58 string.
     *
     * @return ExtKey in Base58 format
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun getPublicPartAsBase58(): String

    /**
     * Extracts ECC PrivateKey.
     *
     * @return ECC key in WIF format
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun getPrivateKey(): String

    /**
     * Extracts ECC PublicKey.
     *
     * @return ECC key in BASE58DER format
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun getPublicKey(): String

    /**
     * Extracts raw ECC PrivateKey.
     *
     * @return ECC PrivateKey
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun getPrivateEncKey(): ByteArray

    /**
     * Extracts ECC PublicKey Address.
     *
     * @return ECC Address in BASE58 format
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun getPublicKeyAsBase58Address(): String

    /**
     * Gets the chain code of Extended Key.
     *
     * @return Raw chain code
     */
    actual external fun getChainCode(): ByteArray

    /**
     * Validates a signature of a message.
     *
     * @param message   data used on validation
     * @param signature signature of data to verify
     * @return message validation result
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun verifyCompactSignatureWithHash(
        message: ByteArray,
        signature: ByteArray
    ): Boolean

    /**
     * Checks if ExtKey is Private.
     *
     * @return returns true if ExtKey is private
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual external fun isPrivate(): Boolean

    @Throws(IllegalStateException::class)
    private external fun deinit()

    actual override fun close() {
        deinit()
    }

}