package com.simplito.kotlin.privmx_endpoint.modules.crypto

import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException

/**
 * Class representing Extended keys and operations on it.
 */
expect class ExtKey : AutoCloseable {
    companion object {
        /**
         * Creates ExtKey from given seed.
         *
         * @param seed the seed used to generate Key
         * @return ExtKey object
         *
         * @throws PrivmxException       thrown when method encounters an exception
         * @throws NativeException       thrown when method encounters an unknown exception
         */
        @Throws(PrivmxException::class, NativeException::class)
        fun fromSeed(seed: ByteArray): ExtKey

        /**
         * Decodes ExtKey from Base58 format.
         *
         * @param base58 the ExtKey in Base58
         * @return ExtKey object
         *
         * @throws PrivmxException       thrown when method encounters an exception
         * @throws NativeException       thrown when method encounters an unknown exception
         */
        @Throws(PrivmxException::class, NativeException::class)
        fun fromBase58(base58: String): ExtKey

        /**
         * Generates a new ExtKey.
         *
         * @return ExtKey object
         *
         * @throws PrivmxException       thrown when method encounters an exception
         * @throws NativeException       thrown when method encounters an unknown exception
         */
        @Throws(PrivmxException::class, NativeException::class)
        fun generateRandom(): ExtKey
    }

    /**
     * Generates child ExtKey from a current ExtKey using BIP32.
     *
     * @param index number from 0 to 2^31-1
     * @return ExtKey object
     *
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun derive(index: Int): ExtKey

    /**
     * Generates hardened child ExtKey from a current ExtKey using BIP32.
     *
     * @param index number from 0 to 2^31-1
     * @return ExtKey object
     *
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun deriveHardened(index: Int): ExtKey

    /**
     * Converts ExtKey to Base58 string.
     *
     * @return ExtKey in Base58 format
     *
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun getPrivatePartAsBase58(): String

    /**
     * Converts the public part of ExtKey to Base58 string.
     *
     * @return ExtKey in Base58 format
     *
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun getPublicPartAsBase58(): String

    /**
     * Extracts ECC PrivateKey.
     *
     * @return ECC key in WIF format
     *
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun getPrivateKey(): String

    /**
     * Extracts ECC PublicKey.
     *
     * @return ECC key in BASE58DER format
     *
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun getPublicKey(): String

    /**
     * Extracts raw ECC PrivateKey.
     *
     * @return ECC PrivateKey
     *
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun getPrivateEncKey(): ByteArray

    /**
     * Extracts ECC PublicKey Address.
     *
     * @return ECC Address in BASE58 format
     *
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun getPublicKeyAsBase58Address(): String

    /**
     * Gets the chain code of Extended Key.
     *
     * @return Raw chain code
     *
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun getChainCode(): ByteArray

    /**
     * Validates a signature of a message.
     *
     * @param message   data used on validation
     * @param signature signature of data to verify
     * @return message validation result
     *
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun verifyCompactSignatureWithHash(message: ByteArray, signature: ByteArray): Boolean

    /**
     * Checks if ExtKey is Private.
     *
     * @return returns true if ExtKey is private
     *
     * @throws PrivmxException       thrown when method encounters an exception
     * @throws NativeException       thrown when method encounters an unknown exception
     * @throws IllegalStateException thrown when instance is closed
     */
    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    fun isPrivate(): Boolean

    override fun close()
}