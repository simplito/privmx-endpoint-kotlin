package com.simplito.kotlin.privmx_endpoint.model

/**
 * Holds all available information about a Kvdb.
 *
 * @property contextId ID of the Context
 * @property kvdbId ID of the Kvdb
 * @property createDate Kvdb creation timestamp
 * @property creator ID of user who created the Kvdb
 * @property lastModificationDate Kvdb last modification timestamp
 * @property lastModifier ID of the user who last modified the Kvdb
 * @property users List of users (their IDs) with access to the Kvdb
 * @property managers List of users (their IDs) with management rights
 * @property version Version number (changes on updates)
 * @property publicMeta Kvdb's public metadata
 * @property privateMeta Kvdb's private metadata
 * @property entries Total number of entries in the Kvdb
 * @property lastEntryDate Timestamp of the last added entry
 * @property policy Kvdb's policies
 * @property statusCode Retrieval and decryption status code
 * @property schemaVersion Version of the Kvdb data structure and how it is encoded/encrypted
 *
 * @category kvdb
 * @group Kvdb
 */
data class Kvdb(
    val contextId: String,
    val kvdbId: String,
    val createDate: Long?,
    val creator: String,
    val lastModificationDate: Long?,
    val lastModifier: String,
    val users: List<String>,
    val managers: List<String>,
    val version: Long?,
    val publicMeta: ByteArray,
    val privateMeta: ByteArray,
    val entries: Long?,
    val lastEntryDate: Long?,
    val policy: ContainerPolicy?,
    val statusCode: Long?,
    val schemaVersion: Long?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Kvdb

        if (createDate != other.createDate) return false
        if (lastModificationDate != other.lastModificationDate) return false
        if (version != other.version) return false
        if (entries != other.entries) return false
        if (lastEntryDate != other.lastEntryDate) return false
        if (statusCode != other.statusCode) return false
        if (schemaVersion != other.schemaVersion) return false
        if (contextId != other.contextId) return false
        if (kvdbId != other.kvdbId) return false
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
        var result = createDate?.hashCode() ?: 0
        result = 31 * result + (lastModificationDate?.hashCode() ?: 0)
        result = 31 * result + (version?.hashCode() ?: 0)
        result = 31 * result + (entries?.hashCode() ?: 0)
        result = 31 * result + (lastEntryDate?.hashCode() ?: 0)
        result = 31 * result + (statusCode?.hashCode() ?: 0)
        result = 31 * result + (schemaVersion?.hashCode() ?: 0)
        result = 31 * result + contextId.hashCode()
        result = 31 * result + kvdbId.hashCode()
        result = 31 * result + creator.hashCode()
        result = 31 * result + lastModifier.hashCode()
        result = 31 * result + users.hashCode()
        result = 31 * result + managers.hashCode()
        result = 31 * result + publicMeta.contentHashCode()
        result = 31 * result + privateMeta.contentHashCode()
        result = 31 * result + (policy?.hashCode() ?: 0)
        return result
    }
}
