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

package com.simplito.kotlin.privmx_endpoint.model

import com.simplito.kotlin.privmx_endpoint.modules.crypto.ExtKey

/**
 * Class containing ECC generated key using BIP-39.
 *
 * @property mnemonic BIP-39 mnemonic
 * @property extKey BIP-39 Ecc Key
 * @property entropy BIP-39 entropy
 */
data class BIP39(
    val mnemonic: String,
    val extKey: ExtKey,
    val entropy: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BIP39

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