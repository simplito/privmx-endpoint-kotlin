package com.simplito.java.privmx_endpoint.model

import com.simplito.kotlin.privmx_endpoint.modules.crypto.ExtKey

/**
 * Class containing ECC generated key using BIP-39.
 *
 * @property mnemonic BIP-39 mnemonic
 * @property extKey BIP-39 mnemonic
 * @property entropy BIP-39 entropy
 */
data class BIP39_t(
    val mnemonic: String,
    val extKey: ExtKey,
    val entropy: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BIP39_t

        if (mnemonic != other.mnemonic) return false
        if (extKey != other.extKey) return false
        if (!entropy.contentEquals(other.entropy)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mnemonic.hashCode()
        result = 31 * result + extKey.hashCode()
        result = 31 * result + entropy.contentHashCode()
        return result
    }
}