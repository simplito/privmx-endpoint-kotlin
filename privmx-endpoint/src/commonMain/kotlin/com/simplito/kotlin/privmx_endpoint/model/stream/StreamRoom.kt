package com.simplito.kotlin.privmx_endpoint.model.stream

import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicyWithoutItem

/**
 * Represents a stream room within a PrivMX context.
 *
 * @property contextId            Identifier of the context this stream room belongs to
 * @property streamRoomId         Identifier of the stream room
 * @property createDate           Timestamp of when the stream room was created
 * @property creator              Identifier of the user who created the stream room
 * @property lastModificationDate Timestamp of when the stream room was last modified
 * @property lastModifier         Identifier of the user who last modified the stream room
 * @property users                IDs of users with management rights in the stream room
 * @property managers             IDs of users with management rights in the stream room
 * @property version              Version number (changes on updates)
 * @property publicMeta           StreamRoom's public metadata
 * @property privateMeta          StreamRoom's private metadata
 * @property policy               StreamRoom's policies
 * @property statusCode           Status code of retrieval and decryption of the [StreamRoom]
 * @property schemaVersion        Version of the StreamRoom data structure and how it is encoded/encrypted
 * @property closed               Whether the stream room is closed
 */
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
    val closed: Boolean,
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
        if (closed != other.closed) return false
        if (contextId != other.contextId) return false
        if (streamRoomId != other.streamRoomId) return false
        if (creator != other.creator) return false
        if (lastModifier != other.lastModifier) return false
        if (users != other.users) return false
        if (managers != other.managers) return false
        if (!publicMeta.contentEquals(other.publicMeta)) return false
        if (!privateMeta.contentEquals(other.privateMeta)) return false
        if (policy != other.policy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = createDate.hashCode()
        result = 31 * result + lastModificationDate.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + statusCode.hashCode()
        result = 31 * result + schemaVersion.hashCode()
        result = 31 * result + closed.hashCode()
        result = 31 * result + contextId.hashCode()
        result = 31 * result + streamRoomId.hashCode()
        result = 31 * result + creator.hashCode()
        result = 31 * result + lastModifier.hashCode()
        result = 31 * result + users.hashCode()
        result = 31 * result + managers.hashCode()
        result = 31 * result + publicMeta.contentHashCode()
        result = 31 * result + privateMeta.contentHashCode()
        result = 31 * result + policy.hashCode()
        return result
    }
}