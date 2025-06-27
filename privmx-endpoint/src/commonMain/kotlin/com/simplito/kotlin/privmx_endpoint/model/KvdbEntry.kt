package com.simplito.kotlin.privmx_endpoint.model

/**
 * Holds all available information about a Entry.
 *
 * @property info Entry information created by server
 * @property publicMeta Entry public metadata
 * @property privateMeta Entry private metadata
 * @property data Entry data
 * @property authorPubKey Public key of an author of the entry
 * @property version Version number (changes on every on existing item)
 * @property statusCode Retrieval and decryption status code
 * @property schemaVersion Version of the Entry data structure and how it is encoded/encrypted
 *
 * @category kvdb
 * @group Kvdb
 */
class KvdbEntry(
    val info: ServerKvdbEntryInfo,
    val publicMeta: ByteArray,
    val privateMeta: ByteArray,
    val data: ByteArray,
    val authorPubKey: String,
    val version: Long?,
    val statusCode: Long?,
    val schemaVersion: Long?
)