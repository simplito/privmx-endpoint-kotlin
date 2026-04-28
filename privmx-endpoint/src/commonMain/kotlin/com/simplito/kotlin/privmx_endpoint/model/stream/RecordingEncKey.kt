package com.simplito.kotlin.privmx_endpoint.model.stream

data class RecordingEncKey(
    val id: ByteArray,
    val key: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RecordingEncKey

        if (!id.contentEquals(other.id)) return false
        if (!key.contentEquals(other.key)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.contentHashCode()
        result = 31 * result + key.contentHashCode()
        return result
    }
}