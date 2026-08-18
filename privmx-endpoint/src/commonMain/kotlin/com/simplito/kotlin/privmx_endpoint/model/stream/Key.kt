package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * Holds an encryption key of a Stream Room.
 *
 * @property keyId  ID of the key
 * @property key    key bytes
 * @property type   determines whether the key is used to encrypt or to decrypt data
 */
data class Key(
    var keyId: String,
    var key: ByteArray,
    val type: KeyType
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Key

        if (keyId != other.keyId) return false
        if (!key.contentEquals(other.key)) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyId.hashCode()
        result = 31 * result + key.contentHashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}