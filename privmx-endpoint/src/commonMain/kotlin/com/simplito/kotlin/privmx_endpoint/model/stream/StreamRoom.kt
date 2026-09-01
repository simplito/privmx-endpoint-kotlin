package com.simplito.kotlin.privmx_endpoint.model.stream

import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicyWithoutItem

/**
 * Holds all available information about a Stream Room.
 *
 * @property contextId              ID of the Context the Stream Room was created in
 * @property streamRoomId           ID of the Stream Room
 * @property createDate             Stream Room creation timestamp
 * @property creator                ID of the user who created the Stream Room
 * @property lastModificationDate   Stream Room last modification timestamp
 * @property lastModifier           ID of the user who last modified the Stream Room
 * @property users                  List of users (their IDs) with access to the Stream Room
 * @property managers               List of users (their IDs) with management rights
 * @property version                version number (changes on updates)
 * @property publicMeta             Stream Room's public metadata
 * @property privateMeta            Stream Room's private metadata
 * @property policy                 Stream Room's policies
 * @property statusCode             Status code of retrieval and decryption of the [StreamRoom]
 * @property schemaVersion          version of the Stream Room data structure and how it is encoded/encrypted
 * @property state                  current state of the Stream Room:
 *                                  - "created" - created for a future meeting, not used yet
 *                                  - "open" - the meeting has started (the first user has joined)
 *                                  - "closed" - the meeting has ended; the room closes itself once all users
 *                                     have left and the emptyRoomTtl grace period has passed. A user joining
 *                                     within that period restarts it
 * @property emptyRoomTtl           grace period (ms) the Stream Room stays open after the last participant leaves
 */
data class StreamRoom(
    val contextId: String,
    val streamRoomId: String,
    val createDate: Long?,
    val creator: String,
    val lastModificationDate: Long?,
    val lastModifier: String,
    val users: List<String>,
    val managers: List<String>,
    val version: Long?,
    val publicMeta: ByteArray,
    val privateMeta: ByteArray,
    val policy: ContainerPolicyWithoutItem,
    val statusCode: Long?,
    val schemaVersion: Long?,
    val state: String,   // "created" | "open" | "closed",
    val emptyRoomTtl: Long?
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
        if (emptyRoomTtl != other.emptyRoomTtl) return false
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
        var result = createDate?.hashCode() ?: 0
        result = 31 * result + (lastModificationDate?.hashCode() ?: 0)
        result = 31 * result + (version?.hashCode() ?: 0)
        result = 31 * result + (statusCode?.hashCode() ?: 0)
        result = 31 * result + (schemaVersion?.hashCode() ?: 0)
        result = 31 * result + (emptyRoomTtl?.hashCode() ?: 0)
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