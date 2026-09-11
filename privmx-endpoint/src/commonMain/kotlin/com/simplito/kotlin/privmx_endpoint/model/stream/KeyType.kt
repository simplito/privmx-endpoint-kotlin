package com.simplito.kotlin.privmx_endpoint.model.stream

/**
 * Types of keys used to encrypt and decrypt Stream data.
 */
enum class KeyType {
    /**
     * key used to encrypt the data sent by the local user
     */
    LOCAL,

    /**
     * key used to decrypt the data received from other participants
     */
    REMOTE
}