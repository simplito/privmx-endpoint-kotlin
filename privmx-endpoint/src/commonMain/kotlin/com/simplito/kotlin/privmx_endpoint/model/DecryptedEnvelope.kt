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

/**
 * The plaintext of an envelope, together with what could be established about who wrote it.
 *
 * @property data Decrypted content
 * @property groupId ID of the Group the envelope was sealed for. Authenticated — an envelope that names a Group
 * it was not sealed for does not decrypt
 * @property authorPubKey Public key of the author (base58-DER encoded), whose signature over the envelope has
 * been verified. EMPTY when [type] is [EnvelopeType.ENVELOPE_ANONYMOUS] — branch on [type], not on this field
 * being non-empty
 * @property type Which of the Group's keys sealed this envelope
 */
data class DecryptedEnvelope(
    val data: ByteArray,
    val groupId: String,
    val authorPubKey: String,
    val type: EnvelopeType
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DecryptedEnvelope

        if (!data.contentEquals(other.data)) return false
        if (groupId != other.groupId) return false
        if (authorPubKey != other.authorPubKey) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + groupId.hashCode()
        result = 31 * result + authorPubKey.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}
