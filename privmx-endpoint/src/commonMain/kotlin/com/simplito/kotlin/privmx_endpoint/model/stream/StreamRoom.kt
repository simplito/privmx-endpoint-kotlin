package com.simplito.kotlin.privmx_endpoint.model.stream

import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicyWithoutItem

data class StreamRoom(
    val contextId: String,
    val streamRoomId: String,
    val createDate: Long,
    val creator: String,
    val lastModificationDate: Long,
    val lastModifier: String,
    val users: List<String>,
    val managers: List<String>,
    val version: Long,
    val publicMeta: ByteArray,
    val privateMeta: ByteArray,
    val policy: ContainerPolicyWithoutItem,
    val statusCode: Long,
    val schemaVersion: Long,
    val state: String,   // "created" | "open" | "closed"
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as StreamRoom

        if (createDate != other.createDate) return false
        if (lastModificationDate != other.lastModificationDate) return false
        if (version != other.version) return false
        if (statusCode != other.statusCode) return false
        if (schemaVersion != other.schemaVersion) return false
        if (contextId != other.contextId) return false
        if (streamRoomId != other.streamRoomId) return false
        if (creator != other.creator) return false
        if (lastModifier != other.lastModifier) return false
        if (users != other.users) return false
        if (managers != other.managers) return false
        if (!publicMeta.contentEquals(other.publicMeta)) return false
        if (!privateMeta.contentEquals(other.privateMeta)) return false
        if (policy != other.policy) return false
        if (state != other.state) return false

        return true
    }

    override fun hashCode(): Int {
        var result = createDate.hashCode()
        result = 31 * result + lastModificationDate.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + statusCode.hashCode()
        result = 31 * result + schemaVersion.hashCode()
        result = 31 * result + contextId.hashCode()
        result = 31 * result + streamRoomId.hashCode()
        result = 31 * result + creator.hashCode()
        result = 31 * result + lastModifier.hashCode()
        result = 31 * result + users.hashCode()
        result = 31 * result + managers.hashCode()
        result = 31 * result + publicMeta.contentHashCode()
        result = 31 * result + privateMeta.contentHashCode()
        result = 31 * result + policy.hashCode()
        result = 31 * result + state.hashCode()
        return result
    }
}